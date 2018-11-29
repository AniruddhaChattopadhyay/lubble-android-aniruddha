package in.lubble.app;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import androidx.core.provider.FontRequest;
import androidx.emoji.text.EmojiCompat;
import androidx.emoji.text.FontRequestEmojiCompatConfig;
import com.clevertap.android.sdk.ActivityLifecycleCallback;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;
import in.lubble.app.database.DbSingleton;
import in.lubble.app.notifications.GroupMappingSharedPrefs;
import in.lubble.app.notifications.KeyMappingSharedPrefs;
import in.lubble.app.notifications.MutedChatsSharedPrefs;
import in.lubble.app.notifications.UnreadChatsSharedPrefs;
import io.branch.referral.Branch;
import io.fabric.sdk.android.Fabric;

import static in.lubble.app.Constants.CHAT_NOTIF_CHANNEL;

/**
 * Created by ishaan on 20/1/18.
 */

public class LubbleApp extends Application {

    private static LubbleApp appContext;

    @Override
    public void onCreate() {
        ActivityLifecycleCallback.register(this);
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        if (BuildConfig.DEBUG) {
            FirebaseDatabase.getInstance().setLogLevel(Logger.Level.DEBUG);
        }
        DbSingleton.initializeInstance(getApplicationContext());

        appContext = this;

        // Initialize the Branch object
        Branch.getAutoInstance(this);
        // Branch logging for debugging
        Branch.enableLogging();

        LubbleSharedPrefs.initializeInstance(getApplicationContext());
        GroupMappingSharedPrefs.initializeInstance(getApplicationContext());
        UnreadChatsSharedPrefs.initializeInstance(getApplicationContext());
        MutedChatsSharedPrefs.initializeInstance(getApplicationContext());
        KeyMappingSharedPrefs.initializeInstance(getApplicationContext());

        Fabric.with(this, new Crashlytics());

        createNotifChannel();
        FirebaseAnalytics.getInstance(this);

        FontRequest fontRequest = new FontRequest(
                "com.google.android.gms.fonts",
                "com.google.android.gms",
                "Noto Color Emoji Compat",
                R.array.com_google_android_gms_fonts_certs);
        EmojiCompat.Config config = new FontRequestEmojiCompatConfig(this, fontRequest);
        EmojiCompat.init(config);

    }

    public static LubbleApp getAppContext() {
        return appContext;
    }

    private void createNotifChannel() {
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /*
            ///////////DELETED////////////
            mNotifyMgr.createNotificationChannel(
                    new NotificationChannel(
                            Constants.CHAT_NOTIF_CHANNEL,
                            "Chat Notifications",
                            NotificationManager.IMPORTANCE_HIGH));*/

            mNotifyMgr.deleteNotificationChannel(CHAT_NOTIF_CHANNEL);

            mNotifyMgr.createNotificationChannel(
                    new NotificationChannel(
                            Constants.NEW_CHAT_NOTIF_CHANNEL,
                            "Chat Messages",
                            NotificationManager.IMPORTANCE_LOW));

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

            mNotifyMgr.createNotificationChannel(
                    new NotificationChannel(
                            Constants.LUBB_NOTIF_CHANNEL,
                            "Message Like Notifications",
                            NotificationManager.IMPORTANCE_LOW));

        }
    }
}
