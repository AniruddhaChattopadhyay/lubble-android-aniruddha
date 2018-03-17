package in.lubble.app.firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;

import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.util.Map;

import in.lubble.app.Constants;
import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.MainActivity;
import in.lubble.app.R;
import in.lubble.app.notifications.NotifData;
import in.lubble.app.utils.NotifUtils;

import static in.lubble.app.Constants.CHAT_NOTIFICATION_ID;
import static in.lubble.app.firebase.RealtimeDbHelper.getMessagesRef;

/**
 * Created by ishaan on 26/1/18.
 */

public class FcmService extends FirebaseMessagingService {

    private static final String TAG = "FcmService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Tasks shorter than 10secs. For long running tasks, schedule a job to Firebase Job Scheduler.
        final Map<String, String> dataMap = remoteMessage.getData();
        if (dataMap.size() > 0) {
            Log.d(TAG, "Message data payload: " + dataMap);

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

    private void sendNotification(NotifData notifData) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String authNameBold = notifData.getAuthorName();
        SpannableString boldStr = new SpannableString(authNameBold);
        boldStr.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, authNameBold.length(), 0);
        CharSequence options[] = new CharSequence[]{boldStr, notifData.getMessageBody()};

        String channelId = Constants.DEFAULT_NOTIF_CHANNEL;
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        final NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_upload)
                        .setContentTitle(notifData.getGroupName())
                        .setContentText(options[0] + ": " + options[1])
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        final NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "DEFAULT",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        try {
            Bitmap theBitmap = GlideApp.with(this)
                    .asBitmap()
                    .load(notifData.getGroupDpUrl().equalsIgnoreCase("") ? R.drawable.ic_account_circle_black_no_padding : notifData.getGroupDpUrl())
                    .circleCrop()
                    .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .get();
            notificationBuilder.setLargeIcon(theBitmap);
            notificationManager.notify(CHAT_NOTIFICATION_ID, notificationBuilder.build());
        } catch (Exception e) {
            notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_account_circle_black_no_padding));
            notificationManager.notify(CHAT_NOTIFICATION_ID, notificationBuilder.build());
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
    }
}
