package in.lubble.app.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.request.target.Target;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import in.lubble.app.Constants;
import in.lubble.app.GlideApp;
import in.lubble.app.MainActivity;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.chat.ChatActivity;
import in.lubble.app.models.NotifData;
import in.lubble.app.notifications.GroupMappingSharedPrefs;
import in.lubble.app.notifications.UnreadChatsSharedPrefs;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static in.lubble.app.chat.ChatActivity.EXTRA_DM_ID;
import static in.lubble.app.chat.ChatActivity.EXTRA_GROUP_ID;
import static in.lubble.app.utils.AppNotifUtils.TRACK_NOTIF_ID;

/**
 * Created by ishaan on 11/3/18.
 */

public class NotifUtils {

    private static final String TAG = "NotifUtils";
    private static final String GROUP_KEY = "1337";
    private static final int SUMMARY_ID = 1325;

    private static HashMap<String, NotificationCompat.MessagingStyle> messagingStyleMap;

    public static void updateChatNotifs(Context context, NotifData notifData) {
        messagingStyleMap = new HashMap<>();

        persistNewMessage(notifData);
        ArrayList<NotifData> msgList = getAllMsgs();
        sortListByTime(msgList);
        Log.d(TAG, "read notif count: " + msgList.size());
        sendAllNotifs(context, msgList);

    }

    private static void sortListByTime(ArrayList<NotifData> msgList) {
        Collections.sort(msgList, new Comparator<NotifData>() {
            @Override
            public int compare(NotifData o1, NotifData o2) {
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                return o1.getTimestamp() < o2.getTimestamp() ? -1 : (o1.getTimestamp() > o2.getTimestamp()) ? 1 : 0;
            }
        });
    }

    private static void sendAllNotifs(final Context context, ArrayList<NotifData> notifDataList) {

        final NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        for (NotifData notifData : notifDataList) {
            buildGroupNotification(getMessagingStyle(notifData.getGroupId()), notifData);
        }

        for (Map.Entry<String, NotificationCompat.MessagingStyle> map : messagingStyleMap.entrySet()) {
            final String groupId = map.getKey();
            final Integer notifId = getNotifId(groupId);

            final String groupDpUrl = getGroupDp(notifDataList, groupId);

            Intent intent = new Intent(context, ChatActivity.class);
            if (TextUtils.isEmpty(map.getValue().getConversationTitle())) {
                // it is a DM notif
                intent.putExtra(EXTRA_DM_ID, groupId);
                intent.putExtra(TRACK_NOTIF_ID, groupId);
            } else {
                intent.putExtra(EXTRA_GROUP_ID, groupId);
                intent.putExtra(TRACK_NOTIF_ID, groupId);
            }
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntentWithParentStack(intent);

            Intent deleteIntent = new Intent(context, NotifDeleteBroadcastRecvr.class);
            deleteIntent.putExtra("groupId", groupId);
            PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), notifId, deleteIntent, 0);

            final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Constants.CHAT_NOTIF_CHANNEL)
                    .setStyle(map.getValue())
                    .setSmallIcon(R.drawable.ic_lubble_notif)
                    .setShowWhen(true)
                    .setGroup(GROUP_KEY)
                    .setDefaults(0)
                    .setAutoCancel(true)
                    .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                    .setContentIntent(stackBuilder.getPendingIntent(notifId, PendingIntent.FLAG_UPDATE_CURRENT))
                    .setDeleteIntent(deletePendingIntent)
                    .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY);

            if (StringUtils.isValidString(groupDpUrl)) {
                new AsyncTask<Void, Void, Void>() {
                    Bitmap theBitmap = null;

                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            theBitmap = GlideApp.with(context).asBitmap()
                                    .load(groupDpUrl).circleCrop()
                                    .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                                    .get();
                        } catch (final ExecutionException e) {
                            Log.e(TAG, e.getMessage());
                        } catch (final InterruptedException e) {
                            Log.e(TAG, e.getMessage());
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void dummy) {
                        if (null != theBitmap) {
                            // The full bitmap should be available here
                            builder.setLargeIcon(theBitmap);
                        }
                        notificationManager.notify(notifId, builder.build());
                        sendNotifAnalyticEvent(AnalyticsEvents.NOTIF_DISPLAYED, groupId, context);
                    }
                }.execute();
            } else {
                builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_group));
                notificationManager.notify(notifId, builder.build());
                sendNotifAnalyticEvent(AnalyticsEvents.NOTIF_DISPLAYED, groupId, context);
            }
        }
        Notification summary = buildSummary(context, GROUP_KEY, notifDataList);
        notificationManager.notify(SUMMARY_ID, summary);
        sendNotifAnalyticEvent(AnalyticsEvents.NOTIF_SUMMARY_DISPLAYED, GROUP_KEY, context);
    }

    @Nullable
    private static String getGroupDp(ArrayList<NotifData> notifDataList, String groupId) {
        for (NotifData notifData : notifDataList) {
            if (notifData.getGroupId().equalsIgnoreCase(groupId)) {
                return notifData.getGroupDpUrl();
            }
        }
        return null;
    }

    private static int getNotifId(String groupId) {
        final int notifID = GroupMappingSharedPrefs.getInstance().getPreferences().getInt(groupId, -1);
        if (notifID == -1) {
            Log.e(TAG, "getNotifId: is not found");
        }
        return notifID;
    }

    private static NotificationCompat.MessagingStyle getMessagingStyle(String groupId) {
        NotificationCompat.MessagingStyle messagingStyle = messagingStyleMap.get(groupId);
        if (messagingStyle == null) {
            final NotificationCompat.MessagingStyle newMsgStyle = new NotificationCompat.MessagingStyle("Me");
            messagingStyleMap.put(groupId, newMsgStyle);
            messagingStyle = newMsgStyle;
        }
        return messagingStyle;
    }

    private static void buildGroupNotification(NotificationCompat.MessagingStyle messagingStyle, NotifData notifData) {

        if ("dm".equalsIgnoreCase(notifData.getNotifType())) {
            messagingStyle.setConversationTitle(null);
        } else {
            messagingStyle.setConversationTitle(notifData.getGroupName());
        }
        messagingStyle.addMessage(notifData.getMessageBody(), notifData.getTimestamp(), notifData.getAuthorName());
        messagingStyleMap.put(notifData.getGroupId(), messagingStyle);
    }

    private static Notification buildSummary(Context context, String groupKey, ArrayList<NotifData> notifDataList) {

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(TRACK_NOTIF_ID, groupKey);
        Log.d(TAG, "TRACK_NOTIF_ID -> " + groupKey);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(intent);

        Intent deleteIntent = new Intent(context, NotifDeleteBroadcastRecvr.class);
        deleteIntent.putExtra("groupId", groupKey);
        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, deleteIntent, 0);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Constants.CHAT_NOTIF_CHANNEL)
                .setStyle(new NotificationCompat.MessagingStyle("Me"))
                .setContentTitle("Lubble")
                .setWhen(notifDataList.get(notifDataList.size() - 1).getTimestamp())
                .setSmallIcon(R.drawable.ic_lubble_notif)
                .setShowWhen(true)
                .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                .setGroup(groupKey)
                .setAutoCancel(true)
                .setContentIntent(stackBuilder.getPendingIntent(SUMMARY_ID, PendingIntent.FLAG_UPDATE_CURRENT))
                .setGroupSummary(true)
                .setDeleteIntent(deletePendingIntent)
                .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY);

        NotificationCompat.InboxStyle inbox = new NotificationCompat.InboxStyle();
        for (NotifData notifData : notifDataList) {
            inbox.addLine(notifData.getMessageBody());
        }

        inbox.setSummaryText(String.format("+ %d", notifDataList.size()));

        builder.setStyle(inbox);
        return builder.build();
    }


    private static void persistNewMessage(NotifData notifData) {
        UnreadChatsSharedPrefs.getInstance().getPreferences().edit().putString(notifData.getMessageId(), new Gson().toJson(notifData)).commit();
        Log.d(TAG, "persistNewMessage: DONE: " + notifData.getMessageBody());
        addGroupIdMapping(notifData);
    }

    private static void addGroupIdMapping(NotifData notifData) {
        final String groupId = notifData.getGroupId();
        final SharedPreferences groupPrefs = GroupMappingSharedPrefs.getInstance().getPreferences();
        final int savedGroupInt = groupPrefs.getInt(groupId, -1);
        if (savedGroupInt == -1) {
            final int lastInt = groupPrefs.getInt(Constants.LAST_GROUP_MAPPING_ID, 500);
            groupPrefs.edit().putInt(groupId, lastInt + 1).commit();
            groupPrefs.edit().putInt(Constants.LAST_GROUP_MAPPING_ID, lastInt + 1).commit();
            Log.d(TAG, "Added new map ID: " + lastInt + 1);
        } else {
            Log.d(TAG, "Map ID exists: " + savedGroupInt);
        }
    }

    private static ArrayList<NotifData> getAllMsgs() {

        final ArrayList<NotifData> notifDataList = new ArrayList<>();

        final Map<String, String> all = (Map<String, String>) UnreadChatsSharedPrefs.getInstance().getPreferences().getAll();
        for (String jsonStr : all.values()) {
            final NotifData readNotifData = new Gson().fromJson(jsonStr, NotifData.class);
            notifDataList.add(readNotifData);
        }
        return notifDataList;
    }

    public static void deleteUnreadMsgsForGroupId(String groupId, Context context) {
        // cancel notification
        final NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(getNotifId(groupId));

        // clear sharedPrefs
        GroupMappingSharedPrefs.getInstance().getPreferences().edit().remove(groupId).commit();
        final SharedPreferences chatSharedPrefs = UnreadChatsSharedPrefs.getInstance().getPreferences();

        final Map<String, String> chatsMap = (Map<String, String>) chatSharedPrefs.getAll();
        for (Map.Entry<String, String> chatEntry : chatsMap.entrySet()) {
            final NotifData notifData = new Gson().fromJson(chatEntry.getValue(), NotifData.class);
            if (notifData.getGroupId().equalsIgnoreCase(groupId)) {
                Log.d(TAG, "Removing msg: " + notifData.getMessageBody());
                chatSharedPrefs.edit().remove(chatEntry.getKey()).commit();
            }
        }

        if (chatSharedPrefs.getAll().size() == 0) {
            notificationManager.cancel(SUMMARY_ID);
        }
    }

    public static void sendNotifAnalyticEvent(String eventName, Map<String, String> dataMap, Context context) {
        try {
            final Bundle bundle = new Bundle();
            for (Map.Entry<String, String> entry : dataMap.entrySet()) {
                if (!entry.getKey().toLowerCase().contains("thumbnail")) {
                    bundle.putString(entry.getKey(), entry.getValue());
                }
            }
            Analytics.triggerEvent(eventName, bundle, context);
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    public static void sendNotifAnalyticEvent(String eventName, String groupId, Context context) {
        try {
            final Bundle bundle = new Bundle();
            bundle.putString("groupId", groupId);
            Analytics.triggerEvent(eventName, bundle, context);
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

}
