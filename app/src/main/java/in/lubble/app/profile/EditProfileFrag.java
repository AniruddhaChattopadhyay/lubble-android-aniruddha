package in.lubble.app.profile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.signature.ObjectKey;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;

import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.models.ProfileData;

import static android.app.Activity.RESULT_OK;
import static in.lubble.app.utils.FileUtils.createImageFile;
import static in.lubble.app.utils.FileUtils.getFileFromInputStreamUri;
import static in.lubble.app.utils.FileUtils.getPickImageIntent;

public class EditProfileFrag extends Fragment {

    private static final String TAG = "EditProfileFrag";
    private static final int REQUEST_CODE_COVER_PIC = 560;
    private static final int REQUEST_CODE_DP = 211;

    private ImageView coverPicIv;
    private ImageView profilePicIv;
    private TextInputLayout fullNameTil;
    private TextInputLayout localityTil;
    private TextInputLayout bioTil;
    private ProfileData profileData;
    private View rootView;
    private String currentPhotoPath;
    private StorageReference storageRef;

    public EditProfileFrag() {
        // Required empty public constructor
    }

    public static EditProfileFrag newInstance() {
        return new EditProfileFrag();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        storageRef = FirebaseStorage.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        coverPicIv = rootView.findViewById(R.id.iv_cover);
        profilePicIv = rootView.findViewById(R.id.iv_profilePic);
        fullNameTil = rootView.findViewById(R.id.til_fullName);
        localityTil = rootView.findViewById(R.id.til_locality);
        bioTil = rootView.findViewById(R.id.til_bio);

        rootView.findViewById(R.id.linearLayout_cover_edit_container).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.iv_dp_edit_overlay).setVisibility(View.VISIBLE);

        //todo profileData = DbSingleton.getInstance().readProfileData(UserSharedPrefs.getInstance().getUserId());

        /*fullNameTil.getEditText().setText(profileData.getName());
        localityTil.getEditText().setText(profileData.getLocality());
        bioTil.getEditText().setText(profileData.getBio());*/

        coverPicIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPhotoPicker(REQUEST_CODE_COVER_PIC);
            }
        });
        profilePicIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPhotoPicker(REQUEST_CODE_DP);
            }
        });

        /*GlideApp.with(getContext()).load(BASE_MEDIA_URL + profileData.getCoverPic())
                .placeholder(R.drawable.cover_pic)
                .into(coverPicIv);
        GlideApp.with(getContext()).load(BASE_MEDIA_URL + profileData.getDp())
                .placeholder(R.drawable.ic_account_circle_black_no_padding)
                .into(profilePicIv);*/

        return rootView;
    }

    private void startPhotoPicker(int REQUEST_CODE) {
        try {
            File cameraPic = createImageFile(getContext());
            currentPhotoPath = cameraPic.getAbsolutePath();
            Intent pickImageIntent = getPickImageIntent(getContext(), cameraPic);
            startActivityForResult(pickImageIntent, REQUEST_CODE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == REQUEST_CODE_DP || requestCode == REQUEST_CODE_COVER_PIC) && resultCode == RESULT_OK) {
            File imageFile;
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                imageFile = getFileFromInputStreamUri(getContext(), uri);
            } else {
                // from camera
                imageFile = new File(currentPhotoPath);
            }
            GlideApp.with(getContext())
                    .load(imageFile)
                    .centerCrop()
                    .circleCrop()
                    .signature(new ObjectKey(imageFile.length() + "@" + imageFile.lastModified()))
                    .into(requestCode == REQUEST_CODE_DP ? profilePicIv : coverPicIv);


            Uri file = Uri.fromFile(imageFile);
            StorageReference riversRef = storageRef
                    .child("images/users/" + FirebaseAuth.getInstance().getUid() + "/dp.jpg");

            riversRef.putFile(file)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get a URL to the uploaded content
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            // ...
                        }
                    });

            //uploadProfileOrCoverPic(imageFile, requestCode);
            //dispatchNewPicJob(imageFile, requestCode);
        }
    }
/*
    private boolean isDataValid() {
        if (!isValidString(getStringFromTil(fullNameTil))) {
            fullNameTil.setError("Name can't be blank");
            return false;
        } else {
            fullNameTil.setError(null);
        }
        if (!isValidString(getStringFromTil(localityTil))) {
            localityTil.setError("Locality can't be blank");
            return false;
        } else {
            localityTil.setError(null);
        }
        if (!isValidString(getStringFromTil(bioTil))) {
            bioTil.setError("Bio can't be blank");
            return false;
        } else {
            bioTil.setError(null);
        }
        return true;
    }*/

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        getActivity().getMenuInflater().inflate(R.menu.edit_profile_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            /*case R.id.action_save_profile: {
                if (isDataValid()) {
                    //todo add dp n cover pic
                    profileData.setName(getStringFromTil(fullNameTil));
                    profileData.setLocality(getStringFromTil(localityTil));
                    profileData.setBio(getStringFromTil(bioTil));
                    uploadProfileEdit(profileData);
                }
                break;
            }*/
        }
        return false;
    }

    private void uploadProfileEdit(ProfileData profileData) {

        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Updating Profile...");
        progressDialog.show();

        /*NetworkClient apiClient = createService();
        HashMap<String, Object> params = new HashMap<>();
        params.put("name", profileData.getName());
        params.put("locality", profileData.getLocality());
        params.put("bio", profileData.getBio());
        Call<Void> Call = apiClient.uploadProfileEdit(params);
        Call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call call, Response response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    d(TAG, "onResponse: SUCCESS!");
                    getFragmentManager().popBackStack();

                } else {
                    d(TAG, "onResponse: FAILURE!");
                    Toast.makeText(getContext(), "Whoops", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                progressDialog.dismiss();
                d(TAG, "onFailure: ");
                Snackbar.make(rootView, "Check Connection", Snackbar.LENGTH_SHORT).show();
            }
        });*/
    }

}
