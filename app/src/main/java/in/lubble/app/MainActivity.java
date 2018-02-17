package in.lubble.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import in.lubble.app.announcements.announcementHistory.AnnouncementsActivity;
import in.lubble.app.auth.LoginActivity;
import in.lubble.app.groups.GroupListFragment;
import in.lubble.app.models.ProfileData;
import in.lubble.app.models.ProfileInfo;
import in.lubble.app.profile.ProfileActivity;

import static in.lubble.app.firebase.RealtimeDbHelper.getThisUserRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getUserLubbleRef;
import static in.lubble.app.utils.UserUtils.isNewUser;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String EXTRA_IDP_RESPONSE = "extra_idp_response";

    private FirebaseAuth firebaseAuth;

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
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView toolbarTitle = findViewById(R.id.lubble_toolbar_title);
        toolbarTitle.setVisibility(View.VISIBLE);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            // user is not signed in, start login flow
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        if (isNewUser(currentUser)) {
            uploadNewUserData(currentUser);
        } else {
            syncFcmToken();
        }
        logUser(currentUser);

        switchFrag(GroupListFragment.newInstance());

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private void logUser(FirebaseUser currentUser) {
        Crashlytics.setUserIdentifier(currentUser.getUid());
        Crashlytics.setUserEmail(currentUser.getEmail());
        Crashlytics.setUserName(currentUser.getDisplayName());
    }


    private void uploadNewUserData(FirebaseUser currentUser) {
        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        ProfileData profileData = new ProfileData();
        profileData.setId(currentUser.getUid());
        final ProfileInfo profileInfo = new ProfileInfo();
        profileInfo.setName(currentUser.getDisplayName());
        profileData.setInfo(profileInfo);
        profileData.setLocality("C - Block");
        profileData.setBio("Android developer and tech enthusiast.\nFitness freak on weekdays,\nparty animal by the weekend");
        profileData.setToken(FirebaseInstanceId.getInstance().getToken());

        getThisUserRef().setValue(profileData);
        getUserLubbleRef().setValue("true");
    }

    private void syncFcmToken() {
        getThisUserRef().child("token")
                .setValue(FirebaseInstanceId.getInstance().getToken());
        //switchFrag(HomeFragment.newInstance());
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    return true;
                case R.id.navigation_dashboard:
                    return true;
                case R.id.navigation_notifications:
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

    public void openProfile(MenuItem item) {
        ProfileActivity.open(this, FirebaseAuth.getInstance().getUid());
    }

    public void openAnnouncements(MenuItem item) {
        AnnouncementsActivity.newInstance(this);
    }

}
