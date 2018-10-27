package in.lubble.app.auth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.StringRes;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.MainActivity;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;

import static in.lubble.app.utils.FragUtils.addFrag;
import static in.lubble.app.utils.FragUtils.replaceFrag;
import static in.lubble.app.utils.FragUtils.replaceStack;
import static in.lubble.app.utils.UserUtils.isNewUser;

public class LoginActivity extends AppCompatActivity {

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
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == RESULT_OK) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null && isNewUser(currentUser)) {
                    // start registration flow
                    NameFrag nameFrag = NameFrag.newInstance(response);
                    addFrag(getSupportFragmentManager(), R.id.frame_fragContainer, nameFrag);
                } else {
                    Analytics.triggerLoginEvent(this);
                    startActivity(MainActivity.createIntent(LoginActivity.this, response));
                    finish();
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
