package in.lubble.app.profile;

import android.Manifest;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.ObjectKey;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.IOException;

import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.UploadFileService;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.models.ProfileData;
import in.lubble.app.utils.StringUtils;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

import static android.app.Activity.RESULT_OK;
import static in.lubble.app.firebase.RealtimeDbHelper.getThisUserRef;
import static in.lubble.app.utils.FileUtils.createImageFile;
import static in.lubble.app.utils.FileUtils.getFileFromInputStreamUri;
import static in.lubble.app.utils.FileUtils.getPickImageIntent;
import static in.lubble.app.utils.FileUtils.showStoragePermRationale;

@RuntimePermissions
public class EditProfileFrag extends Fragment {

    private static final String TAG = "EditProfileFrag";
    private static final int REQUEST_CODE_COVER_PIC = 560;
    private static final int REQUEST_CODE_DP = 211;

    private ImageView coverPicIv;
    private ImageView profilePicIv;
    private TextView fullNameTv;
    private TextView lubbleTv;
    private TextInputLayout bioTil;
    private Button saveBtn;
    private View rootView;
    private String currentPhotoPath;
    private ProgressBar progressBar;
    private Uri newProfilePicUri = null;
    private Uri newCoverPicUri = null;
    private ProfileData fetchedProfileData;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        coverPicIv = rootView.findViewById(R.id.iv_cover);
        profilePicIv = rootView.findViewById(R.id.iv_profilePic);
        fullNameTv = rootView.findViewById(R.id.tv_name);
        lubbleTv = rootView.findViewById(R.id.tv_lubble);
        bioTil = rootView.findViewById(R.id.til_bio);
        saveBtn = rootView.findViewById(R.id.btn_save_profile);
        progressBar = rootView.findViewById(R.id.progressBar_profile);

        rootView.findViewById(R.id.linearLayout_cover_edit_container).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.iv_dp_edit_overlay).setVisibility(View.VISIBLE);

        Analytics.triggerScreenEvent(getContext(), this.getClass());

        // single listener becoz data wudnt have changed going from profile to edit profile.
        getThisUserRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (isAdded()) {
                    fetchedProfileData = dataSnapshot.getValue(ProfileData.class);
                    fullNameTv.setText(fetchedProfileData.getInfo().getName());
                    lubbleTv.setText(fetchedProfileData.getLocality());
                    bioTil.getEditText().setText(fetchedProfileData.getBio());
                    GlideApp.with(getContext())
                            .load(fetchedProfileData.getProfilePic())
                            .error(R.drawable.ic_account_circle_black_no_padding)
                            .placeholder(R.drawable.ic_account_circle_black_no_padding)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    progressBar.setVisibility(View.GONE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    progressBar.setVisibility(View.GONE);
                                    return false;
                                }
                            })
                            .circleCrop()
                            .into(profilePicIv);
                    GlideApp.with(getContext())
                            .load(fetchedProfileData.getCoverPic())
                            .into(coverPicIv);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        coverPicIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditProfileFragPermissionsDispatcher
                        .startPhotoPickerWithPermissionCheck(EditProfileFrag.this, REQUEST_CODE_COVER_PIC);
            }
        });
        profilePicIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditProfileFragPermissionsDispatcher
                        .startPhotoPickerWithPermissionCheck(EditProfileFrag.this, REQUEST_CODE_DP);
            }
        });
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (newProfilePicUri != null) {
                    getContext().startService(new Intent(getContext(), UploadFileService.class)
                            .putExtra(UploadFileService.EXTRA_FILE_NAME, "profile_pic_" + System.currentTimeMillis() + ".jpg")
                            .putExtra(UploadFileService.EXTRA_FILE_URI, newProfilePicUri)
                            .putExtra(UploadFileService.EXTRA_UPLOAD_PATH, "user_profile/" + FirebaseAuth.getInstance().getUid())
                            .setAction(UploadFileService.ACTION_UPLOAD));
                }
                if (newCoverPicUri != null) {
                    getContext().startService(new Intent(getContext(), UploadFileService.class)
                            .putExtra(UploadFileService.EXTRA_FILE_NAME, "cover_pic" + System.currentTimeMillis() + ".jpg")
                            .putExtra(UploadFileService.EXTRA_FILE_URI, newCoverPicUri)
                            .putExtra(UploadFileService.EXTRA_UPLOAD_PATH, "user_profile/" + FirebaseAuth.getInstance().getUid())
                            .setAction(UploadFileService.ACTION_UPLOAD));
                }

                getThisUserRef().child("bio").setValue(StringUtils.getStringFromTil(bioTil));
                getFragmentManager().popBackStack();
            }
        });

        return rootView;
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    public void startPhotoPicker(int REQUEST_CODE) {
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

            if (requestCode == REQUEST_CODE_DP) {
                newProfilePicUri = Uri.fromFile(imageFile);
                GlideApp.with(getContext())
                        .load(imageFile)
                        .centerCrop()
                        .circleCrop()
                        .signature(new ObjectKey(imageFile.length() + "@" + imageFile.lastModified()))
                        .into(profilePicIv);
            } else {
                newCoverPicUri = Uri.fromFile(imageFile);
                GlideApp.with(getContext())
                        .load(imageFile)
                        .centerCrop()
                        .signature(new ObjectKey(imageFile.length() + "@" + imageFile.lastModified()))
                        .into(coverPicIv);
            }

        } else {
            Log.e(TAG, "onActivityResult: Failed to get photo");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        EditProfileFragPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnShowRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
    void showRationaleForCamera(final PermissionRequest request) {
        showStoragePermRationale(getContext(), request);
    }

    @OnPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE)
    void showDeniedForCamera() {
        Toast.makeText(getContext(), "Please grant permission to upload your photos", Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain(Manifest.permission.READ_EXTERNAL_STORAGE)
    void showNeverAskForCamera() {
        Toast.makeText(getContext(), "To enable permissions again, go to app settings of Lubble", Toast.LENGTH_LONG).show();
    }

}
