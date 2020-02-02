package in.lubble.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.clevertap.android.sdk.CleverTapAPI;
import com.codemybrainsout.ratingdialog.RatingDialog;
import com.crashlytics.android.Crashlytics;
import com.freshchat.consumer.sdk.Freshchat;
import com.freshchat.consumer.sdk.FreshchatMessage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.segment.analytics.Traits;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.auth.LoginActivity;
import in.lubble.app.chat.BlockedChatsActiv;
import in.lubble.app.events.EventsFrag;
import in.lubble.app.explore.ExploreActiv;
import in.lubble.app.explore.ExploreFrag;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.groups.GroupListFragment;
import in.lubble.app.leaderboard.LeaderboardActivity;
import in.lubble.app.lubble_info.LubbleActivity;
import in.lubble.app.marketplace.MarketplaceFrag;
import in.lubble.app.models.ProfileInfo;
import in.lubble.app.profile.ProfileActivity;
import in.lubble.app.quiz.GamesFrag;
import in.lubble.app.referrals.ReferralActivity;
import in.lubble.app.services.ServicesFrag;
import in.lubble.app.utils.MainUtils;
import in.lubble.app.utils.StringUtils;
import in.lubble.app.utils.UserUtils;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import it.sephiroth.android.library.xtooltip.ClosePolicy;
import it.sephiroth.android.library.xtooltip.Tooltip;

import static in.lubble.app.Constants.DELIVERY_FEE;
import static in.lubble.app.Constants.GROUP_QUES_ENABLED;
import static in.lubble.app.Constants.IS_QUIZ_SHOWN;
import static in.lubble.app.Constants.IS_RATING_DIALOG_ACTIVE;
import static in.lubble.app.Constants.IS_REWARDS_SHOWN;
import static in.lubble.app.Constants.QUIZ_RESULT_UI;
import static in.lubble.app.Constants.REFER_MSG;
import static in.lubble.app.Constants.REWARDS_EXPLAINER;
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
import static in.lubble.app.utils.MainUtils.fetchAndPersistMplaceItems;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";
    private static final String IS_NEW_USER_IN_THIS_LUBBLE = "IS_NEW_USER_IN_THIS_LUBBLE";

    public static final String EXTRA_TAB_NAME = "extra_tab_name";

    private Toolbar toolbar;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference connectedReference;
    private ValueEventListener presenceValueListener;
    private RelativeLayout dpContainer;
    private ImageView profileIcon;
    private ImageView navHeaderIv;
    private TextView navHeaderNameTv;
    private TextView toolbarRewardsTv;
    private TextView toolbarTitle;
    private View lubbleClickTarget;
    private DrawerLayout drawerLayout;
    private ValueEventListener dpEventListener;
    private BottomNavigationView bottomNavigation;
    private boolean isActive;
    private boolean isNewUserInThisLubble;
    private static final int nav_item_leaderboard = 311;
    private Menu navMenu;


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
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setElevation(10);
        drawerLayout = findViewById(R.id.drawer_layout);
        toolbarTitle = findViewById(R.id.lubble_toolbar_title);
        lubbleClickTarget = findViewById(R.id.lubble_click_target);
        toolbarTitle.setVisibility(View.VISIBLE);

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
            startActivity(new Intent(this, LoginActivity.class));
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
                        Crashlytics.logException(new IllegalAccessException("User has NO lubble ID"));
                        UserUtils.logout(MainActivity.this);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Crashlytics.logException(new IllegalAccessException(databaseError.getCode() + " " + databaseError.getMessage()));
                    UserUtils.logout(MainActivity.this);
                }
            });
        }

        handleExploreActivity();
    }


    private void initEverything() {
        syncFcmToken();
        logUser(FirebaseAuth.getInstance().getCurrentUser());
        Branch.getInstance().setIdentity(FirebaseAuth.getInstance().getUid());

        switchFrag(GroupListFragment.newInstance());

        bottomNavigation = findViewById(R.id.navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        addDebugActivOpener(toolbar);

        if (!FirebaseRemoteConfig.getInstance().getBoolean(Constants.IS_QUIZ_SHOWN)) {
            bottomNavigation.getMenu().clear();
            bottomNavigation.inflateMenu(R.menu.navigation_market);
        }

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(receiver, new IntentFilter(LOGOUT_ACTION));
        updateDefaultGroupId();

        lubbleClickTarget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LubbleActivity.open(MainActivity.this);
            }
        });
        //showBottomNavBadge();
        fetchAndPersistAppFeatures();
        fetchAndPersistMplaceItems();
        initFirebaseRemoteConfig();
        initDrawer();
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

    private void handleExploreActivity() {
        if (isNewUserInThisLubble) {
            // new signup
            ExploreActiv.open(this, true);
        } else {
            if (!LubbleSharedPrefs.getInstance().getIsExploreShown()) {
                RealtimeDbHelper.getThisUserRef().child("isExploreShown").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null && isActive && !isFinishing()) {
                            final Boolean isExploreShownInRdb = dataSnapshot.getValue() == null ? false : dataSnapshot.getValue(Boolean.class);
                            LubbleSharedPrefs.getInstance().setIsExploreShown(isExploreShownInRdb);
                            if (!isExploreShownInRdb) {
                                openExploreWithDelay();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        }
    }

    private void openExploreWithDelay() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isActive && !isFinishing()) {
                    ExploreActiv.open(MainActivity.this, false);
                    overridePendingTransition(R.anim.slide_from_bottom, R.anim.none);
                }
            }
        }, 300);
    }

    private void initFirebaseRemoteConfig() {
        FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        firebaseRemoteConfig.setConfigSettings(configSettings);
        HashMap<String, Object> map = new HashMap<>();
        map.put(REFER_MSG, getString(R.string.refer_msg));
        map.put(IS_QUIZ_SHOWN, true);
        map.put(QUIZ_RESULT_UI, "normal");
        map.put(GROUP_QUES_ENABLED, true);
        map.put(DELIVERY_FEE, 100);
        map.put(IS_REWARDS_SHOWN, false);
        map.put(IS_RATING_DIALOG_ACTIVE, true);
        map.put(REWARDS_EXPLAINER, "https://firebasestorage.googleapis.com/v0/b/lubble-in-default/o/chat_sliders%2Freward_explainer.png?alt=media&token=33f50ce7-c1b7-4d90-84d6-c0ff41f9e39f");
        firebaseRemoteConfig.setDefaults(map);
        if (firebaseRemoteConfig.getBoolean(IS_REWARDS_SHOWN)) {
            toolbarRewardsTv.setVisibility(View.VISIBLE);
        } else {
            toolbarRewardsTv.setVisibility(View.GONE);
        }
    }

    public void openExplore() {
        bottomNavigation.findViewById(R.id.navigation_explore).performClick();
    }

    private void showBottomNavBadge() {
        if (!isNewUserInThisLubble) {
            if (!LubbleSharedPrefs.getInstance().getIsMplaceOpened()) {
                BottomNavigationMenuView bottomNavigationMenuView =
                        (BottomNavigationMenuView) bottomNavigation.getChildAt(0);
                View v = bottomNavigationMenuView.getChildAt(2);
                BottomNavigationItemView itemView = (BottomNavigationItemView) v;

                View badge = LayoutInflater.from(this)
                        .inflate(R.layout.notification_badge, bottomNavigationMenuView, false);

                itemView.addView(badge);
            }
            if (!LubbleSharedPrefs.getInstance().getIsServicesOpened()) {
                BottomNavigationMenuView bottomNavigationMenuView =
                        (BottomNavigationMenuView) bottomNavigation.getChildAt(0);
                View v = bottomNavigationMenuView.getChildAt(3);
                BottomNavigationItemView itemView = (BottomNavigationItemView) v;

                View badge = LayoutInflater.from(this)
                        .inflate(R.layout.notification_badge, bottomNavigationMenuView, false);

                itemView.addView(badge);
            }
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

        if (getIntent().hasExtra(EXTRA_TAB_NAME)) {
            switch (getIntent().getStringExtra(EXTRA_TAB_NAME)) {
                case "events":
                    bottomNavigation.setSelectedItemId(R.id.navigation_events);
                    break;
                case "services":
                    bottomNavigation.setSelectedItemId(R.id.navigation_services);
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
            Crashlytics.logException(e);
        }

        final FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        firebaseRemoteConfig.fetch(TimeUnit.HOURS.toSeconds(1)).addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    firebaseRemoteConfig.activateFetched();
                    if (!firebaseRemoteConfig.getBoolean(Constants.IS_QUIZ_SHOWN)) {
                        bottomNavigation.getMenu().clear();
                        bottomNavigation.inflateMenu(R.menu.navigation_market);
                    } else if (bottomNavigation.getMenu().findItem(R.id.navigation_fun) == null) {
                        // change only if new menu wasnt present before
                        bottomNavigation.getMenu().clear();
                        bottomNavigation.inflateMenu(R.menu.navigation);
                    }
                    if (firebaseRemoteConfig.getBoolean(IS_REWARDS_SHOWN)) {
                        toolbarRewardsTv.setVisibility(View.VISIBLE);
                    } else {
                        toolbarRewardsTv.setVisibility(View.GONE);
                    }
                }
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
                    navHeaderNameTv.setText(StringUtils.getTitleCase(profileInfo.getName()));

                    if (profileInfo != null && !isFinishing()) {
                        com.segment.analytics.Analytics.with(MainActivity.this).identify(firebaseAuth.getUid(), new Traits().putAvatar(profileInfo.getThumbnail()), null);
                    }
                } catch (IllegalArgumentException e) {
                    Crashlytics.logException(e);
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
            toolbar.setOnLongClickListener(new View.OnLongClickListener() {

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
        Crashlytics.setUserIdentifier(currentUser.getUid());
        Crashlytics.setUserName(currentUser.getDisplayName());
    }

    private void syncFcmToken() {
        final String token = FirebaseInstanceId.getInstance().getToken();
        getThisUserRef().child("token")
                .setValue(token);
        CleverTapAPI.getDefaultInstance(this).pushFcmRegistrationId(token, true);
        Freshchat.getInstance(this).setPushRegistrationToken(token);
    }

    private void updateDefaultGroupId() {
        RealtimeDbHelper.getLubbleRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String lubbleName = dataSnapshot.child("title").getValue(String.class);
                toolbarTitle.setText(lubbleName);
                final LubbleSharedPrefs prefs = LubbleSharedPrefs.getInstance();
                prefs.setLubbleName(lubbleName);
                prefs.setDefaultGroupId(dataSnapshot.child("defaultGroup").getValue(String.class));
                prefs.setSupportUid(dataSnapshot.child("supportUid").getValue(String.class));
                navMenu.findItem(nav_item_leaderboard).setTitle(lubbleName + " Leaderboard");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (connectedReference != null) {
            connectedReference.removeEventListener(presenceValueListener);
        }
        getUserInfoRef(firebaseAuth.getUid()).removeEventListener(dpEventListener);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_chats:
                    switchFrag(GroupListFragment.newInstance());
                    return true;
                case R.id.navigation_explore:
                    switchFrag(ExploreFrag.newInstance());
                    return true;
                case R.id.navigation_events:
                    switchFrag(EventsFrag.newInstance());
                    return true;
                case R.id.navigation_fun:
                    switchFrag(GamesFrag.newInstance());
                    return true;
                case R.id.navigation_market:
                    switchFrag(MarketplaceFrag.newInstance());
                    return true;
                case R.id.navigation_services:
                    switchFrag(ServicesFrag.newInstance());
                    return true;
            }
            return false;
        }
    };

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

    private void switchFrag(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.tv_book_author, fragment).commitAllowingStateLoss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    private void openProfile() {
        ProfileActivity.open(this, FirebaseAuth.getInstance().getUid());
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

    public void showRewardsTooltip() {
        if (toolbarRewardsTv.getVisibility() == View.VISIBLE && !LubbleSharedPrefs.getInstance().getIsRewardsOpened() && LubbleSharedPrefs.getInstance().getIsDefaultGroupOpened()) {
            final Tooltip tooltip = new Tooltip.Builder(this)
                    .anchor(toolbarRewardsTv, 0, 0, false)
                    .text("NEW! Get cool rewards nearby")
                    .arrow(true)
                    .floatingAnimation(Tooltip.Animation.Companion.getDEFAULT())
                    .closePolicy(new ClosePolicy.Builder().inside(true).consume(false).outside(true).build())
                    .showDuration(15000)
                    .overlay(true)
                    .styleId(R.style.ToolTipLayoutHoianStyle)
                    .create();

            tooltip.show(toolbarRewardsTv, Tooltip.Gravity.BOTTOM, true);
            GlideApp.with(this).load(FirebaseRemoteConfig.getInstance().getString(REWARDS_EXPLAINER)).preload();
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
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
