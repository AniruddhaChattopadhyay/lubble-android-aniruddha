package in.lubble.app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.provider.FontRequest;
import androidx.emoji.text.EmojiCompat;
import androidx.emoji.text.FontRequestEmojiCompatConfig;
import androidx.multidex.MultiDexApplication;

import com.clevertap.android.sdk.ActivityLifecycleCallback;
import com.clevertap.android.sdk.CleverTapAPI;
import com.freshchat.consumer.sdk.Freshchat;
import com.freshchat.consumer.sdk.FreshchatNotificationConfig;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;
import com.segment.analytics.Analytics;

import in.lubble.app.chat.GroupPromptSharedPrefs;
import in.lubble.app.database.DbSingleton;
import in.lubble.app.notifications.GroupMappingSharedPrefs;
import in.lubble.app.notifications.KeyMappingSharedPrefs;
import in.lubble.app.notifications.SnoozedGroupsSharedPrefs;
import in.lubble.app.notifications.UnreadChatsSharedPrefs;
import in.lubble.app.quiz.AnswerSharedPrefs;
import io.branch.referral.Branch;
import io.getstream.analytics.config.StreamAnalyticsAuth;
import io.getstream.analytics.service.StreamAnalyticsImpl;

import static in.lubble.app.Constants.CHAT_NOTIF_CHANNEL;

/**
 * Created by ishaan on 20/1/18.
 */

public class LubbleApp extends MultiDexApplication {

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
        //Before you initialize in your Application `#onCreate`
        CleverTapAPI clevertapInstance = CleverTapAPI.getDefaultInstance(this);
        // Initialize the Branch object
        Branch branch = Branch.getAutoInstance(this);
        if (clevertapInstance != null) {
            branch.setRequestMetadata("$clevertap_attribution_id",
                    clevertapInstance.getCleverTapAttributionIdentifier());
        }
        // Branch logging for debugging
        Branch.enableLogging();

        LubbleSharedPrefs.initializeInstance(getApplicationContext());
        GroupMappingSharedPrefs.initializeInstance(getApplicationContext());
        UnreadChatsSharedPrefs.initializeInstance(getApplicationContext());
        KeyMappingSharedPrefs.initializeInstance(getApplicationContext());
        AnswerSharedPrefs.initializeInstance(getApplicationContext());
        GroupPromptSharedPrefs.initializeInstance(getApplicationContext());
        SnoozedGroupsSharedPrefs.initializeInstance(getApplicationContext());
        // REMEMBER: Clear the new sharedPrefs file in UserUtils#logout()

        // Create an analytics client with the given context and Segment write key.
        String writeKey;
        if (BuildConfig.DEBUG) {
            writeKey = "1nx6Mc1yYGEJhFICwIvhabj0yKCKj8al";
        } else {
            writeKey = "czyerWm5cWfszKJvMA8qQJP8zFiyhDic";
        }
        Analytics analytics = new Analytics.Builder(this, writeKey)
                .trackApplicationLifecycleEvents() // Enable this to record certain application events automatically!
                //.recordScreenViews() // Enable this to record screen views automatically!
                .build();

        // Set the initialized instance as a globally accessible instance.
        Analytics.setSingletonInstance(analytics);

        createNotifChannel();
        FirebaseAnalytics.getInstance(this);

        FontRequest fontRequest = new FontRequest(
                "com.google.android.gms.fonts",
                "com.google.android.gms",
                "Noto Color Emoji Compat",
                R.array.com_google_android_gms_fonts_certs);
        EmojiCompat.Config config = new FontRequestEmojiCompatConfig(this, fontRequest);
        EmojiCompat.init(config);

        FreshchatNotificationConfig notificationConfig = new FreshchatNotificationConfig()
                .setNotificationSoundEnabled(true)
                .setSmallIcon(R.drawable.ic_lubble_notif)
                .setLargeIcon(R.drawable.ic_support)
                .launchActivityOnFinish(MainActivity.class.getName())
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        Freshchat.getInstance(getApplicationContext()).setNotificationConfig(notificationConfig);

        StreamAnalyticsAuth auth = new StreamAnalyticsAuth("nvhsd4sv68k4", "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJyZXNvdXJjZSI6ImFuYWx5dGljcyIsImFjdGlvbiI6IioiLCJ1c2VyX2lkIjoiKiJ9.JNBodILjaJEuW2fwIjZTZcvKn8lXI0roercYGAZ1xAg");
        StreamAnalyticsImpl.getInstance(auth);
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

            mNotifyMgr.createNotificationChannel(
                    new NotificationChannel(
                            Constants.DM_CHAT_NOTIF_CHANNEL,
                            "Direct Messages",
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

            mNotifyMgr.createNotificationChannel(
                    new NotificationChannel(
                            Constants.LUBB_NOTIF_CHANNEL,
                            "Message Like Notifications",
                            NotificationManager.IMPORTANCE_LOW));

        }
    }
}
