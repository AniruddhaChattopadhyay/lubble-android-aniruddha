package in.lubble.app.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import in.lubble.app.BuildConfig;
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
    public static String ACTION_LIKE = BuildConfig.APPLICATION_ID + ".notif.action.like";

    public static String uid = FirebaseAuth.getInstance().getUid();

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (intent != null && intent.hasExtra("markread.groupId")) {
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
        }
    }
}