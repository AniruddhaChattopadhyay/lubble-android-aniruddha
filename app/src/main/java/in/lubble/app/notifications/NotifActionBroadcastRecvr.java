package in.lubble.app.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.core.app.RemoteInput;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.gson.Gson;
import in.lubble.app.BuildConfig;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.ChatData;
import in.lubble.app.models.NotifData;
import in.lubble.app.utils.NotifUtils;

import java.util.Map;

import static in.lubble.app.firebase.RealtimeDbHelper.getMessagesRef;
import static in.lubble.app.utils.NotifUtils.deleteUnreadMsgsForGroupId;

public class NotifActionBroadcastRecvr extends BroadcastReceiver {

    private static final String TAG = "NotifActionBroadcastRec";

    public static String ACTION_MARK_AS_READ = BuildConfig.APPLICATION_ID + ".notif.action.mark_as_read";
    public static String ACTION_REPLY = BuildConfig.APPLICATION_ID + ".notif.action.reply";
    // Key for the string that's delivered in the action's intent.
    public static final String KEY_TEXT_REPLY = "key_text_reply";

    public static String uid = FirebaseAuth.getInstance().getUid();

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (intent != null && intent.getAction().equalsIgnoreCase(ACTION_MARK_AS_READ) && intent.hasExtra("markread.groupId")) {
            final String groupId = intent.getStringExtra("markread.groupId");
            if (groupId != null) {
                final SharedPreferences chatSharedPrefs = UnreadChatsSharedPrefs.getInstance().getPreferences();

                final Map<String, String> chatsMap = (Map<String, String>) chatSharedPrefs.getAll();
                for (final Map.Entry<String, String> chatEntry : chatsMap.entrySet()) {
                    final NotifData notifData = new Gson().fromJson(chatEntry.getValue(), NotifData.class);
                    if (notifData.getGroupId().equalsIgnoreCase(groupId)) {
                        // mark msg as read
                        DatabaseReference msgRef = RealtimeDbHelper.getMessagesRef().child(groupId).child(notifData.getMessageId());
                        msgRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                final ChatData chatData = dataSnapshot.getValue(ChatData.class);
                                if (chatData != null && chatData.getReadReceipts().get(uid) == null) {
                                    getMessagesRef().child(groupId).child(dataSnapshot.getKey())
                                            .child("readReceipts")
                                            .child(uid)
                                            .setValue(System.currentTimeMillis());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
                // reset user's group unread counter
                RealtimeDbHelper.getUserGroupsRef().child(groupId).child("unreadCount").setValue(0);

                deleteUnreadMsgsForGroupId(groupId, context);
                NotifUtils.sendNotifAnalyticEvent(AnalyticsEvents.NOTIF_MARKED_READ, groupId, context);
            }
        } else if ((intent != null && intent.getAction().equalsIgnoreCase(ACTION_REPLY) && intent.hasExtra("reply.groupId"))) {
            final String groupId = intent.getStringExtra("reply.groupId");
            if (groupId != null) {
                String replyMsg = "";
                Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
                if (remoteInput != null) {
                    replyMsg = String.valueOf(remoteInput.getCharSequence(KEY_TEXT_REPLY));
                    if (!TextUtils.isEmpty(replyMsg)) {

                        final ChatData chatData = new ChatData();
                        chatData.setAuthorUid(uid);
                        chatData.setMessage(replyMsg);
                        chatData.setCreatedTimestamp(System.currentTimeMillis());
                        chatData.setServerTimestamp(ServerValue.TIMESTAMP);
                        chatData.setIsDm(false);

                        getMessagesRef().child(groupId).push().setValue(chatData);

                        // reset user's group unread counter
                        RealtimeDbHelper.getUserGroupsRef().child(groupId).child("unreadCount").setValue(0);

                        deleteUnreadMsgsForGroupId(groupId, context);
                        NotifUtils.sendNotifAnalyticEvent(AnalyticsEvents.NOTIF_REPLIED, groupId, context);
                        LubbleSharedPrefs.getInstance().setShowRatingDialog(true);
                    }
                }
            }
        }
    }
}