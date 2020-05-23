package in.lubble.app.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.util.Map;
import java.util.MissingFormatArgumentException;

import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.NotifData;
import in.lubble.app.utils.NotifUtils;

import static in.lubble.app.utils.NotifUtils.isGroupSnoozed;
import static in.lubble.app.utils.NotifUtils.sendNotifAnalyticEvent;

public class NotificationResultReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationResultRecei";
    private final static int PAGE_SIZE = 20;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: creating notif");
        if (intent != null && intent.hasExtra("remoteMessage")) {
            final RemoteMessage remoteMessage = intent.getParcelableExtra("remoteMessage");
            final Map<String, String> dataMap = remoteMessage.getData();
            String type = dataMap.get("type");
            if ("chat".equalsIgnoreCase(type)) {
                createChatNotif(context, dataMap);
            } else if ("dm".equalsIgnoreCase(type)) {
                createDmNotif(context, dataMap);
            } else {
                Crashlytics.logException(new IllegalArgumentException("NotifResultRecvr: notif recvd with illegal type"));
            }
        } else {
            Crashlytics.logException(new MissingFormatArgumentException("NotifResultRecvr: notif broadcast recvd with no intent data"));
        }
    }

    private void createChatNotif(Context context, Map<String, String> dataMap) {
        Gson gson = new Gson();
        JsonElement jsonElement = gson.toJsonTree(dataMap);
        NotifData notifData = gson.fromJson(jsonElement, NotifData.class);
        // only show notif if that group is not muted or snoozed
        if (isGroupSnoozed(notifData.getGroupId())) {
            sendNotifAnalyticEvent(AnalyticsEvents.NOTIF_SNOOZED, dataMap, context);
        } else {
            NotifUtils.updateChatNotifs(context, notifData);
        }
        updateUnreadCounter(notifData, false);
        pullNewMsgs(notifData);
        //sendDeliveryReceipt(notifData);
    }

    private void createDmNotif(Context context, Map<String, String> dataMap) {
        Gson gson = new Gson();
        JsonElement jsonElement = gson.toJsonTree(dataMap);
        NotifData notifData = gson.fromJson(jsonElement, NotifData.class);
        // only show notif if that group is not in foreground & the group's notifs are not muted
        if (!isGroupSnoozed(notifData.getGroupId())) {
            NotifUtils.updateChatNotifs(context, notifData);
        } else {
            sendNotifAnalyticEvent(AnalyticsEvents.NOTIF_SNOOZED, dataMap, context);
        }
        updateUnreadCounter(notifData, true);
        pullNewDmMsgs(notifData);
        //sendDeliveryReceipt(notifData);
    }

    private void updateUnreadCounter(NotifData notifData, boolean isDm) {
        DatabaseReference unreadCountRef = RealtimeDbHelper.getLubbleGroupsRef().child(notifData.getGroupId()).child("members").child(FirebaseAuth.getInstance().getUid()).child("unreadCount");
        if (isDm) {
            if (notifData.getIsSeller()) {
                unreadCountRef = RealtimeDbHelper.getDmsRef()
                        .child(notifData.getGroupId())
                        .child("members")
                        .child(String.valueOf(LubbleSharedPrefs.getInstance().getSellerId()))
                        .child("unreadCount");
            } else {
                unreadCountRef = RealtimeDbHelper.getDmsRef().child(notifData.getGroupId()).child("members").child(FirebaseAuth.getInstance().getUid()).child("unreadCount");
            }
        }
        unreadCountRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Long oldCount = 0L;
                if (dataSnapshot.getValue() != null) {
                    oldCount = dataSnapshot.getValue(Long.class);
                }
                dataSnapshot.getRef().setValue(++oldCount);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void pullNewMsgs(NotifData notifData) {
        //what works: exactly the same query as in chatFrag
        RealtimeDbHelper.getMessagesRef().child(notifData.getGroupId()).orderByChild("serverTimestamp").limitToLast(PAGE_SIZE).keepSynced(true);
    }

    private void pullNewDmMsgs(NotifData notifData) {
        RealtimeDbHelper.getDmMessagesRef().child(notifData.getGroupId()).orderByChild("serverTimestamp").keepSynced(true);
    }

}
