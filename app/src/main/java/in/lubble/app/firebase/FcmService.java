package in.lubble.app.firebase;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.util.Map;

import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.notifications.AppNotifData;
import in.lubble.app.notifications.MutedChatsSharedPrefs;
import in.lubble.app.notifications.NotifData;
import in.lubble.app.utils.AppNotifUtils;
import in.lubble.app.utils.NotifUtils;
import in.lubble.app.utils.StringUtils;

import static in.lubble.app.firebase.RealtimeDbHelper.getAnnouncementsRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getMessagesRef;

/**
 * Created by ishaan on 26/1/18.
 */

public class FcmService extends FirebaseMessagingService {

    private static final String TAG = "FcmService";
    public static final String LOGOUT_ACTION = "LOGOUT_ACTION";

    private ValueEventListener noticeListener;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Tasks shorter than 10secs. For long running tasks, schedule a job to Firebase Job Scheduler.
        if (remoteMessage.getNotification() != null) {
            AppNotifUtils.showAppNotif(this, remoteMessage.getNotification());

        } else if (remoteMessage.getData().size() > 0) {
            final Map<String, String> dataMap = remoteMessage.getData();
            Log.d(TAG, "Message data payload: " + dataMap);

            final String type = dataMap.get("type");
            if (StringUtils.isValidString(type) && "deleteUser".equalsIgnoreCase(type)
                    && dataMap.get("uid").equalsIgnoreCase(FirebaseAuth.getInstance().getUid())) {
                // nuke user!!
                deleteUser(dataMap);
            } else if (StringUtils.isValidString(type) && (
                    "groupInvitation".equalsIgnoreCase(type))
                    || "notice".equalsIgnoreCase(type)
                    || "referralJoined".equalsIgnoreCase(type)) {
                // group invitation notif!
                Log.d(TAG, "onMessageReceived: app type");
                Gson gson = new Gson();
                JsonElement jsonElement = gson.toJsonTree(dataMap);
                AppNotifData appNotifData = gson.fromJson(jsonElement, AppNotifData.class);
                AppNotifUtils.showAppNotif(this, appNotifData);
                // prefetch notices
                pullNotices(dataMap);
            } else if (StringUtils.isValidString(type) && "chat".equalsIgnoreCase(type)) {
                // create chat notif
                createChatNotif(dataMap);
            } else {
                Crashlytics.logException(new IllegalArgumentException("Illegal notif type: " + type));
            }
        }
    }

    private void pullNotices(Map<String, String> dataMap) {
        final String type = dataMap.get("type");
        if (StringUtils.isValidString(type) && "notice".equalsIgnoreCase(type)) {
            noticeListener = getAnnouncementsRef().addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "Notice pre-fetched: " + dataSnapshot.getKey());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void deleteUser(Map<String, String> dataMap) {
        LubbleSharedPrefs.getInstance().setIsLogoutPending(true);
        Intent intent = new Intent(LOGOUT_ACTION);
        intent.putExtra("UID", dataMap.get("uid"));
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void createChatNotif(Map<String, String> dataMap) {
        Gson gson = new Gson();
        JsonElement jsonElement = gson.toJsonTree(dataMap);
        NotifData notifData = gson.fromJson(jsonElement, NotifData.class);

        if (!notifData.getGroupId().equalsIgnoreCase(LubbleSharedPrefs.getInstance().getCurrentActiveGroupId())) {
            // only show notif if that group is not in foreground & the group's notifs are not muted
            if (!MutedChatsSharedPrefs.getInstance().getPreferences().getBoolean(notifData.getGroupId(), false)) {
                NotifUtils.updateChatNotifs(this, notifData);
            }
            updateUnreadCounter(notifData);
            pullNewMsgs(notifData);
            //sendDeliveryReceipt(notifData);
        }
    }

    private void pullNewMsgs(NotifData notifData) {
        RealtimeDbHelper.getMessagesRef().child(notifData.getGroupId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "Pulled: " + dataSnapshot.getKey());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateUnreadCounter(NotifData notifData) {
        RealtimeDbHelper.getUserGroupsRef().child(notifData.getGroupId())
                .child("unreadCount").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer oldCount = 0;
                if (dataSnapshot.getValue() != null) {
                    oldCount = dataSnapshot.getValue(Integer.class);
                }
                dataSnapshot.getRef().setValue(++oldCount);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendDeliveryReceipt(NotifData notifData) {
        getMessagesRef().child(notifData.getGroupId()).child(notifData.getMessageId())
                .child("deliveryReceipts")
                .child(FirebaseAuth.getInstance().getUid())
                .setValue(System.currentTimeMillis());
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
        //todo
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        getAnnouncementsRef().removeEventListener(noticeListener);
    }
}
