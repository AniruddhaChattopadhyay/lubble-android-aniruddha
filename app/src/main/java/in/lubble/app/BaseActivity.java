package in.lubble.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.crashlytics.android.Crashlytics;
import com.freshchat.consumer.sdk.Freshchat;
import com.freshchat.consumer.sdk.FreshchatConfig;
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
    private final static int IMMEDIATE_REQUEST_CODE = 312, FLEXI_REQUEST_CODE = 313;
    private InstallStateUpdatedListener listener;
    private SharedPreferences sharedPreferences;

    private Button update;
    private ImageView cancelUpdate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!(this instanceof MainActivity) && !(this instanceof LoginActivity) && !(this instanceof LocationActivity) &&
                (FirebaseAuth.getInstance().getCurrentUser() == null || TextUtils.isEmpty(LubbleSharedPrefs.getInstance().getLubbleId()))) {
            // user is not signed in, start login flow
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        if (FirebaseAuth.getInstance().getCurrentUser() != null && !(this instanceof LoginActivity) && !(this instanceof LocationActivity)) {
            // logged in
            try {
                appUpdateManager = AppUpdateManagerFactory.create(BaseActivity.this);
                checkMinAppVersion();
            } catch (Throwable e) {
                Crashlytics.logException(e);
            }
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

        FreshchatConfig freshchatConfig = new FreshchatConfig("8affdb23-6a28-4165-8cc0-14df42b1ad88", "293944df-73eb-4055-95a0-f2537cf42499");
        freshchatConfig.setCameraCaptureEnabled(true);
        freshchatConfig.setGallerySelectionEnabled(true);
        Freshchat.getInstance(getApplicationContext()).init(freshchatConfig);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActive = true;
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
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void checkUpdate(String data) {
        appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        if (data.equals("immediate")) {
            appUpdateInfoTask.addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
                @Override
                public void onSuccess(AppUpdateInfo appUpdateInfo) {
                    if ((appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE || appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS)
                            && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                        try {
                            Analytics.triggerEvent(AnalyticsEvents.APP_UPDATE_BLOCK, BaseActivity.this);
                            appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, BaseActivity.this, IMMEDIATE_REQUEST_CODE);
                        } catch (IntentSender.SendIntentException e) {
                            Crashlytics.logException(e);
                        }
                    }
                }
            });
        } else {
            if (System.currentTimeMillis() - LubbleSharedPrefs.getInstance().getFlexiUpdateTs() > TimeUnit.HOURS.toMillis(24)) {
                appUpdateInfoTask.addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
                    @Override
                    public void onSuccess(final AppUpdateInfo appUpdateInfo) {
                        if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) && isActive) {
                            LubbleSharedPrefs.getInstance().setFlexiUpdateTs(System.currentTimeMillis());
                            Analytics.triggerEvent(AnalyticsEvents.APP_UPDATE_REMINDER, BaseActivity.this);
                            View v = getLayoutInflater().inflate(R.layout.layout_update_bottom_sheet, null);
                            final BottomSheetDialog dialog = new BottomSheetDialog(BaseActivity.this);
                            dialog.setContentView(v);
                            dialog.setCancelable(true);
                            dialog.show();

                            update = v.findViewById(R.id.updateButton);
                            cancelUpdate = v.findViewById(R.id.updateCancel);

                            update.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismiss();
                                    Analytics.triggerEvent(AnalyticsEvents.APP_UPDATE_REMINDER_POSITIVE, BaseActivity.this);
                                    try {
                                        startFlexiUpdate(appUpdateInfo);
                                    } catch (IntentSender.SendIntentException e) {
                                        e.printStackTrace();
                                        Crashlytics.logException(e);
                                    }
                                }
                            });
                            cancelUpdate.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Analytics.triggerEvent(AnalyticsEvents.APP_UPDATE_REMINDER_LATER, BaseActivity.this);
                                    dialog.dismiss();
                                }
                            });

                        }
                    }
                });
            }
        }
    }

    private void startFlexiUpdate(AppUpdateInfo appUpdateInfo) throws IntentSender.SendIntentException {
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
                    snackbar.setActionTextColor(getResources().getColor(R.color.light_colorAccent));
                    snackbar.show();
                }
            }
        };
        appUpdateManager.registerListener(listener);
        appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.FLEXIBLE, BaseActivity.this, FLEXI_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMMEDIATE_REQUEST_CODE && resultCode != RESULT_OK) {
            new AlertDialog.Builder(BaseActivity.this)
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle("Critical Update Pending")
                    .setMessage("Please update the app to continue using Lubble. It will take less than a minute.")
                    .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Analytics.triggerEvent(AnalyticsEvents.APP_UPDATE_BLOCK_RETRY, BaseActivity.this);
                            checkMinAppVersion();
                        }
                    }).setCancelable(false).create().show();
        }
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
