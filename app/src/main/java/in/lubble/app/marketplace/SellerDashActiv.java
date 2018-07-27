package in.lubble.app.marketplace;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.models.marketplace.Item;
import in.lubble.app.models.marketplace.SellerData;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SellerDashActiv extends AppCompatActivity {

    private static final String TAG = "SellerDashActiv";
    private static final String PARAM_SELLER_ID = "PARAM_SELLER_ID";
    public static final String ACTION_IMG_DONE = "ACTION_IMG_DONE";
    public static final String EXTRA_IMG_TYPE = "EXTRA_IMG_TYPE";
    public static final String EXTRA_IMG_ID = "EXTRA_IMG_ID";
    public static final String EXTRA_IMG_URL = "EXTRA_IMG_URL";

    private ImageView sellerPicIv;
    private ProgressBar progressBar;
    private TextView sellerNameTv;
    private TextView sellerBioTv;
    private int sellerId;
    private RecyclerView recyclerView;
    private BigItemAdapter adapter;
    private BroadcastReceiver photoUploadReceiver;

    public static void open(Context context, int sellerId) {
        final Intent intent = new Intent(context, SellerDashActiv.class);
        intent.putExtra(PARAM_SELLER_ID, sellerId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_dash);

        Analytics.triggerScreenEvent(this, this.getClass());

        Toolbar toolbar = findViewById(R.id.text_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sellerPicIv = findViewById(R.id.iv_seller_pic);
        progressBar = findViewById(R.id.progress_bar);
        sellerNameTv = findViewById(R.id.tv_seller_name);
        sellerBioTv = findViewById(R.id.tv_seller_bio);
        final TextView newItemTv = findViewById(R.id.tv_new_item);
        recyclerView = findViewById(R.id.rv_items);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new BigItemAdapter(GlideApp.with(this));
        recyclerView.setAdapter(adapter);

        sellerId = getIntent().getIntExtra(PARAM_SELLER_ID, -1);
        if (sellerId == -1) {
            throw new IllegalArgumentException("no seller ID bruh");
        }

        newItemTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewItemActiv.open(SellerDashActiv.this);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchSellerProfile();
        if (adapter != null) {
            adapter.clear();
        }
        registerMediaUploadCallback();
    }

    private void registerMediaUploadCallback() {
        photoUploadReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //todo hideProgressDialog();
                if (intent.getAction() != null) {
                    switch (intent.getAction()) {
                        case ACTION_IMG_DONE:
                            final String imgType = intent.getStringExtra(EXTRA_IMG_TYPE);
                            final String imgId = intent.getStringExtra(EXTRA_IMG_ID);
                            final String imgUrl = intent.getStringExtra(EXTRA_IMG_URL);
                            if (!TextUtils.isEmpty(imgType) && !TextUtils.isEmpty(imgId)) {
                                if (imgType.equalsIgnoreCase("500")) {
                                    // ITEM
                                    adapter.updateItemPic(Integer.parseInt(imgId), imgUrl);
                                } else if (imgType.equalsIgnoreCase("501")) {
                                    // SELLER
                                    GlideApp.with(SellerDashActiv.this)
                                            .load(imgUrl)
                                            .circleCrop()
                                            .into(sellerPicIv);
                                }
                            }
                            break;
                    }
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(photoUploadReceiver, new IntentFilter(ACTION_IMG_DONE));
    }

    private void fetchSellerProfile() {
        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        endpoints.fetchSellerProfile(sellerId).enqueue(new Callback<SellerData>() {
            @Override
            public void onResponse(Call<SellerData> call, Response<SellerData> response) {
                progressBar.setVisibility(View.GONE);
                final SellerData sellerData = response.body();
                if (sellerData != null) {
                    sellerNameTv.setText(sellerData.getName());
                    sellerBioTv.setText(sellerData.getBio());

                    GlideApp.with(SellerDashActiv.this)
                            .load(sellerData.getPhotoUrl())
                            .circleCrop()
                            .into(sellerPicIv);

                    setTitle(sellerData.getName());

                    for (Item item : sellerData.getItemList()) {
                        adapter.addData(item);
                    }
                } else {
                    Crashlytics.logException(new IllegalArgumentException("seller profile null for seller id: " + sellerId));
                    Toast.makeText(SellerDashActiv.this, R.string.all_try_again, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SellerData> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SellerDashActiv.this, R.string.check_internet, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (photoUploadReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(photoUploadReceiver);
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

}
