package in.lubble.app.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.signature.ObjectKey;

import java.io.File;
import java.io.IOException;

import in.lubble.app.BuildConfig;
import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.UploadFileService;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

import static in.lubble.app.utils.FileUtils.createImageFile;
import static in.lubble.app.utils.FileUtils.getFileFromInputStreamUri;
import static in.lubble.app.utils.FileUtils.getPickImageIntent;
import static in.lubble.app.utils.FileUtils.showStoragePermRationale;

@RuntimePermissions
public class FullScreenImageActivity extends BaseActivity {

    private static final int REQUEST_CODE_GROUP_DP = 418;
    private static final String EXTRA_IMG_PATH = BuildConfig.APPLICATION_ID + "_EXTRA_IMG_PATH";
    private static final String EXTRA_UPLOAD_PATH = BuildConfig.APPLICATION_ID + "_EXTRA_UPLOAD_PATH";
    private static final String EXTRA_ERROR_PIC = BuildConfig.APPLICATION_ID + "_EXTRA_ERROR_PIC";

    private String currentPhotoPath;
    private TouchImageView touchImageView;
    @Nullable
    private String uploadPath;

    /**
     * @param activity
     * @param context
     * @param imgPath
     * @param chatIv
     * @param uploadPath storage Ref path
     * @param errorPic
     */
    public static void open(Activity activity, Context context, String imgPath, ImageView chatIv, @Nullable String uploadPath, @DrawableRes int errorPic) {
        Intent intent = new Intent(context, FullScreenImageActivity.class);
        intent.putExtra(EXTRA_IMG_PATH, imgPath);
        intent.putExtra(EXTRA_UPLOAD_PATH, uploadPath);
        intent.putExtra(EXTRA_ERROR_PIC, errorPic);
        Bundle bundle = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, chatIv, chatIv.getTransitionName()).toBundle();
        }
        context.startActivity(intent, bundle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        Toolbar toolbar = findViewById(R.id.transparent_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("");

        touchImageView = findViewById(R.id.tiv_fullscreen);

        if (getIntent() != null) {
            String imgPath = getIntent().getStringExtra(EXTRA_IMG_PATH);
            int errorPic = getIntent().getIntExtra(EXTRA_ERROR_PIC, R.drawable.ic_cancel_black_24dp);
            uploadPath = getIntent().getStringExtra(EXTRA_UPLOAD_PATH);
            GlideApp.with(this)
                    .load(imgPath)
                    .error(errorPic)
                    .fitCenter()
                    .into(touchImageView);
        }
    }

    public void editGroupDp(MenuItem item) {
        if (uploadPath != null) {
            FullScreenImageActivityPermissionsDispatcher
                    .startPhotoPickerWithPermissionCheck(FullScreenImageActivity.this, REQUEST_CODE_GROUP_DP);
        }
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    public void startPhotoPicker(int REQUEST_CODE) {
        try {
            File cameraPic = createImageFile(this);
            currentPhotoPath = cameraPic.getAbsolutePath();
            Intent pickImageIntent = getPickImageIntent(this, cameraPic);
            startActivityForResult(pickImageIntent, REQUEST_CODE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_GROUP_DP && resultCode == RESULT_OK) {
            File imageFile;
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                imageFile = getFileFromInputStreamUri(this, uri);
            } else {
                // from camera
                imageFile = new File(currentPhotoPath);
            }

            Uri newProfilePicUri = Uri.fromFile(imageFile);
            GlideApp.with(this)
                    .load(imageFile)
                    .centerCrop()
                    .signature(new ObjectKey(imageFile.length() + "@" + imageFile.lastModified()))
                    .into(touchImageView);

            startService(new Intent(this, UploadFileService.class)
                    .putExtra(UploadFileService.EXTRA_FILE_NAME, "profile_pic_" + System.currentTimeMillis() + ".jpg")
                    .putExtra(UploadFileService.EXTRA_FILE_URI, newProfilePicUri)
                    .putExtra(UploadFileService.EXTRA_UPLOAD_PATH, uploadPath)
                    .setAction(UploadFileService.ACTION_UPLOAD));

        } else {
            Toast.makeText(this, "Failed to get photo", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (uploadPath != null) {
            getMenuInflater().inflate(R.menu.full_screen_img_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        FullScreenImageActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnShowRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
    void showRationaleForCamera(final PermissionRequest request) {
        showStoragePermRationale(this, request);
    }

    @OnPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE)
    void showDeniedForCamera() {
        Toast.makeText(this, R.string.storage_perm_denied_text, Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain(Manifest.permission.READ_EXTERNAL_STORAGE)
    void showNeverAskForCamera() {
        Toast.makeText(this, R.string.storage_perm_never_text, Toast.LENGTH_LONG).show();
    }

}
