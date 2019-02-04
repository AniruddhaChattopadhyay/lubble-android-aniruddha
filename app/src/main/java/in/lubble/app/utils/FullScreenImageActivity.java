package in.lubble.app.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.ObjectKey;
import in.lubble.app.*;
import in.lubble.app.BuildConfig;
import in.lubble.app.R;
import permissions.dispatcher.*;

import java.io.File;
import java.io.IOException;

import static in.lubble.app.utils.FileUtils.*;

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
    private ProgressBar progressbar;

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
        progressbar = findViewById(R.id.progress_bar_full);

        if (getIntent() != null) {
            String imgPath = getIntent().getStringExtra(EXTRA_IMG_PATH);
            int errorPic = getIntent().getIntExtra(EXTRA_ERROR_PIC, R.drawable.ic_cancel_black_24dp);
            uploadPath = getIntent().getStringExtra(EXTRA_UPLOAD_PATH);
            progressbar.setVisibility(View.VISIBLE);
            GlideApp.with(this)
                    .load(imgPath)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .error(errorPic)
                    .fitCenter()
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            progressbar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            progressbar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(touchImageView);
        }
    }

    public void editGroupDp(MenuItem item) {
        if (uploadPath != null) {
            FullScreenImageActivityPermissionsDispatcher
                    .startPhotoPickerWithPermissionCheck(FullScreenImageActivity.this, REQUEST_CODE_GROUP_DP);
        }
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
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

    @OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showRationaleForCamera(final PermissionRequest request) {
        showStoragePermRationale(this, request);
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showDeniedForCamera() {
        Toast.makeText(this, R.string.storage_perm_denied_text, Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showNeverAskForCamera() {
        Toast.makeText(this, R.string.storage_perm_never_text, Toast.LENGTH_LONG).show();
    }

}
