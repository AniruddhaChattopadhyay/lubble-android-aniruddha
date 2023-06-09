package in.lubble.app;

import static in.lubble.app.Constants.DEFAULT_SHOP_PIC;
import static in.lubble.app.Constants.DELIVERY_FEE;
import static in.lubble.app.Constants.EVENTS_MAINTENANCE_IMG;
import static in.lubble.app.Constants.EVENTS_MAINTENANCE_TEXT;
import static in.lubble.app.Constants.GROUP_QUES_ENABLED;
import static in.lubble.app.Constants.IS_CHATS_ENABLED_FOR_KML;
import static in.lubble.app.Constants.IS_GAMES_ENABLED;
import static in.lubble.app.Constants.IS_IMPRESSIONS_COUNT_ENABLED;
import static in.lubble.app.Constants.IS_MAP_SHOWN;
import static in.lubble.app.Constants.IS_NOTIF_SNOOZE_ON;
import static in.lubble.app.Constants.IS_QUIZ_SHOWN;
import static in.lubble.app.Constants.IS_RATING_DIALOG_ACTIVE;
import static in.lubble.app.Constants.IS_REWARDS_SHOWN;
import static in.lubble.app.Constants.IS_TIME_SHOWN;
import static in.lubble.app.Constants.IS_UXCAM_ENABLED;
import static in.lubble.app.Constants.MAP_BTN_URL;
import static in.lubble.app.Constants.MAP_HTML;
import static in.lubble.app.Constants.MAP_SHARE_TEXT;
import static in.lubble.app.Constants.MARKET_MAINTENANCE_TEXT;
import static in.lubble.app.Constants.MSG_WATERMARK_TEXT;
import static in.lubble.app.Constants.QUIZ_RESULT_UI;
import static in.lubble.app.Constants.REFER_MSG;
import static in.lubble.app.Constants.REWARDS_EXPLAINER;
import static in.lubble.app.Constants.SHOW_IMPRESSIONS_COUNT;
import static in.lubble.app.Constants.WIKI_URL;
import static in.lubble.app.analytics.AnalyticsEvents.RATING_DIALOG_FORM;
import static in.lubble.app.analytics.AnalyticsEvents.RATING_DIALOG_FORM_YES;
import static in.lubble.app.analytics.AnalyticsEvents.RATING_DIALOG_SHOWN;
import static in.lubble.app.analytics.AnalyticsEvents.RATING_DIALOG_STARS;
import static in.lubble.app.analytics.AnalyticsEvents.RATING_STORE_DIALOG;
import static in.lubble.app.analytics.AnalyticsEvents.RATING_STORE_DIALOG_NEVER;
import static in.lubble.app.analytics.AnalyticsEvents.RATING_STORE_DIALOG_YES;
import static in.lubble.app.firebase.FcmService.LOGOUT_ACTION;
import static in.lubble.app.firebase.RealtimeDbHelper.getThisUserRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getUserInfoRef;
import static in.lubble.app.utils.AppNotifUtils.TRACK_NOTIF_ID;
import static in.lubble.app.utils.MainUtils.fetchAndPersistAppFeatures;
import static in.lubble.app.utils.UiUtils.determineYOffset;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.clevertap.android.sdk.CleverTapAPI;
import com.codemybrainsout.ratingdialog.RatingDialog;
import com.freshchat.consumer.sdk.Freshchat;
import com.freshchat.consumer.sdk.FreshchatMessage;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.segment.analytics.Traits;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.auth.WelcomeActivity;
import in.lubble.app.chat.BlockedChatsActiv;
import in.lubble.app.chat.GroupPromptSharedPrefs;
import in.lubble.app.explore.ExploreActiv;
import in.lubble.app.feed_groups.FeedExploreActiv;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.groups.ChatGroupListActivity;
import in.lubble.app.groups.ChatSearchListener;
import in.lubble.app.leaderboard.LeaderboardActivity;
import in.lubble.app.lubble_info.LubbleActivity;
import in.lubble.app.marketplace.ItemListActiv;
import in.lubble.app.models.ProfileInfo;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import in.lubble.app.profile.ProfileActivity;
import in.lubble.app.referrals.ReferralActivity;
import in.lubble.app.services.FeedServices;
import in.lubble.app.utils.MainUtils;
import in.lubble.app.utils.ActivityResultListener;
import in.lubble.app.utils.StringUtils;
import in.lubble.app.utils.UiUtils;
import in.lubble.app.utils.UserUtils;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import it.sephiroth.android.library.xtooltip.ClosePolicy;
import it.sephiroth.android.library.xtooltip.Tooltip;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";
    private static final String IS_NEW_USER_IN_THIS_LUBBLE = "IS_NEW_USER_IN_THIS_LUBBLE";

    public static final String EXTRA_TAB_NAME = "extra_tab_name";
    private static final int REQ_CODE_JOIN_GROUPS = 272;

    private Toolbar toolbar;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference connectedReference;
    private ValueEventListener presenceValueListener;
    private RelativeLayout dpContainer;
    private ImageView profileIcon;
    private ImageView navHeaderIv, chatsIv;
    private TextView navHeaderNameTv, toolbarRewardsTv, toolbarSearchTv, unreadChatTv;
    private SearchView searchView;
    private ImageView searchBackIv;
    private TextView toolbarTitle;
    private View lubbleClickTarget;
    private DrawerLayout drawerLayout;
    private ValueEventListener dpEventListener;
    private BottomNavigationView bottomNavigation;
    private boolean isActive;
    private boolean isNewUserInThisLubble;
    private static final int nav_item_leaderboard = 311;
    private Menu navMenu;
    private ActionMode actionMode;
    private ChatSearchListener chatSearchListener;
    private SwipeRefreshLayout.OnRefreshListener feedRefreshListener;
    private ActivityResultListener activityResultListener;
    private boolean isSearchVisible;
    private long unreadChatsCount = 0;

    public static Intent createIntent(Context context, boolean isNewUserInThisLubble) {
        Intent startIntent = new Intent(context, MainActivity.class);
        startIntent.putExtra(IS_NEW_USER_IN_THIS_LUBBLE, isNewUserInThisLubble);
        return startIntent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.lubble_toolbar);
        setSupportActionBar(toolbar);
        profileIcon = toolbar.findViewById(R.id.iv_toolbar_profile);
        dpContainer = toolbar.findViewById(R.id.container_dp);
        toolbarRewardsTv = toolbar.findViewById(R.id.tv_toolbar_rewards);
        toolbarSearchTv = toolbar.findViewById(R.id.tv_toolbar_search);
        chatsIv = toolbar.findViewById(R.id.iv_chats);
        unreadChatTv = toolbar.findViewById(R.id.unread_chats_count_tv);
        searchView = toolbar.findViewById(R.id.search_view);
        searchBackIv = toolbar.findViewById(R.id.iv_search_back);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setElevation(10);
        drawerLayout = findViewById(R.id.drawer_layout);
        toolbarTitle = findViewById(R.id.lubble_toolbar_title);
        lubbleClickTarget = findViewById(R.id.lubble_click_target);

        toolbarRewardsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReferralActivity.open(MainActivity.this);
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null || TextUtils.isEmpty(LubbleSharedPrefs.getInstance().getLubbleId())) {
            // user is not signed in, start login flow
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
            return;
        }

        GlideApp.with(this).load(currentUser.getPhotoUrl())
                .placeholder(R.drawable.ic_account_circle_black_no_padding)
                .circleCrop()
                .into(profileIcon);

        //used to determine whether to show explore dialog or not
        isNewUserInThisLubble = getIntent().hasExtra(IS_NEW_USER_IN_THIS_LUBBLE) && getIntent().getBooleanExtra(IS_NEW_USER_IN_THIS_LUBBLE, false);

        if (!TextUtils.isEmpty(LubbleSharedPrefs.getInstance().getLubbleId())) {
            initEverything();
        } else {
            RealtimeDbHelper.getThisUserRef().child("lubbles").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    HashMap<String, String> map = (HashMap<String, String>) dataSnapshot.getValue();
                    if (map != null && !map.isEmpty()) {
                        LubbleSharedPrefs.getInstance().setLubbleId((String) map.keySet().toArray()[0]);
                        initEverything();
                    } else {
                        FirebaseCrashlytics.getInstance().recordException(new IllegalAccessException("User has NO lubble ID"));
                        UserUtils.logout(MainActivity.this);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    FirebaseCrashlytics.getInstance().recordException(new IllegalAccessException(databaseError.getCode() + " " + databaseError.getMessage()));
                    UserUtils.logout(MainActivity.this);
                }
            });
        }

        if (isNewUserInThisLubble) {
            if (!TextUtils.isEmpty(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())) {
                try {
                    String phNum = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber().substring(3, 13);
                    fetchAssociatedSeller(phNum);
                } catch (IndexOutOfBoundsException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            }
        }

        toolbarSearchTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onToolbarSearchClicked();
            }
        });

        searchBackIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UiUtils.hideKeyboard(MainActivity.this);
                searchView.setQuery("", true);
                toggleSearchViewVisibility(false);
            }
        });

        chatsIv.setOnClickListener(v -> {
            ChatGroupListActivity.Companion.open(this);
        });
    }

    private void onToolbarSearchClicked() {
        toggleSearchViewVisibility(true);
        if (chatSearchListener != null) {
            chatSearchListener.toggleSliderVisibility(false);
            chatSearchListener.reInitGroupListCopy();
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                if (chatSearchListener != null) {
                    chatSearchListener.filterGroups(newText);
                }
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                if (chatSearchListener != null) {
                    chatSearchListener.filterGroups(query);
                }
                return false;
            }
        });
    }

    public void setIsSearchEnabled(boolean isSearchEnabled) {
        toolbarSearchTv.setEnabled(isSearchEnabled);
    }

    public void toggleChatInToolbar(boolean isEnabled) {
        chatsIv.setVisibility(isEnabled ? View.VISIBLE : View.GONE);
        //showUnreadChatCount();
    }

    /*private void showUnreadChatCount() {
        if (chatsIv.getVisibility() == View.VISIBLE) {
            RealtimeDbHelper.getThisUserRef().addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if (chatsIv.getVisibility() == View.VISIBLE) {
                        try {
                            unreadChatsCount = 0;
                            HashMap<String, Object> h = (HashMap<String, Object>) snapshot.getValue();
                            assert h != null;
                            if (h.containsKey("dms")) {
                                HashMap<String, Object> dms = (HashMap<String, Object>) h.get("dms");
                                assert dms != null;
                                for (Object e : dms.values()) {
                                    assert e != null;
                                    HashMap<String, Object> temp = (HashMap<String, Object>) e;
                                    if (temp.get("unreadCount") != null)
                                        unreadChatsCount += (long) temp.get("unreadCount");
                                }
                            }
                            HashMap<String, HashMap<String, Object>> lubbles = (HashMap<String, HashMap<String, Object>>) h.get("lubbles");
                            HashMap<String, Object> groups = (HashMap<String, Object>) lubbles.get(LubbleSharedPrefs.getInstance().getLubbleId()).get("groups");
                            assert groups != null;
                            for (Object e : groups.values()) {
                                assert e != null;
                                HashMap<String, Object> temp = (HashMap<String, Object>) e;
                                if (temp.get("unreadCount") != null)
                                    unreadChatsCount += (long) temp.get("unreadCount");
                            }
                            if (unreadChatsCount != 0) {
                                unreadChatTv.setVisibility(View.VISIBLE);
                                unreadChatTv.setText(Long.toString(unreadChatsCount));
                            } else
                                unreadChatTv.setVisibility(View.GONE);
                        } catch (Exception e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                        }
                    }

                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        }
    }*/

    public void toggleSearchInToolbar(boolean show) {
        toggleSearchViewVisibility(false);
        toolbarSearchTv.setVisibility(show ? View.VISIBLE : View.GONE);
        isSearchVisible = show;
    }

    public ActionMode toggleActionMode(boolean show, ActionMode.Callback callback) {
        if (show && callback != null) {
            return actionMode = startSupportActionMode(callback);
        } else {
            if (actionMode != null) {
                actionMode.finish();
            }
        }
        return null;
    }

    private void toggleSearchViewVisibility(boolean show) {
        if (show) {
            searchView.setVisibility(View.VISIBLE);
            searchView.setFocusable(true);
            searchView.requestFocusFromTouch();
            searchView.setIconifiedByDefault(false);
            searchView.setIconified(false);
            searchBackIv.setVisibility(View.VISIBLE);
            dpContainer.setVisibility(View.GONE);
            lubbleClickTarget.setVisibility(View.GONE);
            toolbarSearchTv.setVisibility(View.GONE);
            toolbarRewardsTv.setVisibility(View.GONE);
            toolbarTitle.setVisibility(View.GONE);
            if (chatSearchListener != null) {
                chatSearchListener.toggleSliderVisibility(false);
            }
        } else {
            searchView.setOnQueryTextListener(null);
            searchView.setVisibility(View.GONE);
            searchBackIv.setVisibility(View.GONE);
            dpContainer.setVisibility(View.VISIBLE);
            toolbarSearchTv.setVisibility(isSearchVisible ? View.VISIBLE : View.GONE);
            lubbleClickTarget.setVisibility(View.VISIBLE);
            //toolbarRewardsTv.setVisibility(View.VISIBLE);
            toolbarTitle.setVisibility(View.VISIBLE);
            if (chatSearchListener != null) {
                chatSearchListener.toggleSliderVisibility(true);
            }
        }
    }

    private void initEverything() {
        syncFcmToken();
        logUser(FirebaseAuth.getInstance().getCurrentUser());

        Branch branch = Branch.getInstance();
        CleverTapAPI clevertapInstance = CleverTapAPI.getDefaultInstance(this);
        if (clevertapInstance != null) {
            branch.setRequestMetadata("$clevertap_attribution_id",
                    clevertapInstance.getCleverTapAttributionIdentifier());
        }
        branch.setIdentity(FirebaseAuth.getInstance().getUid());

        bottomNavigation = findViewById(R.id.navigation);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        addDebugActivOpener(toolbar);

        bottomNavigation.getMenu().clear();

        String lubbleId = LubbleSharedPrefs.getInstance().getLubbleId();
        if (FirebaseRemoteConfig.getInstance().getBoolean(IS_CHATS_ENABLED_FOR_KML) && ("koramangala".equalsIgnoreCase(lubbleId) || "saraswati_vihar".equalsIgnoreCase(lubbleId))) {
            // for existing users show chat-first menu
            NavGraph chatsNavGraph = navController.getNavInflater().inflate(R.navigation.nav_graph_chats);
            navController.setGraph(chatsNavGraph);
            bottomNavigation.inflateMenu(R.menu.navigation_chat_n_feed);
            if (isNewUserInThisLubble) {
                // new signup
                ExploreActiv.open(this, true);
            }
            showFeedTooltip();
        } else {
            NavGraph feedNavGraph = navController.getNavInflater().inflate(R.navigation.nav_graph);
            navController.setGraph(feedNavGraph);
            // Feed-first menu for new n'hoods
            bottomNavigation.inflateMenu(R.menu.navigation_menu_feed);
            if (isNewUserInThisLubble) {
                // new signup; open Feed Explore
                startActivityForResult(FeedExploreActiv.getIntent(MainActivity.this, true, false), REQ_CODE_JOIN_GROUPS);
            } else if (!LubbleSharedPrefs.getInstance().getCheckIfFeedGroupJoined()) {
                // (old user/reinstalled) User might not have joined any feed groups, check with backend
                fetchNewFeedUserStatus();
            }
        }
        NavigationUI.setupWithNavController(bottomNavigation, navController);

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(receiver, new IntentFilter(LOGOUT_ACTION));
        updateDefaultGroupId();

        lubbleClickTarget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LubbleActivity.open(MainActivity.this);
                overridePendingTransition(R.anim.slide_in_from_top, R.anim.none);
            }
        });
        fetchAndPersistAppFeatures();
        initFirebaseRemoteConfig();
        initDrawer();
        initFeedCreds();
    }

    private void initFeedCreds() {
        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        String feedUserToken = LubbleSharedPrefs.getInstance().getFeedUserToken();
        String feedApiKey = LubbleSharedPrefs.getInstance().getFeedApiKey();
        if (TextUtils.isEmpty(feedApiKey) || TextUtils.isEmpty(feedUserToken)) {
            Call<Endpoints.StreamCredentials> call = endpoints.getStreamCredentials(FirebaseAuth.getInstance().getUid());
            call.enqueue(new Callback<Endpoints.StreamCredentials>() {
                @Override
                public void onResponse(Call<Endpoints.StreamCredentials> call, Response<Endpoints.StreamCredentials> response) {
                    if (response.isSuccessful() && !isFinishing()) {
                        assert response.body() != null;
                        final Endpoints.StreamCredentials credentials = response.body();
                        try {
                            FeedServices.initTimelineClient(credentials.getApi_key(), credentials.getUser_token());
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<Endpoints.StreamCredentials> call, Throwable t) {
                }
            });
        }
    }

    public void fetchNewFeedUserStatus() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Setting up Nearby Feed for you...");
        progressDialog.setMessage(getString(R.string.all_please_wait));
        progressDialog.setCancelable(false);
        progressDialog.show();

        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        endpoints.checkIfGroupJoined().enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                if (response.isSuccessful() && !isFinishing()) {
                    progressDialog.dismiss();
                    String message = response.body();
                    if (message != null && message.equals("New User")) {
                        startActivityForResult(FeedExploreActiv.getIntent(MainActivity.this, true, true), REQ_CODE_JOIN_GROUPS);
                    } else {
                        LubbleSharedPrefs.getInstance().setCheckIfFeedGroupJoined();
                    }
                } else if (!isFinishing()) {
                    progressDialog.dismiss();
                    new MaterialAlertDialogBuilder(MainActivity.this)
                            .setTitle("Failed to set up Feed")
                            .setMessage("Error: " + (response.message() == null ? getString(R.string.check_internet) : response.message()))
                            .setPositiveButton(R.string.all_retry, (dialog, which) -> fetchNewFeedUserStatus())
                            .setCancelable(false)
                            .show();
                }
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                if (!isFinishing()) {
                    progressDialog.dismiss();
                    new MaterialAlertDialogBuilder(MainActivity.this)
                            .setTitle("Failed to set up Feed")
                            .setMessage("Error: " + (t.getMessage() == null ? getString(R.string.check_internet) : t.getMessage()))
                            .setPositiveButton(R.string.all_retry, (dialog, which) -> fetchNewFeedUserStatus())
                            .setCancelable(false)
                            .show();
                }
            }
        });
    }

    private void initDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navMenu = navigationView.getMenu();
        final MenuItem item = navMenu.add(R.id.group_top, nav_item_leaderboard, 2, LubbleSharedPrefs.getInstance().getLubbleName() + " Leaderboard");
        item.setIcon(R.drawable.ic_favorite_white_16dp);

        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.inflateHeaderView(R.layout.nav_header_main);
        navHeaderIv = headerView.findViewById(R.id.nav_header_imageView);
        navHeaderNameTv = headerView.findViewById(R.id.nav_header_username);
        final FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        GlideApp.with(this).load(currentUser.getPhotoUrl())
                .placeholder(R.drawable.ic_account_circle_black_no_padding)
                .circleCrop()
                .into(navHeaderIv);

        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileActivity.open(MainActivity.this, firebaseAuth.getUid());
            }
        });

        dpContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });
    }

    private void showRatingsDialog() {
        final LubbleSharedPrefs prefs = LubbleSharedPrefs.getInstance();
        if (prefs.getShowRatingDialog()
                && FirebaseRemoteConfig.getInstance().getBoolean(IS_RATING_DIALOG_ACTIVE)
                && prefs.getRatingDialogLastShown() != 0L //NEVER show when set to ZERO
                && System.currentTimeMillis() - prefs.getRatingDialogLastShown() > TimeUnit.SECONDS.toMillis(20)) {
            prefs.setShowRatingDialog(false);
            prefs.setRatingDialogLastShown(System.currentTimeMillis());

            final RatingDialog ratingDialog = new RatingDialog.Builder(this)
                    //.icon(drawable)
                    .threshold(4)
                    .title("Enjoying Lubble?")
                    .titleTextColor(R.color.black)
                    .positiveButtonText("Not Now")
                    .positiveButtonTextColor(R.color.gray)
                    .negativeButtonText("Never")
                    .negativeButtonTextColor(R.color.gray)
                    .formTitle("Submit Feedback")
                    .formHint("Tell us where we can improve")
                    .formSubmitText("Submit")
                    .formCancelText("Cancel")
                    .ratingBarColor(R.color.gold)
                    .ratingBarBackgroundColor(R.color.light_gray)
                    .onRatingChanged(new RatingDialog.Builder.RatingDialogListener() {
                        @Override
                        public void onRatingSelected(float rating, boolean thresholdCleared) {
                            final Bundle bundle = new Bundle();
                            bundle.putFloat("rating", rating);
                            Analytics.triggerEvent(RATING_DIALOG_STARS, bundle, MainActivity.this);
                            if (!thresholdCleared) {
                                Analytics.triggerEvent(RATING_DIALOG_FORM, MainActivity.this);
                            }
                        }
                    })
                    .onThresholdCleared(new RatingDialog.Builder.RatingThresholdClearedListener() {
                        @Override
                        public void onThresholdCleared(RatingDialog ratingDialog, float rating, boolean thresholdCleared) {
                            ratingDialog.dismiss();
                            showRateInPlayStoreDialog();
                        }
                    })
                    .onRatingBarFormSumbit(new RatingDialog.Builder.RatingDialogFormListener() {
                        @Override
                        public void onFormSubmitted(String feedback) {
                            String tag = "rating_form_submitted";
                            FreshchatMessage FreshchatMessage = new FreshchatMessage().setTag(tag).setMessage("RATING FEEDBACK: " + feedback);
                            Freshchat.sendMessage(MainActivity.this, FreshchatMessage);

                            prefs.setRatingDialogLastShown(0L);
                            Analytics.triggerEvent(RATING_DIALOG_FORM_YES, MainActivity.this);
                        }
                    }).build();

            ratingDialog.show();
            Analytics.triggerEvent(RATING_DIALOG_SHOWN, this);
        }
    }

    private void showRateInPlayStoreDialog() {
        Analytics.triggerEvent(RATING_STORE_DIALOG, this);
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("How about a rating on the Play Store, then?")
                .setMessage("It would mean the world to us if you could rate Lubble on Play Store :)")
                .setPositiveButton("Ok, sure", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainUtils.openPlayStore(MainActivity.this);
                        LubbleSharedPrefs.getInstance().setRatingDialogLastShown(0L);
                        Analytics.triggerEvent(RATING_STORE_DIALOG_YES, MainActivity.this);
                        dialog.dismiss();
                    }
                })
                .setNeutralButton("Never", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LubbleSharedPrefs.getInstance().setRatingDialogLastShown(0L);
                        Analytics.triggerEvent(RATING_STORE_DIALOG_NEVER, MainActivity.this);
                        dialog.dismiss();
                    }
                })
                .show();
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(ContextCompat.getColor(this, R.color.dark_gray));
    }

    private void showQuizBadge() {
        if (!LubbleSharedPrefs.getInstance().getIsQuizOpened() && FirebaseRemoteConfig.getInstance().getBoolean(Constants.IS_QUIZ_SHOWN)) {
            BottomNavigationMenuView bottomNavigationMenuView =
                    (BottomNavigationMenuView) bottomNavigation.getChildAt(0);
            View v = bottomNavigationMenuView.getChildAt(3);
            BottomNavigationItemView itemView = (BottomNavigationItemView) v;

            View badge = LayoutInflater.from(this)
                    .inflate(R.layout.notification_badge, bottomNavigationMenuView, false);

            itemView.addView(badge);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        isActive = true;
        Branch branch = Branch.getInstance();

        // Branch init
        branch.initSession(new Branch.BranchReferralInitListener() {
            @Override
            public void onInitFinished(JSONObject referringParams, BranchError error) {
                if (error == null) {
                    // params are the deep linked params associated with the link that the user clicked -> was re-directed to this app
                    // params will be empty if no data found
                    Log.i("BRANCH SDK", referringParams.toString());
                    final String referrerUid = referringParams.optString("referrer_uid");
                    final String groupId = referringParams.optString("group_id");
                    final LubbleSharedPrefs prefs = LubbleSharedPrefs.getInstance();
                    if (!TextUtils.isEmpty(referrerUid)) {
                        prefs.setReferrerUid(referrerUid);
                    }
                    if (!TextUtils.isEmpty(groupId)) {
                        prefs.setInvitedGroupId(groupId);
                        processNewGroupInvite(prefs);
                    }
                } else {
                    Log.i("BRANCH SDK", error.getMessage());
                }
            }
        }, this.getIntent().getData(), this);
        branch.setIdentity(FirebaseAuth.getInstance().getUid());

        final LubbleSharedPrefs sharedPrefs = LubbleSharedPrefs.getInstance();
        if (!sharedPrefs.getInvitedGroupId().isEmpty()) {
            processNewGroupInvite(sharedPrefs);
        }
    }

    private void processNewGroupInvite(LubbleSharedPrefs prefs) {
        final DatabaseReference inviteesRef = FirebaseDatabase.getInstance().getReference("users/" + prefs.getReferrerUid()
                + "/lubbles/" + prefs.requireLubbleId()).child("groups").child(prefs.getInvitedGroupId()).child("invitees");
        inviteesRef.child(firebaseAuth.getUid()).setValue(Boolean.TRUE);
        prefs.setInvitedGroupId("");
        prefs.setReferrerUid("");
    }

    private void initFirebaseRemoteConfig() {
        FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(TimeUnit.HOURS.toSeconds(1))
                .build();
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        HashMap<String, Object> map = new HashMap<>();
        map.put(REFER_MSG, getString(R.string.refer_msg));
        map.put(IS_QUIZ_SHOWN, true);
        map.put(QUIZ_RESULT_UI, "normal");
        map.put(GROUP_QUES_ENABLED, true);
        map.put(DELIVERY_FEE, 100);
        map.put(IS_REWARDS_SHOWN, false);
        map.put(IS_RATING_DIALOG_ACTIVE, true);
        map.put(REWARDS_EXPLAINER, "https://firebasestorage.googleapis.com/v0/b/lubble-in-default/o/chat_sliders%2Freward_explainer.png?alt=media&token=33f50ce7-c1b7-4d90-84d6-c0ff41f9e39f");
        map.put(MAP_HTML, "<iframe src=\"https://www.google.com/maps/d/u/0/embed?mid=1WOlTsxB_1GBnn6PCVL-uOJO7ePw_X2rO\" frameborder=\"0\" style=\"overflow: hidden; height: 100%; width: 100%; position: absolute;\"></iframe>");
        map.put(MAP_BTN_URL, "https://mplace.typeform.com/to/M1OXzy?name=^^username&uid=^^uid");
        map.put(MAP_SHARE_TEXT, "Check out this map of open grocery stores in Koramangala: https://lubble.in/online-grocery-stores-map-koramangala/");
        map.put(EVENTS_MAINTENANCE_TEXT, "");
        map.put(EVENTS_MAINTENANCE_IMG, "");
        map.put(MARKET_MAINTENANCE_TEXT, "");
        map.put(IS_NOTIF_SNOOZE_ON, true);
        map.put(IS_TIME_SHOWN, true);
        map.put(MSG_WATERMARK_TEXT, "-via Lubble, the local app for {lubble}. Download: ");
        map.put(WIKI_URL, "https://lubble.in/neighbourhoods/");
        map.put(IS_UXCAM_ENABLED, true);
        map.put(IS_GAMES_ENABLED, false);
        map.put(IS_MAP_SHOWN, false);
        map.put(DEFAULT_SHOP_PIC, "https://i.imgur.com/thqJQxg.png");
        map.put(IS_CHATS_ENABLED_FOR_KML, false);
        map.put(IS_IMPRESSIONS_COUNT_ENABLED, true);
        map.put(SHOW_IMPRESSIONS_COUNT, false);
        firebaseRemoteConfig.setDefaultsAsync(map);
        if (firebaseRemoteConfig.getBoolean(IS_REWARDS_SHOWN)) {
            //toolbarRewardsTv.setVisibility(View.VISIBLE);
        } else {
            toolbarRewardsTv.setVisibility(View.GONE);
        }
    }

    private void showFeedTooltip() {
        if (!LubbleSharedPrefs.getInstance().getIsFeedVisited()) {
            BottomNavigationMenuView bottomNavigationMenuView = (BottomNavigationMenuView) bottomNavigation.getChildAt(0);
            View feedView = bottomNavigationMenuView.getChildAt(1);
            feedView.post(() -> {
                Tooltip tooltip = new Tooltip.Builder(MainActivity.this)
                        .anchor(feedView, 0, determineYOffset(this), false)
                        .closePolicy(ClosePolicy.Companion.getTOUCH_ANYWHERE_NO_CONSUME())
                        .showDuration(45000)
                        .overlay(true)
                        .floatingAnimation(Tooltip.Animation.Companion.getDEFAULT())
                        .styleId(R.style.FeedTooltipLayout)
                        .text("NEW: View nearby posts")
                        .create();
                tooltip.show(feedView, Tooltip.Gravity.TOP, true);
            });
        }
    }

    public BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String uid = intent.getStringExtra("UID");
                if (uid.equalsIgnoreCase(FirebaseAuth.getInstance().getUid())) {
                    UserUtils.logout(MainActivity.this);
                }
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        if (LubbleSharedPrefs.getInstance().getIsLogoutPending()) {
            UserUtils.logout(this);
            for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            }
            return;
        }

        handlePresence();
        setDp();
        toggleSearchViewVisibility(false);
        searchView.setQuery("", false);

        if (getIntent().hasExtra(EXTRA_TAB_NAME)) {
            switch (getIntent().getStringExtra(EXTRA_TAB_NAME)) {
                case "events":
                    bottomNavigation.setSelectedItemId(R.id.navigation_events);
                    break;
                /*case "map":
                    bottomNavigation.setSelectedItemId(R.id.navigation_map);
                    break;*/
                case "services":
                    bottomNavigation.setSelectedItemId(R.id.navigation_market);
                    break;
                case "mplace":
                    bottomNavigation.setSelectedItemId(R.id.navigation_market);
                    break;
                case "feed":
                    bottomNavigation.setSelectedItemId(R.id.navigation_feed);
                    break;
                case "explore":
                    bottomNavigation.setSelectedItemId(R.id.navigation_feed_groups);
                    break;
                case "games":
                    bottomNavigation.setSelectedItemId(R.id.navigation_fun);
                    break;
            }
            getIntent().removeExtra(EXTRA_TAB_NAME);
        }
        try {
            Intent intent = this.getIntent();
            if (intent != null && intent.getExtras() != null && intent.getExtras().containsKey(TRACK_NOTIF_ID)
                    && (intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0) {
                String notifId = this.getIntent().getExtras().getString(TRACK_NOTIF_ID);

                if (!TextUtils.isEmpty(notifId)) {
                    final Bundle bundle = new Bundle();
                    bundle.putString("notifKey", notifId);
                    bundle.putString("tabName", getIntent().getStringExtra(EXTRA_TAB_NAME));
                    Analytics.triggerEvent(AnalyticsEvents.NOTIF_OPENED, bundle, this);
                }
            }
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        final FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        firebaseRemoteConfig.fetch(TimeUnit.HOURS.toSeconds(1)).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                firebaseRemoteConfig.activate().addOnCompleteListener(this, t -> {
                    if (firebaseRemoteConfig.getBoolean(IS_REWARDS_SHOWN)) {
                        //toolbarRewardsTv.setVisibility(View.VISIBLE);
                    } else {
                        toolbarRewardsTv.setVisibility(View.GONE);
                    }
                });
            }
        });
        showRatingsDialog();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        /*
        When MainActivity is called via Server Notification, if it's already open then onResume will be called but
        it'll have the old intent, so here we reset it. onNewIntent() is called before onResume.
        */
        this.setIntent(intent);
    }

    private void setDp() {
        dpEventListener = getUserInfoRef(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ProfileInfo profileInfo = dataSnapshot.getValue(ProfileInfo.class);
                try {
                    if (profileInfo != null) {
                        LubbleSharedPrefs.getInstance().setUserFlair(profileInfo.getBadge());
                        GlideApp.with(MainActivity.this)
                                .load(profileInfo == null ? "" : profileInfo.getThumbnail())
                                .circleCrop()
                                .placeholder(R.drawable.ic_account_circle_black_no_padding)
                                .error(R.drawable.ic_account_circle_black_no_padding)
                                .into(profileIcon);
                        GlideApp.with(MainActivity.this)
                                .load(profileInfo == null ? "" : profileInfo.getThumbnail())
                                .circleCrop()
                                .placeholder(R.drawable.ic_account_circle_black_no_padding)
                                .error(R.drawable.ic_account_circle_black_no_padding)
                                .into(navHeaderIv);
                        navHeaderNameTv.setText(StringUtils.getTitleCase(profileInfo != null ? profileInfo.getName() : ""));
                        LubbleSharedPrefs.getInstance().setProfilePicUrl(profileInfo == null ? "" : profileInfo.getThumbnail());

                        if (profileInfo != null && !isFinishing()) {
                            com.segment.analytics.Analytics.with(MainActivity.this).identify(firebaseAuth.getUid(), new Traits().putAvatar(profileInfo.getThumbnail()), null);
                        }
                    }
                } catch (IllegalArgumentException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void handlePresence() {
        connectedReference = RealtimeDbHelper.getConnectedInfoRef();
        presenceValueListener = connectedReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = dataSnapshot.getValue(Boolean.class);
                if (connected) {
                    final DatabaseReference presenceRef = RealtimeDbHelper.getPresenceRef()
                            .child(FirebaseAuth.getInstance().getUid());

                    // when this device disconnects, remove it
                    presenceRef.onDisconnect().setValue(Boolean.FALSE);

                    presenceRef.setValue(Boolean.TRUE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("Listener was cancelled at .info/connected");
            }
        });
    }

    private void addDebugActivOpener(Toolbar toolbar) {
        if (BuildConfig.DEBUG) {
            lubbleClickTarget.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    final Intent intent = new Intent(MainActivity.this, DebugActivity.class);
                    startActivity(intent);
                    return false;
                }
            });
        }
    }

    private void logUser(FirebaseUser currentUser) {
        FirebaseCrashlytics.getInstance().setUserId(currentUser.getUid());
    }

    private void syncFcmToken() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    String token = task.getResult();
                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put("token", token);
                    childUpdates.put("tokenTimestamp", System.currentTimeMillis());
                    getThisUserRef().updateChildren(childUpdates);
                    CleverTapAPI.getDefaultInstance(MainActivity.this).pushFcmRegistrationId(token, true);
                    Freshchat.getInstance(MainActivity.this).setPushRegistrationToken(token);
                } else {
                    Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                }
            });
        }
    }

    private void updateDefaultGroupId() {
        RealtimeDbHelper.getLubbleInfoRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String lubbleName = dataSnapshot.child("title").getValue(String.class);
                toolbarTitle.setText(lubbleName);
                toolbarTitle.setVisibility(View.VISIBLE);
                final LubbleSharedPrefs prefs = LubbleSharedPrefs.getInstance();
                prefs.setLubbleName(lubbleName);
                prefs.setDefaultGroupId(dataSnapshot.child("defaultGroup").getValue(String.class));
                prefs.setSupportUid(dataSnapshot.child("supportUid").getValue(String.class));
                navMenu.findItem(nav_item_leaderboard).setTitle(lubbleName + " Leaderboard");
                if (isNewUserInThisLubble) {
                    GroupPromptSharedPrefs.getInstance().putGroupId(dataSnapshot.child("introGroup").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void fetchAssociatedSeller(String phoneNumber) {
        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        endpoints.fetchExistingSellerFromPh(phoneNumber).enqueue(new Callback<Endpoints.ExistingSellerData>() {
            @Override
            public void onResponse(Call<Endpoints.ExistingSellerData> call, Response<Endpoints.ExistingSellerData> response) {
                final Endpoints.ExistingSellerData existingSellerData = response.body();
                if (existingSellerData != null && existingSellerData.getSellerIdList() != null && !existingSellerData.getSellerIdList().isEmpty()) {
                    final int sellerId = Integer.parseInt(existingSellerData.getSellerIdList().get(0));
                    LubbleSharedPrefs.getInstance().setSellerId(sellerId);
                    UiUtils.showBottomSheetAlertLight(MainActivity.this, getLayoutInflater(),
                            "Your business is already on Lubble!", "\nWe have found a business associated with your phone number! You can now manage your business profile & start selling locally on Lubble for Free!\n",
                            R.drawable.ic_open, "Check My Business", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ItemListActiv.open(MainActivity.this, true, sellerId);
                                    Analytics.triggerEvent(AnalyticsEvents.EXISTING_SELLER_DIALOG_CLICK, MainActivity.this);
                                }
                            });
                    Analytics.triggerEvent(AnalyticsEvents.EXISTING_SELLER_DIALOG_SHOWN, MainActivity.this);
                } else {
                    Log.e(TAG, "onResponse failed: " + response.body().toString());
                }
            }

            @Override
            public void onFailure(Call<Endpoints.ExistingSellerData> call, Throwable t) {
                Log.e(TAG, "onFailure: ");
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (connectedReference != null && presenceValueListener != null) {
            connectedReference.removeEventListener(presenceValueListener);
        }
        getUserInfoRef(firebaseAuth.getUid()).removeEventListener(dpEventListener);
    }

    public void setRefreshListener(SwipeRefreshLayout.OnRefreshListener listener) {
        this.feedRefreshListener = listener;
    }

    public void setOnActivResultForFrag(ActivityResultListener listener) {
        this.activityResultListener = listener;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_item_profile:
                ProfileActivity.open(this, firebaseAuth.getUid());
                break;
            case R.id.nav_item_refer:
                ReferralActivity.open(this, true);
                break;
            case nav_item_leaderboard:
                LeaderboardActivity.open(this);
                break;
            case R.id.nav_item_support:
                Freshchat.showConversations(this);
                break;
            case R.id.nav_item_blocked_chats:
                BlockedChatsActiv.open(this);
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_JOIN_GROUPS && feedRefreshListener != null) {
            feedRefreshListener.onRefresh();
        } else if (activityResultListener != null) {
            activityResultListener.onActivityResultForFrag(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    public void removeMplaceBadge() {
        BottomNavigationMenuView bottomNavigationMenuView = (BottomNavigationMenuView) bottomNavigation.getChildAt(0);
        View v = bottomNavigationMenuView.getChildAt(2);
        BottomNavigationItemView itemView = (BottomNavigationItemView) v;
        if (itemView != null && itemView.getChildAt(2) != null) {
            itemView.removeViewAt(2);
        }
    }

    public void removeServicesBadge() {
        BottomNavigationMenuView bottomNavigationMenuView = (BottomNavigationMenuView) bottomNavigation.getChildAt(0);
        View v = bottomNavigationMenuView.getChildAt(3);
        BottomNavigationItemView itemView = (BottomNavigationItemView) v;
        if (itemView != null && itemView.getChildAt(2) != null) {
            itemView.removeViewAt(2);
        }
    }

    public void removeQuizBadge() {
        BottomNavigationMenuView bottomNavigationMenuView = (BottomNavigationMenuView) bottomNavigation.getChildAt(0);
        View v = bottomNavigationMenuView.getChildAt(3);
        BottomNavigationItemView itemView = (BottomNavigationItemView) v;
        if (itemView != null && itemView.getChildAt(2) != null) {
            itemView.removeViewAt(2);
        }
    }

    public void showEventsBadge(int count) {
        BottomNavigationMenuView bottomNavigationMenuView =
                (BottomNavigationMenuView) bottomNavigation.getChildAt(0);
        View v = bottomNavigationMenuView.getChildAt(2);
        BottomNavigationItemView itemView = (BottomNavigationItemView) v;

        if (count > 0) {
            View badge = LayoutInflater.from(this)
                    .inflate(R.layout.unread_badge, bottomNavigationMenuView, false);
            ((TextView) badge.findViewById(R.id.tv_count)).setText(String.valueOf(count));
            itemView.addView(badge);
        } else {
            if (itemView != null && itemView.getChildAt(2) != null) {
                itemView.removeViewAt(2);
            }
        }
    }

    public void setChatSearchListener(ChatSearchListener listener) {
        this.chatSearchListener = listener;
    }

    public void setSelectedNavPos(int itemId) {
        bottomNavigation.setSelectedItemId(itemId);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (searchView.getVisibility() == View.VISIBLE) {
            UiUtils.hideKeyboard(MainActivity.this);
            searchView.setQuery("", true);
            toggleSearchViewVisibility(false);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        isActive = false;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }
}
