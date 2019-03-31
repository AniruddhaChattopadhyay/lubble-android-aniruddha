package in.lubble.app.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.ProfileInfo;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static in.lubble.app.auth.LoginActivity.RC_SIGN_IN;

public class WelcomeFrag extends Fragment {

    private static final String ARG_REFERRER_INTENT = "ARG_REFERRER_INTENT";

    private Intent referrerIntent = null;
    private LinearLayout referrerContainer;
    private TextView referrerHintTv;
    private TextView referrerNameTv;
    private ImageView referrerDpIv;

    public WelcomeFrag() {
        // Required empty public constructor
    }

    public static WelcomeFrag newInstance(@NonNull Intent intent) {
        final WelcomeFrag welcomeFrag = new WelcomeFrag();
        final Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_REFERRER_INTENT, intent);
        welcomeFrag.setArguments(bundle);
        return welcomeFrag;
    }

    @Override
    public void onStart() {
        super.onStart();
        referrerIntent = getArguments().getParcelable(ARG_REFERRER_INTENT);
        if (referrerIntent != null) {
            trackReferral();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_welcome, container, false);

        referrerContainer = rootView.findViewById(R.id.referrer_container);
        referrerHintTv = rootView.findViewById(R.id.tv_referrer_hint);
        referrerNameTv = rootView.findViewById(R.id.tv_referrer_name);
        referrerDpIv = rootView.findViewById(R.id.iv_referrer_dp);

        Analytics.triggerScreenEvent(getContext(), this.getClass());
        rootView.findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAuthActivity();
            }
        });

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startAuthActivity() {

        AuthUI.IdpConfig facebookIdp = new AuthUI.IdpConfig.FacebookBuilder()
                .build();

        List<String> whitelistedCountries = new ArrayList<String>();
        whitelistedCountries.add("in");
        List<AuthUI.IdpConfig> selectedProviders = new ArrayList<>();
        selectedProviders
                .add(new AuthUI.IdpConfig.PhoneBuilder()
                        .setDefaultCountryIso("in")
                        .setWhitelistedCountries(whitelistedCountries)
                        .build());
        selectedProviders.add(facebookIdp);

        AuthMethodPickerLayout customLayout = new AuthMethodPickerLayout
                .Builder(R.layout.custom_login_layout)
                .setFacebookButtonId(R.id.btn_sign_in_fb)
                .setPhoneButtonId(R.id.tv_sign_in_phone)
                .setTosAndPrivacyPolicyId(R.id.tv_tos)
                .build();

        Intent intent = AuthUI.getInstance().createSignInIntentBuilder()
                .setLogo(R.drawable.ic_android_black_24dp)
                .setAvailableProviders(selectedProviders)
                .setAuthMethodPickerLayout(customLayout)
                .setTheme(R.style.AppTheme)
                .setTosAndPrivacyPolicyUrls("https://lubble.in/policies/terms", "https://lubble.in/policies/privacy")
                .setIsSmartLockEnabled(false, false)
                .build();
        getActivity().startActivityForResult(intent, RC_SIGN_IN);
    }

    private void trackReferral() {
        Branch.getInstance().initSession(new Branch.BranchReferralInitListener() {
            @Override
            public void onInitFinished(JSONObject referringParams, BranchError error) {
                if (error == null) {
                    Log.i("BRANCH SDK", referringParams.toString());
                    final String referrerUid = referringParams.optString("referrer_uid");
                    if (!TextUtils.isEmpty(referrerUid)) {
                        LubbleSharedPrefs.getInstance().setReferrerUid(referrerUid);
                        // single listener as the user is new, has no cache.
                        // referrer profile will be fetched via network, there wudnt be any cache hits.
                        // Even if its cached, dsnt matter really, just shows who referred you, an outdated dp wont do much harm..
                        RealtimeDbHelper.getUserInfoRef(referrerUid).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                final ProfileInfo profileInfo = dataSnapshot.getValue(ProfileInfo.class);
                                if (profileInfo != null) {

                                    referrerHintTv.setVisibility(View.VISIBLE);
                                    referrerContainer.setVisibility(View.VISIBLE);
                                    referrerNameTv.setText(profileInfo.getName());
                                    GlideApp.with(getContext())
                                            .load(profileInfo.getThumbnail())
                                            .placeholder(R.drawable.ic_account_circle_black_no_padding)
                                            .circleCrop()
                                            .into(referrerDpIv);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                    }
                } else {
                    Log.e("BRANCH SDK", error.getMessage());
                }
            }
        }, getActivity().getIntent().getData(), getActivity());
    }

}
