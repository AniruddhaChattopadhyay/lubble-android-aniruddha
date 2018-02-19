package in.lubble.app.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;

import java.util.ArrayList;
import java.util.List;

import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.ProfileInfo;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        Bundle params = new Bundle();
        params.putString(AuthUI.EXTRA_DEFAULT_COUNTRY_CODE, "in");
        List<AuthUI.IdpConfig> selectedProviders = new ArrayList<>();
        selectedProviders
                .add(new AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER)
                        .setParams(params)
                        .build());
        selectedProviders
                .add(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER)
                        .build());

        Intent intent = AuthUI.getInstance().createSignInIntentBuilder()
                .setLogo(R.drawable.ic_android_black_24dp)
                .setAvailableProviders(selectedProviders)
                .setIsSmartLockEnabled(true, true)
                .setAllowNewEmailAccounts(true)
                .build();
        getActivity().startActivityForResult(intent, RC_SIGN_IN);
    }

    private void trackReferral() {
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(referrerIntent)
                .addOnSuccessListener(getActivity(), new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                        }
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user == null && deepLink != null
                                && deepLink.getBooleanQueryParameter("invitedby", false)) {

                            String referrerUid = deepLink.getQueryParameter("invitedby");
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
                    }
                });
    }

}
