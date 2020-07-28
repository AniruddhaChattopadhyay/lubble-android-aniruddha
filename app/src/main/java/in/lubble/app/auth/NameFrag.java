package in.lubble.app.auth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.UserProfileChangeRequest;

import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.models.ProfileData;
import in.lubble.app.models.ProfileInfo;
import in.lubble.app.utils.StringUtils;

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

    public static NameFrag newInstance() {
        NameFrag fragment = new NameFrag();
        Bundle args = new Bundle();
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
                final String fullNameStr = StringUtils.getTitleCase(fullNameTil.getEditText().getText().toString());
                if (!TextUtils.isEmpty(fullNameStr)) {
                    progressDialog.show();
                    if (!TextUtils.isEmpty(referralCodeTil.getEditText().getText().toString())) {
                        LubbleSharedPrefs.getInstance().setReferralCode(referralCodeTil.getEditText().getText().toString().trim());
                    }

                    final UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(fullNameStr)
                            .build();

                    final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        currentUser.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    final ProfileData profileData = new ProfileData();
                                    final ProfileInfo profileInfo = new ProfileInfo();
                                    profileInfo.setId(FirebaseAuth.getInstance().getUid());
                                    profileInfo.setName(fullNameStr);
                                    profileData.setInfo(profileInfo);

                                    currentUser.getIdToken(false).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<GetTokenResult> task) {
                                            if (task.isSuccessful()) {
                                                profileData.setToken(task.getResult().getToken());
                                                profileData.setReferredBy(LubbleSharedPrefs.getInstance().getReferrerUid());

                                                getThisUserRef().setValue(profileData)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                if (isAdded() && isVisible()) {
                                                                    progressDialog.dismiss();
                                                                    final Intent intent = new Intent(getContext(), LocationActivity.class);
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
                                            } else {
                                                if (progressDialog != null) {
                                                    progressDialog.dismiss();
                                                }
                                                Toast.makeText(requireContext(), R.string.all_try_again, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                } else {
                                    if (progressDialog != null) {
                                        progressDialog.dismiss();
                                    }
                                    Toast.makeText(requireContext(), R.string.all_try_again, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                        }
                        Toast.makeText(requireContext(), R.string.all_try_again, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        return rootView;
    }


}
