package in.lubble.app.marketplace;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.models.marketplace.Category;
import in.lubble.app.models.marketplace.Item;
import in.lubble.app.models.marketplace.SellerData;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ItemListActiv extends AppCompatActivity {

    private static final String TAG = "SellerDashActiv";
    private static final String PARAM_IS_SELLER = "PARAM_IS_SELLER";
    private static final String PARAM_ID = "PARAM_ID";

    private ImageView sellerPicIv;
    private TextView sellerNameTv;
    private TextView sellerBioTv;
    private boolean isSeller;
    private int sellerId;
    private RecyclerView recyclerView;
    private BigItemAdapter adapter;

    public static void open(Context context, boolean isSeller, int id) {
        final Intent intent = new Intent(context, ItemListActiv.class);
        intent.putExtra(PARAM_IS_SELLER, isSeller);
        intent.putExtra(PARAM_ID, id);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        Analytics.triggerScreenEvent(this, this.getClass());

        Toolbar toolbar = findViewById(R.id.text_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sellerPicIv = findViewById(R.id.iv_seller_pic);
        sellerNameTv = findViewById(R.id.tv_seller_name);
        sellerBioTv = findViewById(R.id.tv_seller_bio);
        recyclerView = findViewById(R.id.rv_items);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new BigItemAdapter(GlideApp.with(this));
        recyclerView.setAdapter(adapter);

        isSeller = getIntent().getBooleanExtra(PARAM_IS_SELLER, false);
        sellerId = getIntent().getIntExtra(PARAM_ID, -1);

        if (sellerId == -1 || !getIntent().hasExtra(PARAM_IS_SELLER)) {
            throw new IllegalArgumentException("no seller ID bruh");
        }

        if (!isSeller) {
            sellerPicIv.setVisibility(View.GONE);
            sellerBioTv.setVisibility(View.GONE);
            fetchCategoryItems();
        } else {
            fetchSellerItems();
        }

    }

    private void fetchCategoryItems() {
        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        endpoints.fetchCategoryItems(sellerId).enqueue(new Callback<Category>() {
            @Override
            public void onResponse(Call<Category> call, Response<Category> response) {
                final Category categoryData = response.body();
                if (categoryData != null) {
                    sellerNameTv.setText(categoryData.getName());
                    //sellerBioTv.setText(categoryData.getBio());

                    /*GlideApp.with(ItemListActiv.this)
                            .load(categoryData.getPhotoUrl())
                            .circleCrop()
                            .into(sellerPicIv);*/

                    setTitle(categoryData.getName());

                    if (categoryData.getItems() != null) {
                        for (Item item : categoryData.getItems()) {
                            adapter.addData(item);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<Category> call, Throwable t) {
                Log.e(TAG, "onFailure: ");
            }
        });
    }

    private void fetchSellerItems() {
        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        endpoints.fetchSellerItems(sellerId).enqueue(new Callback<SellerData>() {
            @Override
            public void onResponse(Call<SellerData> call, Response<SellerData> response) {
                final SellerData sellerData = response.body();
                if (sellerData != null) {
                    sellerNameTv.setText(sellerData.getName());
                    sellerBioTv.setText(sellerData.getBio());

                    GlideApp.with(ItemListActiv.this)
                            .load(sellerData.getPhotoUrl())
                            .circleCrop()
                            .into(sellerPicIv);

                    setTitle(sellerData.getName());

                    if (sellerData.getItemList() != null) {
                        for (Item item : sellerData.getItemList()) {
                            adapter.addData(item);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<SellerData> call, Throwable t) {
                Log.e(TAG, "onFailure: ");
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
