package in.lubble.app.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

import static in.lubble.app.utils.FileUtils.createImageFile;
import static in.lubble.app.utils.FileUtils.getFileFromInputStreamUri;
import static in.lubble.app.utils.FileUtils.getPickImageIntent;

public class FullScreenImageActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_GROUP_DP = 418;
    private static final String EXTRA_IMG_PATH = BuildConfig.APPLICATION_ID + "_EXTRA_IMG_PATH";
    private static final String EXTRA_UPLOAD_PATH = BuildConfig.APPLICATION_ID + "_EXTRA_UPLOAD_PATH";

    private String currentPhotoPath;
    private TouchImageView touchImageView;
    @Nullable
    private String uploadPath;

    public static void open(Activity activity, Context context, String imgPath, ImageView chatIv, @Nullable String uploadPath) {
        Intent intent = new Intent(context, FullScreenImageActivity.class);
        intent.putExtra(EXTRA_IMG_PATH, imgPath);
        intent.putExtra(EXTRA_UPLOAD_PATH, uploadPath);
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
            uploadPath = getIntent().getStringExtra(EXTRA_UPLOAD_PATH);
            GlideApp.with(this)
                    .load(imgPath)
                    .placeholder(R.drawable.ic_account_circle_black_no_padding)
                    .fitCenter()
                    .into(touchImageView);
        }
    }

    public void editGroupDp(MenuItem item) {
        if (uploadPath != null) {
            startPhotoPicker(REQUEST_CODE_GROUP_DP);
        }
    }

    private void startPhotoPicker(int REQUEST_CODE) {
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

}
