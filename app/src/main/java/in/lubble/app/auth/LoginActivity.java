package in.lubble.app.auth;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import com.crashlytics.android.Crashlytics;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import in.lubble.app.BaseActivity;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.MainActivity;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.ProfileData;
import in.lubble.app.models.ProfileInfo;

import java.util.ArrayList;
import java.util.HashMap;

import static in.lubble.app.firebase.RealtimeDbHelper.getThisUserRef;
import static in.lubble.app.utils.FragUtils.*;

public class LoginActivity extends BaseActivity {

    public static final int RC_SIGN_IN = 777;
    static final int REQUEST_LOCATION = 636;

    private FirebaseAuth firebaseAuth;
    private View rootLayout;
    private ProgressDialog progressDialog;

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
                if (response.isNewUser()) {
                    // start registration flow
                    if (currentUser.getProviders().get(0).equals(FacebookAuthProvider.PROVIDER_ID)) {
                        registerFacebookUser(response, currentUser);
                    } else if (currentUser.getProviders().get(0).equals(PhoneAuthProvider.PROVIDER_ID)) {
                        NameFrag nameFrag = NameFrag.newInstance(response);
                        addFrag(getSupportFragmentManager(), R.id.frame_fragContainer, nameFrag);
                    }
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

    private void registerFacebookUser(final IdpResponse response, FirebaseUser currentUser) {

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.all_please_wait));
        progressDialog.setMessage(getString(R.string.all_updating));
        progressDialog.setCancelable(false);
        progressDialog.show();

        LubbleSharedPrefs.getInstance().setFullName(currentUser.getDisplayName());

        final ProfileData profileData = new ProfileData();
        final ProfileInfo profileInfo = new ProfileInfo();
        profileInfo.setId(FirebaseAuth.getInstance().getUid());
        profileInfo.setName(currentUser.getDisplayName());
        if (currentUser.getPhotoUrl() != null) {
            profileInfo.setThumbnail(currentUser.getPhotoUrl().toString());
            profileData.setProfilePic(currentUser.getPhotoUrl().toString().concat("?type=large"));
        }
        profileData.setInfo(profileInfo);
        profileData.setToken(FirebaseInstanceId.getInstance().getToken());
        profileData.setReferredBy(LubbleSharedPrefs.getInstance().getReferrerUid());

        getThisUserRef().setValue(profileData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (!isFinishing()) {
                            progressDialog.dismiss();
                            final Intent intent = new Intent(LoginActivity.this, LocationActivity.class);
                            intent.putExtra("idpResponse", response);
                            startActivityForResult(intent, REQUEST_LOCATION);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Write failed
                        if (!isFinishing()) {
                            progressDialog.dismiss();
                            Crashlytics.log("OMG Failed to write FB profile info");
                            Crashlytics.logException(e);
                            if (!isFinishing()) {
                                Toast.makeText(LoginActivity.this, R.string.all_try_again, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void showSnackbar(@StringRes int errorMessageRes) {
        Snackbar.make(rootLayout, errorMessageRes, Snackbar.LENGTH_SHORT).show();
    }

}
