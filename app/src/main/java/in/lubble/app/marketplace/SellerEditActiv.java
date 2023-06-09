package in.lubble.app.marketplace;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.signature.ObjectKey;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import in.lubble.app.BaseActivity;
import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.UploadFileService;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.models.FeatureData;
import in.lubble.app.models.marketplace.Item;
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
import static in.lubble.app.utils.UiUtils.compressImage;

@RuntimePermissions
public class SellerEditActiv extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "SellerEditActiv";
    private static final int REQUEST_PHOTO = 304;

    private ImageView photoIv;
    private TextView changePicHintTv;
    private TextInputLayout sellerNameTil;
    private TextInputLayout phoneNumberTil;
    private TextInputLayout sellerAboutTil;
    private EditText shopWebNameEt;
    private Button submitBtn;
    private int sellerId = -1;

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
        phoneNumberTil = findViewById(R.id.til_mobile_number);
        sellerAboutTil = findViewById(R.id.til_seller_about);
        shopWebNameEt = findViewById(R.id.et_shop_name);
        submitBtn = findViewById(R.id.btn_submit);

        Toolbar toolbar = findViewById(R.id.text_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Seller Profile");

        Analytics.triggerScreenEvent(this, this.getClass());

        photoIv.setOnClickListener(this);
        changePicHintTv.setOnClickListener(this);
        submitBtn.setOnClickListener(this);

        String userPhoneNumber = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        if (!TextUtils.isEmpty(userPhoneNumber) && userPhoneNumber.startsWith("+91")) {
            userPhoneNumber = userPhoneNumber.substring(3);
        }
        phoneNumberTil.getEditText().setText(userPhoneNumber);
        phoneNumberTil.getEditText().selectAll();

        sellerId = LubbleSharedPrefs.getInstance().getSellerId();
        if (sellerId != -1) {
            // seller already exists, pre-fill seller details for editing
            fetchSellerProfile(sellerId);
            submitBtn.setText(R.string.all_update);
        } else {
            fetchSellerId();
            syncSellerWebName();
        }
    }

    private void syncSellerWebName() {
        sellerNameTil.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    setShopWebLink(s.toString());
                }
            }
        });
    }

    private void setShopWebLink(String s) {
        shopWebNameEt.setText(s.trim().toLowerCase().replace(" ", "-"));
    }

    private void fetchSellerId() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.all_please_wait));
        progressDialog.show();

        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        endpoints.fetchAppFeatures().enqueue(new Callback<FeatureData>() {
            @Override
            public void onResponse(Call<FeatureData> call, Response<FeatureData> response) {
                progressDialog.dismiss();
                final FeatureData featureData = response.body();
                if (featureData != null) {
                    final List<Integer> sellerList = featureData.getSellers();
                    if (sellerList != null && sellerList.size() > 0) {
                        sellerId = sellerList.get(0);
                        LubbleSharedPrefs.getInstance().setSellerId(sellerId);
                        startActivity(SellerDashActiv.getIntent(SellerEditActiv.this, sellerId, false, Item.ITEM_PRODUCT));
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(Call<FeatureData> call, Throwable t) {
                Log.e(TAG, "onFailure: ");
                progressDialog.dismiss();
            }
        });
    }

    private void fetchSellerProfile(final int sellerId) {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.all_please_wait));
        progressDialog.show();

        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        endpoints.fetchSellerProfile(sellerId).enqueue(new Callback<SellerData>() {
            @Override
            public void onResponse(Call<SellerData> call, Response<SellerData> response) {
                progressDialog.dismiss();
                final SellerData sellerData = response.body();
                if (sellerData != null) {
                    sellerNameTil.getEditText().setText(sellerData.getName());
                    if (!TextUtils.isEmpty(sellerData.getPhone())) {
                        phoneNumberTil.getEditText().setText(sellerData.getPhone());
                    }
                    sellerAboutTil.getEditText().setText(sellerData.getBio());
                    final String webLink = sellerData.getWebLink();
                    if (!TextUtils.isEmpty(webLink)) {
                        // web link exists, do not allow updation
                        shopWebNameEt.setText(webLink);
                        shopWebNameEt.setEnabled(false);
                    } else {
                        // web link does NOT exist
                        shopWebNameEt.setEnabled(true);
                        setShopWebLink(sellerData.getName());
                        syncSellerWebName();
                    }
                    if (!TextUtils.isEmpty(sellerData.getPhotoUrl())) {
                        photoIv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        GlideApp.with(SellerEditActiv.this)
                                .load(sellerData.getPhotoUrl())
                                .error(R.drawable.ic_shop)
                                .circleCrop()
                                .into(photoIv);
                    } else {
                        photoIv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                        GlideApp.with(SellerEditActiv.this)
                                .load(R.drawable.ic_shop)
                                .into(photoIv);
                    }
                    setTitle(sellerData.getName());

                } else {
                    FirebaseCrashlytics.getInstance().recordException(new IllegalArgumentException("seller profile null for seller id: " + sellerId));
                    Toast.makeText(SellerEditActiv.this, R.string.all_try_again, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SellerData> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(SellerEditActiv.this, R.string.check_internet, Toast.LENGTH_SHORT).show();
            }
        });
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
        shopWebNameEt.setError(null);

        HashMap<String, Object> params = new HashMap<>();

        params.put("name", sellerNameTil.getEditText().getText().toString().trim());
        params.put("phone", phoneNumberTil.getEditText().getText().toString().trim());
        params.put("bio", sellerAboutTil.getEditText().getText().toString().trim());
        params.put("web_link", shopWebNameEt.getText().toString().trim());

        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        final Call<SellerData> sellerDataCall;
        if (sellerId == -1) {
            // new seller
            params.put("client_timestamp", System.currentTimeMillis());
            sellerDataCall = endpoints.uploadSellerProfile(RequestBody.create(MEDIA_TYPE, new JSONObject(params).toString()));
        } else {
            // updating old seller
            sellerDataCall = endpoints.updateSellerProfile(sellerId, RequestBody.create(MEDIA_TYPE, new JSONObject(params).toString()));
        }

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.all_updating));
        progressDialog.show();

        sellerDataCall.enqueue(new Callback<SellerData>() {
            @Override
            public void onResponse(Call<SellerData> call, Response<SellerData> response) {
                final SellerData sellerData = response.body();
                progressDialog.dismiss();
                if (response.isSuccessful() && sellerData != null && !isFinishing()) {
                    if (picUri != null) {
                        // upload pic only if it has changed, might not when editing seller
                        Log.d(TAG, "OG file size: " + new File(picUri.getPath()).length() / 1024);
                        picUri = Uri.fromFile(compressImage(picUri.getPath()));
                        Log.d(TAG, "NEW file size: " + new File(picUri.getPath()).length() / 1024);
                        startService(new Intent(SellerEditActiv.this, UploadFileService.class)
                                .putExtra(UploadFileService.EXTRA_FILE_NAME, "seller_pic_" + System.currentTimeMillis() + ".jpg")
                                .putExtra(UploadFileService.EXTRA_FILE_URI, picUri)
                                .putExtra(UploadFileService.EXTRA_BUCKET, BUCKET_MARKETPLACE)
                                .putExtra(UploadFileService.EXTRA_UPLOAD_PATH, "marketplace/seller/" + sellerData.getId())
                                .setAction(UploadFileService.ACTION_UPLOAD));
                    }
                    LubbleSharedPrefs.getInstance().setSellerId(sellerData.getId());
                    startActivity(SellerDashActiv.getIntent(SellerEditActiv.this, sellerData.getId(), true, Item.ITEM_PRODUCT));
                    finish();
                } else if (!isFinishing()) {
                    try {
                        final String errorString = response.errorBody().string();
                        if (!TextUtils.isEmpty(errorString)) {
                            final JSONObject jsonObject = new JSONObject(errorString);
                            if (jsonObject.get("web_link_failure") == Boolean.TRUE) {
                                shopWebNameEt.setError("Already Taken. Try a unique name");
                            } else {
                                shopWebNameEt.setError(null);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<SellerData> call, Throwable t) {
                Log.e(TAG, "onFailure: ");
                Toast.makeText(SellerEditActiv.this, R.string.check_internet, Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
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
        if (!isValidString(phoneNumberTil.getEditText().getText().toString().trim())) {
            phoneNumberTil.setError(getString(R.string.phone_error));
            return false;
        } else {
            phoneNumberTil.setError(null);
        }
        if (!isValidString(sellerAboutTil.getEditText().getText().toString())) {
            sellerAboutTil.setError(getString(R.string.desc_error));
            return false;
        } else {
            sellerAboutTil.setError(null);
        }
        if (!isValidString(shopWebNameEt.getText().toString())) {
            shopWebNameEt.setError(getString(R.string.all_fill_details));
            return false;
        } else {
            shopWebNameEt.setError(null);
        }
        if (picUri == null && sellerId == -1) {
            // new seller; pic is reqd
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        SellerEditActivPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
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
