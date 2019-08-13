package in.lubble.app;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.emoji.text.EmojiCompat;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.auth.LocationActivity;
import in.lubble.app.auth.LoginActivity;
import in.lubble.app.firebase.RealtimeDbHelper;

public class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";
    private boolean isActive;
    private ValueEventListener minAppEventListener;

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

    @Override
    protected void onResume() {
        super.onResume();
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
                            final AlertDialog alertDialog = new AlertDialog.Builder(BaseActivity.this).create();
                            alertDialog.setTitle(getString(R.string.update_dialog_title));
                            alertDialog.setMessage(getString(R.string.update_dialog_msg));
                            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.all_update), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    alertDialog.dismiss();
                                    Analytics.triggerEvent(AnalyticsEvents.APP_UPDATE_BLOCK_POSITIVE, BaseActivity.this);
                                    final String appPackageName = getPackageName();
                                    try {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                    } catch (ActivityNotFoundException anfe) {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                    }
                                }
                            });
                            alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.update_recheck), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    alertDialog.dismiss();
                                    Analytics.triggerEvent(AnalyticsEvents.APP_UPDATE_BLOCK_RETRY, BaseActivity.this);
                                    checkMinAppVersion();
                                }
                            });
                            alertDialog.setCancelable(false);
                            alertDialog.show();
                            Analytics.triggerEvent(AnalyticsEvents.APP_UPDATE_BLOCK, BaseActivity.this);
                            return;
                        }
                    } else if (child.getKey().equalsIgnoreCase("softMinApp") && BaseActivity.this instanceof MainActivity) {
                        Integer minAppVersion = child.getValue(Integer.class);
                        minAppVersion = minAppVersion == null ? 27 : minAppVersion;

                        if (BuildConfig.VERSION_CODE < minAppVersion && !isFinishing() && isActive) {
                            // prompt to optionally update app
                            try {
                                final AlertDialog alertDialog = new AlertDialog.Builder(BaseActivity.this).create();
                                alertDialog.setTitle(EmojiCompat.get().process("New App Update 🎁"));
                                alertDialog.setMessage(EmojiCompat.get().process("Cool new features await you! Get the latest update now 🎉"));
                                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.all_update), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        alertDialog.dismiss();
                                        Analytics.triggerEvent(AnalyticsEvents.APP_UPDATE_REMINDER_POSITIVE, BaseActivity.this);
                                        final String appPackageName = getPackageName();
                                        try {
                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                        } catch (ActivityNotFoundException anfe) {
                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                        }
                                    }
                                });
                                alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.all_later), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        alertDialog.dismiss();
                                        Analytics.triggerEvent(AnalyticsEvents.APP_UPDATE_REMINDER_LATER, BaseActivity.this);
                                    }
                                });
                                alertDialog.setCancelable(false);
                                alertDialog.show();
                                Analytics.triggerEvent(AnalyticsEvents.APP_UPDATE_REMINDER, BaseActivity.this);
                            } catch (Exception e) {
                                Crashlytics.logException(e);
                            }
                            return;
                        }
                    }
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
