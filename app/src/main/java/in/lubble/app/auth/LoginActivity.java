package in.lubble.app.auth;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.StringRes;

import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import in.lubble.app.BaseActivity;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.MainActivity;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.ProfileData;
import in.lubble.app.models.ProfileInfo;
import in.lubble.app.utils.StringUtils;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;

import static in.lubble.app.firebase.RealtimeDbHelper.getThisUserRef;
import static in.lubble.app.utils.FragUtils.addFrag;
import static in.lubble.app.utils.FragUtils.replaceStack;
import static in.lubble.app.utils.UserUtils.isNewUser;

public class LoginActivity extends BaseActivity {

    public static final int RC_SIGN_IN = 777;
    static final int REQUEST_LOCATION = 636;

    private FirebaseAuth firebaseAuth;
    private View rootLayout;
    private ProgressDialog progressDialog;
    private String link = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        rootLayout = findViewById(R.id.root_layout);
        link = getIntent().getStringExtra("Link");
        firebaseAuth = FirebaseAuth.getInstance();
        Analytics.triggerScreenEvent(this, this.getClass());

        LubbleSharedPrefs.getInstance().setIsLogoutPending(false);
        LubbleSharedPrefs.getInstance().setLubbleId("");
        if (firebaseAuth.getCurrentUser() != null) {
            startSignInFlow(isNewUser(firebaseAuth.getCurrentUser()));
        } else {
            startAuthActivity();
            overridePendingTransition(0, 0);
            //if (!LubbleSharedPrefs.getInstance().getIsAppIntroShown()) {
            //    startActivity(new Intent(this, IntroActivity.class));
            //}
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        trackReferral();
    }

    private void startAuthActivity() {

        AuthUI.IdpConfig facebookIdp = new AuthUI.IdpConfig.FacebookBuilder()
                .build();

        AuthUI.IdpConfig googleIdp = new AuthUI.IdpConfig.GoogleBuilder()
                .build();

        final AuthUI.IdpConfig emailIdp = new AuthUI.IdpConfig.EmailBuilder().build();

        List<String> whitelistedCountries = new ArrayList<String>();
        whitelistedCountries.add("IN");
        List<AuthUI.IdpConfig> selectedProviders = new ArrayList<>();
        AuthUI.IdpConfig idpConfig;
        try {
            idpConfig = new AuthUI.IdpConfig.PhoneBuilder()
                    .setDefaultCountryIso("IN")
                    .setAllowedCountries(whitelistedCountries)
                    .build();
        } catch (IllegalStateException ex) {
            FirebaseCrashlytics.getInstance().recordException(ex);
            idpConfig = new AuthUI.IdpConfig.PhoneBuilder()
                    .setAllowedCountries(whitelistedCountries)
                    .build();
        }
        selectedProviders.add(idpConfig);
        selectedProviders.add(facebookIdp);
        selectedProviders.add(googleIdp);
        selectedProviders.add(emailIdp);

        AuthMethodPickerLayout customLayout = new AuthMethodPickerLayout
                .Builder(R.layout.custom_login_layout)
                .setEmailButtonId(R.id.tv_sign_in_email)
                .setFacebookButtonId(R.id.btn_sign_in_fb)
                .setGoogleButtonId(R.id.btn_sign_in_google)
                .setPhoneButtonId(R.id.tv_sign_in_phone)
                .setTosAndPrivacyPolicyId(R.id.tv_tos)
                .build();

        Intent intent = AuthUI.getInstance().createSignInIntentBuilder()
                .setTheme(R.style.AppTheme)
                .setAvailableProviders(selectedProviders)
                .setAuthMethodPickerLayout(customLayout)
                .setTosAndPrivacyPolicyUrls("https://lubble.in/policies/terms", "https://lubble.in/policies/privacy")
                .setIsSmartLockEnabled(false, false)
                .build();


        if (link != null) {
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setTheme(R.style.AppTheme)
                            .setEmailLink(link)
                            .setAvailableProviders(selectedProviders)
                            .build(),
                    RC_SIGN_IN);
        } else {
            startActivityForResult(intent, RC_SIGN_IN);
        }

    }

    private void trackReferral() {
        Branch.getInstance().initSession(new Branch.BranchReferralInitListener() {
            @Override
            public void onInitFinished(JSONObject referringParams, BranchError error) {
                if (error == null) {
                    Log.i("BRANCH SDK", referringParams.toString());
                    final String referrerUid = referringParams.optString("referrer_uid");
                    final String groupId = referringParams.optString("group_id");
                    if (!TextUtils.isEmpty(referrerUid)) {
                        LubbleSharedPrefs.getInstance().setReferrerUid(referrerUid);
                    }
                    if (!TextUtils.isEmpty(groupId)) {
                        LubbleSharedPrefs.getInstance().setInvitedGroupId(groupId);
                    }
                } else {
                    Log.e("BRANCH SDK", error.getMessage());
                }
            }
        }, getIntent().getData(), this);
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
                    for (UserInfo user : currentUser.getProviderData()) {
                        if (user.getProviderId().equals(FacebookAuthProvider.PROVIDER_ID)) {
                            registerSocialUser(response, currentUser, "?width=300&height=300");
                            break;
                        } else if (user.getProviderId().equals(GoogleAuthProvider.PROVIDER_ID)) {
                            registerSocialUser(response, currentUser, "?sz=300");
                            break;
                        } else if (user.getProviderId().equals(PhoneAuthProvider.PROVIDER_ID)) {
                            NameFrag nameFrag = NameFrag.newInstance();
                            addFrag(getSupportFragmentManager(), R.id.frame_fragContainer, nameFrag);
                            break;
                        } else if (user.getProviderId().equals(EmailAuthProvider.PROVIDER_ID)) {
                            registerSocialUser(response, currentUser, "");
                            break;
                        }
                    }
                } else {
                    // start signin flow: fetch lubble ID
                    startSignInFlow(response.isNewUser());
                }
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    finish();
                    return;
                }
                FirebaseCrashlytics.getInstance().recordException(response.getError());

                if (response.getError() != null && response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, R.string.check_internet, Toast.LENGTH_SHORT).show();
                    startAuthActivity();
                    return;
                }
                Toast.makeText(this, R.string.all_try_again, Toast.LENGTH_SHORT).show();
                startAuthActivity();
            }
        } else if (requestCode == REQUEST_LOCATION) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                LubbleChooserFrag lubbleChooserFrag = LubbleChooserFrag.newInstance((ArrayList<LocationsData>) data.getSerializableExtra("lubbleDataList"));
                replaceStack(LoginActivity.this, lubbleChooserFrag, R.id.frame_fragContainer);
                /*UserNameFrag userNameFrag = UserNameFrag.newInstance(data.getParcelableExtra("idpResponse"));
                addFrag(getSupportFragmentManager(), R.id.frame_fragContainer, userNameFrag);*/
            } else {
                finish();
            }
        } else {
            finish();
        }
    }

    private void startSignInFlow(final boolean isNewUser) {
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
                    startActivity(MainActivity.createIntent(LoginActivity.this, isNewUser));
                    finish();
                } else {
                    if (!TextUtils.isEmpty(FirebaseAuth.getInstance().getCurrentUser().getDisplayName())) {
                        final Intent intent = new Intent(LoginActivity.this, LocationActivity.class);
                        startActivityForResult(intent, REQUEST_LOCATION);
                    } else {
                        NameFrag nameFrag = NameFrag.newInstance();
                        addFrag(getSupportFragmentManager(), R.id.frame_fragContainer, nameFrag);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, R.string.all_try_again, Toast.LENGTH_SHORT).show();
                FirebaseCrashlytics.getInstance().recordException(new IllegalAccessException(databaseError.getCode() + " " + databaseError.getMessage()));
            }
        });
    }

    private void registerSocialUser(final IdpResponse response, final FirebaseUser currentUser, String bigPicString) {

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.all_please_wait));
        progressDialog.setMessage(getString(R.string.all_updating));
        progressDialog.setCancelable(false);
        progressDialog.show();

        final ProfileData profileData = new ProfileData();
        final ProfileInfo profileInfo = new ProfileInfo();
        profileInfo.setId(FirebaseAuth.getInstance().getUid());
        profileInfo.setName(StringUtils.getTitleCase(currentUser.getDisplayName()));
        if (currentUser.getPhotoUrl() != null) {
            String smallPicUrl = currentUser.getPhotoUrl().toString();
            profileInfo.setThumbnail(smallPicUrl);
            if (smallPicUrl.contains("s96-c")) {
                profileData.setProfilePic(smallPicUrl.replace("s96-c", "s300-c"));
            } else {
                profileData.setProfilePic(smallPicUrl.concat(bigPicString));
            }
        }
        profileData.setInfo(profileInfo);
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                profileData.setToken(task.getResult());
                profileData.setTokenTimestamp(System.currentTimeMillis());
                profileData.setReferredBy(LubbleSharedPrefs.getInstance().getReferrerUid());

                getThisUserRef().setValue(profileData)
                        .addOnSuccessListener(aVoid -> {
                            if (!isFinishing()) {
                                progressDialog.dismiss();
                                final Intent intent = new Intent(LoginActivity.this, LocationActivity.class);
                                intent.putExtra("idpResponse", response);
                                startActivityForResult(intent, REQUEST_LOCATION);
                            }
                        })
                        .addOnFailureListener(e -> {
                            // Write failed
                            if (!isFinishing()) {
                                progressDialog.dismiss();
                                FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
                                crashlytics.log("OMG Failed to write FB profile info");
                                crashlytics.recordException(e);
                                if (!isFinishing()) {
                                    Toast.makeText(LoginActivity.this, R.string.all_try_again, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            } else {
                progressDialog.dismiss();
                if (!isFinishing()) {
                    Toast.makeText(LoginActivity.this, R.string.all_try_again, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showSnackbar(@StringRes int errorMessageRes) {
        Snackbar.make(rootLayout, errorMessageRes, Snackbar.LENGTH_SHORT).show();
    }

}
