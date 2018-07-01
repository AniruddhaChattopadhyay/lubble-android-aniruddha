package in.lubble.app.auth;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.signature.ObjectKey;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.IOException;

import in.lubble.app.GlideApp;
import in.lubble.app.MainActivity;
import in.lubble.app.R;
import in.lubble.app.UploadFileService;
import in.lubble.app.analytics.Analytics;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

import static android.app.Activity.RESULT_OK;
import static in.lubble.app.utils.FileUtils.createImageFile;
import static in.lubble.app.utils.FileUtils.getFileFromInputStreamUri;
import static in.lubble.app.utils.FileUtils.getPickImageIntent;
import static in.lubble.app.utils.FileUtils.showStoragePermRationale;

@RuntimePermissions
public class ProfilePicFrag extends Fragment {

    private static final String ARG_PROFILE_PIC_IDP_RESPONSE = "arg_dp_idp_resposne";
    private static final int REQUEST_CODE_DP = 379;

    private Parcelable idpResponse;
    private TextView dpHint;
    private String currentPhotoPath;
    private ImageView welcomeDpIv;
    private Button submitBtn;
    private File imageFile;

    public ProfilePicFrag() {
        // Required empty public constructor
    }

    public static ProfilePicFrag newInstance(Parcelable idpResponse) {
        ProfilePicFrag fragment = new ProfilePicFrag();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PROFILE_PIC_IDP_RESPONSE, idpResponse);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            idpResponse = getArguments().getParcelable(ARG_PROFILE_PIC_IDP_RESPONSE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile_pic, container, false);

        submitBtn = rootView.findViewById(R.id.btn_submit);
        dpHint = rootView.findViewById(R.id.tv_welcome_name);
        TextView skipTv = rootView.findViewById(R.id.tv_skipBtn);
        welcomeDpIv = rootView.findViewById(R.id.iv_welcome_dp);
        Analytics.triggerScreenEvent(getContext(), this.getClass());
        dpHint.setText(
                String.format(getString(R.string.profile_pic_hint)
                        , FirebaseAuth.getInstance().getCurrentUser().getDisplayName().split(" ")[0])
        );

        skipTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMain();
            }
        });

        welcomeDpIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfilePicFragPermissionsDispatcher
                        .startPhotoPickerWithPermissionCheck(ProfilePicFrag.this, REQUEST_CODE_DP);
            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getContext().startService(new Intent(getContext(), UploadFileService.class)
                        .putExtra(UploadFileService.EXTRA_FILE_NAME, "profile_pic_" + System.currentTimeMillis() + ".jpg")
                        .putExtra(UploadFileService.EXTRA_FILE_URI, Uri.fromFile(imageFile))
                        .putExtra(UploadFileService.EXTRA_UPLOAD_PATH, "user_profile/" + FirebaseAuth.getInstance().getUid())
                        .setAction(UploadFileService.ACTION_UPLOAD));
                startMain();
            }
        });

        return rootView;
    }

    private void startMain() {
        Analytics.triggerSignUpEvent(getContext());
        startActivity(MainActivity.createIntent(getContext(), ((IdpResponse) idpResponse)));
        getActivity().finishAffinity();
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
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_DP && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                imageFile = getFileFromInputStreamUri(getContext(), uri);
            } else {
                // from camera
                imageFile = new File(currentPhotoPath);
            }
            GlideApp.with(getContext())
                    .load(imageFile)
                    .circleCrop()
                    .signature(new ObjectKey(imageFile.length() + "@" + imageFile.lastModified()))
                    .into(welcomeDpIv);
            submitBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        ProfilePicFragPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnShowRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
    void showRationaleForCamera(final PermissionRequest request) {
        showStoragePermRationale(getContext(), request);
    }

    @OnPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE)
    void showDeniedForCamera() {
        Toast.makeText(getContext(), R.string.storage_perm_denied_text, Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain(Manifest.permission.READ_EXTERNAL_STORAGE)
    void showNeverAskForCamera() {
        Toast.makeText(getContext(), R.string.storage_perm_never_text, Toast.LENGTH_LONG).show();
    }

}
