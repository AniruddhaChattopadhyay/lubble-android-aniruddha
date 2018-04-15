package in.lubble.app.firebase;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

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
import in.lubble.app.notifications.NotifData;
import in.lubble.app.utils.NotifUtils;
import in.lubble.app.utils.StringUtils;

import static in.lubble.app.firebase.RealtimeDbHelper.getMessagesRef;

/**
 * Created by ishaan on 26/1/18.
 */

public class FcmService extends FirebaseMessagingService {

    private static final String TAG = "FcmService";
    public static final String LOGOUT_ACTION = "LOGOUT_ACTION";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Tasks shorter than 10secs. For long running tasks, schedule a job to Firebase Job Scheduler.
        final Map<String, String> dataMap = remoteMessage.getData();
        if (dataMap.size() > 0) {
            Log.d(TAG, "Message data payload: " + dataMap);

            final String type = dataMap.get("type");
            if (StringUtils.isValidString(type) && type.equalsIgnoreCase("deleteUser")
                    && dataMap.get("uid").equalsIgnoreCase(FirebaseAuth.getInstance().getUid())) {

                LubbleSharedPrefs.getInstance().setIsLogoutPending(true);
                Intent intent = new Intent(LOGOUT_ACTION);
                intent.putExtra("UID", dataMap.get("uid"));
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            } else {
                // create chat notif
                Gson gson = new Gson();
                JsonElement jsonElement = gson.toJsonTree(dataMap);
                NotifData notifData = gson.fromJson(jsonElement, NotifData.class);

                if (!notifData.getGroupId().equalsIgnoreCase(LubbleSharedPrefs.getInstance().getCurrentActiveGroupId())) {
                    NotifUtils.updateChatNotifs(this, notifData);
                    updateUnreadCounter(notifData);
                    pullNewMsgs(notifData);
                    //sendDeliveryReceipt(notifData);
                }
            }
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
    }
}
