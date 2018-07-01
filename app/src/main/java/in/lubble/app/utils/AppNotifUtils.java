package in.lubble.app.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

import in.lubble.app.Constants;
import in.lubble.app.MainActivity;
import in.lubble.app.R;
import in.lubble.app.announcements.announcementHistory.AnnouncementsActivity;
import in.lubble.app.chat.ChatActivity;
import in.lubble.app.events.EventInfoActivity;
import in.lubble.app.models.AppNotifData;
import in.lubble.app.notifications.KeyMappingSharedPrefs;

import static in.lubble.app.MainActivity.EXTRA_TAB_NAME;
import static in.lubble.app.chat.ChatActivity.EXTRA_GROUP_ID;
import static in.lubble.app.events.EventInfoActivity.KEY_EVENT_ID;
import static in.lubble.app.utils.DateTimeUtils.getTimeBasedUniqueInt;

/**
 * Created by ishaan on 24/4/18.
 */

public class AppNotifUtils {

    private static final String TAG = "AppNotifUtils";

    public static void showAppNotif(Context context, RemoteMessage.Notification notification) {
        Log.d(TAG, "showAppNotif: notif");

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Constants.APP_NOTIF_CHANNEL)
                .setContentTitle(notification.getTitle())
                .setSmallIcon(R.drawable.ic_lubble_notif)
                .setAutoCancel(true)
                .setChannelId(Constants.APP_NOTIF_CHANNEL)
                .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notification.getBody()))
                .setContentText(notification.getBody());

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(DateTimeUtils.getTimeBasedUniqueInt(), builder.build());
    }

    public static void showAppNotif(Context context, AppNotifData appNotifData) {
        Log.d(TAG, "showAppNotif: data ");
        String notifChannel = Constants.APP_NOTIF_CHANNEL;
        if (appNotifData.getType().equalsIgnoreCase("notice")) {
            notifChannel = Constants.NOTICE_NOTIF_CHANNEL;
        }
        addGroupIdMapping(appNotifData.getNotifKey());

        final TaskStackBuilder stackBuilder = getPendingIntent(context, appNotifData);
        final int notifId = getNotifId(appNotifData.getNotifKey());

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, notifChannel)
                .setContentTitle(appNotifData.getTitle())
                .setSmallIcon(R.drawable.ic_lubble_notif)
                .setAutoCancel(true)
                .setChannelId(notifChannel)
                .setContentIntent(stackBuilder.getPendingIntent(notifId, PendingIntent.FLAG_UPDATE_CURRENT))
                .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(appNotifData.getMsg()))
                .setContentText(appNotifData.getMsg());

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(notifId, builder.build());
    }

    private static TaskStackBuilder getPendingIntent(Context context, AppNotifData appNotifData) {

        if (appNotifData.getType().equalsIgnoreCase("groupInvitation")) {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra(EXTRA_GROUP_ID, appNotifData.getGroupId());
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            return stackBuilder.addNextIntentWithParentStack(intent);

        } else if (appNotifData.getType().equalsIgnoreCase("notice")) {
            Intent intent = new Intent(context, AnnouncementsActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            return stackBuilder.addNextIntentWithParentStack(intent);
        } else if (appNotifData.getType().equalsIgnoreCase("events")) {
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra(EXTRA_TAB_NAME, "events");
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            return stackBuilder.addNextIntentWithParentStack(intent);
        } else if (appNotifData.getType().equalsIgnoreCase("new_event")) {
            Intent intent = new Intent(context, EventInfoActivity.class);
            intent.putExtra(KEY_EVENT_ID, appNotifData.getNotifKey());
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            return stackBuilder.addNextIntentWithParentStack(intent);
        } else {
            Intent intent = new Intent(context, MainActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            return stackBuilder.addNextIntentWithParentStack(intent);
        }
    }

    private static void addGroupIdMapping(String key) {
        final SharedPreferences keyPrefs = KeyMappingSharedPrefs.getInstance().getPreferences();
        final int savedKeyInt = keyPrefs.getInt(key, -1);
        if (savedKeyInt == -1) {
            final int lastInt = keyPrefs.getInt(Constants.LAST_KEY_MAPPING_ID, 500);
            keyPrefs.edit().putInt(key, lastInt + 1).commit();
            keyPrefs.edit().putInt(Constants.LAST_KEY_MAPPING_ID, lastInt + 1).commit();
            Log.d(TAG, "Added new key map ID: " + lastInt + 1);
        } else {
            Log.d(TAG, "Key Map ID exists: " + savedKeyInt);
        }
    }

    private static int getNotifId(String key) {
        return KeyMappingSharedPrefs.getInstance().getPreferences().getInt(key, getTimeBasedUniqueInt());
    }

    public static void deleteAppNotif(Context context, String key) {
        final int notifId = getNotifId(key);
        // cancel notification
        final NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notifId);
    }

}
