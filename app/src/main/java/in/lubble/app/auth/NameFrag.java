package in.lubble.app.auth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.crashlytics.android.Crashlytics;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
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
    private ProgressDialog progressDialog;

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

        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setTitle(getString(R.string.all_please_wait));
        progressDialog.setMessage(getString(R.string.all_updating));
        progressDialog.setCancelable(false);

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(fullNameTil.getEditText().getText())) {
                    progressDialog.show();
                    LubbleSharedPrefs.getInstance().setFullName(fullNameTil.getEditText().getText().toString().trim());
                    if (!TextUtils.isEmpty(referralCodeTil.getEditText().getText().toString())) {
                        LubbleSharedPrefs.getInstance().setReferralCode(referralCodeTil.getEditText().getText().toString().trim());
                    }

                    final ProfileData profileData = new ProfileData();
                    final ProfileInfo profileInfo = new ProfileInfo();
                    profileInfo.setId(FirebaseAuth.getInstance().getUid());
                    profileInfo.setName(LubbleSharedPrefs.getInstance().getFullName());
                    profileData.setInfo(profileInfo);
                    profileData.setToken(FirebaseInstanceId.getInstance().getToken());
                    profileData.setReferredBy(LubbleSharedPrefs.getInstance().getReferrerUid());

                    getThisUserRef().setValue(profileData)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    if (isAdded() && isVisible()) {
                                        progressDialog.dismiss();
                                        final Intent intent = new Intent(getContext(), LocationActivity.class);
                                        intent.putExtra("idpResponse", idpResponse);
                                        getActivity().startActivityForResult(intent, REQUEST_LOCATION);
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Write failed
                                    if (isAdded() && isVisible()) {
                                        progressDialog.dismiss();
                                        Crashlytics.log("OMG Failed to write profile info inside NameFrag");
                                        Crashlytics.logException(e);
                                        if (isAdded() && isVisible()) {
                                            Toast.makeText(requireContext(), R.string.all_try_again, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            });

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (isAdded() && isVisible() && getContext() != null) {
                                if (progressDialog != null) {
                                    progressDialog.dismiss();
                                }
                                Toast.makeText(requireContext(), R.string.check_internet, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, 7000);
                }
            }
        });
        return rootView;
    }


}
