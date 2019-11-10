package in.lubble.app;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.emoji.text.EmojiCompat;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.auth.LocationActivity;
import in.lubble.app.auth.LoginActivity;
import in.lubble.app.firebase.RealtimeDbHelper;

public class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";
    private boolean isActive;
    private ValueEventListener minAppEventListener;

    private AppUpdateManager appUpdateManager;
    private com.google.android.play.core.tasks.Task<AppUpdateInfo> appUpdateInfoTask;
    private final static int MY_REQUEST_CODE = 312, MY_REQUEST_CODE_1 = 313;
    private InstallStateUpdatedListener listener;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = BaseActivity.this.getSharedPreferences("updateTime", MODE_PRIVATE);
        if (!(this instanceof MainActivity) && !(this instanceof LoginActivity) && !(this instanceof LocationActivity) &&
                (FirebaseAuth.getInstance().getCurrentUser() == null || TextUtils.isEmpty(LubbleSharedPrefs.getInstance().getLubbleId()))) {
            // user is not signed in, start login flow
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        final FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        firebaseRemoteConfig.fetch().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    firebaseRemoteConfig.activateFetched();
                }
            }
        });
    }

    private void checkUpdate(String data) {

        appUpdateManager = AppUpdateManagerFactory.create(BaseActivity.this);


        appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        if (data.equals("immediate")) {
            appUpdateInfoTask.addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
                @Override
                public void onSuccess(AppUpdateInfo appUpdateInfo) {
                    if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                        try {
                            appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, BaseActivity.this, MY_REQUEST_CODE);
                        } catch (IntentSender.SendIntentException e) {
                            Toast.makeText(BaseActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        } else {
            View v = getLayoutInflater().inflate(R.layout.layout_update_bottom_sheet, null);
            long previous_time = sharedPreferences.getLong("timestampnow", 0);
            long current_time = Calendar.getInstance().getTimeInMillis();
            if (current_time > previous_time) {
                final BottomSheetDialog dialog = new BottomSheetDialog(BaseActivity.this);
                dialog.setContentView(v);
                dialog.show();

                Button update = v.findViewById(R.id.updateButton), cancelUpdate = v.findViewById(R.id.updateCancel);

                cancelUpdate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        long current_time = Calendar.getInstance().getTimeInMillis();
                        sharedPreferences.edit().putLong("timestampnow", current_time + TimeUnit.HOURS.toMillis(24)).apply();
                        Analytics.triggerEvent(AnalyticsEvents.APP_UPDATE_REMINDER_LATER, BaseActivity.this);
                        dialog.cancel();
                    }
                });

                update.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Analytics.triggerEvent(AnalyticsEvents.APP_UPDATE_BLOCK_POSITIVE, BaseActivity.this);
                        appUpdateInfoTask.addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
                            @Override
                            public void onSuccess(AppUpdateInfo appUpdateInfo) {
                                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                                    try {
                                        appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.FLEXIBLE, BaseActivity.this, MY_REQUEST_CODE_1);
                                        listener = new InstallStateUpdatedListener() {
                                            @Override
                                            public void onStateUpdate(InstallState state) {
                                                if (state.installStatus() == InstallStatus.DOWNLOADED) {
                                                    Snackbar snackbar =
                                                            Snackbar.make(
                                                                    findViewById(R.id.content_frame),
                                                                    "An update has just been downloaded.",
                                                                    Snackbar.LENGTH_INDEFINITE);
                                                    snackbar.setAction("RESTART", new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            appUpdateManager.completeUpdate();
                                                            appUpdateManager.unregisterListener(listener);
                                                        }
                                                    });
                                                    snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
                                                    snackbar.show();
                                                }
                                            }
                                        };
                                        appUpdateManager.registerListener(listener);


                                    } catch (IntentSender.SendIntentException e) {
                                        Toast.makeText(BaseActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                    }


                                }


                            }
                        });
                    }
                });
            }
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_REQUEST_CODE && resultCode != RESULT_OK) {
            new AlertDialog.Builder(BaseActivity.this).setIcon(R.mipmap.ic_launcher).setTitle(R.string.app_name).setMessage("Please Update the app").setPositiveButton("Update", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    checkMinAppVersion();
                }
            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    BaseActivity.this.finish();
                }
            }).create().show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //checkMinAppVersion();
        isActive = true;
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            // logged in
            try {
                checkMinAppVersion();
            } catch (Throwable e) {
                Crashlytics.logException(e);
            }
        }
    }

    private void checkMinAppVersion() {
        // orderByKey to ensure we always check for hard stop firstm then evaluate the soft block
        minAppEventListener = RealtimeDbHelper.getAppInfoRef().orderByKey().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    if (child.getKey().equalsIgnoreCase("minApp")) {
                        Integer minAppVersion = child.getValue(Integer.class);
                        minAppVersion = minAppVersion == null ? 27 : minAppVersion;

                        if (BuildConfig.VERSION_CODE < minAppVersion && !isFinishing() && isActive) {
                            // block app
                            checkUpdate("immediate");
                            return;
                        }
                    } else if (child.getKey().equalsIgnoreCase("softMinApp") && BaseActivity.this instanceof MainActivity) {
                        Integer minAppVersion = child.getValue(Integer.class);
                        minAppVersion = minAppVersion == null ? 27 : minAppVersion;

                        if (BuildConfig.VERSION_CODE < minAppVersion && !isFinishing() && isActive) {
                            checkUpdate("flexible");
                        }
                        // prompt to optionally update app
                        //Analytics.triggerEvent(AnalyticsEvents.APP_UPDATE_REMINDER_POSITIVE, BaseActivity.this);
                        //Analytics.triggerEvent(AnalyticsEvents.APP_UPDATE_REMINDER_LATER, BaseActivity.this);
                        //Analytics.triggerEvent(AnalyticsEvents.APP_UPDATE_REMINDER, BaseActivity.this);
                    }
                    return;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActive = false;
        if (minAppEventListener != null) {
            RealtimeDbHelper.getAppInfoRef().orderByKey().removeEventListener(minAppEventListener);
        }
    }
}
