package in.lubble.app.marketplace;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
import in.lubble.app.models.marketplace.Item;
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
public class NewItemActiv extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "NewItemActiv";

    private static final int REQUEST_CODE_ITEM_PIC = 469;
    private Uri picUri = null;

    private ScrollView parentScrollView;
    private ImageView photoIv;
    private LinearLayout changePicHintContainer;
    private TextInputLayout nameTil;
    private TextInputLayout descTil;
    private TextInputLayout mrpTil;
    private TextInputLayout sellingPriceTil;
    private Button submitBtn;
    private String currentPhotoPath;

    public static void open(Context context) {
        context.startActivity(new Intent(context, NewItemActiv.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_item);

        parentScrollView = findViewById(R.id.scrollview_parent);
        photoIv = findViewById(R.id.iv_item_image);
        changePicHintContainer = findViewById(R.id.linearlayout_changepic_hint);
        nameTil = findViewById(R.id.til_item_name);
        descTil = findViewById(R.id.til_item_desc);
        mrpTil = findViewById(R.id.til_item_mrp);
        sellingPriceTil = findViewById(R.id.til_item_sellingprice);
        submitBtn = findViewById(R.id.btn_submit);

        Analytics.triggerScreenEvent(this, this.getClass());

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidationPassed()) {
                    uploadNewItem();
                }
            }
        });

        photoIv.setOnClickListener(this);
        changePicHintContainer.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_item_image:
            case R.id.linearlayout_changepic_hint:
                NewItemActivPermissionsDispatcher
                        .startPhotoPickerWithPermissionCheck(NewItemActiv.this, REQUEST_CODE_ITEM_PIC);
                break;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_ITEM_PIC && resultCode == RESULT_OK) {
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
                    .signature(new ObjectKey(imageFile.length() + "@" + imageFile.lastModified()))
                    .into(photoIv);
        }
    }

    private void uploadNewItem() {

        HashMap<String, Object> params = new HashMap<>();

        params.put("name", nameTil.getEditText().getText().toString());
        params.put("category", 1);
        params.put("mrp", mrpTil.getEditText().getText().toString());
        params.put("selling_price", sellingPriceTil.getEditText().getText().toString());
        params.put("client_timestamp", System.currentTimeMillis());

        final JSONObject jsonObject = new JSONObject(params);

        RequestBody body = RequestBody.create(MEDIA_TYPE, jsonObject.toString());

        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        endpoints.uploadNewItem(body).enqueue(new Callback<Item>() {
            @Override
            public void onResponse(Call<Item> call, Response<Item> response) {
                final Item item = response.body();
                if (item != null) {
                    //todo compress img
                    startService(new Intent(NewItemActiv.this, UploadFileService.class)
                            .putExtra(UploadFileService.EXTRA_FILE_NAME, "item_pic_" + System.currentTimeMillis() + ".jpg")
                            .putExtra(UploadFileService.EXTRA_FILE_URI, picUri)
                            .putExtra(UploadFileService.EXTRA_BUCKET, BUCKET_MARKETPLACE)
                            .putExtra(UploadFileService.EXTRA_UPLOAD_PATH, "marketplace/items/" + item.getId())
                            .setAction(UploadFileService.ACTION_UPLOAD));
                }
            }

            @Override
            public void onFailure(Call<Item> call, Throwable t) {
                Log.e(TAG, "onFailure: ");
            }
        });
    }

    private boolean isValidationPassed() {
        if (!isValidString(nameTil.getEditText().getText().toString().trim())) {
            nameTil.setError(getString(R.string.event_name_error));
            parentScrollView.smoothScrollTo(0, 0);
            return false;
        } else {
            nameTil.setError(null);
        }
        if (!isValidString(descTil.getEditText().getText().toString())) {
            descTil.setError(getString(R.string.event_desc_error));
            parentScrollView.smoothScrollTo(0, 0);
            return false;
        } else {
            descTil.setError(null);
        }
        if (!isValidString(mrpTil.getEditText().getText().toString())) {
            mrpTil.setError(getString(R.string.event_organizer_error));
            parentScrollView.smoothScrollTo(0, 0);
            return false;
        } else {
            mrpTil.setError(null);
        }
        if (!isValidString(sellingPriceTil.getEditText().getText().toString())) {
            sellingPriceTil.setError(getString(R.string.event_date_error));
            parentScrollView.smoothScrollTo(0, 0);
            return false;
        } else {
            sellingPriceTil.setError(null);
        }
        if (!isValidString(picUri.toString())) {
            Toast.makeText(this, R.string.no_photo, Toast.LENGTH_SHORT).show();
            parentScrollView.smoothScrollTo(0, 0);
            return false;
        }
        return true;
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
        NewItemActivPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
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
