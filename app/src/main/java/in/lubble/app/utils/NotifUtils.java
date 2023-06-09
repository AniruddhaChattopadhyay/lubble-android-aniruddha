package in.lubble.app.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.lubble.app.Constants;
import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.MainActivity;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.chat.ChatActivity;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.NotifData;
import in.lubble.app.notifications.GroupMappingSharedPrefs;
import in.lubble.app.notifications.NotifActionBroadcastRecvr;
import in.lubble.app.notifications.SnoozedGroupsSharedPrefs;
import in.lubble.app.notifications.UnreadChatsSharedPrefs;

import static in.lubble.app.Constants.IS_NOTIF_SNOOZE_ON;
import static in.lubble.app.chat.ChatActivity.EXTRA_DM_ID;
import static in.lubble.app.chat.ChatActivity.EXTRA_GROUP_ID;
import static in.lubble.app.firebase.RealtimeDbHelper.getDmsRef;
import static in.lubble.app.notifications.NotifActionBroadcastRecvr.ACTION_BLOCK;
import static in.lubble.app.notifications.NotifActionBroadcastRecvr.ACTION_MARK_AS_READ;
import static in.lubble.app.notifications.NotifActionBroadcastRecvr.ACTION_REPLY;
import static in.lubble.app.notifications.NotifActionBroadcastRecvr.ACTION_SNOOZE;
import static in.lubble.app.notifications.NotifActionBroadcastRecvr.KEY_TEXT_REPLY;
import static in.lubble.app.notifications.SnoozedGroupsSharedPrefs.DISABLED_NOTIFS_TS;
import static in.lubble.app.utils.AppNotifUtils.TRACK_NOTIF_ID;

/**
 * Created by ishaan on 11/3/18.
 */

public class NotifUtils {

    private static final String TAG = "NotifUtils";
    private static final String GROUP_KEY = "1337";
    private static final int SUMMARY_ID = 1325;

    private static HashMap<String, NotificationCompat.MessagingStyle> messagingStyleMap;

    public static void showAllPendingChatNotifs(Context context, boolean clearPrevNotif) {
        messagingStyleMap = new HashMap<>();

        if (clearPrevNotif) {
            final NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.cancel(SUMMARY_ID);
            }
        }
        ArrayList<NotifData> msgList = getAllMsgs();
        if (!msgList.isEmpty()) {
            sortListByTime(msgList);
            sendAllNotifs(context, msgList, false);
        }
    }

    public static void updateChatNotifs(Context context, NotifData notifData) {
        messagingStyleMap = new HashMap<>();

        persistNewMessage(notifData);
        ArrayList<NotifData> msgList = getAllMsgs();
        sortListByTime(msgList);
        Log.d(TAG, "read notif count: " + msgList.size());
        String uid = FirebaseAuth.getInstance().getUid();
        if (notifData.getNotifType().equalsIgnoreCase("dm")
                || (uid != null && uid.equalsIgnoreCase(LubbleSharedPrefs.getInstance().getSupportUid()))) {
            // group notifs must not be sent, send only dm notifs
            sendAllNotifs(context, msgList, notifData.getNotifType().equalsIgnoreCase("dm"));
        }

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

    private static void sendAllNotifs(final Context context, final ArrayList<NotifData> notifDataList, boolean isDmOnly) {
        if (LubbleSharedPrefs.getInstance().getShowNotifDigest() || isDmOnly) {
            sendNotifAnalyticEvent(AnalyticsEvents.NOTIF_DIGEST_CREATED, notifDataList.get(0).getGroupId(), context);

            final NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            for (NotifData notifData : notifDataList) {
                if (!isDmOnly || notifData.getNotifType().equalsIgnoreCase("dm")) {
                    buildGroupNotification(getMessagingStyle(notifData.getGroupId()), notifData);
                }
            }

            for (Map.Entry<String, NotificationCompat.MessagingStyle> map : messagingStyleMap.entrySet()) {
                final String groupId = map.getKey();
                final Integer notifId = getNotifId(groupId);
                NotifData notifData = getInfo(notifDataList,groupId);
                final String authorId = notifData.getAuthorId();
                final String groupDpUrl = notifData.getGroupDpUrl();
                final boolean isBlockNeeded = notifData.getBlockNeeded();

                Intent intent = new Intent(context, ChatActivity.class);
                String channel;
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
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY);

                List<NotificationCompat.MessagingStyle.Message> messageList = map.getValue().getMessages();
                if (messageList.size() > 0) {
                    NotificationCompat.MessagingStyle.Message lastMsg = messageList.get(messageList.size() - 1);
                    builder.setWhen(lastMsg.getTimestamp());
                }

                if (!TextUtils.isEmpty(map.getValue().getConversationTitle())) {
                    // not a DM, add actions
                    addActionReply(context, groupId, builder);
                    if (FirebaseRemoteConfig.getInstance().getBoolean(IS_NOTIF_SNOOZE_ON)) {
                        addActionSnooze(context, groupId, builder);
                    }
                    addActionMarkAsRead(context, groupId, builder);
                }
                //for dms
               else{
                   if (groupId != null ) {
                        if(isBlockNeeded)
                            addActionBlock(context,groupId,authorId ,builder);
                   }
               }

                if (StringUtils.isValidString(groupDpUrl)) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            GlideApp.with(context).asBitmap().load(groupDpUrl).circleCrop().into(new CustomTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    builder.setLargeIcon(resource);
                                    notificationManager.notify(notifId, builder.build());
                                    Notification summary = buildSummary(context, GROUP_KEY, notifDataList);
                                    notificationManager.notify(SUMMARY_ID, summary);
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {

                                }
                            });
                        }
                    });
                } else {
                    builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_group));
                    notificationManager.notify(notifId, builder.build());
                    Notification summary = buildSummary(context, GROUP_KEY, notifDataList);
                    notificationManager.notify(SUMMARY_ID, summary);
                }
                LubbleSharedPrefs.getInstance().setShowNotifDigest(false);
            }
        }
    }

    private static void addActionBlock(Context context, String groupId, String authorId, NotificationCompat.Builder builder) {
        Intent markBlockIntent = new Intent(context, NotifActionBroadcastRecvr.class);
        markBlockIntent.setAction(ACTION_BLOCK);
        markBlockIntent.putExtra("markBlock.groupId", groupId);
        markBlockIntent.putExtra("markBlock.authorId", authorId); // put author id here
        PendingIntent markBlockPendingIntent =
                PendingIntent.getBroadcast(context, getNotifId(groupId), markBlockIntent, 0);

        builder.addAction(0, "Block", markBlockPendingIntent);
    }
    private static void addActionReply(Context context, String groupId, NotificationCompat.Builder builder) {
        RemoteInput remoteInput = new RemoteInput.Builder(KEY_TEXT_REPLY)
                .setLabel("Reply")
                .build();

        Intent replyIntent = new Intent(context, NotifActionBroadcastRecvr.class);
        replyIntent.setAction(ACTION_REPLY);
        replyIntent.putExtra("reply.groupId", groupId);

        PendingIntent replyPendingIntent =
                PendingIntent.getBroadcast(context,
                        getNotifId(groupId),
                        replyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        // Create the reply action and add the remote input.
        NotificationCompat.Action action =
                new NotificationCompat.Action.Builder(0,
                        "Reply", replyPendingIntent)
                        .addRemoteInput(remoteInput)
                        .build();

        builder.addAction(action);
    }

    private static void addActionMarkAsRead(Context context, String groupId, NotificationCompat.Builder builder) {
        Intent markReadIntent = new Intent(context, NotifActionBroadcastRecvr.class);
        markReadIntent.setAction(ACTION_MARK_AS_READ);
        markReadIntent.putExtra("markread.groupId", groupId);
        PendingIntent markReadPendingIntent =
                PendingIntent.getBroadcast(context, getNotifId(groupId), markReadIntent, 0);

        builder.addAction(0, "Mark As Read", markReadPendingIntent);
    }

    private static void addActionSnooze(Context context, String groupId, NotificationCompat.Builder builder) {
        Intent snoozeIntent = new Intent(context, NotifActionBroadcastRecvr.class);
        snoozeIntent.setAction(ACTION_SNOOZE);
        snoozeIntent.putExtra("snooze.groupId", groupId);
        PendingIntent snoozePendingIntent =
                PendingIntent.getBroadcast(context, getNotifId(groupId), snoozeIntent, 0);

        builder.addAction(0, "Snooze", snoozePendingIntent);
    }

    private static boolean getIsBlockNeeded(ArrayList<NotifData> notifDataList, String groupId) {
        for (NotifData notifData : notifDataList) {
            if (notifData.getGroupId().equalsIgnoreCase(groupId)) {
                return notifData.getBlockNeeded();
            }
        }
        return false;
    }

    @Nullable
    private static String getAuthorId(ArrayList<NotifData> notifDataList, String groupId) {
        for (NotifData notifData : notifDataList) {
            if (notifData.getGroupId().equalsIgnoreCase(groupId)) {
                return notifData.getAuthorId();
            }
        }
        return null;
    }

    @Nullable
    private static NotifData getInfo(ArrayList<NotifData> notifDataList, String groupId){
        for (NotifData notifData : notifDataList) {
            if (notifData.getGroupId().equalsIgnoreCase(groupId)) {
                return notifData;
            }
        }
        return null;
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
            messagingStyle.setGroupConversation(true);
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
            inbox.addLine(notifData.getAuthorName() + ": " + notifData.getMessageBody());
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
        LubbleSharedPrefs.getInstance().setShowNotifDigest(true);
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
            if (!isGroupSnoozed(readNotifData.getGroupId())) {
                notifDataList.add(readNotifData);
            }
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

    public static boolean isGroupSnoozed(String groupId) {
        long snoozedTill = SnoozedGroupsSharedPrefs.getInstance().getPreferences().getLong(groupId, 0L);
        return (System.currentTimeMillis() <= snoozedTill) || snoozedTill == DISABLED_NOTIFS_TS;
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
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    public static void sendNotifAnalyticEvent(String eventName, String groupId, Context context) {
        try {
            final Bundle bundle = new Bundle();
            bundle.putString("groupId", groupId);
            Analytics.triggerEvent(eventName, bundle, context);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

}
