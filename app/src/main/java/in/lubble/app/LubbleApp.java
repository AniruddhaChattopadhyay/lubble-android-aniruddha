package in.lubble.app;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;

import in.lubble.app.notifications.GroupMappingSharedPrefs;
import in.lubble.app.notifications.KeyMappingSharedPrefs;
import in.lubble.app.notifications.MutedChatsSharedPrefs;
import in.lubble.app.notifications.UnreadChatsSharedPrefs;
import io.fabric.sdk.android.Fabric;

/**
 * Created by ishaan on 20/1/18.
 */

public class LubbleApp extends Application {

    private static LubbleApp appContext;

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        if (BuildConfig.DEBUG) {
            FirebaseDatabase.getInstance().setLogLevel(Logger.Level.DEBUG);
        }

        appContext = this;

        LubbleSharedPrefs.initializeInstance(getApplicationContext());
        GroupMappingSharedPrefs.initializeInstance(getApplicationContext());
        UnreadChatsSharedPrefs.initializeInstance(getApplicationContext());
        MutedChatsSharedPrefs.initializeInstance(getApplicationContext());
        KeyMappingSharedPrefs.initializeInstance(getApplicationContext());

        Fabric.with(this, new Crashlytics());

        createNotifChannel();
        FirebaseAnalytics.getInstance(this);
    }

    public static LubbleApp getAppContext() {
        return appContext;
    }

    private void createNotifChannel() {
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotifyMgr.createNotificationChannel(
                    new NotificationChannel(
                            Constants.CHAT_NOTIF_CHANNEL,
                            "Chat Notifications",
                            NotificationManager.IMPORTANCE_HIGH));

            final NotificationChannel mediaChannel = new NotificationChannel(
                    Constants.SENDING_MEDIA_NOTIF_CHANNEL,
                    "Send Media",
                    NotificationManager.IMPORTANCE_HIGH);
            mediaChannel.setSound(null, null);
            mNotifyMgr.createNotificationChannel(
                    mediaChannel);

            mNotifyMgr.createNotificationChannel(
                    new NotificationChannel(
                            Constants.NOTICE_NOTIF_CHANNEL,
                            "Notice Notifications",
                            NotificationManager.IMPORTANCE_HIGH));

            mNotifyMgr.createNotificationChannel(
                    new NotificationChannel(
                            Constants.APP_NOTIF_CHANNEL,
                            "App Notifications",
                            NotificationManager.IMPORTANCE_HIGH));

        }
    }
}
