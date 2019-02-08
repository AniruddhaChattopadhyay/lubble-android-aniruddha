package in.lubble.app.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import in.lubble.app.Constants;
import in.lubble.app.GlideApp;
import in.lubble.app.MainActivity;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.chat.ChatActivity;
import in.lubble.app.models.NotifData;
import in.lubble.app.notifications.GroupMappingSharedPrefs;
import in.lubble.app.notifications.NotifActionBroadcastRecvr;
import in.lubble.app.notifications.UnreadChatsSharedPrefs;

import java.util.*;

import static in.lubble.app.chat.ChatActivity.EXTRA_DM_ID;
import static in.lubble.app.chat.ChatActivity.EXTRA_GROUP_ID;
import static in.lubble.app.notifications.NotifActionBroadcastRecvr.ACTION_MARK_AS_READ;
import static in.lubble.app.utils.AppNotifUtils.TRACK_NOTIF_ID;

/**
 * Created by ishaan on 11/3/18.
 */

public class NotifUtils {

    private static final String TAG = "NotifUtils";
    private static final String GROUP_KEY = "1337";
    private static final int SUMMARY_ID = 1325;

    private static HashMap<String, NotificationCompat.MessagingStyle> messagingStyleMap;

    public static void showAllPendingChatNotifs(Context context) {
        messagingStyleMap = new HashMap<>();

        ArrayList<NotifData> msgList = getAllMsgs();
        if (!msgList.isEmpty()) {
            sortListByTime(msgList);
            sendAllNotifs(context, msgList);
        }
    }

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

    private static void sendAllNotifs(final Context context, final ArrayList<NotifData> notifDataList) {

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
            String channel = Constants.NEW_CHAT_NOTIF_CHANNEL;
            if (TextUtils.isEmpty(map.getValue().getConversationTitle())) {
                // it is a DM notif
                intent.putExtra(EXTRA_DM_ID, groupId);
                intent.putExtra(TRACK_NOTIF_ID, groupId);
                channel = Constants.DM_CHAT_NOTIF_CHANNEL;
            } else {
                intent.putExtra(EXTRA_GROUP_ID, groupId);
                intent.putExtra(TRACK_NOTIF_ID, groupId);
                channel = Constants.NEW_CHAT_NOTIF_CHANNEL;
            }
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntentWithParentStack(intent);

            Intent deleteIntent = new Intent(context, NotifDeleteBroadcastRecvr.class);
            deleteIntent.putExtra("groupId", groupId);
            PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), notifId, deleteIntent, 0);

            final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channel)
                    .setStyle(map.getValue())
                    .setSmallIcon(R.drawable.ic_lubble_notif)
                    .setShowWhen(true)
                    .setGroup(GROUP_KEY)
                    .setDefaults(0)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setAutoCancel(true)
                    .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                    .setContentIntent(stackBuilder.getPendingIntent(notifId, PendingIntent.FLAG_UPDATE_CURRENT))
                    .setDeleteIntent(deletePendingIntent)
                    .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY);

            if (!TextUtils.isEmpty(map.getValue().getConversationTitle())) {
                // not a DM, add actions
                Intent markReadIntent = new Intent(context, NotifActionBroadcastRecvr.class);
                markReadIntent.setAction(ACTION_MARK_AS_READ);
                markReadIntent.putExtra("markread.groupId", groupId);
                PendingIntent markReadPendingIntent =
                        PendingIntent.getBroadcast(context, getNotifId(groupId), markReadIntent, 0);

                builder.addAction(0, "Mark As Read", markReadPendingIntent);
            }

            if (StringUtils.isValidString(groupDpUrl)) {
                GlideApp.with(context).asBitmap().load(groupDpUrl).circleCrop().into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        builder.setLargeIcon(resource);
                        notificationManager.notify(notifId, builder.build());
                        Notification summary = buildSummary(context, GROUP_KEY, notifDataList);
                        notificationManager.notify(SUMMARY_ID, summary);
                    }
                });
            } else {
                builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_group));
                notificationManager.notify(notifId, builder.build());
                Notification summary = buildSummary(context, GROUP_KEY, notifDataList);
                notificationManager.notify(SUMMARY_ID, summary);
            }
        }
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

    public static int getNotifId(String groupId) {
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

        Intent intent;
        if (isSummaryForOneGroup(notifDataList)) {
            intent = new Intent(context, ChatActivity.class);
            final NotifData firstNotifData = notifDataList.get(0);
            String groupId = firstNotifData.getGroupId();
            if (firstNotifData.getNotifType().equalsIgnoreCase("dm")) {
                // it is a DM notif
                intent.putExtra(EXTRA_DM_ID, groupId);
                intent.putExtra(TRACK_NOTIF_ID, groupId);
            } else {
                intent.putExtra(EXTRA_GROUP_ID, groupId);
                intent.putExtra(TRACK_NOTIF_ID, groupId);
            }
        } else {
            intent = new Intent(context, MainActivity.class);
            intent.putExtra(TRACK_NOTIF_ID, groupKey);
        }
        Log.d(TAG, "TRACK_NOTIF_ID -> " + groupKey);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(intent);

        Intent deleteIntent = new Intent(context, NotifDeleteBroadcastRecvr.class);
        deleteIntent.putExtra("groupId", groupKey);
        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, deleteIntent, 0);

        final NotifData lastNotifData = notifDataList.get(notifDataList.size() - 1);
        String channel = "dm".equalsIgnoreCase(lastNotifData.getNotifType()) ? Constants.DM_CHAT_NOTIF_CHANNEL : Constants.NEW_CHAT_NOTIF_CHANNEL;
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channel)
                .setStyle(new NotificationCompat.MessagingStyle("Me"))
                .setContentTitle("Lubble")
                .setWhen(lastNotifData.getTimestamp())
                .setSmallIcon(R.drawable.ic_lubble_notif)
                .setShowWhen(true)
                .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                .setGroup(groupKey)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(true)
                .setContentIntent(stackBuilder.getPendingIntent(SUMMARY_ID, PendingIntent.FLAG_UPDATE_CURRENT))
                .setGroupSummary(true)
                .setDeleteIntent(deletePendingIntent)
                .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY);

        NotificationCompat.InboxStyle inbox = new NotificationCompat.InboxStyle();
        for (NotifData notifData : notifDataList) {
            inbox.addLine(notifData.getMessageBody());
        }
        builder.setContentText(lastNotifData.getMessageBody());

        inbox.setSummaryText(String.format("+ %d", notifDataList.size()));

        builder.setStyle(inbox);
        return builder.build();
    }

    private static boolean isSummaryForOneGroup(ArrayList<NotifData> notifDataList) {
        for (int i = 1; i < notifDataList.size(); i++) {
            if (!notifDataList.get(i).getGroupId().equalsIgnoreCase(notifDataList.get(i - 1).getGroupId())) {
                return false;
            }
        }
        return true;
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
