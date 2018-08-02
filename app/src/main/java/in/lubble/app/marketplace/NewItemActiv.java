package in.lubble.app.marketplace;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.signature.ObjectKey;
import com.crashlytics.android.Crashlytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.UploadFileService;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.models.marketplace.Item;
import in.lubble.app.models.marketplace.ServiceData;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import in.lubble.app.utils.UiUtils;
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

import static android.text.InputType.TYPE_CLASS_NUMBER;
import static android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS;
import static android.view.Gravity.RIGHT;
import static in.lubble.app.Constants.MEDIA_TYPE;
import static in.lubble.app.UploadFileService.BUCKET_MARKETPLACE;
import static in.lubble.app.models.marketplace.Item.ITEM_PRODUCT;
import static in.lubble.app.models.marketplace.Item.ITEM_SERVICE;
import static in.lubble.app.utils.FileUtils.createImageFile;
import static in.lubble.app.utils.FileUtils.getFileFromInputStreamUri;
import static in.lubble.app.utils.FileUtils.getPickImageIntent;
import static in.lubble.app.utils.FileUtils.showStoragePermRationale;
import static in.lubble.app.utils.StringUtils.isValidString;
import static in.lubble.app.utils.UiUtils.dpToPx;

@RuntimePermissions
public class NewItemActiv extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "NewItemActiv";
    private static final String ARG_EDIT_ITEM_ID = "ARG_EDIT_ITEM_ID";

    private static final int REQUEST_CODE_ITEM_PIC = 469;
    private static final int REQUEST_CODE_CATEGORY = 339;
    private Uri picUri = null;

    private ScrollView parentScrollView;
    private ImageView photoIv;
    private LinearLayout changePicHintContainer;
    private RadioButton productRadioBtn;
    private RadioButton serviceRadioBtn;
    private TextInputLayout nameTil;
    private TextInputLayout categoryTil;
    private TextInputLayout descTil;
    private LinearLayout catalogueLinearLayout;
    private TextInputLayout mrpTil;
    private TextInputLayout sellingPriceTil;
    private Button submitBtn;
    private String currentPhotoPath;
    private int categoryId = -1;
    private String categoryName;
    private ArrayList<ServiceData> serviceDataList;
    private int selectedItemType = ITEM_PRODUCT;
    private int itemId = -1;

    public static void open(Context context, int itemId) {
        final Intent intent = new Intent(context, NewItemActiv.class);
        intent.putExtra(ARG_EDIT_ITEM_ID, itemId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_item);

        parentScrollView = findViewById(R.id.scrollview_parent);
        photoIv = findViewById(R.id.iv_item_image);
        changePicHintContainer = findViewById(R.id.linearlayout_changepic_hint);
        RadioGroup itemTypeRadioGroup = findViewById(R.id.radio_group_type);
        productRadioBtn = findViewById(R.id.rb_product);
        serviceRadioBtn = findViewById(R.id.rb_service);
        nameTil = findViewById(R.id.til_item_name);
        categoryTil = findViewById(R.id.til_category);
        descTil = findViewById(R.id.til_item_desc);
        catalogueLinearLayout = findViewById(R.id.linearlayout_catalogue);
        mrpTil = findViewById(R.id.til_item_mrp);
        sellingPriceTil = findViewById(R.id.til_item_sellingprice);
        submitBtn = findViewById(R.id.btn_submit);

        productRadioBtn.setChecked(true);

        Analytics.triggerScreenEvent(this, this.getClass());

        itemId = getIntent().getIntExtra(ARG_EDIT_ITEM_ID, -1);

        Toolbar toolbar = findViewById(R.id.text_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (itemId != -1) {
            setTitle("Edit Item");
            fetchItemDetails();
        } else {
            setTitle("New Item");
        }
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidationPassed()) {
                    uploadNewItem();
                }
            }
        });

        UiUtils.hideKeyboard(this);

        photoIv.setOnClickListener(this);
        changePicHintContainer.setOnClickListener(this);
        categoryTil.getEditText().setOnClickListener(this);

        itemTypeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_product) {
                    selectedItemType = ITEM_PRODUCT;
                    catalogueLinearLayout.setVisibility(View.GONE);
                } else {
                    selectedItemType = ITEM_SERVICE;
                    showCatalogue();
                }
            }
        });
    }

    private void fetchItemDetails() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.all_updating);
        progressDialog.show();

        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        endpoints.fetchItemDetails(itemId).enqueue(new Callback<Item>() {
            @Override
            public void onResponse(Call<Item> call, Response<Item> response) {
                progressDialog.dismiss();
                final Item item = response.body();
                if (item != null) {
                    nameTil.getEditText().setText(item.getName());
                    descTil.getEditText().setText(item.getDescription());
                    mrpTil.getEditText().setText(item.getMrp());
                    sellingPriceTil.getEditText().setText(item.getSellingPrice());
                    if (item.getType() == ITEM_PRODUCT) {
                        productRadioBtn.setChecked(true);
                        serviceRadioBtn.setChecked(false);
                    } else {
                        productRadioBtn.setChecked(false);
                        serviceRadioBtn.setChecked(true);
                    }
                    handleServiceCatalog(item.getServiceDataList());
                    //todo categoryTil.getEditText().setText(item.getCategory());
                } else {
                    if (response.code() == 404) {
                        final Bundle bundle = new Bundle();
                        bundle.putInt("item_id", itemId);
                        Analytics.triggerEvent(AnalyticsEvents.ITEM_NOT_FOUND, bundle, NewItemActiv.this);
                        Toast.makeText(NewItemActiv.this, "Item Not Found", Toast.LENGTH_LONG).show();
                    } else {
                        Crashlytics.logException(new IllegalArgumentException("Item null for item ID: " + itemId));
                        Toast.makeText(NewItemActiv.this, R.string.all_try_again, Toast.LENGTH_SHORT).show();
                    }
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Item> call, Throwable t) {
                progressDialog.dismiss();
            }
        });
    }

    private void handleServiceCatalog(ArrayList<ServiceData> serviceDataList) {
        showCatalogue();
    }

    private void showCatalogue() {
        catalogueLinearLayout.removeAllViews();
        catalogueLinearLayout.invalidate();

        addNewService(0);
        addNewService(0);

        catalogueLinearLayout.setVisibility(View.VISIBLE);
        final TextView catalogueTitle = new TextView(this);
        catalogueTitle.setText("List all your services");
        //catalogueTitle.setGravity(CENTER);
        catalogueTitle.setTextSize(16);
        catalogueTitle.setPadding(0, dpToPx(12), 0, dpToPx(8));
        catalogueLinearLayout.addView(catalogueTitle, 0);

        addBtnLayout();
    }

    private void addBtnLayout() {
        final LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setWeightSum(10);

        final Button newServiceBtn = new Button(this);
        final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 7);
        newServiceBtn.setLayoutParams(lp);
        newServiceBtn.setText("Add Service");
        newServiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewService(catalogueLinearLayout.getChildCount() - 1);
            }
        });
        final Button deleteServiceBtn = new Button(this);
        final LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 3);
        deleteServiceBtn.setLayoutParams(lp1);
        deleteServiceBtn.setText("Delete");
        deleteServiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo remove data too
                if (catalogueLinearLayout.getChildCount() > 3) {
                    catalogueLinearLayout.removeViewAt(catalogueLinearLayout.getChildCount() - 2);
                } else {
                    Toast.makeText(NewItemActiv.this, "Please add at least 1 service", Toast.LENGTH_SHORT).show();
                }
            }
        });
        linearLayout.addView(newServiceBtn);
        linearLayout.addView(deleteServiceBtn);
        catalogueLinearLayout.addView(linearLayout);
    }

    private void addNewService(int pos) {
        final LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setWeightSum(10);

        final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 7);
        final TextInputLayout textInputLayout = new TextInputLayout(this);
        textInputLayout.setLayoutParams(lp);
        final EditText editText = new EditText(this);
        //editText.setLayoutParams(lp);
        textInputLayout.addView(editText);
        textInputLayout.getEditText().setInputType(TYPE_TEXT_FLAG_CAP_WORDS);
        textInputLayout.setHint("Service Name");

        final LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 3);
        final TextInputLayout textInputLayout1 = new TextInputLayout(this);
        textInputLayout1.setLayoutParams(lp1);
        textInputLayout1.setGravity(RIGHT);
        textInputLayout1.setHorizontalGravity(RIGHT);
        final EditText editText1 = new EditText(this);
        textInputLayout1.addView(editText1);
        //editText1.setLayoutParams(lp1);
        textInputLayout1.setHint("Price");
        textInputLayout1.getEditText().setInputType(TYPE_CLASS_NUMBER);

        linearLayout.addView(textInputLayout);
        linearLayout.addView(textInputLayout1);
        catalogueLinearLayout.addView(linearLayout, pos);
    }

    private ArrayList<ServiceData> getAllServiceDatas() {
        serviceDataList = new ArrayList<>();
        int serviceCount = catalogueLinearLayout.getChildCount() - 1;
        for (int i = 1; i < serviceCount; i++) {
            final LinearLayout serviceLinearLayout = (LinearLayout) catalogueLinearLayout.getChildAt(i);
            final TextInputLayout nameTil = (TextInputLayout) serviceLinearLayout.getChildAt(0);
            final TextInputLayout priceTil = (TextInputLayout) serviceLinearLayout.getChildAt(1);

            final ServiceData serviceData
                    = new ServiceData(nameTil.getEditText().getText().toString(), Integer.parseInt(priceTil.getEditText().getText().toString()));
            serviceDataList.add(serviceData);
        }
        return serviceDataList;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_item_image:
            case R.id.linearlayout_changepic_hint:
                NewItemActivPermissionsDispatcher
                        .startPhotoPickerWithPermissionCheck(NewItemActiv.this, REQUEST_CODE_ITEM_PIC);
                break;
            case R.id.et_category:
                startActivityForResult(CategoryChooserActiv.getIntent(NewItemActiv.this), REQUEST_CODE_CATEGORY);
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
        } else if (requestCode == REQUEST_CODE_CATEGORY && resultCode == RESULT_OK) {
            categoryId = data.getIntExtra("cat_id", -1);
            categoryName = data.getStringExtra("cat_name");
            categoryTil.getEditText().setText(categoryName);
        }
    }

    private void uploadNewItem() {

        HashMap<String, Object> params = new HashMap<>();

        params.put("seller", LubbleSharedPrefs.getInstance().getSellerId());
        params.put("type", selectedItemType);
        params.put("name", nameTil.getEditText().getText().toString());
        params.put("description", descTil.getEditText().getText().toString());
        params.put("category", categoryId);
        params.put("mrp", mrpTil.getEditText().getText().toString());
        params.put("selling_price", sellingPriceTil.getEditText().getText().toString());
        params.put("client_timestamp", System.currentTimeMillis());

        if (selectedItemType == ITEM_SERVICE) {
            JSONArray serviceCatalog = new JSONArray();
            for (ServiceData serviceData : getAllServiceDatas()) {
                final JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("title", serviceData.getTitle());
                    jsonObject.put("price", serviceData.getPrice());
                    serviceCatalog.put(jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            params.put("service_catalog", serviceCatalog);
        }
        RequestBody body = RequestBody.create(MEDIA_TYPE, new JSONObject(params).toString());

        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        endpoints.uploadNewItem(body).enqueue(new Callback<Item>() {
            @Override
            public void onResponse(Call<Item> call, Response<Item> response) {
                final Item item = response.body();
                if (item != null) {
                    // todo FileUtils.compressImage(new File(picUri.getPath()).getAbsolutePath(), 80);
                    //todo progress bar
                    //todo profile (test) this compression
                    startService(new Intent(NewItemActiv.this, UploadFileService.class)
                            .putExtra(UploadFileService.EXTRA_FILE_NAME, "item_pic_" + System.currentTimeMillis() + ".jpg")
                            .putExtra(UploadFileService.EXTRA_FILE_URI, picUri)
                            .putExtra(UploadFileService.EXTRA_BUCKET, BUCKET_MARKETPLACE)
                            .putExtra(UploadFileService.EXTRA_UPLOAD_PATH, "marketplace/items/" + item.getId())
                            .setAction(UploadFileService.ACTION_UPLOAD));
                    finish();
                } else {
                    Toast.makeText(NewItemActiv.this, R.string.all_try_again, Toast.LENGTH_SHORT).show();
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
            nameTil.setError(getString(R.string.name_error));
            parentScrollView.smoothScrollTo(0, 0);
            return false;
        } else {
            nameTil.setError(null);
        }
        if (!isValidString(descTil.getEditText().getText().toString())) {
            descTil.setError(getString(R.string.desc_error));
            parentScrollView.smoothScrollTo(0, 0);
            return false;
        } else {
            descTil.setError(null);
        }
        if (!isValidString(mrpTil.getEditText().getText().toString())) {
            mrpTil.setError(getString(R.string.mrp_error));
            parentScrollView.smoothScrollTo(0, 0);
            return false;
        } else {
            mrpTil.setError(null);
        }
        if (!isValidString(sellingPriceTil.getEditText().getText().toString())) {
            sellingPriceTil.setError(getString(R.string.selling_price_error));
            parentScrollView.smoothScrollTo(0, 0);
            return false;
        } else {
            sellingPriceTil.setError(null);
        }
        if (categoryId == -1) {
            Toast.makeText(this, R.string.no_category, Toast.LENGTH_SHORT).show();
            parentScrollView.smoothScrollTo(0, 0);
            return false;
        }
        if (picUri == null) {
            Toast.makeText(this, R.string.no_photo, Toast.LENGTH_SHORT).show();
            parentScrollView.smoothScrollTo(0, 0);
            return false;
        }
        if (selectedItemType == ITEM_SERVICE) {
            // check all services
            int serviceCount = catalogueLinearLayout.getChildCount() - 1;
            for (int i = 1; i < serviceCount; i++) {
                final LinearLayout serviceLinearLayout = (LinearLayout) catalogueLinearLayout.getChildAt(i);
                final TextInputLayout nameTil = (TextInputLayout) serviceLinearLayout.getChildAt(0);
                final TextInputLayout priceTil = (TextInputLayout) serviceLinearLayout.getChildAt(1);

                if (TextUtils.isEmpty(nameTil.getEditText().getText()) || TextUtils.isEmpty(priceTil.getEditText().getText())) {
                    Snackbar.make(parentScrollView, "Services cannot be empty", Snackbar.LENGTH_LONG).show();
                    return false;
                }
            }
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
