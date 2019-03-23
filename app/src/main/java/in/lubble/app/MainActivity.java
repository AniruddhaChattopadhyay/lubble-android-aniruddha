package in.lubble.app;

import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.clevertap.android.sdk.CleverTapAPI;
import com.crashlytics.android.Crashlytics;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.auth.LoginActivity;
import in.lubble.app.events.EventsFrag;
import in.lubble.app.explore.ExploreActiv;
import in.lubble.app.explore.ExploreFrag;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.groups.GroupListFragment;
import in.lubble.app.lubble_info.LubbleActivity;
import in.lubble.app.marketplace.MarketplaceFrag;
import in.lubble.app.models.ProfileInfo;
import in.lubble.app.profile.ProfileActivity;
import in.lubble.app.quiz.GamesFrag;
import in.lubble.app.services.ServicesFrag;
import in.lubble.app.utils.StringUtils;
import in.lubble.app.utils.UserUtils;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import org.json.JSONObject;

import java.util.HashMap;

import static in.lubble.app.Constants.*;
import static in.lubble.app.firebase.FcmService.LOGOUT_ACTION;
import static in.lubble.app.firebase.RealtimeDbHelper.getThisUserRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getUserInfoRef;
import static in.lubble.app.utils.AppNotifUtils.TRACK_NOTIF_ID;
import static in.lubble.app.utils.MainUtils.fetchAndPersistAppFeatures;
import static in.lubble.app.utils.MainUtils.fetchAndPersistMplaceItems;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    private static final String EXTRA_IDP_RESPONSE = "extra_idp_response";

    public static final String EXTRA_TAB_NAME = "extra_tab_name";

    private Toolbar toolbar;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference connectedReference;
    private ValueEventListener presenceValueListener;
    private ImageView profileIcon;
    private TextView toolbarTitle;
    private View lubbleClickTarget;
    private ValueEventListener dpEventListener;
    private BottomNavigationView bottomNavigation;
    private boolean isActive;
    private boolean isNewUser;

    public static Intent createIntent(Context context, IdpResponse idpResponse) {
        Intent startIntent = new Intent(context, MainActivity.class);
        if (idpResponse != null) {
            startIntent.putExtra(EXTRA_IDP_RESPONSE, idpResponse);
        }
        return startIntent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.lubble_toolbar);
        setSupportActionBar(toolbar);
        profileIcon = toolbar.findViewById(R.id.iv_toolbar_profile);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setElevation(10);
        toolbarTitle = findViewById(R.id.lubble_toolbar_title);
        lubbleClickTarget = findViewById(R.id.lubble_click_target);
        toolbarTitle.setVisibility(View.VISIBLE);
        profileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProfile();
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null || !StringUtils.isValidString(currentUser.getDisplayName())) {
            // user is not signed in, start login flow
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        if (LubbleSharedPrefs.getInstance().getIsLogoutPending()) {
            UserUtils.logout(this);
            return;
        }

        GlideApp.with(this).load(currentUser.getPhotoUrl())
                .placeholder(R.drawable.ic_account_circle_black_no_padding)
                .circleCrop()
                .into(profileIcon);

        isNewUser = getIntent().hasExtra(EXTRA_IDP_RESPONSE) && ((IdpResponse) getIntent().getParcelableExtra(EXTRA_IDP_RESPONSE)).isNewUser();

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
                } else {
                    Log.i("BRANCH SDK", error.getMessage());
                }
            }
        }, this.getIntent().getData(), this);
        branch.setIdentity(FirebaseAuth.getInstance().getUid());

    }

    private void handleExploreActivity() {
        if (isNewUser) {
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
        firebaseRemoteConfig.setDefaults(map);
    }

    public void openExplore() {
        bottomNavigation.findViewById(R.id.navigation_explore).performClick();
    }

    private void showBottomNavBadge() {
        if (!isNewUser) {
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
        firebaseRemoteConfig.fetch().addOnCompleteListener(this, new OnCompleteListener<Void>() {
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
                    showQuizBadge();
                }
            }
        });
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
        getThisUserRef().child("token")
                .setValue(FirebaseInstanceId.getInstance().getToken());
        CleverTapAPI.getDefaultInstance(this).pushFcmRegistrationId(FirebaseInstanceId.getInstance().getToken(), true);
    }

    private void updateDefaultGroupId() {
        RealtimeDbHelper.getLubbleRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                toolbarTitle.setText(dataSnapshot.child("title").getValue(String.class));
                LubbleSharedPrefs.getInstance().setDefaultGroupId(dataSnapshot.child("defaultGroup").getValue(String.class));
                LubbleSharedPrefs.getInstance().setSupportUid(dataSnapshot.child("supportUid").getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        connectedReference.removeEventListener(presenceValueListener);
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

    private void switchFrag(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.content, fragment).commitAllowingStateLoss();
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

    @Override
    protected void onStop() {
        super.onStop();
        isActive = false;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }
}
