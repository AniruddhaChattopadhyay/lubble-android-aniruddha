package in.lubble.app.marketplace;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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

    private ImageView sellerPicIv;
    private ProgressBar progressBar;
    private TextView sellerNameTv;
    private TextView sellerBioTv;
    private int sellerId;
    private RecyclerView recyclerView;
    private BigItemAdapter adapter;

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

        fetchSellerProfile();

        newItemTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewItemActiv.open(SellerDashActiv.this);
            }
        });
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
