package in.lubble.app.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;

import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.models.ProfileData;
import in.lubble.app.models.ProfileInfo;

import static in.lubble.app.auth.LoginActivity.REQUEST_LOCATION;
import static in.lubble.app.firebase.RealtimeDbHelper.getThisUserRef;

public class NameFrag extends Fragment {

    private static final String TAG = "NameFrag";

    private TextInputLayout fullNameTil;
    private TextInputLayout referralCodeTil;
    private Button continueBtn;

    public NameFrag() {
        // Required empty public constructor
    }

    public static NameFrag newInstance(IdpResponse response) {
        NameFrag fragment = new NameFrag();
        Bundle args = new Bundle();
        args.putParcelable("idpResponse", response);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_name, container, false);

        fullNameTil = rootView.findViewById(R.id.til_full_name);
        referralCodeTil = rootView.findViewById(R.id.til_referral_code);
        continueBtn = rootView.findViewById(R.id.btn_continue);

        Analytics.triggerScreenEvent(getContext(), this.getClass());

        final Parcelable idpResponse = getArguments().getParcelable("idpResponse");

        if (TextUtils.isEmpty(LubbleSharedPrefs.getInstance().getReferrerUid())) {
            referralCodeTil.setVisibility(View.VISIBLE);
        } else {
            referralCodeTil.setVisibility(View.GONE);
        }

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(fullNameTil.getEditText().getText())) {
                    LubbleSharedPrefs.getInstance().setFullName(fullNameTil.getEditText().getText().toString().trim());
                    if (!TextUtils.isEmpty(referralCodeTil.getEditText().getText().toString())) {
                        LubbleSharedPrefs.getInstance().setReferralCode(referralCodeTil.getEditText().getText().toString().trim());
                    }

                    ProfileData profileData = new ProfileData();
                    final ProfileInfo profileInfo = new ProfileInfo();
                    profileInfo.setId(FirebaseAuth.getInstance().getUid());
                    profileInfo.setName(LubbleSharedPrefs.getInstance().getFullName());
                    profileData.setInfo(profileInfo);
                    profileData.setToken(FirebaseInstanceId.getInstance().getToken());
                    profileData.setReferredBy(LubbleSharedPrefs.getInstance().getReferrerUid());

                    getThisUserRef().setValue(profileData);

                    final Intent intent = new Intent(getContext(), LocationActivity.class);
                    intent.putExtra("idpResponse", idpResponse);
                    getActivity().startActivityForResult(intent, REQUEST_LOCATION);
                }
            }
        });
        return rootView;
    }


}
