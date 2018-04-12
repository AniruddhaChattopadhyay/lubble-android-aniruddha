package in.lubble.app.auth;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.ProfileData;
import in.lubble.app.models.ProfileInfo;
import in.lubble.app.utils.FragUtils;

import static in.lubble.app.firebase.RealtimeDbHelper.getThisUserRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getUserLubbleRef;
import static in.lubble.app.utils.StringUtils.isValidString;
import static in.lubble.app.utils.UiUtils.hideKeyboard;

public class UserNameFrag extends Fragment {
    private static final String TAG = "UserNameFrag";
    private static final String ARG_IDP_RESPONSE = "arg_idp_response";

    private Parcelable idpResponse;

    private TextInputLayout firstNameTil;
    private TextInputLayout lastNameTil;
    private ProgressBar progressBar;
    private Spinner blockSpinner;
    private LinearLayout ownerContainer;
    private LinearLayout tenantContainer;
    private TextView ownerTv;
    private TextView tenantTv;
    private ImageView ownerIcon;
    private ImageView tenantIcon;
    private Button submitBtn;
    private boolean isOwner = true;
    private ArrayAdapter<CharSequence> adapter;

    public UserNameFrag() {
        // Required empty public constructor
    }

    public static UserNameFrag newInstance(Parcelable idpResponse) {
        UserNameFrag fragment = new UserNameFrag();
        Bundle args = new Bundle();
        args.putParcelable(ARG_IDP_RESPONSE, idpResponse);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            idpResponse = getArguments().getParcelable(ARG_IDP_RESPONSE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_user_name, container, false);

        firstNameTil = rootView.findViewById(R.id.til_fname);
        lastNameTil = rootView.findViewById(R.id.til_lname);
        progressBar = rootView.findViewById(R.id.progressBar);
        blockSpinner = rootView.findViewById(R.id.spinner_block);
        ownerContainer = rootView.findViewById(R.id.linearLayout_owner_container);
        tenantContainer = rootView.findViewById(R.id.linearLayout_tenant_container);
        ownerIcon = rootView.findViewById(R.id.ic_owner);
        ownerTv = rootView.findViewById(R.id.tv_owner);
        tenantIcon = rootView.findViewById(R.id.ic_tenant);
        tenantTv = rootView.findViewById(R.id.tv_tenant);
        submitBtn = rootView.findViewById(R.id.btn_submit);


        RealtimeDbHelper.getLubbleBlocksRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);
                blockSpinner.setVisibility(View.VISIBLE);

                adapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item);
                adapter.add("Select Block");
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    adapter.add(child.getKey());
                }
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                blockSpinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: " + databaseError.getMessage());
            }
        });

        ownerContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleTab(true);
            }
        });

        tenantContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleTab(false);
            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(getContext());
                if (isDataValid()) {
                    updateUserProfile();
                } else {
                    Snackbar.make(rootView, "Please enter all details", Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        return rootView;
    }

    private void toggleTab(boolean ownerTab) {
        if (ownerTab) {
            ownerContainer.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
            ownerIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);
            ownerTv.setTextColor(ContextCompat.getColor(getContext(), R.color.white));

            tenantContainer.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.very_light_gray));
            tenantIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.darker_gray), android.graphics.PorterDuff.Mode.SRC_IN);
            tenantTv.setTextColor(ContextCompat.getColor(getContext(), R.color.default_text_color));

            isOwner = true;
        } else {
            tenantContainer.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
            tenantIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);
            tenantTv.setTextColor(ContextCompat.getColor(getContext(), R.color.white));

            ownerContainer.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.very_light_gray));
            ownerIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.darker_gray), android.graphics.PorterDuff.Mode.SRC_IN);
            ownerTv.setTextColor(ContextCompat.getColor(getContext(), R.color.default_text_color));

            isOwner = false;
        }
    }

    private void updateUserProfile() {
        String firstName = firstNameTil.getEditText().getText().toString();
        String lastName = lastNameTil.getEditText().getText().toString();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        uploadNewUserData(user, firstName + " " + lastName);
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(firstName + " " + lastName)
                .build();

        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Updating");
        progressDialog.show();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated.");
                            openDpFrag();
                        } else {
                            Toast.makeText(getContext(), "Try Again", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void uploadNewUserData(FirebaseUser currentUser, String fullName) {
        ProfileData profileData = new ProfileData();
        final ProfileInfo profileInfo = new ProfileInfo();
        profileInfo.setId(currentUser.getUid());
        profileInfo.setName(fullName);
        profileData.setInfo(profileInfo);
        profileData.setLocality(getSelectedBlock());
        profileData.setIsOwner(isOwner);
        profileData.setBio("");
        profileData.setToken(FirebaseInstanceId.getInstance().getToken());
        profileData.setReferredBy(LubbleSharedPrefs.getInstance().getReferrerUid());

        getThisUserRef().setValue(profileData);
        getUserLubbleRef().setValue("true");
    }

    private String getSelectedBlock() {
        final int pos = blockSpinner.getSelectedItemPosition();
        final CharSequence blockName = adapter.getItem(pos);
        if (blockName != null) {
            return blockName.toString();
        } else {
            return "";
        }
    }

    private void openDpFrag() {
        ProfilePicFrag profilePicFrag = ProfilePicFrag.newInstance(idpResponse);
        FragUtils.addFrag(getFragmentManager(), R.id.frame_fragContainer, profilePicFrag);
    }

    private boolean isDataValid() {
        return isValidString(firstNameTil.getEditText().getText().toString())
                && isValidString(lastNameTil.getEditText().getText().toString())
                && blockSpinner.getSelectedItemPosition() > 0;
    }

}
