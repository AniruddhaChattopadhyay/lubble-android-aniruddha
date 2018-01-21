package in.lubble.app.auth;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import in.lubble.app.R;
import in.lubble.app.utils.FragUtils;

import static in.lubble.app.utils.StringUtils.isValidString;

public class UserNameFrag extends Fragment {
    private static final String TAG = "UserNameFrag";
    private static final String ARG_IDP_RESPONSE = "arg_idp_response";

    private Parcelable idpResponse;

    TextInputLayout firstNameTil;
    TextInputLayout lastNameTil;
    Button submitBtn;

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
        submitBtn = rootView.findViewById(R.id.btn_submit);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isDataValid()) {
                    updateUserProfile();
                } else {
                    Snackbar.make(rootView, "Please enter your name", Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        return rootView;
    }

    private void updateUserProfile() {
        String firstName = firstNameTil.getEditText().getText().toString();
        String lastName = lastNameTil.getEditText().getText().toString();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
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

    private void openDpFrag() {
        ProfilePicFrag profilePicFrag = ProfilePicFrag.newInstance(idpResponse);
        FragUtils.addFrag(getFragmentManager(), R.id.frame_fragContainer, profilePicFrag);
    }

    private boolean isDataValid() {
        return isValidString(firstNameTil.getEditText().getText().toString())
                && isValidString(lastNameTil.getEditText().getText().toString());
    }

}
