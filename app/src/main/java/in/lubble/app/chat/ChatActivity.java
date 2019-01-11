package in.lubble.app.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import in.lubble.app.BaseActivity;
import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.models.ChatData;
import in.lubble.app.models.NotifData;
import in.lubble.app.utils.StringUtils;

import java.util.Map;
import java.util.MissingFormatArgumentException;

import static in.lubble.app.Constants.NEW_CHAT_ACTION;
import static in.lubble.app.utils.AppNotifUtils.TRACK_NOTIF_ID;
import static in.lubble.app.utils.FragUtils.replaceFrag;
import static in.lubble.app.utils.NotifUtils.sendNotifAnalyticEvent;

public class ChatActivity extends BaseActivity {

    public static final String EXTRA_GROUP_ID = "chat_activ_group_id";
    public static final String EXTRA_MSG_ID = "chat_activ_msg_id";
    public static final String EXTRA_CHAT_DATA = "chat_activ_chat_data";
    public static final String EXTRA_IMG_URI_DATA = "EXTRA_IMG_URI_DATA";
    // if we need to show joining progress dialog
    public static final String EXTRA_IS_JOINING = "chat_activ_is_joining";
    // for when DM exists
    public static final String EXTRA_DM_ID = "EXTRA_DM_ID";
    // for new DMs
    public static final String EXTRA_RECEIVER_ID = "EXTRA_RECEIVER_ID";
    public static final String EXTRA_RECEIVER_NAME = "EXTRA_RECEIVER_NAME";
    public static final String EXTRA_RECEIVER_DP_URL = "EXTRA_RECEIVER_DP_URL";
    public static final String EXTRA_ITEM_TITLE = "EXTRA_ITEM_TITLE";
    private ImageView toolbarIcon;
    private ImageView toolbarLockIcon;
    private TextView toolbarTv;
    private ChatFragment targetFrag = null;
    private String groupId;
    private String dmId;

    public static void openForGroup(@NonNull Context context, @NonNull String groupId, boolean isJoining, @Nullable String msgId) {
        final Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(EXTRA_GROUP_ID, groupId);
        intent.putExtra(EXTRA_IS_JOINING, isJoining);
        intent.putExtra(EXTRA_MSG_ID, msgId);
        context.startActivity(intent);
    }

    public static void openForGroup(@NonNull Context context, @NonNull String groupId, boolean isJoining, @Nullable String msgId, ChatData chatData, @Nullable Uri imgUri) {
        final Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(EXTRA_GROUP_ID, groupId);
        intent.putExtra(EXTRA_IS_JOINING, isJoining);
        intent.putExtra(EXTRA_MSG_ID, msgId);
        intent.putExtra(EXTRA_CHAT_DATA, chatData);
        intent.putExtra(EXTRA_IMG_URI_DATA, imgUri);
        context.startActivity(intent);
    }

    public static void openForDm(@NonNull Context context, @NonNull String dmId, @Nullable String msgId, @Nullable String itemTitle) {
        final Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(EXTRA_DM_ID, dmId);
        intent.putExtra(EXTRA_MSG_ID, msgId);
        intent.putExtra(EXTRA_ITEM_TITLE, itemTitle);
        context.startActivity(intent);
    }

    public static void openForDm(@NonNull Context context, @NonNull String dmId, @Nullable String msgId, @Nullable String itemTitle, ChatData chatData, @Nullable Uri imgUri) {
        final Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(EXTRA_DM_ID, dmId);
        intent.putExtra(EXTRA_MSG_ID, msgId);
        intent.putExtra(EXTRA_ITEM_TITLE, itemTitle);
        intent.putExtra(EXTRA_CHAT_DATA, chatData);
        intent.putExtra(EXTRA_IMG_URI_DATA, imgUri);
        context.startActivity(intent);
    }

    public static void openForEmptyDm(@NonNull Context context, @Nullable String receiverId, @Nullable String receiverName, @Nullable String receiverDpUrl,
                                      @Nullable String itemTitle) {
        final Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(EXTRA_RECEIVER_ID, receiverId);
        intent.putExtra(EXTRA_RECEIVER_NAME, receiverName);
        intent.putExtra(EXTRA_RECEIVER_DP_URL, receiverDpUrl);
        intent.putExtra(EXTRA_ITEM_TITLE, itemTitle);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.icon_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbarIcon = toolbar.findViewById(R.id.iv_toolbar);
        toolbarLockIcon = toolbar.findViewById(R.id.iv_lock_icon);
        TextView toolbarInviteHint = toolbar.findViewById(R.id.tv_invite_hint);
        toolbarTv = toolbar.findViewById(R.id.tv_toolbar_title);
        setTitle("");

        toolbarIcon.setImageResource(R.drawable.ic_circle_group_24dp);

        groupId = getIntent().getStringExtra(EXTRA_GROUP_ID);
        final String msgId = getIntent().getStringExtra(EXTRA_MSG_ID);
        final boolean isJoining = getIntent().getBooleanExtra(EXTRA_IS_JOINING, false);
        dmId = getIntent().getStringExtra(EXTRA_DM_ID);

        if (!LubbleSharedPrefs.getInstance().getIsGroupInfoOpened() && !TextUtils.isEmpty(groupId)) {
            toolbarInviteHint.setVisibility(View.VISIBLE);
            toolbarInviteHint.setHorizontallyScrolling(true);
            toolbarInviteHint.setSelected(true);
        } else {
            toolbarInviteHint.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(groupId)) {
            targetFrag = ChatFragment.newInstanceForGroup(
                    groupId, isJoining, msgId, (ChatData) getIntent().getSerializableExtra(EXTRA_CHAT_DATA), (Uri) getIntent().getParcelableExtra(EXTRA_IMG_URI_DATA)
            );
        } else if (!TextUtils.isEmpty(dmId)) {
            targetFrag = ChatFragment.newInstanceForDm(
                    dmId, msgId,
                    getIntent().getStringExtra(EXTRA_ITEM_TITLE), (ChatData) getIntent().getSerializableExtra(EXTRA_CHAT_DATA), (Uri) getIntent().getParcelableExtra(EXTRA_IMG_URI_DATA)
            );
        } else if (getIntent().hasExtra(EXTRA_RECEIVER_ID) && getIntent().hasExtra(EXTRA_RECEIVER_NAME)) {
            targetFrag = ChatFragment.newInstanceForEmptyDm(
                    getIntent().getStringExtra(EXTRA_RECEIVER_ID),
                    getIntent().getStringExtra(EXTRA_RECEIVER_NAME),
                    getIntent().getStringExtra(EXTRA_RECEIVER_DP_URL),
                    getIntent().getStringExtra(EXTRA_ITEM_TITLE)
            );
        } else {
            throw new RuntimeException("Invalid Args, see the valid factory methods by searching for this error string");
        }

        replaceFrag(getSupportFragmentManager(), targetFrag, R.id.frame_fragContainer);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (targetFrag != null) {
                    targetFrag.openGroupInfo();
                }
            }
        });
        try {
            Intent intent = this.getIntent();
            if (intent != null && intent.getExtras() != null && intent.getExtras().containsKey(TRACK_NOTIF_ID)
                    && (intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0) {
                String notifId = this.getIntent().getExtras().getString(TRACK_NOTIF_ID);

                if (!TextUtils.isEmpty(notifId)) {
                    final Bundle bundle = new Bundle();
                    bundle.putString("notifId", String.valueOf(notifId));
                    bundle.putString("groupId", String.valueOf(groupId));
                    bundle.putString("dmId", String.valueOf(dmId));
                    bundle.putString("msgId", String.valueOf(msgId));
                    Analytics.triggerEvent(AnalyticsEvents.NOTIF_OPENED, bundle, this);
                }
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    /**
     * ensures that notifs for this chat do not appear when activity is in foreground
     */
    private BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Since we will process the message and update the UI, we don't need to show a notif in Status Bar
            // To do this, we call abortBroadcast()
            Log.d("NotificationResultRecei", "onReceive: in activity");
            if (intent != null && intent.hasExtra("remoteMessage")) {
                final RemoteMessage remoteMessage = intent.getParcelableExtra("remoteMessage");
                Gson gson = new Gson();
                final Map<String, String> dataMap = remoteMessage.getData();
                JsonElement jsonElement = gson.toJsonTree(dataMap);
                NotifData notifData = gson.fromJson(jsonElement, NotifData.class);
                String type = dataMap.get("type");
                if ("chat".equalsIgnoreCase(type)) {
                    if (notifData.getGroupId().equalsIgnoreCase(groupId)) {
                        abortBroadcast();
                        sendNotifAnalyticEvent(AnalyticsEvents.NOTIF_ABORTED, dataMap, ChatActivity.this);
                    }
                } else if ("dm".equalsIgnoreCase(type)) {
                    if (notifData.getGroupId().equalsIgnoreCase(dmId)) {
                        abortBroadcast();
                        sendNotifAnalyticEvent(AnalyticsEvents.NOTIF_ABORTED, dataMap, ChatActivity.this);
                    }
                } else {
                    Crashlytics.logException(new IllegalArgumentException("chatactiv: notif recvd with illegal type"));
                }
            } else {
                Crashlytics.logException(new MissingFormatArgumentException("chatactiv: notif broadcast recvd with no intent data"));
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(NEW_CHAT_ACTION);
        filter.setPriority(1);
        registerReceiver(notificationReceiver, filter);

    }

    public void setGroupMeta(String title, String thumbnailUrl, boolean isPrivate) {
        toolbarTv.setText(title);
        if (StringUtils.isValidString(thumbnailUrl)) {
            GlideApp.with(this).load(thumbnailUrl).circleCrop().into(toolbarIcon);
        }
        toolbarLockIcon.setVisibility(isPrivate ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(notificationReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            unregisterReceiver(notificationReceiver);
        } catch (IllegalArgumentException e) {
            //already unregistered
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(notificationReceiver);
        } catch (IllegalArgumentException e) {
            //already unregistered
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
