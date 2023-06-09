package in.lubble.app.chat;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Browser;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.base.Joiner;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.MissingFormatArgumentException;
import java.util.Set;

import in.lubble.app.BaseActivity;
import in.lubble.app.GlideApp;
import in.lubble.app.LubbleApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.chat.stories.StoriesRecyclerViewAdapter;
import in.lubble.app.chat.stories.StoryData;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.ChatData;
import in.lubble.app.models.GroupData;
import in.lubble.app.models.NotifData;
import in.lubble.app.models.ProfileData;
import in.lubble.app.models.ProfileInfo;
import in.lubble.app.models.search.Hit;
import in.lubble.app.models.search.SearchResultData;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import in.lubble.app.user_search.UserSearchActivity;
import in.lubble.app.utils.FragUtils;
import in.lubble.app.utils.StringUtils;
import in.lubble.app.utils.UiUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING;
import static in.lubble.app.Constants.NEW_CHAT_ACTION;
import static in.lubble.app.utils.AppNotifUtils.TRACK_NOTIF_ID;
import static in.lubble.app.utils.NotifUtils.sendNotifAnalyticEvent;

public class ChatActivity extends BaseActivity implements SearchView.OnQueryTextListener {

    private static final String TAG = "ChatActivity";

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
    public static final String TAG_CHAT_FAG = "TAG_CHAT_FAG";

    private ImageView toolbarIcon, toolbarLockIcon;
    private Toolbar toolbar;
    private TextView toolbarTv, toolbarInviteHint, highlightNamesTv, memberCountTV, pinnedMsgTv;
    private LinearLayout inviteContainer;
    private RelativeLayout pinnedMessageContainer;
    private ImageView searchBackIv, searchUpIv, searchDownIv, pinnedMessageCancel;
    private SearchView searchView;
    private ChatFragment targetFrag = null;
    private String groupId;
    //    private TabLayout tabLayout;
    private String dmId;
    private SearchResultData searchResultData = null;
    private int currSearchCursorPos = 0;
    private ProgressDialog searchProgressDialog;
    private SharedPreferences sharedPreferences;
    private final String MyPrefs = "ChatActivity";
    Set<String> groupList;
    private final String pinnedMessageDontShowGroupList = "PINNED_MESSAGE_DONT_SHOW_GROUPLIST";
    private ArrayList<StoryData> storyDataList = new ArrayList<>();
    private int heightOfLayout = 0;
    private RecyclerView storiesRv;
    private LinearLayout storiesLayout;
    private FrameLayout fragContainer;
    private DatabaseReference storiesRef;
    private GroupData groupData;

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
        Log.d("mime type chat", "&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&" + imgUri);
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
                    FirebaseCrashlytics.getInstance().recordException(new IllegalArgumentException("chatactiv: notif recvd with illegal type"));
                }
            } else {
                FirebaseCrashlytics.getInstance().recordException(new MissingFormatArgumentException("chatactiv: notif broadcast recvd with no intent data"));
            }
        }
    };

    private void closeSearch() {
        UiUtils.hideKeyboard(ChatActivity.this);
        searchView.setQuery("", false);
        searchView.clearFocus();
        searchResultData = null;
        currSearchCursorPos = 0;
        toolbar.clearFocus();
        toggleSearchViewVisibility(false);
        targetFrag.removeSearchHighlights();
    }

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        toolbar = findViewById(R.id.icon_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbarIcon = toolbar.findViewById(R.id.iv_toolbar);
        toolbarLockIcon = toolbar.findViewById(R.id.iv_lock_icon);
        toolbarInviteHint = toolbar.findViewById(R.id.tv_invite_hint);
        highlightNamesTv = toolbar.findViewById(R.id.tv_names);
        memberCountTV = toolbar.findViewById(R.id.tv_member_count);
        toolbarTv = toolbar.findViewById(R.id.tv_toolbar_title);
        inviteContainer = toolbar.findViewById(R.id.container_invite);
        searchBackIv = toolbar.findViewById(R.id.iv_search_back);
        searchView = toolbar.findViewById(R.id.search_view);
        searchUpIv = toolbar.findViewById(R.id.iv_search_up);
        searchDownIv = toolbar.findViewById(R.id.iv_search_down);
        pinnedMessageContainer = findViewById(R.id.pinned_message_container);
        pinnedMsgTv = findViewById(R.id.pinned_message_content);
        pinnedMessageCancel = findViewById(R.id.pinned_message_cross);
        storiesRv = findViewById(R.id.stories_recycler_view);
        storiesLayout = findViewById(R.id.ll_stories);
        fragContainer = findViewById(R.id.frame_frag);
        searchView.setOnQueryTextListener(this);
        setTitle("");

        toolbarIcon.setImageResource(R.drawable.ic_circle_group_24dp);

        groupId = getIntent().getStringExtra(EXTRA_GROUP_ID);
        int tabPosition = getIntent().getIntExtra(EXTRA_TAB_POS, 0);
        final String msgId = getIntent().getStringExtra(EXTRA_MSG_ID);
        final boolean isJoining = getIntent().getBooleanExtra(EXTRA_IS_JOINING, false);
        dmId = getIntent().getStringExtra(EXTRA_DM_ID);

        if (!TextUtils.isEmpty(dmId)) {
            // for DMs
            //tabLayout.setVisibility(View.GONE);
            inviteContainer.setVisibility(View.GONE);
            toolbarInviteHint.setVisibility(View.VISIBLE);
            toolbarInviteHint.setText("Personal Chat");
            highlightNamesTv.setVisibility(View.GONE);
            memberCountTV.setVisibility(View.GONE);
        } else {
            //tabLayout.setVisibility(View.VISIBLE);
            toolbarInviteHint.setVisibility(View.GONE);
            inviteContainer.setVisibility(View.VISIBLE);
        }

        FragUtils.replaceFrag(getSupportFragmentManager(), getTargetChatFrag(msgId, isJoining), fragContainer.getId(), TAG_CHAT_FAG);
        final Bundle groupBundle = new Bundle();
        groupBundle.putString("groupid", groupId);
        Analytics.triggerEvent(AnalyticsEvents.GROUP_CHAT_FRAG, groupBundle, ChatActivity.this);

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
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        inviteContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserSearchActivity.newInstance(ChatActivity.this, groupId);
            }
        });

        searchBackIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeSearch();
            }
        });

        searchUpIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UiUtils.hideKeyboard(ChatActivity.this);
                if (searchResultData != null && searchResultData.getQuery().equalsIgnoreCase(searchView.getQuery().toString())) {
                    if (searchResultData.getNbHits() > 0) {
                        if (currSearchCursorPos < searchResultData.getHits().size() - 1) {
                            currSearchCursorPos++;
                        }
                        Hit searchHit = searchResultData.getHits().get(currSearchCursorPos);
                        String highlightedStr = searchHit.get_highlightResult().getText().getValue();
                        targetFrag.scrollToChatId(searchHit.getChatId(), highlightedStr.substring(highlightedStr.indexOf("<hem>") + 5, highlightedStr.indexOf("</hem>")));
                    } else {
                        Toast.makeText(ChatActivity.this, "No results found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    onQueryTextSubmit(searchView.getQuery().toString());
                }
            }
        });

        searchDownIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UiUtils.hideKeyboard(ChatActivity.this);
                if (searchResultData != null && searchResultData.getQuery().equalsIgnoreCase(searchView.getQuery().toString())) {
                    if (searchResultData.getNbHits() > 0) {
                        if (currSearchCursorPos > 0) {
                            currSearchCursorPos--;
                        }
                        Hit searchHit = searchResultData.getHits().get(currSearchCursorPos);
                        String highlightedStr = searchHit.get_highlightResult().getText().getValue();
                        targetFrag.scrollToChatId(searchHit.getChatId(), highlightedStr.substring(highlightedStr.indexOf("<hem>") + 5, highlightedStr.indexOf("</hem>")));
                    } else {
                        Toast.makeText(ChatActivity.this, "No results found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    onQueryTextSubmit(searchView.getQuery().toString());
                }
            }
        });
        sharedPreferences = ChatActivity.this.getSharedPreferences(MyPrefs, Context.MODE_PRIVATE);
        groupList = sharedPreferences.getStringSet(pinnedMessageDontShowGroupList, null);
        if (dmId == null && (groupList == null || !(groupList.contains(groupId)))) {
            RealtimeDbHelper.getLubbleGroupInfoRef(groupId).child("pinned_message").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        final String message = snapshot.getValue(String.class);
                        if (!TextUtils.isEmpty(message)) {
                            final String escapedMessage = message.replaceAll("\\\\n", "\n");
                            pinnedMessageContainer.setVisibility(View.VISIBLE);
                            pinnedMsgTv.setText(escapedMessage);
                            pinnedMessageCancel.setVisibility(View.VISIBLE);

                            final ViewTreeObserver viewTreeObserver = pinnedMsgTv.getViewTreeObserver();
                            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                @Override
                                public void onGlobalLayout() {
                                    // Past the maximum number of lines we want to display.
                                    if (viewTreeObserver.isAlive()) {
                                        viewTreeObserver.removeOnGlobalLayoutListener(this);
                                    }
                                    if (pinnedMsgTv.getLineCount() > 3) {
                                        int lastCharShown = pinnedMsgTv.getLayout().getLineVisibleEnd(3 - 1);

                                        pinnedMsgTv.setMaxLines(3);

                                        String moreString = "read more";
                                        String suffix = "  " + moreString;

                                        // 3 is a "magic number" but it's just basically the length of the ellipsis we're going to insert
                                        String actionDisplayText = escapedMessage.substring(0, lastCharShown - suffix.length() - 3) + "..." + suffix;

                                        SpannableString truncatedSpannableString = new SpannableString(actionDisplayText);
                                        int startIndex = actionDisplayText.indexOf(moreString);
                                        truncatedSpannableString.setSpan(
                                                new ForegroundColorSpan(ContextCompat.getColor(LubbleApp.getAppContext(), R.color.colorAccent)),
                                                startIndex, startIndex + moreString.length(),
                                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        pinnedMsgTv.setText(truncatedSpannableString);
                                    }
                                    heightOfLayout = pinnedMessageContainer.getHeight();
                                }
                            });
                        }
                    } else {
                        showStories();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            pinnedMessageContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PinnedMessageBottomSheet pinnedMessageBottomSheet = new PinnedMessageBottomSheet(groupId);
                    pinnedMessageBottomSheet.show(getSupportFragmentManager(), pinnedMessageBottomSheet.getTag());

                    Bundle bundle = new Bundle();
                    bundle.putString("group_id", groupId);
                    Analytics.triggerEvent(AnalyticsEvents.EXPAND_PIN_MSG, bundle, ChatActivity.this);
                }
            });
            pinnedMessageCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pinnedMessageContainer.setVisibility(View.GONE);
//                    Set<String> groupList = sharedPreferences.getStringSet(pinnedMessageDontShowGroupList,null);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    if (groupList != null) {
                        groupList = new HashSet<>(groupList);
                        groupList.add(groupId);
                    } else {
                        groupList = new HashSet<>();
                        groupList.add(groupId);
                    }
                    editor.putStringSet(pinnedMessageDontShowGroupList, groupList);
                    editor.apply();
                    showStories();
                    Bundle bundle = new Bundle();
                    bundle.putString("group_id", groupId);
                    Analytics.triggerEvent(AnalyticsEvents.DISMISS_PIN_MSG, bundle, ChatActivity.this);
                }
            });
        }

        if (dmId == null && groupList != null) {
            if (groupList.contains(groupId))
                showStories();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(NEW_CHAT_ACTION);
        filter.setPriority(1);
        registerReceiver(notificationReceiver, filter);

    }

    @Override
    public void startActivity(Intent intent) {
        // for appending linkify links with extra params to let branch open links correctly
        // ref: https://github.com/BranchMetrics/android-branch-deep-linking-attribution/issues/617

        if (TextUtils.equals(intent.getAction(), Intent.ACTION_VIEW)) {
            String app_id = intent.getStringExtra(Browser.EXTRA_APPLICATION_ID);
            if (TextUtils.equals(getApplicationContext().getPackageName(), app_id)) {
                // This intent is a view coming from Linkify; handle internally
                intent.putExtra("branch_force_new_session", true);
            }
        }
        super.startActivity(intent);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if (!TextUtils.isEmpty(query)) {
            HashMap<Object, Object> map = new HashMap<>();
            map.put("query", query);
            map.put("lubbleId", LubbleSharedPrefs.getInstance().getLubbleId());
            map.put("groupId", groupId);
            DatabaseReference pushQueryRef = RealtimeDbHelper.getSearchQueryRef().push();
            pushQueryRef.setValue(map);
            initSearchResultListener(pushQueryRef.getKey());
        }
        return false;
    }

    private void showStories() {
        storiesRef = RealtimeDbHelper.getStoriesRef(groupId);
        storiesRef.addValueEventListener(storiesListener);
    }

    ValueEventListener storiesListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (snapshot.exists()) {
                storyDataList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    StoryData storyData = dataSnapshot.getValue(StoryData.class);
                    storyDataList.add(storyData);
                }
                if (!storyDataList.isEmpty()) {
                    initStoriesRecyclerView();
                } else {
                    storiesLayout.setVisibility(GONE);
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
        }
    };

    private void initStoriesRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        storiesLayout.setVisibility(View.VISIBLE);
        final ViewTreeObserver observer = storiesLayout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                heightOfLayout = storiesLayout.getHeight();
                if (observer.isAlive()) {
                    observer.removeOnGlobalLayoutListener(this);
                }
            }
        });
        storiesRv.setLayoutManager(layoutManager);
        StoriesRecyclerViewAdapter adapter = new StoriesRecyclerViewAdapter(this, storyDataList, groupId);
        storiesRv.setAdapter(adapter);
    }

    private void initSearchResultListener(String searchKey) {
        if (searchProgressDialog == null) {
            searchProgressDialog = new ProgressDialog(this);
        }
        searchProgressDialog.setTitle("Searching");
        searchProgressDialog.setMessage("Please Wait");
        searchProgressDialog.setCancelable(false);
        searchProgressDialog.show();
        RealtimeDbHelper.getSearchResultRef().child(searchKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                searchResultData = dataSnapshot.getValue(SearchResultData.class);
                if (searchResultData != null) {
                    searchProgressDialog.dismiss();
                    if (searchResultData.getNbHits() > 0) {
                        Hit searchHit = searchResultData.getHits().get(0);
                        String highlightedStr = searchHit.get_highlightResult().getText().getValue();
                        targetFrag.scrollToChatId(searchHit.getChatId(), highlightedStr.substring(highlightedStr.indexOf("<hem>") + 5, highlightedStr.indexOf("</hem>")));
                        currSearchCursorPos = 0;
                    } else {
                        Toast.makeText(ChatActivity.this, "No results found", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                searchProgressDialog.dismiss();
            }
        });
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    void toggleStoriesVisibility(boolean show) {
        if (show) {
            storiesLayout.setVisibility(VISIBLE);
        } else {
            storiesLayout.setVisibility(GONE);
        }
    }

    void scrollStories(int dy, int state) {
        if (storyDataList != null && !storyDataList.isEmpty()) {
            if (dy < 0 && state == SCROLL_STATE_DRAGGING && storiesLayout.getVisibility() == VISIBLE && storiesLayout.getAnimation() == null) {
                //scrolling up
                Animation hideAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up_hide);
                hideAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        storiesLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                storiesLayout.startAnimation(hideAnimation);
            } else if (dy > 0 && state == SCROLL_STATE_DRAGGING && storiesLayout.getVisibility() == GONE && storiesLayout.getAnimation() == null) {
                Animation showAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_down_show);
                showAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        storiesLayout.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                storiesLayout.startAnimation(showAnimation);
            }
        }
    }

    public void setGroupMeta(String title, final boolean isGroupJoined, String thumbnailUrl, boolean isPrivate, final int memberCount) {
        toolbarTv.setText(title);
        if (StringUtils.isValidString(thumbnailUrl)) {
            GlideApp.with(this).load(thumbnailUrl).circleCrop().into(toolbarIcon);
        }
        toolbarLockIcon.setVisibility(isPrivate ? View.VISIBLE : View.GONE);
        toolbar.setOnClickListener(v -> {
            if (targetFrag != null) {
                targetFrag.openGroupInfo();
            }
        });

        groupData = new GroupData();
        groupData.setId(groupId);
        groupData.setTitle(title);
        groupData.setThumbnail(thumbnailUrl);
        groupData.setIsPrivate(isPrivate);

        if (dmId != null) {
            toolbarInviteHint.setText(getString(R.string.personal_chat));
            toolbarInviteHint.setVisibility(View.VISIBLE);
            highlightNamesTv.setVisibility(View.GONE);
            memberCountTV.setVisibility(View.GONE);
        } else {
            toolbarInviteHint.setVisibility(View.GONE);
            highlightNamesTv.setVisibility(View.VISIBLE);
            if (memberCount < 3) {
                memberCountTV.setVisibility(View.GONE);
                highlightNamesTv.setText(R.string.click_group_info);
            } else {
                memberCountTV.setVisibility(View.VISIBLE);
                // fetch token
                FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                mUser.getIdToken(false)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                String idToken = task.getResult().getToken();
                                try {
                                    fetchGroupMembers(idToken, isGroupJoined, memberCount);
                                } catch (Exception e) {
                                    // dont crash app if this breaks
                                    FirebaseCrashlytics.getInstance().recordException(e);
                                }
                            } else {
                                Toast.makeText(this, "Failed to fetch access token", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }

    private void fetchGroupMembers(String idToken, boolean isGroupJoined, int memberCount) {
        final Endpoints endpoints = ServiceGenerator.createFirebaseService(Endpoints.class);
        String lubbleId = LubbleSharedPrefs.getInstance().requireLubbleId();
        //fetch 5 members & show max 3 non-null names
        Call<JsonObject> lubbleMembersCall =
                endpoints.fetchLubbleMembersLimit("\"lubbles/" + lubbleId + "\"", "\"\"", 5, idToken);
        lubbleMembersCall.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                if (response.isSuccessful() && !isFinishing()) {
                    final JsonObject responseJson = response.body();
                    Gson gson = new Gson();
                    ArrayList<String> nameList = new ArrayList<>();
                    for (Map.Entry<String, JsonElement> entry : responseJson.entrySet()) {
                        ProfileData profileData = gson.fromJson(entry.getValue(), ProfileData.class);
                        final ProfileInfo profileInfo = profileData.getInfo();
                        HashMap<String, Object> groupsMap = profileData.getLubbles().get(lubbleId).get("groups");
                        if (groupsMap != null && groupsMap.containsKey(groupId) && profileInfo != null && profileInfo.getName() != null && !profileData.getIsDeleted()) {
                            profileInfo.setId(entry.getKey());
                            String firstName = getFirstName(profileInfo.getName());
                            if (firstName != null) {
                                nameList.add(firstName);
                                if (nameList.size() >= 3) break;
                            }
                        }
                    }
                    if (isGroupJoined) {
                        String nameUser = getFirstName(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                        nameList.add(0, "<b>" + nameUser + "</b>");
                        highlightNamesTv.setText(Html.fromHtml(Joiner.on(", ").join(nameList)));
                        String count = "+" + (memberCount - 3);
                        if (memberCount > 3)
                            memberCountTV.setText(count);
                    } else {
                        highlightNamesTv.setText(Joiner.on(", ").join(nameList));
                        String count = "+" + (memberCount - 3);
                        if (memberCount > 3)
                            memberCountTV.setText(count);
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                //ignore
            }
        });
    }

    private String getFirstName(String name) {
        if (name == null)
            return name;
        if (name.contains(" "))
            return name.substring(0, name.indexOf(" "));
        else
            return name;
    }

    private void toggleSearchViewVisibility(boolean show) {
        if (show) {
            searchView.setVisibility(View.VISIBLE);
            searchView.setFocusable(true);
            searchView.requestFocusFromTouch();
            searchView.setIconifiedByDefault(false);
            searchView.setIconified(false);
            searchView.setOnQueryTextListener(this);
            searchBackIv.setVisibility(View.VISIBLE);
            searchUpIv.setVisibility(View.VISIBLE);
            searchDownIv.setVisibility(View.VISIBLE);
            toolbarIcon.setVisibility(View.GONE);
            toolbarLockIcon.setVisibility(View.GONE);
            toolbarTv.setVisibility(View.GONE);
            toolbarInviteHint.setVisibility(View.GONE);
            inviteContainer.setVisibility(View.GONE);
            //tabLayout.setVisibility(View.GONE);
            memberCountTV.setVisibility(View.GONE);
            highlightNamesTv.setVisibility(View.GONE);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        } else {
            searchView.setOnQueryTextListener(null);
            searchView.setVisibility(View.GONE);
            searchBackIv.setVisibility(View.GONE);
            searchUpIv.setVisibility(View.GONE);
            searchDownIv.setVisibility(View.GONE);
            toolbarIcon.setVisibility(View.VISIBLE);
            toolbarTv.setVisibility(View.VISIBLE);
            toolbarLockIcon.setVisibility(groupData.getIsPrivate() ? View.VISIBLE : View.GONE);
            inviteContainer.setVisibility(View.VISIBLE);
            //tabLayout.setVisibility(View.VISIBLE);
            highlightNamesTv.setVisibility(View.VISIBLE);
            memberCountTV.setVisibility(View.VISIBLE);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    int getTopLayoutHeight() {
        return heightOfLayout;
    }

    private void blockAccount() {
        String title = "Block this person?";
        if (groupData != null && !TextUtils.isEmpty(groupData.getTitle())) {
            title = "Block " + groupData.getTitle() + "?";
        }
        UiUtils.showBottomSheetAlertLight(ChatActivity.this, getLayoutInflater(), title,
                "They will no longer be able to message you. You can unblock anytime from the navigation.",
                R.drawable.ic_block_black_24dp, "BLOCK", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        targetFrag.setBlockedStatus("BLOCKED");
                        Analytics.triggerEvent(AnalyticsEvents.DM_BLOCKED, ChatActivity.this);
                        finish();
                    }
                });
    }

    private void reportAccount() {
        String title = "Report this person to Lubble?";
        if (groupData != null && !TextUtils.isEmpty(groupData.getTitle())) {
            title = "Report " + groupData.getTitle() + " to Lubble?";
        }
        UiUtils.showBottomSheetAlertLight(ChatActivity.this, getLayoutInflater(), title,
                "We will investigate their profile for spam, abusive, or inappropriate behaviour & take necessary action.\nThis will also block them.",
                R.drawable.ic_error_outline_black_24dp, "REPORT", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        targetFrag.setBlockedStatus("REPORTED");
                        // push DM id to firebase, which triggers an email for the report
                        DatabaseReference pushRef = FirebaseDatabase.getInstance().getReference("dm_reports").push();
                        final HashMap<Object, Object> map = new HashMap<>();
                        map.put("dm_id", dmId);
                        pushRef.setValue(map);
                        Analytics.triggerEvent(AnalyticsEvents.DM_REPORTED, ChatActivity.this);
                        finish();
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(notificationReceiver);
        if (storiesRef != null && storiesListener != null) {
            storiesRef.removeEventListener(storiesListener);
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        int menuId = dmId != null ? R.menu.chat_menu : R.menu.group_chat_menu;
        getMenuInflater().inflate(menuId, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.block:
                blockAccount();
                return true;
            case R.id.report:
                reportAccount();
                return true;
            case R.id.group_info:
                if (targetFrag != null && groupData != null) {
                    targetFrag.openGroupInfo();
                }
                return true;
            case R.id.group_menu_search:
                toggleSearchViewVisibility(true);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (searchView.getVisibility() == View.VISIBLE) {
            closeSearch();
        } else {
            super.onBackPressed();
        }
    }
}
