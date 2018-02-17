package in.lubble.app.auth;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import in.lubble.app.MainActivity;
import in.lubble.app.R;

import static in.lubble.app.utils.FragUtils.addFrag;
import static in.lubble.app.utils.UserUtils.isNewUser;

public class LoginActivity extends AppCompatActivity {

    public static final int RC_SIGN_IN = 777;

    private FirebaseAuth firebaseAuth;
    private View rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        rootLayout = findViewById(R.id.root_layout);
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        addFrag(getSupportFragmentManager(), R.id.frame_fragContainer, WelcomeFrag.newInstance(getIntent()));
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
                    UserNameFrag userNameFrag = UserNameFrag.newInstance(response);
                    addFrag(getSupportFragmentManager(), R.id.frame_fragContainer, userNameFrag);
                } else {
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

                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showSnackbar(R.string.no_internet_connection);
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    showSnackbar(R.string.unknown_error);
                    return;
                }
            }
            showSnackbar(R.string.unknown_sign_in_response);
        }
    }

    private void showSnackbar(@StringRes int errorMessageRes) {
        Snackbar.make(rootLayout, errorMessageRes, Snackbar.LENGTH_SHORT).show();
    }

}
