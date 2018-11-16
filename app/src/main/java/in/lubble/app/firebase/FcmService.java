package in.lubble.app.firebase;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.NotificationInfo;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.models.AppNotifData;
import in.lubble.app.models.NotifData;
import in.lubble.app.notifications.MutedChatsSharedPrefs;
import in.lubble.app.utils.AppNotifUtils;
import in.lubble.app.utils.NotifUtils;
import in.lubble.app.utils.StringUtils;

import java.util.Map;

import static in.lubble.app.analytics.AnalyticsEvents.NOTIF_SHOWN;
import static in.lubble.app.firebase.RealtimeDbHelper.*;
import static in.lubble.app.marketplace.SellerDashActiv.*;

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

            sendShownAnalyticEvent(dataMap);

            Bundle extras = new Bundle();
            for (Map.Entry<String, String> entry : remoteMessage.getData().entrySet()) {
                extras.putString(entry.getKey(), entry.getValue());
            }

            NotificationInfo info = CleverTapAPI.getNotificationInfo(extras);

            if (info.fromCleverTap) {
                CleverTapAPI.createNotification(getApplicationContext(), extras);
            } else {
                // not from CleverTap handle yourself
                final String type = dataMap.get("type");
                if (StringUtils.isValidString(type) && "deleteUser".equalsIgnoreCase(type)
                        && dataMap.get("uid").equalsIgnoreCase(FirebaseAuth.getInstance().getUid())) {
                    // nuke user!!
                    deleteUser(dataMap);
                } else if (StringUtils.isValidString(type) && (
                        "groupInvitation".equalsIgnoreCase(type))
                        || "notice".equalsIgnoreCase(type)
                        || "referralJoined".equalsIgnoreCase(type)
                        || "new_event".equalsIgnoreCase(type)
                        || "services".equalsIgnoreCase(type)) {
                    Log.d(TAG, "onMessageReceived: type -> " + type);
                    Gson gson = new Gson();
                    JsonElement jsonElement = gson.toJsonTree(dataMap);
                    AppNotifData appNotifData = gson.fromJson(jsonElement, AppNotifData.class);
                    AppNotifUtils.showAppNotif(this, appNotifData);
                    // prefetch notices
                    pullNotices(dataMap);
                } else if (StringUtils.isValidString(type) && "lubb".equalsIgnoreCase(type)) {
                    // create like notif
                    Gson gson = new Gson();
                    JsonElement jsonElement = gson.toJsonTree(dataMap);
                    AppNotifData appNotifData = gson.fromJson(jsonElement, AppNotifData.class);
                    AppNotifUtils.showAppNotif(this, appNotifData);
                } else if (StringUtils.isValidString(type) && ("chat".equalsIgnoreCase(type))) {
                    // create chat notif
                    createChatNotif(dataMap);
                } else if (StringUtils.isValidString(type) && ("dm".equalsIgnoreCase(type))) {
                    // create chat notif
                    createDmNotif(dataMap);
                } else if (StringUtils.isValidString(type) && "mplace_img_done".equalsIgnoreCase(type)) {
                    // mplace image uploaded, send broadcast
                    sendMarketplaceImgBroadcast(dataMap);
                } else if (StringUtils.isValidString(type) && "mplace_approval".equalsIgnoreCase(type)) {
                    // mplace item approval status changed
                    Gson gson = new Gson();
                    JsonElement jsonElement = gson.toJsonTree(dataMap);
                    AppNotifData appNotifData = gson.fromJson(jsonElement, AppNotifData.class);
                    AppNotifUtils.showNormalAppNotif(this, appNotifData);
                } else if (StringUtils.isValidString(type) && "deep_link".equalsIgnoreCase(type)) {
                    Gson gson = new Gson();
                    JsonElement jsonElement = gson.toJsonTree(dataMap);
                    AppNotifData appNotifData = gson.fromJson(jsonElement, AppNotifData.class);
                    AppNotifUtils.showNormalAppNotif(this, appNotifData);
                } else {
                    Crashlytics.logException(new IllegalArgumentException("Illegal notif type: " + type));
                }
            }
        }
    }

    private void sendShownAnalyticEvent(Map<String, String> dataMap) {
        try {
            final Bundle bundle = new Bundle();
            for (Map.Entry<String, String> entry : dataMap.entrySet()) {
                if (!entry.getKey().toLowerCase().contains("thumbnail")) {
                    bundle.putString(entry.getKey(), entry.getValue());
                }
            }
            Analytics.triggerEvent(NOTIF_SHOWN, bundle, this);
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    private void sendMarketplaceImgBroadcast(Map<String, String> dataMap) {

        Intent broadcast = new Intent(ACTION_IMG_DONE)
                .putExtra(EXTRA_IMG_TYPE, dataMap.get("img_type"))
                .putExtra(EXTRA_IMG_ID, dataMap.get("id"))
                .putExtra(EXTRA_IMG_URL, dataMap.get("url"));

        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
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
            updateUnreadCounter(notifData, false);
            pullNewMsgs(notifData);
            //sendDeliveryReceipt(notifData);
        }
    }

    private void createDmNotif(Map<String, String> dataMap) {
        Gson gson = new Gson();
        JsonElement jsonElement = gson.toJsonTree(dataMap);
        NotifData notifData = gson.fromJson(jsonElement, NotifData.class);

        if (!notifData.getGroupId().equalsIgnoreCase(LubbleSharedPrefs.getInstance().getCurrentActiveGroupId())) {
            // only show notif if that group is not in foreground & the group's notifs are not muted
            if (!MutedChatsSharedPrefs.getInstance().getPreferences().getBoolean(notifData.getGroupId(), false)) {
                NotifUtils.updateChatNotifs(this, notifData);
            }
            updateUnreadCounter(notifData, true);
            pullNewDmMsgs(notifData);
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

    private void pullNewDmMsgs(NotifData notifData) {
        RealtimeDbHelper.getDmMessagesRef().child(notifData.getGroupId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "Pulled: " + dataSnapshot.getKey());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateUnreadCounter(NotifData notifData, boolean isDm) {
        DatabaseReference unreadCountRef = RealtimeDbHelper.getUserGroupsRef().child(notifData.getGroupId()).child("unreadCount");
        if (isDm) {
            if (notifData.getIsSeller()) {
                unreadCountRef = RealtimeDbHelper.getSellerRef()
                        .child(String.valueOf(LubbleSharedPrefs.getInstance().getSellerId()))
                        .child("dms")
                        .child(notifData.getGroupId())
                        .child("unreadCount");
            } else {
                unreadCountRef = RealtimeDbHelper.getUserDmsRef().child(notifData.getGroupId()).child("unreadCount");
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

    private void sendDeliveryReceipt(NotifData notifData) {
        getMessagesRef().child(notifData.getGroupId()).child(notifData.getMessageId())
                .child("deliveryReceipts")
                .child(FirebaseAuth.getInstance().getUid())
                .setValue(System.currentTimeMillis());
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        // Get updated InstanceID token.
        Log.d(TAG, "Refreshed token: " + token);
        CleverTapAPI.getDefaultInstance(this).pushFcmRegistrationId(token, true);
        try {
            getThisUserRef().child("token").setValue(token);
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
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
        if (noticeListener != null) {
            getAnnouncementsRef().removeEventListener(noticeListener);
        }
    }
}
