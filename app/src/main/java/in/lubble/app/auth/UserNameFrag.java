package in.lubble.app.auth;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Parcelable;
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

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.utils.FragUtils;

import static in.lubble.app.firebase.RealtimeDbHelper.getUserLubbleRef;
import static in.lubble.app.utils.StringUtils.isValidString;
import static in.lubble.app.utils.UiUtils.hideKeyboard;

public class UserNameFrag extends Fragment {
    private static final String TAG = "UserNameFrag";
    private static final String ARG_IDP_RESPONSE = "arg_idp_response";
    private static final String ARG_CHOSEN_LOCATION_DATA = "ARG_CHOSEN_LOCATION_DATA";

    private Parcelable idpResponse;
    private LocationsData chosenLocationData;

    private TextInputLayout firstNameTil;
    private TextInputLayout lastNameTil;
    private TextInputLayout bioTil;
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

    public static UserNameFrag newInstance(Parcelable idpResponse, LocationsData chosenLocationData) {
        UserNameFrag fragment = new UserNameFrag();
        Bundle args = new Bundle();
        args.putParcelable(ARG_IDP_RESPONSE, idpResponse);
        args.putSerializable(ARG_CHOSEN_LOCATION_DATA, chosenLocationData);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            idpResponse = getArguments().getParcelable(ARG_IDP_RESPONSE);
            chosenLocationData = (LocationsData) getArguments().getSerializable(ARG_CHOSEN_LOCATION_DATA);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_user_name, container, false);

        firstNameTil = rootView.findViewById(R.id.til_fname);
        lastNameTil = rootView.findViewById(R.id.til_lname);
        bioTil = rootView.findViewById(R.id.til_bio);
        progressBar = rootView.findViewById(R.id.progressBar);
        blockSpinner = rootView.findViewById(R.id.spinner_block);
        ownerContainer = rootView.findViewById(R.id.linearLayout_owner_container);
        tenantContainer = rootView.findViewById(R.id.linearLayout_tenant_container);
        ownerIcon = rootView.findViewById(R.id.ic_owner);
        ownerTv = rootView.findViewById(R.id.tv_owner);
        tenantIcon = rootView.findViewById(R.id.ic_tenant);
        tenantTv = rootView.findViewById(R.id.tv_tenant);
        submitBtn = rootView.findViewById(R.id.btn_submit);

        Analytics.triggerScreenEvent(getContext(), this.getClass());

        RealtimeDbHelper.getLubbleBlocksRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);
                blockSpinner.setVisibility(View.VISIBLE);

                adapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item);
                adapter.add(getString(R.string.select_block));
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
                    Snackbar.make(rootView, getString(R.string.all_fill_details), Snackbar.LENGTH_SHORT).show();
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
        getUserLubbleRef().setValue("true");
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(firstName + " " + lastName)
                .build();

        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage(getString(R.string.all_updating));
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
                            Toast.makeText(getContext(), R.string.all_try_again, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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
