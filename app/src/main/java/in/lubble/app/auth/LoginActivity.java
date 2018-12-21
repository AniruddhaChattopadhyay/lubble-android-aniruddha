package in.lubble.app.auth;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.StringRes;
import com.crashlytics.android.Crashlytics;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import in.lubble.app.BaseActivity;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.MainActivity;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.firebase.RealtimeDbHelper;

import java.util.ArrayList;
import java.util.HashMap;

import static in.lubble.app.utils.FragUtils.*;
import static in.lubble.app.utils.UserUtils.isNewUser;

public class LoginActivity extends BaseActivity {

    public static final int RC_SIGN_IN = 777;
    static final int REQUEST_LOCATION = 636;

    private FirebaseAuth firebaseAuth;
    private View rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        rootLayout = findViewById(R.id.root_layout);
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (!LubbleSharedPrefs.getInstance().getIsAppIntroShown()) {
            startActivity(new Intent(this, IntroActivity.class));
        }

        replaceFrag(getSupportFragmentManager(), WelcomeFrag.newInstance(getIntent()), R.id.frame_fragContainer);
        LubbleSharedPrefs.getInstance().setIsLogoutPending(false);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            final IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == RESULT_OK) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null && isNewUser(currentUser)) {
                    // start registration flow
                    NameFrag nameFrag = NameFrag.newInstance(response);
                    addFrag(getSupportFragmentManager(), R.id.frame_fragContainer, nameFrag);
                } else {
                    // start signin flow: fetch lubble ID
                    final ProgressDialog progressDialog = new ProgressDialog(this);
                    progressDialog.setTitle(getString(R.string.signing_in));
                    progressDialog.setMessage(getString(R.string.all_please_wait));
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    RealtimeDbHelper.getThisUserRef().child("lubbles").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            HashMap<String, String> map = (HashMap<String, String>) dataSnapshot.getValue();
                            progressDialog.dismiss();
                            if (map != null && !map.isEmpty()) {
                                // fetch & set lubble ID before login
                                LubbleSharedPrefs.getInstance().setLubbleId((String) map.keySet().toArray()[0]);
                                Analytics.triggerLoginEvent(LoginActivity.this);
                                startActivity(MainActivity.createIntent(LoginActivity.this, response));
                                finish();
                            } else {
                                Crashlytics.logException(new IllegalAccessException("User has NO lubble ID"));
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, R.string.all_try_again, Toast.LENGTH_SHORT).show();
                            Crashlytics.logException(new IllegalAccessException(databaseError.getCode() + " " + databaseError.getMessage()));
                        }
                    });
                }
                return;
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    showSnackbar(R.string.sign_in_cancelled);
                    return;
                }

                if (response.getError() != null && response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showSnackbar(R.string.no_internet_connection);
                    return;
                }

                showSnackbar(R.string.unknown_error);
            }
        } else if (requestCode == REQUEST_LOCATION) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                LubbleChooserFrag userNameFrag = LubbleChooserFrag.newInstance(
                        data.getParcelableExtra("idpResponse"),
                        (ArrayList<LocationsData>) data.getSerializableExtra("lubbleDataList")
                );
                addFrag(getSupportFragmentManager(), R.id.frame_fragContainer, userNameFrag);
                /*UserNameFrag userNameFrag = UserNameFrag.newInstance(data.getParcelableExtra("idpResponse"));
                addFrag(getSupportFragmentManager(), R.id.frame_fragContainer, userNameFrag);*/
            } else {
                replaceStack(this, WelcomeFrag.newInstance(getIntent()), R.id.frame_fragContainer);
            }
        }
    }

    private void showSnackbar(@StringRes int errorMessageRes) {
        Snackbar.make(rootLayout, errorMessageRes, Snackbar.LENGTH_SHORT).show();
    }

}
