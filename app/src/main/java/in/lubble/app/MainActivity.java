package in.lubble.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.FirebaseDatabase;

import in.lubble.app.auth.LoginActivity;
import in.lubble.app.group.GroupActivity;
import in.lubble.app.models.ProfileData;

import static in.lubble.app.utils.StringUtils.isValidString;
import static in.lubble.app.utils.UserUtils.isNewUser;
import static in.lubble.app.utils.UserUtils.logout;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private TextView mTextMessage;
    private static final String EXTRA_IDP_RESPONSE = "extra_idp_response";
    private static final int REQUEST_CODE_DP = 379;

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

        Toolbar toolbar = findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null || !isValidString(currentUser.getDisplayName())) {
            // user is not signed in, start login flow
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        if (isNewUser(currentUser)) {
            uploadNewUserData(currentUser);
        } else if (!isValidString(UserSharedPrefs.getInstance().getAuthToken())) {
            // no token, fetch one and give it to server
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Signing in");
            currentUser.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                @Override
                public void onComplete(@NonNull Task<GetTokenResult> task) {
                    if (task.isSuccessful()) {
                        String idToken = task.getResult().getToken();
                        UserSharedPrefs.getInstance().setAuthToken(idToken);
                    } else {
                        //log the user out
                        logout(MainActivity.this);
                    }
                }
            });
        } else {
            authCompleted();
        }

        mTextMessage = findViewById(R.id.message);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private void uploadNewUserData(FirebaseUser currentUser) {
        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.setPersistenceEnabled(true);
        UserSharedPrefs.getInstance().setUserId(currentUser.getUid());
        ProfileData profileData = new ProfileData();
        profileData.setId(currentUser.getUid());
        profileData.setName(currentUser.getDisplayName());
        profileData.setLocality("C - Block");
        profileData.setBio("Android developer and tech enthusiast.\nFitness freak on weekdays,\nparty animal by the weekend");

        database.getReference("users").child(currentUser.getUid()).setValue(profileData);
    }

    private void authCompleted() {
        //todo switchFrag(HomeFragment.newInstance());
        saveUserProfile();
    }

    private void saveUserProfile() {
        if (UserSharedPrefs.getInstance().getUserId().equalsIgnoreCase(UserSharedPrefs.DEFAULT_USER_ID)) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            UserSharedPrefs.getInstance().setUserId(currentUser.getUid());
            ProfileData profileData = new ProfileData();
            profileData.setId(currentUser.getUid());
            profileData.setName(currentUser.getDisplayName());
            profileData.setLocality("C - Block");
            profileData.setBio("Android developer and tech enthusiast.\nFitness freak on weekdays,\nparty animal by the weekend");
            // todo DbSingleton.getInstance().createProfileData(profileData);
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    final Intent intent = new Intent(MainActivity.this, GroupActivity.class);
                    startActivity(intent);
                    return true;
            }
            return false;
        }
    };

}
