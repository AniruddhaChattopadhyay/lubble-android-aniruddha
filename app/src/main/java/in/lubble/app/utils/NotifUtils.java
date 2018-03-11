package in.lubble.app.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import in.lubble.app.Constants;
import in.lubble.app.R;
import in.lubble.app.notifications.NotifData;

/**
 * Created by ishaan on 11/3/18.
 */

public class NotifUtils {

    private static final String TAG = "NotifUtils";
    private static final String GROUP_KEY = "1337";
    private static final int SUMMARY_ID = 1325;

    private static HashMap<Integer, NotificationCompat.MessagingStyle> messagingStyleMap;
    private static SharedPreferences chatSharedPrefs;
    private static SharedPreferences groupMappingSharedPrefs;

    public static void updateChatNotifs(Context context, NotifData notifData) {
        messagingStyleMap = new HashMap<>();
        chatSharedPrefs = context.getSharedPreferences("chats2", Context.MODE_PRIVATE);
        groupMappingSharedPrefs = context.getSharedPreferences("group_mapping2", Context.MODE_PRIVATE);

        persistNewMessage(notifData);
        ArrayList<NotifData> msgList = getAllMsgs();
        Log.d(TAG, "read notif count: " + msgList.size());
        sendAllNotifs(context, msgList);

    }

    private static void sendAllNotifs(Context context, ArrayList<NotifData> notifDataList) {

        final NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        for (NotifData notifData : notifDataList) {
            int notifId = getNotifId(notifData);
            final Notification notification = buildNotification(context, getMessagingStyle(notifId), notifData, GROUP_KEY);
            notificationManager.notify(notifId, notification);
            Notification summary = buildSummary(context, GROUP_KEY);
            notificationManager.notify(SUMMARY_ID, summary);
        }
    }

    private static int getNotifId(NotifData notifData) {
        final int notifID = groupMappingSharedPrefs.getInt(notifData.getGroupId(), -1);
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

    private static Notification buildNotification(Context context, NotificationCompat.MessagingStyle messagingStyle, NotifData notifData, String groupKey) {
        messagingStyle.setConversationTitle(notifData.getGroupName());

        messagingStyle.addMessage(notifData.getMessageBody(), notifData.getTimestamp(), notifData.getAuthorName());
        return new NotificationCompat.Builder(context, Constants.DEFAULT_NOTIF_CHANNEL)
                .setStyle(messagingStyle)
                .setSmallIcon(R.drawable.ic_upload)
                .setShowWhen(true)
                .setGroup(groupKey)
                .build();
    }

    private static Notification buildSummary(Context context, String groupKey) {
        return new NotificationCompat.Builder(context, Constants.DEFAULT_NOTIF_CHANNEL)
                .setStyle(new NotificationCompat.MessagingStyle("Me"))
                .setContentTitle("Nougat Messenger")
                .setContentText("You have unread messages")
                //.setWhen(message.timestamp())
                .setSmallIcon(R.drawable.ic_upload)
                .setShowWhen(true)
                .setGroup(groupKey)
                .setGroupSummary(true)
                .build();
    }


    private static void persistNewMessage(NotifData notifData) {
        chatSharedPrefs.edit().putString(notifData.getMessageId(), new Gson().toJson(notifData)).commit();
        Log.d(TAG, "persistNewMessage: DONE: " + notifData.getMessageBody());
        addGroupIdMapping(notifData);
    }

    private static void addGroupIdMapping(NotifData notifData) {
        final String groupId = notifData.getGroupId();
        final int savedGroupInt = groupMappingSharedPrefs.getInt(groupId, -1);
        if (savedGroupInt == -1) {
            final int lastInt = groupMappingSharedPrefs.getInt(Constants.LAST_GROUP_MAPPING_ID, 500);
            groupMappingSharedPrefs.edit().putInt(groupId, lastInt + 1).commit();
            groupMappingSharedPrefs.edit().putInt(Constants.LAST_GROUP_MAPPING_ID, lastInt + 1).commit();
            Log.d(TAG, "Added new map ID: " + lastInt + 1);
        } else {
            Log.d(TAG, "Map ID exists: " + savedGroupInt);
        }
    }

    private static ArrayList<NotifData> getAllMsgs() {

        final ArrayList<NotifData> notifDataList = new ArrayList<>();

        final Map<String, String> all = (Map<String, String>) chatSharedPrefs.getAll();
        for (String jsonStr : all.values()) {
            final NotifData readNotifData = new Gson().fromJson(jsonStr, NotifData.class);
            notifDataList.add(readNotifData);
        }
        return notifDataList;
    }

}
