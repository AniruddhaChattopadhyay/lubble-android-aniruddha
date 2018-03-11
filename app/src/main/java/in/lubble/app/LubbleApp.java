package in.lubble.app;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;

import in.lubble.app.notifications.GroupMappingSharedPrefs;
import in.lubble.app.notifications.UnreadChatsSharedPrefs;
import io.fabric.sdk.android.Fabric;

/**
 * Created by ishaan on 20/1/18.
 */

public class LubbleApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        if (BuildConfig.DEBUG) {
            FirebaseDatabase.getInstance().setLogLevel(Logger.Level.DEBUG);
        }
        LubbleSharedPrefs.initializeInstance(getApplicationContext());
        GroupMappingSharedPrefs.initializeInstance(getApplicationContext());
        UnreadChatsSharedPrefs.initializeInstance(getApplicationContext());

        Fabric.with(this, new Crashlytics());

        createNotifChannel();
    }

    private void createNotifChannel() {
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotifyMgr.createNotificationChannel(
                    new NotificationChannel(
                            Constants.DEFAULT_NOTIF_CHANNEL,
                            "Default",
                            NotificationManager.IMPORTANCE_HIGH));
        }
    }
}
