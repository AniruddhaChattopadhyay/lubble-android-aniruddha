package in.lubble.app.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import in.lubble.app.Constants;
import in.lubble.app.MainActivity;
import in.lubble.app.R;
import in.lubble.app.notifications.GroupMappingSharedPrefs;
import in.lubble.app.notifications.NotifData;
import in.lubble.app.notifications.UnreadChatsSharedPrefs;

/**
 * Created by ishaan on 11/3/18.
 */

public class NotifUtils {

    private static final String TAG = "NotifUtils";
    private static final String GROUP_KEY = "1337";
    private static final int SUMMARY_ID = 1325;

    private static HashMap<Integer, NotificationCompat.MessagingStyle> messagingStyleMap;

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

    private static void sendAllNotifs(Context context, ArrayList<NotifData> notifDataList) {

        final NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        for (NotifData notifData : notifDataList) {
            int notifId = getNotifId(notifData.getGroupId());
            buildGroupNotification(context, getMessagingStyle(notifId), notifData, GROUP_KEY);
        }

        for (Map.Entry<Integer, NotificationCompat.MessagingStyle> map : messagingStyleMap.entrySet()) {
            final Integer notifId = map.getKey();
            final Notification notification = new NotificationCompat.Builder(context, Constants.DEFAULT_NOTIF_CHANNEL)
                    .setStyle(map.getValue())
                    .setSmallIcon(R.drawable.ic_upload)
                    .setShowWhen(true)
                    .setGroup(GROUP_KEY)
                    .setDefaults(0)
                    .setContentIntent(PendingIntent.getActivity(context, 0,
                            new Intent(context, MainActivity.class), 0))
                    .setGroupAlertBehavior(Notification.GROUP_ALERT_SUMMARY)
                    .build();
            notificationManager.notify(notifId, notification);
        }
        Notification summary = buildSummary(context, GROUP_KEY, notifDataList.get(notifDataList.size() - 1).getTimestamp());
        notificationManager.notify(SUMMARY_ID, summary);
    }

    private static int getNotifId(String groupId) {
        final int notifID = GroupMappingSharedPrefs.getInstance().getPreferences().getInt(groupId, -1);
        if (notifID == -1) {
            Log.e(TAG, "getNotifId: is not found");
        }
        return notifID;
    }

    private static NotificationCompat.MessagingStyle getMessagingStyle(int notifId) {
        NotificationCompat.MessagingStyle messagingStyle = messagingStyleMap.get(notifId);
        if (messagingStyle == null) {
            final NotificationCompat.MessagingStyle newMsgStyle = new NotificationCompat.MessagingStyle("Me");
            messagingStyleMap.put(notifId, newMsgStyle);
            messagingStyle = newMsgStyle;
        }
        return messagingStyle;
    }

    private static void buildGroupNotification(Context context, NotificationCompat.MessagingStyle messagingStyle, NotifData notifData, String groupKey) {
        messagingStyle.setConversationTitle(notifData.getGroupName());

        messagingStyle.addMessage(notifData.getMessageBody(), notifData.getTimestamp(), notifData.getAuthorName());
        messagingStyleMap.put(getNotifId(notifData.getGroupId()), messagingStyle);
    }

    private static Notification buildSummary(Context context, String groupKey, long timestamp) {
        //todo this must be inbox style becoz on < M only this notif is shown
        return new NotificationCompat.Builder(context, Constants.DEFAULT_NOTIF_CHANNEL)
                .setStyle(new NotificationCompat.MessagingStyle("Me"))
                .setContentTitle("Nougat Messenger")
                .setContentText("You have unread messages")
                .setWhen(timestamp)
                .setSmallIcon(R.drawable.ic_upload)
                .setShowWhen(true)
                .setGroup(groupKey)
                .setGroupSummary(true)
                .setGroupAlertBehavior(Notification.GROUP_ALERT_SUMMARY)
                .build();
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

}
