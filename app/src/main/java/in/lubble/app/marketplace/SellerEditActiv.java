package in.lubble.app.marketplace;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.signature.ObjectKey;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.UploadFileService;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.models.marketplace.SellerData;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import okhttp3.RequestBody;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static in.lubble.app.Constants.MEDIA_TYPE;
import static in.lubble.app.UploadFileService.BUCKET_MARKETPLACE;
import static in.lubble.app.utils.FileUtils.createImageFile;
import static in.lubble.app.utils.FileUtils.getFileFromInputStreamUri;
import static in.lubble.app.utils.FileUtils.getPickImageIntent;
import static in.lubble.app.utils.FileUtils.showStoragePermRationale;
import static in.lubble.app.utils.StringUtils.isValidString;

@RuntimePermissions
public class SellerEditActiv extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SellerEditActiv";
    private static final int REQUEST_PHOTO = 304;

    private ImageView photoIv;
    private TextView changePicHintTv;
    private TextInputLayout sellerNameTil;
    private TextInputLayout sellerAboutTil;
    private Button submitBtn;

    private String currentPhotoPath;
    private Uri picUri = null;

    public static void open(Context context) {
        context.startActivity(new Intent(context, SellerEditActiv.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_edit);

        photoIv = findViewById(R.id.iv_seller_pic);
        changePicHintTv = findViewById(R.id.tv_change_pic_hint);
        sellerNameTil = findViewById(R.id.til_seller_name);
        sellerAboutTil = findViewById(R.id.til_seller_about);
        submitBtn = findViewById(R.id.btn_submit);

        Toolbar toolbar = findViewById(R.id.text_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Seller Profile");

        Analytics.triggerScreenEvent(this, this.getClass());

        photoIv.setOnClickListener(this);
        changePicHintTv.setOnClickListener(this);
        submitBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_seller_pic:
            case R.id.tv_change_pic_hint:
                SellerEditActivPermissionsDispatcher
                        .startPhotoPickerWithPermissionCheck(SellerEditActiv.this, REQUEST_PHOTO);
                break;
            case R.id.btn_submit:
                if (isValidationPassed()) {
                    uploadSellerProfile();
                }
                break;
        }
    }

    private void uploadSellerProfile() {

        HashMap<String, Object> params = new HashMap<>();

        params.put("name", sellerNameTil.getEditText().getText().toString());
        params.put("bio", sellerAboutTil.getEditText().getText().toString());
        params.put("client_timestamp", System.currentTimeMillis());

        final JSONObject jsonObject = new JSONObject(params);

        RequestBody body = RequestBody.create(MEDIA_TYPE, jsonObject.toString());

        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        endpoints.uploadSellerProfile(body).enqueue(new Callback<SellerData>() {
            @Override
            public void onResponse(Call<SellerData> call, Response<SellerData> response) {
                final SellerData sellerData = response.body();
                if (sellerData != null) {
                    //todo compress img
                    startService(new Intent(SellerEditActiv.this, UploadFileService.class)
                            .putExtra(UploadFileService.EXTRA_FILE_NAME, "seller_pic_" + System.currentTimeMillis() + ".jpg")
                            .putExtra(UploadFileService.EXTRA_FILE_URI, picUri)
                            .putExtra(UploadFileService.EXTRA_BUCKET, BUCKET_MARKETPLACE)
                            .putExtra(UploadFileService.EXTRA_UPLOAD_PATH, "marketplace/seller/" + sellerData.getId())
                            .setAction(UploadFileService.ACTION_UPLOAD));
                }
            }

            @Override
            public void onFailure(Call<SellerData> call, Throwable t) {
                Log.e(TAG, "onFailure: ");
            }
        });
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PHOTO && resultCode == RESULT_OK) {
            File imageFile;
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                imageFile = getFileFromInputStreamUri(this, uri);
            } else {
                // from camera
                imageFile = new File(currentPhotoPath);
            }
            picUri = Uri.fromFile(imageFile);
            GlideApp.with(this)
                    .load(imageFile)
                    .circleCrop()
                    .signature(new ObjectKey(imageFile.length() + "@" + imageFile.lastModified()))
                    .into(photoIv);
        }
    }

    private boolean isValidationPassed() {
        if (!isValidString(sellerNameTil.getEditText().getText().toString().trim())) {
            sellerNameTil.setError(getString(R.string.name_error));
            return false;
        } else {
            sellerNameTil.setError(null);
        }
        if (!isValidString(sellerAboutTil.getEditText().getText().toString())) {
            sellerAboutTil.setError(getString(R.string.desc_error));
            return false;
        } else {
            sellerAboutTil.setError(null);
        }
        if (picUri == null) {
            Toast.makeText(this, R.string.no_photo, Toast.LENGTH_SHORT).show();
            return false;
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        SellerEditActivPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
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
