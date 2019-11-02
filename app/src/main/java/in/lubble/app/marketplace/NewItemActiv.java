package in.lubble.app.marketplace;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.signature.ObjectKey;
import com.crashlytics.android.Crashlytics;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import in.lubble.app.*;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.models.marketplace.Item;
import in.lubble.app.models.marketplace.ServiceData;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import in.lubble.app.utils.UiUtils;
import okhttp3.RequestBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import permissions.dispatcher.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static android.text.InputType.*;
import static android.view.Gravity.RIGHT;
import static in.lubble.app.Constants.MEDIA_TYPE;
import static in.lubble.app.UploadFileService.BUCKET_MARKETPLACE;
import static in.lubble.app.analytics.AnalyticsEvents.HELP_BTN_CLICKED;
import static in.lubble.app.analytics.AnalyticsEvents.HELP_PHONE_CLICKED;
import static in.lubble.app.models.marketplace.Item.*;
import static in.lubble.app.utils.FileUtils.*;
import static in.lubble.app.utils.StringUtils.isValidString;
import static in.lubble.app.utils.UiUtils.compressImage;
import static in.lubble.app.utils.UiUtils.dpToPx;

@RuntimePermissions
public class NewItemActiv extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "NewItemActiv";
    private static final String ARG_EDIT_ITEM_ID = "ARG_EDIT_ITEM_ID";
    private static final String ARG_DEFAULT_ITEM_TYPE = "ARG_DEFAULT_ITEM_TYPE";

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
    private Spinner priceSpinner;
    private TextInputLayout mrpTil;
    private ImageView mrpIv;
    private ImageView sellingPriceIv;
    private TextInputLayout sellingPriceTil;
    private TextView priceHintTv;
    private Button submitBtn;
    private String currentPhotoPath;
    private int categoryId = -1;
    private String categoryName;
    private ArrayList<ServiceData> serviceDataList;
    private int selectedItemType = ITEM_PRODUCT;
    private int itemId = -1;
    private ProgressDialog progressDialog;
    @Nullable
    private Item item;
    private int selectedPriceOption = Item.ITEM_PRICING_PAID;

    public static void open(Context context, int itemId, int defaultType) {
        final Intent intent = new Intent(context, NewItemActiv.class);
        intent.putExtra(ARG_EDIT_ITEM_ID, itemId);
        intent.putExtra(ARG_DEFAULT_ITEM_TYPE, defaultType);
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
        priceSpinner = findViewById(R.id.spinner_price);
        mrpTil = findViewById(R.id.til_item_mrp);
        mrpIv = findViewById(R.id.iv_mrp);
        sellingPriceIv = findViewById(R.id.iv_selling_price);
        sellingPriceTil = findViewById(R.id.til_item_sellingprice);
        priceHintTv = findViewById(R.id.tv_selling_price_hint);
        submitBtn = findViewById(R.id.btn_submit);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.price_options_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        priceSpinner.setAdapter(adapter);
        priceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                handleChangeInPriceOptions(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Analytics.triggerScreenEvent(this, this.getClass());
        productRadioBtn.setChecked(true);
        itemId = getIntent().getIntExtra(ARG_EDIT_ITEM_ID, -1);
        selectedItemType = getIntent().getIntExtra(ARG_DEFAULT_ITEM_TYPE, Item.ITEM_PRODUCT);
        Toolbar toolbar = findViewById(R.id.text_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidationPassed()) {
                    updateServiceDataFromUi();
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
                    mrpTil.setVisibility(View.VISIBLE);
                    mrpIv.setVisibility(View.VISIBLE);
                    sellingPriceTil.setVisibility(View.VISIBLE);
                    sellingPriceIv.setVisibility(View.VISIBLE);
                } else {
                    selectedItemType = ITEM_SERVICE;
                    showCatalogue(null);
                    mrpTil.setVisibility(View.GONE);
                    mrpIv.setVisibility(View.GONE);
                    sellingPriceTil.setVisibility(View.GONE);
                    sellingPriceIv.setVisibility(View.GONE);
                }
            }
        });

        if (selectedItemType == ITEM_PRODUCT) {
            productRadioBtn.setChecked(true);
            serviceRadioBtn.setChecked(false);
        } else {
            productRadioBtn.setChecked(false);
            serviceRadioBtn.setChecked(true);
        }
        if (itemId != -1) {
            setTitle("Edit Item");
            fetchItemDetails();
        } else {
            setTitle("New Item");
        }
    }

    private void handleChangeInPriceOptions(int selectedOption) {
        selectedPriceOption = selectedOption;
        switch (selectedOption) {
            case Item.ITEM_PRICING_PAID:
                if (selectedItemType == Item.ITEM_PRODUCT) {
                    sellingPriceIv.setVisibility(View.VISIBLE);
                    sellingPriceTil.setVisibility(View.VISIBLE);
                    mrpIv.setVisibility(View.VISIBLE);
                    mrpTil.setVisibility(View.VISIBLE);
                } else {
                    if (itemId != -1 && item != null) {
                        showCatalogue(item.getServiceDataList());
                    } else {
                        showCatalogue(null);
                    }
                }
                priceHintTv.setVisibility(View.GONE);
                break;
            case Item.ITEM_PRICING_ON_REQUEST:
                if (selectedItemType == Item.ITEM_PRODUCT) {
                    sellingPriceIv.setVisibility(View.GONE);
                    sellingPriceTil.setVisibility(View.GONE);
                    mrpIv.setVisibility(View.GONE);
                    mrpTil.setVisibility(View.GONE);
                } else {
                    if (itemId != -1 && item != null) {
                        showCatalogue(item.getServiceDataList());
                    } else {
                        showCatalogue(null);
                    }
                }
                priceHintTv.setVisibility(View.VISIBLE);
                priceHintTv.setText("Buyers will have to contact you and ask for price. No price will be listed on the product." +
                        "\n\nThis is NOT recommended as hiding price from buyers can lead to lower sales.");
                break;
        }
    }

    private void fetchItemDetails() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.all_please_wait);
        progressDialog.show();

        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        endpoints.fetchItemDetails(itemId).enqueue(new Callback<Item>() {
            @Override
            public void onResponse(Call<Item> call, Response<Item> response) {
                progressDialog.dismiss();
                item = response.body();
                if (item != null) {
                    nameTil.getEditText().setText(item.getName());
                    descTil.getEditText().setText(item.getDescription());
                    if (item.getMrp() != null) {
                        mrpTil.getEditText().setText(String.valueOf(item.getMrp()));
                    }
                    if (item.getSellingPrice() != null) {
                        sellingPriceTil.getEditText().setText(String.valueOf(item.getSellingPrice()));
                    }
                    selectedItemType = item.getType();
                    if (item.getType() == ITEM_PRODUCT) {
                        productRadioBtn.setChecked(true);
                        serviceRadioBtn.setChecked(false);
                        serviceRadioBtn.setEnabled(false);
                    } else {
                        productRadioBtn.setChecked(false);
                        productRadioBtn.setEnabled(false);
                        serviceRadioBtn.setChecked(true);
                    }
                    if (item.getPhotos() != null && !item.getPhotos().isEmpty()) {
                        GlideApp.with(NewItemActiv.this)
                                .load(item.getPhotos().get(0).getUrl())
                                .thumbnail(0.1F)
                                .into(photoIv);
                    }
                    selectedPriceOption = item.getPricingOption();
                    priceSpinner.setSelection(selectedPriceOption);
                    serviceDataList = item.getServiceDataList();
                    if (serviceDataList != null && serviceDataList.size() > 0) {
                        showCatalogue(item.getServiceDataList());
                    }
                    categoryId = item.getCategory().getId();
                    categoryTil.getEditText().setText(item.getCategory().getName());
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

    private void showCatalogue(@Nullable ArrayList<ServiceData> serviceDataList) {
        catalogueLinearLayout.removeAllViews();
        catalogueLinearLayout.invalidate();

        if (serviceDataList == null || serviceDataList.isEmpty()) {
            addNewService(0);
            addNewService(0);
        } else {
            for (int i = 0; i < serviceDataList.size(); i++) {
                final ServiceData serviceData = serviceDataList.get(i);
                addNewService(i, serviceData.getTitle(), serviceData.getPrice());
            }
        }

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
                if (catalogueLinearLayout.getChildCount() > 3) {
                    if (serviceDataList != null) {
                        serviceDataList.remove(serviceDataList.size() - 1);
                    }
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
        textInputLayout.getEditText().setInputType(TYPE_CLASS_TEXT | TYPE_TEXT_FLAG_CAP_WORDS);
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
        textInputLayout1.getEditText().setInputType(TYPE_CLASS_NUMBER | TYPE_NUMBER_FLAG_SIGNED);
        if (selectedPriceOption == Item.ITEM_PRICING_PAID) {
            textInputLayout1.getEditText().setText("");
            textInputLayout1.getEditText().setEnabled(true);
        } else {
            textInputLayout1.getEditText().setText("On Request");
            textInputLayout1.getEditText().setEnabled(false);
        }

        linearLayout.addView(textInputLayout);
        linearLayout.addView(textInputLayout1);
        catalogueLinearLayout.addView(linearLayout, pos);
    }

    private void addNewService(Integer pos, String title, Integer price) {
        final LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setWeightSum(10);

        final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 7);
        final TextInputLayout textInputLayout = new TextInputLayout(this);
        textInputLayout.setLayoutParams(lp);
        final EditText editText = new EditText(this);
        //editText.setLayoutParams(lp);
        textInputLayout.addView(editText);
        textInputLayout.getEditText().setInputType(TYPE_CLASS_TEXT | TYPE_TEXT_FLAG_CAP_WORDS);
        textInputLayout.setHint("Service Name");
        textInputLayout.getEditText().setText(title);

        final LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 3);
        final TextInputLayout textInputLayout1 = new TextInputLayout(this);
        textInputLayout1.setLayoutParams(lp1);
        textInputLayout1.setGravity(RIGHT);
        textInputLayout1.setHorizontalGravity(RIGHT);
        final EditText editText1 = new EditText(this);
        textInputLayout1.addView(editText1);
        //editText1.setLayoutParams(lp1);
        textInputLayout1.setHint("Price");
        textInputLayout1.getEditText().setInputType(TYPE_CLASS_NUMBER | TYPE_NUMBER_FLAG_SIGNED);
        if (selectedPriceOption == Item.ITEM_PRICING_PAID) {
            if (price != null && price >= 0) {
                textInputLayout1.getEditText().setText(String.valueOf(price));
            } else {
                textInputLayout1.getEditText().setText("");
            }
            textInputLayout1.getEditText().setEnabled(true);
        } else {
            textInputLayout1.getEditText().setText("On Request");
            textInputLayout1.getEditText().setEnabled(false);
        }

        linearLayout.addView(textInputLayout);
        linearLayout.addView(textInputLayout1);
        catalogueLinearLayout.addView(linearLayout, pos);
    }

    private ArrayList<ServiceData> updateServiceDataFromUi() {
        if (serviceDataList == null || serviceDataList.isEmpty()) {
            // creating new item
            serviceDataList = new ArrayList<>();
            int serviceCount = catalogueLinearLayout.getChildCount() - 1;
            for (int i = 1; i < serviceCount; i++) {
                final LinearLayout serviceLinearLayout = (LinearLayout) catalogueLinearLayout.getChildAt(i);
                final TextInputLayout nameTil = (TextInputLayout) serviceLinearLayout.getChildAt(0);
                final TextInputLayout priceTil = (TextInputLayout) serviceLinearLayout.getChildAt(1);

                final ServiceData serviceData
                        = new ServiceData();
                serviceData.setTitle(nameTil.getEditText().getText().toString());
                if (selectedPriceOption == ITEM_PRICING_PAID) {
                    serviceData.setPrice(Integer.parseInt(priceTil.getEditText().getText().toString()));
                } else {
                    serviceData.setPrice(null);
                }
                serviceDataList.add(serviceData);
            }
        } else {
            // editing old item
            int serviceCount = catalogueLinearLayout.getChildCount() - 1;
            for (int i = 1; i < serviceCount; i++) {
                final LinearLayout serviceLinearLayout = (LinearLayout) catalogueLinearLayout.getChildAt(i);
                final TextInputLayout nameTil = (TextInputLayout) serviceLinearLayout.getChildAt(0);
                final TextInputLayout priceTil = (TextInputLayout) serviceLinearLayout.getChildAt(1);

                ServiceData serviceData;
                if (i - 1 < serviceDataList.size()) {
                    serviceData = serviceDataList.get(i - 1);
                    serviceData.setTitle(nameTil.getEditText().getText().toString());
                    if (selectedPriceOption == ITEM_PRICING_PAID) {
                        serviceData.setPrice(Integer.parseInt(priceTil.getEditText().getText().toString()));
                    } else {
                        serviceData.setPrice(null);
                    }
                    serviceDataList.set(i - 1, serviceData);
                } else {
                    serviceData = new ServiceData();
                    serviceData.setTitle(nameTil.getEditText().getText().toString());
                    if (selectedPriceOption == ITEM_PRICING_PAID) {
                        serviceData.setPrice(Integer.parseInt(priceTil.getEditText().getText().toString()));
                    } else {
                        serviceData.setPrice(null);
                    }
                    serviceDataList.add(serviceData);
                }
            }
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
                startActivityForResult(CategoryChooserActiv.getIntent(NewItemActiv.this, selectedItemType), REQUEST_CODE_CATEGORY);
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

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.all_please_wait);
        progressDialog.show();

        HashMap<String, Object> params = new HashMap<>();

        params.put("seller", LubbleSharedPrefs.getInstance().getSellerId());
        params.put("type", selectedItemType);
        params.put("pricing_option", selectedPriceOption);
        params.put("name", nameTil.getEditText().getText().toString());
        params.put("description", descTil.getEditText().getText().toString());
        params.put("category_id", categoryId);
        params.put("approval_status", Item.ITEM_PENDING_APPROVAL);
        if (selectedItemType == ITEM_PRODUCT) {
            if (selectedPriceOption == ITEM_PRICING_PAID) {
                params.put("mrp", mrpTil.getEditText().getText().toString());
                params.put("selling_price", sellingPriceTil.getEditText().getText().toString());
            } else {
                params.put("mrp", null);
                params.put("selling_price", null);
            }
        } else {
            if (selectedPriceOption == ITEM_PRICING_PAID) {
                int minValueService = getMinValueService();
                params.put("starting_price", String.valueOf(minValueService));
            } else {
                params.put("starting_price", null);
            }
        }
        if (itemId == -1) {
            params.put("client_timestamp", System.currentTimeMillis());
        }
        if (selectedItemType == ITEM_SERVICE) {
            JSONArray serviceCatalog = new JSONArray();
            for (ServiceData serviceData : serviceDataList) {
                final JSONObject jsonObject = new JSONObject();
                try {
                    if (itemId != -1 && !TextUtils.isEmpty(serviceData.getId())) {
                        jsonObject.put("id", serviceData.getId());
                    }
                    jsonObject.put("title", serviceData.getTitle());
                    jsonObject.put("price", serviceData.getPrice() == null ? JSONObject.NULL : serviceData.getPrice());
                    serviceCatalog.put(jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            params.put("service_catalog", serviceCatalog);
        }
        RequestBody body = RequestBody.create(MEDIA_TYPE, new JSONObject(params).toString());

        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        final Call<Item> itemCall;
        if (itemId == -1) {
            itemCall = endpoints.uploadNewItem(body);
        } else {
            itemCall = endpoints.updateItem(itemId, body);
        }
        itemCall.enqueue(new Callback<Item>() {
            @Override
            public void onResponse(Call<Item> call, Response<Item> response) {
                progressDialog.dismiss();
                final Item item = response.body();
                if (item != null) {
                    if (picUri != null) {
                        Log.d(TAG, "OG file size: " + new File(picUri.getPath()).length() / 1024);
                        picUri = Uri.fromFile(compressImage(picUri.getPath()));
                        Log.d(TAG, "NEW file size: " + new File(picUri.getPath()).length() / 1024);
                        startService(new Intent(NewItemActiv.this, UploadFileService.class)
                                .putExtra(UploadFileService.EXTRA_FILE_NAME, "item_pic_" + System.currentTimeMillis() + ".jpg")
                                .putExtra(UploadFileService.EXTRA_FILE_URI, picUri)
                                .putExtra(UploadFileService.EXTRA_BUCKET, BUCKET_MARKETPLACE)
                                .putExtra(UploadFileService.EXTRA_UPLOAD_PATH, "marketplace/items/" + item.getId())
                                .setAction(UploadFileService.ACTION_UPLOAD));
                    }
                    Toast.makeText(NewItemActiv.this, "Submitted", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(NewItemActiv.this, R.string.all_try_again, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Item> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(NewItemActiv.this, R.string.check_internet, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onFailure: ");
            }
        });
    }

    private int getMinValueService() {
        int minValue = Integer.MAX_VALUE;
        for (ServiceData serviceData : serviceDataList) {
            if (serviceData.getPrice() < minValue) {
                minValue = serviceData.getPrice();
            }
        }
        return minValue;
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
        if (selectedItemType == ITEM_PRODUCT && selectedPriceOption == Item.ITEM_PRICING_PAID && !isValidString(mrpTil.getEditText().getText().toString())) {
            mrpTil.setError(getString(R.string.mrp_error));
            return false;
        } else {
            mrpTil.setError(null);
        }
        if (selectedItemType == ITEM_PRODUCT && selectedPriceOption == Item.ITEM_PRICING_PAID && !isValidString(sellingPriceTil.getEditText().getText().toString())) {
            sellingPriceTil.setError(getString(R.string.selling_price_error));
            return false;
        } else {
            sellingPriceTil.setError(null);
        }
        if (categoryId == -1) {
            Toast.makeText(this, R.string.no_category, Toast.LENGTH_SHORT).show();
            parentScrollView.smoothScrollTo(0, 0);
            return false;
        }
        if (picUri == null && itemId == -1) {
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_item_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Respond to the action bar's Up/Home button
                onBackPressed();
                return true;
            case R.id.action_help:
                openHelpBottomSheet();
                Analytics.triggerEvent(HELP_BTN_CLICKED, this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openHelpBottomSheet() {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.help_bottom_sheet, null);
        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();

        final TextView phoneTv = sheetView.findViewById(R.id.tv_phone_number);

        phoneTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Analytics.triggerEvent(HELP_PHONE_CLICKED, NewItemActiv.this);
                Intent sendIntent = new Intent("android.intent.action.MAIN");
                sendIntent.setAction(Intent.ACTION_VIEW);
                sendIntent.setPackage("com.whatsapp");
                String url = "https://api.whatsapp.com/send?phone=" + "+917676622668" + "&text=" + "Hi please help me with products";
                sendIntent.setData(Uri.parse(url));
                if (sendIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(sendIntent);
                } else {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:+916361686026"));
                    startActivity(intent);
                }
            }
        });

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
        NewItemActivPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
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
