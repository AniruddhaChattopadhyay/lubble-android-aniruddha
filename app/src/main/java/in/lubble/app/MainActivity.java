package in.lubble.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import in.lubble.app.announcements.announcementHistory.AnnouncementsFrag;
import in.lubble.app.auth.LoginActivity;
import in.lubble.app.domestic_directory.DomesticDirectoryFrag;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.groups.GroupListFragment;
import in.lubble.app.models.ProfileInfo;
import in.lubble.app.profile.ProfileActivity;
import in.lubble.app.utils.StringUtils;
import in.lubble.app.utils.UserUtils;

import static in.lubble.app.firebase.FcmService.LOGOUT_ACTION;
import static in.lubble.app.firebase.RealtimeDbHelper.getThisUserRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getUserInfoRef;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String EXTRA_IDP_RESPONSE = "extra_idp_response";

    private FirebaseAuth firebaseAuth;
    private DatabaseReference connectedReference;
    private ValueEventListener presenceValueListener;
    private ImageView profileIcon;

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

        Toolbar toolbar = findViewById(R.id.lubble_toolbar);
        setSupportActionBar(toolbar);
        profileIcon = toolbar.findViewById(R.id.iv_toolbar_profile);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setElevation(10);
        TextView toolbarTitle = findViewById(R.id.lubble_toolbar_title);
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

        setDp();
        GlideApp.with(this).load(currentUser.getPhotoUrl())
                .placeholder(R.drawable.ic_account_circle_black_no_padding)
                .circleCrop()
                .into(profileIcon);

        syncFcmToken();
        logUser(currentUser);

        switchFrag(GroupListFragment.newInstance());

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        addDebugActivOpener(toolbar);

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(receiver, new IntentFilter(LOGOUT_ACTION));
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

    private void setDp() {
        getUserInfoRef(firebaseAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ProfileInfo profileInfo = dataSnapshot.getValue(ProfileInfo.class);
                GlideApp.with(MainActivity.this)
                        .load(profileInfo == null ? "" : profileInfo.getThumbnail())
                        .circleCrop()
                        .placeholder(R.drawable.ic_account_circle_black_no_padding)
                        .error(R.drawable.ic_account_circle_black_no_padding)
                        .into(profileIcon);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        handlePresence();
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
        Crashlytics.setUserEmail(currentUser.getEmail());
        Crashlytics.setUserName(currentUser.getDisplayName());
    }

    private void syncFcmToken() {
        getThisUserRef().child("token")
                .setValue(FirebaseInstanceId.getInstance().getToken());
        //switchFrag(HomeFragment.newInstance());
    }

    @Override
    protected void onPause() {
        super.onPause();
        connectedReference.removeEventListener(presenceValueListener);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_chats:
                    switchFrag(GroupListFragment.newInstance());
                    return true;
                case R.id.navigation_notices:
                    switchFrag(AnnouncementsFrag.newInstance());
                    return true;
                case R.id.navigation_domestic_help:
                    switchFrag(DomesticDirectoryFrag.newInstance());
                    return true;
            }
            return false;
        }
    };

    private void switchFrag(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.content, fragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    private void openProfile() {
        ProfileActivity.open(this, FirebaseAuth.getInstance().getUid());
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }
}
