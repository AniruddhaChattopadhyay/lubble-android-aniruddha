package in.lubble.app.marketplace;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crashlytics.android.Crashlytics;
import com.google.android.material.button.MaterialButton;

import in.lubble.app.BaseActivity;
import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.models.marketplace.Category;
import in.lubble.app.models.marketplace.Item;
import in.lubble.app.models.marketplace.SellerData;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import in.lubble.app.profile.DmIntroBottomSheet;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static in.lubble.app.analytics.AnalyticsEvents.RECOMMEND_BTN_CLICK;

public class ItemListActiv extends BaseActivity {

    private static final String TAG = "SellerDashActiv";
    private static final String PARAM_IS_SELLER = "PARAM_IS_SELLER";
    private static final String PARAM_ID = "PARAM_ID";
    private static final String PARAM_SELLER_NAME = "PARAM_SELLER_NAME";

    private ImageView sellerPicIv;
    private ProgressBar progressBar;
    private TextView sellerNameTv, sellerBioTv;
    private MaterialButton msgBtn;
    private boolean isSeller;
    private int sellerId;
    @Nullable
    private String sellerUniqueName = null;
    private RecyclerView recyclerView;
    private TextView noItemsHintTv;
    private BigItemAdapter adapter;
    private TextView recommendationCountTv;
    private RelativeLayout sellerActionContainer;
    private LinearLayout shareContainer,recommendContainer;
    private ImageView recommendIv;
    private TextView recommendHintTV;
    private boolean isRecommended;
    private long recommendationCount = 0;

    public static void open(Context context, boolean isSeller, int id) {
        final Intent intent = new Intent(context, ItemListActiv.class);
        intent.putExtra(PARAM_IS_SELLER, isSeller);
        intent.putExtra(PARAM_ID, id);
        context.startActivity(intent);
    }

    public static void open(Context context, boolean isSeller, String sellerUniqueName) {
        final Intent intent = new Intent(context, ItemListActiv.class);
        intent.putExtra(PARAM_IS_SELLER, isSeller);
        intent.putExtra(PARAM_SELLER_NAME, sellerUniqueName);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        Toolbar toolbar = findViewById(R.id.text_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sellerPicIv = findViewById(R.id.iv_seller_pic);
        progressBar = findViewById(R.id.progress_bar);
        sellerNameTv = findViewById(R.id.tv_seller_name);
        sellerBioTv = findViewById(R.id.tv_seller_bio);
        msgBtn = findViewById(R.id.btn_msg);
        recyclerView = findViewById(R.id.rv_items);
        noItemsHintTv = findViewById(R.id.tv_no_items_hint);
        recommendIv = findViewById(R.id.iv_recommend);
        recommendHintTV = findViewById(R.id.tv_recommend_hint);
        recommendContainer = findViewById(R.id.container_recommend_btn);
        recommendationCountTv = findViewById(R.id.tv_recommendation_count);
        sellerActionContainer = findViewById(R.id.container_action);
        shareContainer = findViewById(R.id.container_seller_share);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new BigItemAdapter(GlideApp.with(this), false);
        recyclerView.setAdapter(adapter);


        isSeller = getIntent().getBooleanExtra(PARAM_IS_SELLER, false);
        sellerId = getIntent().getIntExtra(PARAM_ID, -1);
        sellerUniqueName = getIntent().getStringExtra(PARAM_SELLER_NAME);

        if (sellerId == -1 && TextUtils.isEmpty(sellerUniqueName)) {
            throw new IllegalArgumentException("no seller ID bruh");
        }

        final Bundle attrs = new Bundle();
        attrs.putBoolean("is_seller", isSeller);
        attrs.putInt("seller_id", sellerId);
        attrs.putString("seller_name", sellerUniqueName);
        Analytics.triggerScreenEvent(this, this.getClass());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isSeller) {
            msgBtn.setVisibility(View.GONE);
            sellerActionContainer.setVisibility(View.GONE);
            sellerBioTv.setVisibility(View.GONE);
            recommendationCountTv.setVisibility(View.GONE);
            fetchCategoryItems();
        } else {
            fetchSellerItems();
            msgBtn.setVisibility(View.VISIBLE);
            sellerActionContainer.setVisibility(View.VISIBLE);
            recommendationCountTv.setVisibility(View.VISIBLE);
        }
    }

    private void updateRecommendation() {
        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        final Call<RatingData> call;
        if (isRecommended) {
            call = endpoints.deleteRecommendation(sellerId);
        } else {
            call = endpoints.uploadRecommendation(sellerId);
        }
        call.enqueue(new Callback<RatingData>() {
            @Override
            public void onResponse(Call<RatingData> call, Response<RatingData> response) {
                if (response.isSuccessful()) {
                    isRecommended = !isRecommended;
                    updateRecommendContainer();
                    if (isRecommended) {
                        recommendationCountTv.setText(++recommendationCount + " recommendations");
                    } else {
                        recommendationCountTv.setText(--recommendationCount + " recommendations");
                    }
                }
            }

            @Override
            public void onFailure(Call<RatingData> call, Throwable t) {

            }
        });
    }

    private void updateRecommendContainer() {
        if (isRecommended) {
            recommendIv.setImageResource(R.drawable.ic_favorite_24dp);
            recommendHintTV.setText("Recommended");
        } else {
            recommendIv.setImageResource(R.drawable.ic_favorite_border_24dp);
            recommendHintTV.setText("Recommend");
        }
    }

    private void fetchCategoryItems() {
        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        endpoints.fetchCategoryItems(sellerId).enqueue(new Callback<Category>() {
            @Override
            public void onResponse(Call<Category> call, Response<Category> response) {
                progressBar.setVisibility(View.GONE);
                final Category categoryData = response.body();
                if (categoryData != null) {
                    sellerNameTv.setText(categoryData.getName());

                    GlideApp.with(ItemListActiv.this)
                            .load(categoryData.getIcon())
                            .circleCrop()
                            .into(sellerPicIv);

                    sellerPicIv.setBackgroundResource(R.drawable.circle);

                    setTitle(categoryData.getName());

                    if (categoryData.getItems() != null && !categoryData.getItems().isEmpty()) {
                        recyclerView.setVisibility(View.VISIBLE);
                        noItemsHintTv.setVisibility(View.GONE);
                        adapter.clear();
                        for (Item item : categoryData.getItems()) {
                            adapter.addData(item);
                        }
                    } else {
                        recyclerView.setVisibility(View.GONE);
                        noItemsHintTv.setVisibility(View.VISIBLE);
                    }
                } else {
                    Crashlytics.logException(new IllegalArgumentException("Category null for cat id: " + sellerId));
                    Toast.makeText(ItemListActiv.this, R.string.all_try_again, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Category> call, Throwable t) {
                Log.e(TAG, "onFailure: ");
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ItemListActiv.this, R.string.check_internet, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchSellerItems() {
        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        final Call<SellerData> sellerDataCall;
        if (sellerId != -1) {
            sellerDataCall = endpoints.fetchSellerItems(sellerId);
        } else {
            sellerDataCall = endpoints.fetchSellerItems(sellerUniqueName);
        }
        sellerDataCall.enqueue(new Callback<SellerData>() {
            @Override
            public void onResponse(Call<SellerData> call, Response<SellerData> response) {
                progressBar.setVisibility(View.GONE);
                final SellerData sellerData = response.body();
                if (sellerData != null) {
                    sellerId = sellerData.getId();
                    sellerNameTv.setText(sellerData.getName());
                    sellerBioTv.setText(sellerData.getBio());

                    GlideApp.with(ItemListActiv.this)
                            .load(sellerData.getPhotoUrl())
                            .circleCrop()
                            .into(sellerPicIv);

                    setTitle(sellerData.getName());

                    if (sellerData.getItemList() != null) {
                        adapter.clear();
                        for (Item item : sellerData.getItemList()) {
                            adapter.addData(item);
                        }
                    }
                    recommendationCount = sellerData.getRecommendationCount();
                    recommendationCountTv.setText(recommendationCount + " recommendations");
                    isRecommended = sellerData.getIsRecommended();
                    updateRecommendContainer();

                    msgBtn.setEnabled(true);
                    msgBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DmIntroBottomSheet.newInstance(String.valueOf(sellerId), sellerData.getName(), sellerData.getPhotoUrl(), sellerData.getPhone())
                                    .show(getSupportFragmentManager(), null);
                        }
                    });

                    recommendContainer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final Bundle bundle = new Bundle();
                            bundle.putInt("seller_id", sellerId);
                            Analytics.triggerEvent(RECOMMEND_BTN_CLICK, bundle, ItemListActiv.this);
                            updateRecommendation();
                        }
                    });
                    shareContainer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!TextUtils.isEmpty(sellerData.getShareLink())) {
                                Analytics.triggerEvent(AnalyticsEvents.SHARE_CATALOGUE, ItemListActiv.this);
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("text/plain");
                                intent.putExtra(Intent.EXTRA_TEXT, "Hey! Look at this amazing listing I found: " + sellerData.getShareLink());
                                startActivity(Intent.createChooser(intent, "Share"));
                            }
                        }
                    });
                } else {
                    Crashlytics.logException(new IllegalArgumentException("sellerData null for seller id: " + sellerId));
                    Toast.makeText(ItemListActiv.this, R.string.all_try_again, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SellerData> call, Throwable t) {
                Log.e(TAG, "onFailure: ");
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ItemListActiv.this, R.string.check_internet, Toast.LENGTH_SHORT).show();
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
