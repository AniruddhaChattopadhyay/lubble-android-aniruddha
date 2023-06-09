package in.lubble.app.firebase;

import static in.lubble.app.Constants.NEW_CHAT_ACTION;
import static in.lubble.app.firebase.RealtimeDbHelper.getAnnouncementsRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getMessagesRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getThisUserRef;
import static in.lubble.app.marketplace.SellerDashActiv.ACTION_IMG_DONE;
import static in.lubble.app.marketplace.SellerDashActiv.EXTRA_IMG_ID;
import static in.lubble.app.marketplace.SellerDashActiv.EXTRA_IMG_TYPE;
import static in.lubble.app.marketplace.SellerDashActiv.EXTRA_IMG_URL;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.pushnotification.NotificationInfo;
import com.freshchat.consumer.sdk.Freshchat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.Map;

import in.lubble.app.BuildConfig;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.models.AppNotifData;
import in.lubble.app.models.NotifData;
import in.lubble.app.notifications.UnreadChatsSharedPrefs;
import in.lubble.app.utils.AppNotifUtils;
import in.lubble.app.utils.NotifUtils;
import in.lubble.app.utils.StringUtils;

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

        } else if (Freshchat.isFreshchatNotification(remoteMessage)) {
            Freshchat.handleFcmMessage(this, remoteMessage);
        } else if (remoteMessage.getData().size() > 0) {
            final Map<String, String> dataMap = remoteMessage.getData();
            Log.d(TAG, "Message data payload: " + dataMap);

            NotifUtils.sendNotifAnalyticEvent(AnalyticsEvents.NOTIF_RECVD, dataMap, this);

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
                    LubbleSharedPrefs.getInstance().setShowRatingDialog(true);
                } else if (StringUtils.isValidString(type) && (("chat".equalsIgnoreCase(type)) || "dm".equalsIgnoreCase(type))) {
                    // create chat notif
                    Intent broadcast = new Intent();
                    broadcast.putExtra("remoteMessage", remoteMessage);
                    broadcast.setAction(NEW_CHAT_ACTION);
                    broadcast.setPackage(BuildConfig.APPLICATION_ID);
                    sendOrderedBroadcast(broadcast, null, null, null, Activity.RESULT_OK, null, null);
                } else if (StringUtils.isValidString(type) && "notif_digest".equalsIgnoreCase(type)) {
                    // show all chat notifs now
                    NotifUtils.showAllPendingChatNotifs(this, false);
                } else if (StringUtils.isValidString(type) && "delete_chat".equalsIgnoreCase(type) && !TextUtils.isEmpty(dataMap.get("messageId"))) {
                    // delete this MsgID from SharedPrefs & re-trigger notif_digest
                    UnreadChatsSharedPrefs.getInstance().getPreferences().edit().remove(dataMap.get("messageId")).commit();
                    NotifUtils.showAllPendingChatNotifs(this, true);
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
                    FirebaseCrashlytics.getInstance().recordException(new IllegalArgumentException("Illegal notif type: " + type));
                }
            }
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
        Freshchat.getInstance(this).setPushRegistrationToken(token);
        try {
            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("token", token);
            childUpdates.put("tokenTimestamp", System.currentTimeMillis());
            getThisUserRef().updateChildren(childUpdates);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
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
