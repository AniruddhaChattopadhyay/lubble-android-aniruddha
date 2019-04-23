package in.lubble.app.chat.books;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import in.lubble.app.BaseActivity;
import in.lubble.app.Constants;
import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.chat.books.airtable_pojo.AirtableBooksFields;
import in.lubble.app.chat.books.airtable_pojo.AirtableBooksRecord;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.ProfileData;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import in.lubble.app.referrals.ReferralActivity;
import okhttp3.RequestBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.HashMap;
import java.util.Map;

import static in.lubble.app.Constants.MEDIA_TYPE;
import static in.lubble.app.chat.books.MyBooksActivity.ARG_SELECT_BOOK;
import static in.lubble.app.chat.books.MyBooksActivity.SELECTED_BOOK_RECORD;
import static in.lubble.app.firebase.RealtimeDbHelper.getThisUserRef;

public class BookCheckoutActiv extends BaseActivity {

    private static final String TAG = "BookCheckoutActiv";
    private static final int REQUEST_MY_BOOK = 163;

    private static final String ARG_BOOK_DATA = "BookCheckoutActiv.ARG_BOOK_DATA";
    private static final String ARG_MY_BOOK_COUNT = "BookCheckoutActiv.ARG_MY_BOOK_COUNT";

    private ImageView selectedBookIv;
    private ImageView giveBookIv;
    private TextView bookTitleTv;
    private TextView giveBookTitleTv;
    private TextView bookAuthorTv;
    private TextView giveBookAuthorTv;
    private TextView giveBookChangeTv;
    private TextView addressTv;
    private TextView deliveryFeeTv;
    private TextView balanceCoinsTv;
    private TextView toPayTv;
    private TextView phoneTv;
    private TextView useCoinsTv;
    private LinearLayout addBookContainer;
    private RelativeLayout bookGiveContainer;
    private RelativeLayout addAddressContainer;
    private RelativeLayout timeAddressContainer;
    private Button addressBtn;
    private Button placeOrderBtn;
    private AirtableBooksRecord airtableBooksRecord;
    private int myBookCount = -1;
    private ValueEventListener addressValueListener;
    private String takenBookId;
    private String givenBookId;
    private static long DELIVERY_FEE = 100;

    public static void open(Context context, AirtableBooksRecord airtableBooksRecord, int myBooks) {
        final Intent intent = new Intent(context, BookCheckoutActiv.class);
        intent.putExtra(ARG_BOOK_DATA, airtableBooksRecord);
        intent.putExtra(ARG_MY_BOOK_COUNT, myBooks);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activ_book_checkout);

        Toolbar toolbar = findViewById(R.id.text_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Book Exchange");

        selectedBookIv = findViewById(R.id.iv_selected_book);
        bookTitleTv = findViewById(R.id.tv_book_title);
        bookAuthorTv = findViewById(R.id.tv_book_author);
        addBookContainer = findViewById(R.id.container_add_book);
        bookGiveContainer = findViewById(R.id.container_book_give);
        giveBookIv = findViewById(R.id.iv_give_selected_book);
        giveBookTitleTv = findViewById(R.id.tv_give_book_title);
        giveBookAuthorTv = findViewById(R.id.tv_give_book_author);
        giveBookChangeTv = findViewById(R.id.tv_give_change);
        addAddressContainer = findViewById(R.id.container_add_address);
        timeAddressContainer = findViewById(R.id.container_time_address);
        deliveryFeeTv = findViewById(R.id.tv_delivery_fee);
        balanceCoinsTv = findViewById(R.id.tv_balance_coins);
        toPayTv = findViewById(R.id.tv_to_pay);
        addressTv = findViewById(R.id.tv_addr);
        phoneTv = findViewById(R.id.tv_phone);
        addressBtn = findViewById(R.id.btn_address);
        useCoinsTv = findViewById(R.id.tv_use_coins);
        placeOrderBtn = findViewById(R.id.btn_place_order);

        if (!getIntent().hasExtra(ARG_BOOK_DATA)) {
            Log.e(TAG, "onCreate: ARG_BOOK_DATA missing");
            finish();
            return;
        }

        airtableBooksRecord = (AirtableBooksRecord) getIntent().getSerializableExtra(ARG_BOOK_DATA);
        myBookCount = getIntent().getIntExtra(ARG_MY_BOOK_COUNT, -1);

        takenBookId = airtableBooksRecord.getId();
        final AirtableBooksFields booksFields = airtableBooksRecord.getFields();
        GlideApp.with(this).load(booksFields.getPhoto()).diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(selectedBookIv);
        bookTitleTv.setText(booksFields.getTitle());
        bookAuthorTv.setText(booksFields.getAuthor());

        addBookContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myBookCount == 0) {
                    final Intent intent = new Intent(BookCheckoutActiv.this, BookSearchActiv.class);
                    intent.putExtra(ARG_SELECT_BOOK, true);
                    startActivityForResult(intent, REQUEST_MY_BOOK);
                } else {
                    final Intent intent = new Intent(BookCheckoutActiv.this, MyBooksActivity.class);
                    intent.putExtra(ARG_SELECT_BOOK, true);
                    startActivityForResult(intent, REQUEST_MY_BOOK);
                }
            }
        });

        giveBookChangeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(BookCheckoutActiv.this, MyBooksActivity.class);
                intent.putExtra(ARG_SELECT_BOOK, true);
                startActivityForResult(intent, REQUEST_MY_BOOK);
            }
        });

        addressBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddressChooserActiv.open(BookCheckoutActiv.this);
            }
        });

        DELIVERY_FEE = FirebaseRemoteConfig.getInstance().getLong(Constants.DELIVERY_FEE);
        deliveryFeeTv.setText(DELIVERY_FEE + " Coins");
        toPayTv.setText(DELIVERY_FEE + " Coins");
        toPayTv.setText(DELIVERY_FEE + " Coins");
        useCoinsTv.setText("use " + DELIVERY_FEE + " Coins");
    }

    @Override
    protected void onResume() {
        super.onResume();

        addressValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final ProfileData profileData = dataSnapshot.getValue(ProfileData.class);
                if (profileData != null) {
                    if (profileData.getProfileAddress() != null) {
                        addAddressContainer.setVisibility(View.GONE);
                        timeAddressContainer.setVisibility(View.VISIBLE);
                        addressTv.setText(profileData.getProfileAddress().getHouseNumber() + " " + profileData.getProfileAddress().getLocation());
                        balanceCoinsTv.setText(profileData.getCoins() + " Coins");

                        if (!TextUtils.isEmpty(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())) {
                            phoneTv.setText(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
                            setCtaToPlaceOrder(profileData);
                        } else if (!TextUtils.isEmpty(profileData.getPhone())) {
                            phoneTv.setText("+91 " + profileData.getPhone());
                            setCtaToPlaceOrder(profileData);
                        } else {
                            phoneTv.setText("");
                            setCtaToGetPhone();
                        }
                    } else {
                        addAddressContainer.setVisibility(View.VISIBLE);
                        timeAddressContainer.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        RealtimeDbHelper.getThisUserRef().addValueEventListener(addressValueListener);
    }

    private void setCtaToPlaceOrder(final ProfileData profileData) {
        placeOrderBtn.setText("Place Order");
        placeOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(profileData.getPhone()) && profileData.getProfileAddress() != null && !TextUtils.isEmpty(profileData.getProfileAddress().getHouseNumber())
                        && takenBookId != null && givenBookId != null) {
                    try {
                        uploadNewOrder(profileData);
                    } catch (JSONException e) {
                        Crashlytics.logException(e);
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(BookCheckoutActiv.this, "Please enter all info", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void uploadNewOrder(ProfileData profileData) throws JSONException {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Placing Order");
        progressDialog.setMessage(getString(R.string.all_please_wait));
        progressDialog.setCancelable(false);
        progressDialog.show();

        HashMap<String, Object> params = new HashMap<>();
        HashMap<String, Object> fieldParams = new HashMap<>();
        fieldParams.put("User ID", FirebaseAuth.getInstance().getUid());
        final JSONArray givenArray = new JSONArray();
        givenArray.put(givenBookId);
        fieldParams.put("Given Book ID", givenArray);
        final JSONArray takenArray = new JSONArray();
        takenArray.put(takenBookId);
        fieldParams.put("Taken Book ID", takenArray);
        fieldParams.put("Coins Used", DELIVERY_FEE);
        fieldParams.put("House Number", profileData.getProfileAddress().getHouseNumber());
        fieldParams.put("Location", profileData.getProfileAddress().getLocation());
        fieldParams.put("Latitude", profileData.getProfileAddress().getLatitude());
        fieldParams.put("Longitude", profileData.getProfileAddress().getLongitude());
        fieldParams.put("Landmark", profileData.getProfileAddress().getLandmark());
        String phone;
        if (!TextUtils.isEmpty(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())) {
            phone = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        } else {
            phone = profileData.getPhone();
        }
        fieldParams.put("Phone", phone);
        params.put("fields", fieldParams);
        RequestBody body = RequestBody.create(MEDIA_TYPE, new JSONObject(params).toString());

        String url = "https://api.airtable.com/v0/appbhSWmy7ZS6UeTy/Orders";

        final Endpoints endpoints = ServiceGenerator.createAirtableService(Endpoints.class);
        endpoints.uploadNewOrder(url, body).enqueue(new Callback<AirtableBooksRecord>() {
            @Override
            public void onResponse(Call<AirtableBooksRecord> call, Response<AirtableBooksRecord> response) {
                final AirtableBooksRecord airtableData = response.body();
                if (response.isSuccessful() && airtableData != null && airtableData.getId() != null && !isFinishing()) {
                    deductCoins(progressDialog);
                } else {
                    if (!isFinishing()) {
                        progressDialog.dismiss();
                        Toast.makeText(BookCheckoutActiv.this, R.string.all_try_again, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<AirtableBooksRecord> call, Throwable t) {
                if (!isFinishing()) {
                    progressDialog.dismiss();
                    Toast.makeText(BookCheckoutActiv.this, R.string.check_internet, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onFailure: ");
                }
            }
        });

    }

    private void deductCoins(final ProgressDialog progressDialog) {
        getThisUserRef().runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                ProfileData profileData = mutableData.getValue(ProfileData.class);
                if (profileData == null) {
                    return Transaction.success(mutableData);
                }

                if (profileData.getCoins() >= DELIVERY_FEE) {
                    // Set value and report transaction success
                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put("coins", profileData.getCoins() - DELIVERY_FEE);
                    getThisUserRef().updateChildren(childUpdates);
                    return Transaction.success(mutableData);
                } else {
                    return Transaction.abort();
                }
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                // Transaction completed
                if (committed && !isFinishing()) {
                    progressDialog.dismiss();
                    OrderDoneActiv.open(BookCheckoutActiv.this);
                    finish();
                } else if (!isFinishing()) {
                    progressDialog.dismiss();
                    Toast.makeText(BookCheckoutActiv.this, R.string.all_try_again, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setCtaToGetPhone() {
        useCoinsTv.setVisibility(View.VISIBLE);
        placeOrderBtn.setText("Update Contact Number");
        placeOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PhoneBottomSheet phoneBottomSheet = PhoneBottomSheet.newInstance();
                phoneBottomSheet.show(getSupportFragmentManager(), null);
            }
        });
    }

    public void earnMore(View view) {
        ReferralActivity.open(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MY_BOOK && resultCode == Activity.RESULT_OK) {
            bookGiveContainer.setVisibility(View.VISIBLE);
            addBookContainer.setVisibility(View.GONE);

            AirtableBooksRecord airtableBooksRecord = (AirtableBooksRecord) data.getSerializableExtra(SELECTED_BOOK_RECORD);
            final AirtableBooksFields bookFields = airtableBooksRecord.getFields();
            GlideApp.with(this).load(bookFields.getPhoto()).diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(giveBookIv);
            giveBookTitleTv.setText(bookFields.getTitle());
            giveBookAuthorTv.setText(bookFields.getAuthor());
            givenBookId = airtableBooksRecord.getId();
        }
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
    protected void onPause() {
        super.onPause();
        if (addressValueListener != null) {
            RealtimeDbHelper.getThisUserRef().removeEventListener(addressValueListener);
        }
    }
}
