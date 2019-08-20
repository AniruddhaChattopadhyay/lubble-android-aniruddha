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
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.crashlytics.android.Crashlytics;
import com.google.android.material.tabs.TabLayout;
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
import in.lubble.app.models.GroupData;
import in.lubble.app.models.NotifData;
import in.lubble.app.user_search.UserSearchActivity;
import in.lubble.app.utils.StringUtils;

import java.util.Map;
import java.util.MissingFormatArgumentException;

import static in.lubble.app.Constants.NEW_CHAT_ACTION;
import static in.lubble.app.utils.AppNotifUtils.TRACK_NOTIF_ID;
import static in.lubble.app.utils.NotifUtils.sendNotifAnalyticEvent;

public class ChatActivity extends BaseActivity implements ChatMoreFragment.FlairUpdateListener {

    public static final String EXTRA_GROUP_ID = "chat_activ_group_id";
    public static final String EXTRA_MSG_ID = "chat_activ_msg_id";
    public static final String EXTRA_CHAT_DATA = "chat_activ_chat_data";
    public static final String EXTRA_IMG_URI_DATA = "EXTRA_IMG_URI_DATA";
    public static final String EXTRA_TAB_POS = "EXTRA_TAB_POS";
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
    private LinearLayout inviteContainer;
    private ChatFragment targetFrag = null;
    private String groupId;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private String dmId;

    public static void openForGroup(@NonNull Context context, @NonNull String groupId, boolean isJoining, @Nullable String msgId) {
        final Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(EXTRA_GROUP_ID, groupId);
        intent.putExtra(EXTRA_IS_JOINING, isJoining);
        intent.putExtra(EXTRA_MSG_ID, msgId);
        context.startActivity(intent);
    }

    public static void openGroupMore(@NonNull Context context, @NonNull String groupId) {
        final Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(EXTRA_GROUP_ID, groupId);
        intent.putExtra(EXTRA_TAB_POS, 1);
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
        inviteContainer = toolbar.findViewById(R.id.container_invite);
        setTitle("");

        viewPager = findViewById(R.id.viewpager_chat);
        tabLayout = findViewById(R.id.tablayout_chat);
        tabLayout.setupWithViewPager(viewPager);

        toolbarIcon.setImageResource(R.drawable.ic_circle_group_24dp);

        groupId = getIntent().getStringExtra(EXTRA_GROUP_ID);
        int tabPosition = getIntent().getIntExtra(EXTRA_TAB_POS, 0);
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

        ChatViewPagerAdapter adapter = new ChatViewPagerAdapter(getSupportFragmentManager(), msgId, isJoining);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(tabPosition, true);

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
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                final Bundle bundle = new Bundle();
                bundle.putString("groupid", groupId);
                if (tab.getPosition() == 0) {
                    Analytics.triggerEvent(AnalyticsEvents.GROUP_CHAT_FRAG, bundle, ChatActivity.this);
                } else if (tab.getPosition() == 1) {
                    Analytics.triggerEvent(AnalyticsEvents.GROUP_MORE_FRAG, bundle, ChatActivity.this);
                    final View customView = tab.getCustomView();
                    if (customView != null) {
                        ((TextView) customView.findViewById(android.R.id.text1)).setTextColor(ContextCompat.getColor(ChatActivity.this, R.color.black));
                        customView.findViewById(R.id.badge).setVisibility(View.GONE);
                        LubbleSharedPrefs.getInstance().setIsBookExchangeOpened(true);
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab.getPosition() == 1) {
                    final View customView = tab.getCustomView();
                    if (customView != null) {
                        ((TextView) customView.findViewById(android.R.id.text1)).setTextColor(ContextCompat.getColor(ChatActivity.this, R.color.default_text_color));
                    }
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        inviteContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserSearchActivity.newInstance(ChatActivity.this, groupId);
            }
        });
    }

    private GroupData groupData;

    private ChatFragment getTargetChatFrag(String msgId, boolean isJoining) {
        if (!TextUtils.isEmpty(groupId)) {
            return targetFrag = ChatFragment.newInstanceForGroup(
                    groupId, isJoining, msgId, (ChatData) getIntent().getSerializableExtra(EXTRA_CHAT_DATA), (Uri) getIntent().getParcelableExtra(EXTRA_IMG_URI_DATA)
            );
        } else if (!TextUtils.isEmpty(dmId)) {
            return targetFrag = ChatFragment.newInstanceForDm(
                    dmId, msgId,
                    getIntent().getStringExtra(EXTRA_ITEM_TITLE), (ChatData) getIntent().getSerializableExtra(EXTRA_CHAT_DATA), (Uri) getIntent().getParcelableExtra(EXTRA_IMG_URI_DATA)
            );
        } else if (getIntent().hasExtra(EXTRA_RECEIVER_ID) && getIntent().hasExtra(EXTRA_RECEIVER_NAME)) {
            return targetFrag = ChatFragment.newInstanceForEmptyDm(
                    getIntent().getStringExtra(EXTRA_RECEIVER_ID),
                    getIntent().getStringExtra(EXTRA_RECEIVER_NAME),
                    getIntent().getStringExtra(EXTRA_RECEIVER_DP_URL),
                    getIntent().getStringExtra(EXTRA_ITEM_TITLE)
            );
        } else {
            throw new RuntimeException("Invalid Args, see the valid factory methods by searching for this error string");
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

    public class ChatViewPagerAdapter extends FragmentPagerAdapter {

        private String title[] = {"Chats", "Collections"};
        private String msgId;
        private boolean isJoining;

        public ChatViewPagerAdapter(FragmentManager manager, String msgId, boolean isJoining) {
            super(manager);
            this.msgId = msgId;
            this.isJoining = isJoining;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return getTargetChatFrag(msgId, isJoining);
            } else {
                return ChatMoreFragment.newInstance(groupId);
            }
        }

        @Override
        public int getCount() {
            return title.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return title[position];
        }
    }

    public void setGroupMeta(String title, String thumbnailUrl, boolean isPrivate) {
        toolbarTv.setText(title);
        if (StringUtils.isValidString(thumbnailUrl)) {
            GlideApp.with(this).load(thumbnailUrl).circleCrop().into(toolbarIcon);
        }
        toolbarLockIcon.setVisibility(isPrivate ? View.VISIBLE : View.GONE);

        groupData = new GroupData();
        groupData.setId(groupId);
        groupData.setTitle(title);
        groupData.setThumbnail(thumbnailUrl);
    }

    public void showNewBadge() {
        tabLayout.getTabAt(1).setCustomView(R.layout.tab_with_badge);
        final View customView = tabLayout.getTabAt(1).getCustomView();
        if (customView != null) {
            customView.findViewById(R.id.badge).setVisibility(View.VISIBLE);
        }
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

    @Override
    public void onFlairUpdated() {
        ChatFragment frag = (ChatFragment)
                getSupportFragmentManager().findFragmentByTag(makeFragmentName(viewPager.getId(), 0));
        frag.updateThisUserFlair();
    }

    private static String makeFragmentName(int viewPagerId, int index) {
        return "android:switcher:" + viewPagerId + ":" + index;
    }

}
