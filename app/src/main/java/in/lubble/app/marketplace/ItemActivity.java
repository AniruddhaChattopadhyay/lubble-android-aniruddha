package in.lubble.app.marketplace;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import in.lubble.app.BaseActivity;
import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.chat.ChatActivity;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.ProfileInfo;
import in.lubble.app.models.marketplace.Item;
import in.lubble.app.models.marketplace.PhotoData;
import in.lubble.app.models.marketplace.SellerData;
import in.lubble.app.models.marketplace.ServiceData;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import in.lubble.app.profile.DmIntroBottomSheet;
import in.lubble.app.utils.FullScreenImageActivity;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static in.lubble.app.Constants.MEDIA_TYPE;
import static in.lubble.app.analytics.AnalyticsEvents.CALL_BTN_CLICKED;
import static in.lubble.app.analytics.AnalyticsEvents.MPLACE_CHAT_BTN_CLICKED;
import static in.lubble.app.analytics.AnalyticsEvents.VISIT_SHOP_CLICK;
import static in.lubble.app.firebase.RealtimeDbHelper.getUserInfoRef;
import static in.lubble.app.models.marketplace.Item.ITEM_APPROVED;

public class ItemActivity extends BaseActivity {

    private static final String TAG = "ItemActivity";
    private static final String PARAM_ITEM_ID = "PARAM_ITEM_ID";

    private ImageView imageIv;
    private TextView savingPercentTv;
    private ProgressBar itemProgressBar;
    private ProgressBar progressBar;
    private TextView titleTv;
    private TextView priceTv;
    private TextView mrpTv;
    private RelativeLayout itemPvtInfoLayout;
    private TextView editItemTv;
    private TextView viewCountTv;
    private ImageView approvalIconIv;
    private TextView approvalStatusTv;
    private TextView descTv;
    private RelativeLayout itemShareContainer;
    private TextView serviceHintTv;
    private RecyclerView serviceRv;
    private RelativeLayout userReviewContainer;
    private ImageView ratingAccountIv;
    private TextView ratingHintTv;
    private RatingBar ratingBar;
    private TextView myReviewTv;
    private EditText reviewEt;
    private TextView submitRatingTv;
    private ProgressBar submitRatingProgressBar;
    private RecyclerView reviewsRecyclerView;
    private TextView avgRatingTv;
    private TextView ratingCountTv;
    private TextView sellerHeaderTv;
    private RatingBar avgRatingBar;
    private ImageView sellerIv;
    private TextView sellerNameTv;
    private TextView sellerBioTv;
    private RecyclerView sellerItemsRv;
    private TextView visitShopTv;
    private Button chatBtn;
    private TextView chatHintTv;
    private ValueEventListener sellerDmIdValueEventListener;
    private Query sellerDmQuery;
    @Nullable
    private String dmId = null;

    private int itemId;
    private TextView recommendationCountTv;
    private LinearLayout sellerShareContainer;
    private LinearLayout recommendBtnContainer;
    private ImageView recommendIv;
    private TextView recommendHintTV;
    private boolean isRecommended;
    private long recommendationCount = 0;
    private ServiceCatalogAdapter serviceCatalogAdapter;

    private RelativeLayout dealPriceContainer;
    private RelativeLayout normalPriceContainer;
    private TextView dealMrpTv;
    private TextView dealSellingPriceTv;
    private TextView dealPriceTv;
    private TextView dealSavingsTv;
    private TextView normalSavingsTv;
    private TextView serviceDealTv;

    public static Intent getIntent(Context context, int itemId) {
        final Intent intent = new Intent(context, ItemActivity.class);
        intent.putExtra(PARAM_ITEM_ID, itemId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        itemProgressBar = findViewById(R.id.item_progress_bar);
        progressBar = findViewById(R.id.progress_bar);
        imageIv = findViewById(R.id.iv_item_image);
        savingPercentTv = findViewById(R.id.tv_saving_text);
        titleTv = findViewById(R.id.tv_item_title);

        dealPriceContainer = findViewById(R.id.container_deal_price);
        normalPriceContainer = findViewById(R.id.container_normal_price);
        dealMrpTv = findViewById(R.id.tv_deal_mrp);
        dealSellingPriceTv = findViewById(R.id.tv_deal_selling_price);
        dealPriceTv = findViewById(R.id.tv_deal_price);
        dealSavingsTv = findViewById(R.id.tv_deal_savings);

        priceTv = findViewById(R.id.tv_price);
        mrpTv = findViewById(R.id.tv_mrp);
        normalSavingsTv = findViewById(R.id.tv_normal_savings);
        serviceDealTv = findViewById(R.id.tv_service_deal);

        itemPvtInfoLayout = findViewById(R.id.relativelayout_item_pvt_info);
        editItemTv = findViewById(R.id.tv_edit_item);
        viewCountTv = findViewById(R.id.tv_view_count);
        approvalIconIv = findViewById(R.id.iv_approval_icon);
        approvalStatusTv = findViewById(R.id.tv_approval_status);
        chatBtn = findViewById(R.id.btn_chat);
        chatHintTv = findViewById(R.id.tv_chat_seller_hint);
        descTv = findViewById(R.id.tv_item_desc);
        itemShareContainer = findViewById(R.id.container_item_share);
        serviceHintTv = findViewById(R.id.tv_service_catalog_hint);
        serviceRv = findViewById(R.id.rv_service_catalog);

        userReviewContainer = findViewById(R.id.container_user_review);
        ratingAccountIv = findViewById(R.id.iv_account);
        ratingHintTv = findViewById(R.id.tv_rate_hint);
        ratingBar = findViewById(R.id.ratingbar);
        myReviewTv = findViewById(R.id.tv_my_review);
        reviewEt = findViewById(R.id.et_review);
        submitRatingTv = findViewById(R.id.tv_rating_submit);
        submitRatingProgressBar = findViewById(R.id.progressBar_rating_submit);
        reviewsRecyclerView = findViewById(R.id.rv_reviews);
        avgRatingTv = findViewById(R.id.tv_avg_rating);
        ratingCountTv = findViewById(R.id.tv_rating_count);
        avgRatingBar = findViewById(R.id.ratingbar_avg);

        sellerHeaderTv = findViewById(R.id.tv_seller_header);
        sellerIv = findViewById(R.id.iv_seller_pic);
        sellerNameTv = findViewById(R.id.tv_seller_name);
        sellerBioTv = findViewById(R.id.tv_seller_bio);
        recommendIv = findViewById(R.id.iv_recommend);
        recommendHintTV = findViewById(R.id.tv_recommend_hint);
        recommendBtnContainer = findViewById(R.id.container_recommend_btn);
        recommendationCountTv = findViewById(R.id.tv_recommendation_count);
        sellerShareContainer = findViewById(R.id.container_seller_share);
        sellerItemsRv = findViewById(R.id.rv_items);
        visitShopTv = findViewById(R.id.tv_visit_shop);

        mrpTv.setPaintFlags(mrpTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        dealMrpTv.setPaintFlags(dealMrpTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        dealSellingPriceTv.setPaintFlags(dealSellingPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        Toolbar toolbar = findViewById(R.id.text_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        Intent intent = getIntent();
        Uri data = intent.getData();
        if (data != null) {
            try {
                itemId = Integer.parseInt(data.getLastPathSegment());
            } catch (Exception e) {
                e.printStackTrace();
                FirebaseCrashlytics.getInstance().recordException(e);
                finish();
            }
        } else {
            itemId = intent.getIntExtra(PARAM_ITEM_ID, -1);
        }
        if (itemId == -1) {
            throw new IllegalArgumentException("No ITEM ID passed");
        }

        setDpForRating();
        handleNewRatings();

        Analytics.triggerScreenEvent(this, this.getClass());
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchItemDetails();
    }

    private void handleNewRatings() {
        ratingBar.setMax(5);
        ratingBar.setStepSize(1);
        ratingBar.setNumStars(5);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(final RatingBar ratingBar, float rating, boolean fromUser) {
                submitRatingTv.setTextColor(ContextCompat.getColor(ItemActivity.this, R.color.colorAccent));
                submitRatingTv.setText("SUBMIT");
                submitRatingTv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        uploadRating(ratingBar.getRating());
                    }
                });
            }
        });
    }

    private void uploadRating(float rating) {

        submitRatingTv.setVisibility(View.INVISIBLE);
        submitRatingProgressBar.setVisibility(View.VISIBLE);
        HashMap<String, Object> params = new HashMap<>();

        params.put("rating", Math.round(rating));

        final JSONObject jsonObject = new JSONObject(params);

        RequestBody body = RequestBody.create(MEDIA_TYPE, jsonObject.toString());
        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        endpoints.uploadNewRating(itemId, body).enqueue(new Callback<RatingData>() {
            @Override
            public void onResponse(Call<RatingData> call, Response<RatingData> response) {
                submitRatingTv.setVisibility(View.VISIBLE);
                submitRatingProgressBar.setVisibility(View.GONE);
                final RatingData ratingData = response.body();
                if (response.isSuccessful() && ratingData != null) {
                    showReviewLayout(ratingData.getRatingId());
                } else {
                    Toast.makeText(ItemActivity.this, R.string.all_try_again, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RatingData> call, Throwable t) {
                submitRatingTv.setVisibility(View.VISIBLE);
                submitRatingProgressBar.setVisibility(View.GONE);
                Toast.makeText(ItemActivity.this, R.string.check_internet, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showReviewLayout(final int ratingId) {
        ratingBar.setVisibility(View.GONE);
        reviewEt.setVisibility(View.VISIBLE);
        submitRatingTv.setText("FINISH");
        submitRatingTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadReview(ratingId);
            }
        });
    }

    private void uploadReview(int ratingId) {
        submitRatingTv.setVisibility(View.INVISIBLE);
        submitRatingProgressBar.setVisibility(View.VISIBLE);
        HashMap<String, Object> params = new HashMap<>();

        params.put("review", reviewEt.getText().toString().trim());

        final JSONObject jsonObject = new JSONObject(params);

        RequestBody body = RequestBody.create(MEDIA_TYPE, jsonObject.toString());
        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        endpoints.updateRating(ratingId, body).enqueue(new Callback<RatingData>() {
            @Override
            public void onResponse(Call<RatingData> call, Response<RatingData> response) {
                submitRatingTv.setVisibility(View.VISIBLE);
                submitRatingProgressBar.setVisibility(View.GONE);
                final RatingData ratingData = response.body();
                if (response.isSuccessful() && ratingData != null) {
                    showMyRatingLayout(ratingData);
                } else {
                    Toast.makeText(ItemActivity.this, R.string.all_try_again, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RatingData> call, Throwable t) {
                submitRatingTv.setVisibility(View.VISIBLE);
                submitRatingProgressBar.setVisibility(View.GONE);
                Toast.makeText(ItemActivity.this, R.string.check_internet, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showMyRatingLayout(RatingData ratingData) {
        ratingBar.setVisibility(View.VISIBLE);
        submitRatingTv.setVisibility(View.GONE);
        reviewEt.setVisibility(View.GONE);
        submitRatingProgressBar.setVisibility(View.GONE);
        if (!TextUtils.isEmpty(ratingData.getReview())) {
            myReviewTv.setVisibility(View.VISIBLE);
            myReviewTv.setText(ratingData.getReview());
        } else {
            myReviewTv.setVisibility(View.GONE);
        }
        ratingHintTv.setText("Your Rating");
        ratingBar.setIsIndicator(true);
        ratingBar.setRating(ratingData.getStarRating());
    }

    private void fetchItemDetails() {
        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        endpoints.fetchItemDetails(itemId).enqueue(new Callback<Item>() {
            @Override
            public void onResponse(Call<Item> call, Response<Item> response) {
                final Item item = response.body();
                itemProgressBar.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                if (item != null && !isFinishing()) {
                    titleTv.setText(item.getName());

                    if (item.getPricingOption() == Item.ITEM_PRICING_PAID) {
                        if (item.getType() == Item.ITEM_SERVICE) {
                            final Integer startingPrice = item.getStartingPrice() == null ? item.getSellingPrice() : item.getStartingPrice();
                            if (startingPrice < 0) {
                                priceTv.setVisibility(View.GONE);
                                mrpTv.setVisibility(View.GONE);
                            } else {
                                priceTv.setText("₹" + startingPrice + " onwards");
                                mrpTv.setVisibility(View.GONE);
                            }
                            sellerHeaderTv.setText("Service Provider");
                        } else {
                            // PRODUCT
                            sellerHeaderTv.setText("Sold By");
                            if (item.getDealPrice() > 0) {
                                dealPriceContainer.setVisibility(View.VISIBLE);
                                normalPriceContainer.setVisibility(View.GONE);
                                dealMrpTv.setText("₹ " + String.valueOf(item.getMrp()));
                                dealSellingPriceTv.setText("₹ " + String.valueOf(item.getSellingPrice()));
                                dealPriceTv.setText("₹ " + String.valueOf(item.getDealPrice()));
                                dealSavingsTv.setText(item.getSavingsText());
                            } else {
                                dealPriceContainer.setVisibility(View.GONE);
                                normalPriceContainer.setVisibility(View.VISIBLE);
                                priceTv.setText("₹" + item.getSellingPrice());
                                if (item.getMrp().equals(item.getSellingPrice())) {
                                    mrpTv.setVisibility(View.GONE);
                                    normalSavingsTv.setVisibility(View.GONE);
                                } else {
                                    mrpTv.setText("₹ " + String.valueOf(item.getMrp()));
                                    mrpTv.setVisibility(View.VISIBLE);
                                    normalSavingsTv.setText(item.getSavingsText());
                                    normalSavingsTv.setVisibility(View.VISIBLE);
                                }
                            }
                            if (!TextUtils.isEmpty(item.getSavingPercentText())) {
                                savingPercentTv.setVisibility(View.VISIBLE);
                                savingPercentTv.setText(item.getSavingPercentText());
                            } else {
                                savingPercentTv.setVisibility(View.GONE);
                            }
                        }
                        priceTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                    } else {
                        priceTv.setText("Request Price");
                        priceTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                        mrpTv.setVisibility(View.GONE);
                    }
                    // show service deal if applicable, regardless of pricing option or anything
                    if (item.getType() == Item.ITEM_SERVICE && item.getDealPercent() > 0) {
                        serviceDealTv.setVisibility(View.VISIBLE);
                        serviceDealTv.setText(item.getDealPercent() + "% off");
                    }

                    descTv.setText(item.getDescription());

                    if (item.getType() == Item.ITEM_SERVICE) {
                        toggleServiceCatalog(true, item);
                    } else {
                        toggleServiceCatalog(false, null);
                    }

                    final ArrayList<PhotoData> photoList = item.getPhotos();
                    if (photoList.size() > 0) {
                        GlideApp.with(ItemActivity.this)
                                .load(photoList.get(0).getUrl())
                                .listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                        imageIv.setBackgroundColor(ContextCompat.getColor(ItemActivity.this, R.color.very_light_gray));
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                        imageIv.setBackgroundColor(ContextCompat.getColor(ItemActivity.this, R.color.white));
                                        return false;
                                    }
                                })
                                .thumbnail(0.1f)
                                .into(imageIv);
                        imageIv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                FullScreenImageActivity.open(
                                        ItemActivity.this, ItemActivity.this,
                                        photoList.get(0).getUrl(), imageIv, null,
                                        R.drawable.ic_cancel_black_24dp
                                );
                            }
                        });
                    }
                    setTitle(item.getName());

                    final SellerData sellerData = item.getSellerData();
                    if (sellerData != null) {

                        sellerNameTv.setText(sellerData.getName());
                        sellerBioTv.setText(sellerData.getBio());
                        if (!TextUtils.isEmpty(sellerData.getPhotoUrl())) {
                            sellerIv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            GlideApp.with(ItemActivity.this).load(sellerData.getPhotoUrl()).error(R.drawable.ic_shop).circleCrop().into(sellerIv);
                        } else {
                            sellerIv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                            GlideApp.with(ItemActivity.this).load(R.drawable.ic_shop).into(sellerIv);
                        }
                        Linkify.addLinks(sellerBioTv, Linkify.ALL);

                        sellerItemsRv.setLayoutManager(new GridLayoutManager(ItemActivity.this, 2));
                        final BigItemAdapter itemAdapter = new BigItemAdapter(GlideApp.with(ItemActivity.this), false);
                        sellerItemsRv.setAdapter(itemAdapter);
                        for (Item sellerItem : sellerData.getItemList()) {
                            itemAdapter.addData(sellerItem);
                        }
                        visitShopTv.setVisibility(View.VISIBLE);
                        visitShopTv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final Bundle bundle = new Bundle();
                                bundle.putInt("seller_id", sellerData.getId());
                                Analytics.triggerEvent(VISIT_SHOP_CLICK, bundle, ItemActivity.this);
                                ItemListActiv.open(ItemActivity.this, true, sellerData.getId());
                            }
                        });
                        syncDmId(sellerData);
                        if (sellerData.isCallEnabled()) {
                            ViewCompat.setBackgroundTintList(chatBtn, ColorStateList.valueOf(ContextCompat.getColor(ItemActivity.this, R.color.mb_green)));
                            chatBtn.setText("Call for details");
                            chatHintTv.setText("Call the seller to buy or know more");
                        } else {
                            ViewCompat.setBackgroundTintList(chatBtn, ColorStateList.valueOf(ContextCompat.getColor(ItemActivity.this, R.color.colorAccent)));
                            chatBtn.setText("Ask For Details");
                        }
                        if (sellerData.getId() == LubbleSharedPrefs.getInstance().getSellerId()) {
                            chatBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Toast.makeText(ItemActivity.this, "You cannot contact yourself :)", Toast.LENGTH_SHORT).show();
                                }
                            });
                            ViewCompat.setBackgroundTintList(chatBtn, ColorStateList.valueOf(ContextCompat.getColor(ItemActivity.this, R.color.gray)));
                            itemPvtInfoLayout.setVisibility(View.VISIBLE);
                            editItemTv.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    NewItemActiv.open(ItemActivity.this, itemId, Item.ITEM_PRODUCT);
                                }
                            });
                            if (LubbleSharedPrefs.getInstance().getIsViewCountEnabled()) {
                                viewCountTv.setVisibility(View.VISIBLE);
                                viewCountTv.setText("Views: " + sellerData.getViewCount());
                            } else {
                                viewCountTv.setVisibility(View.GONE);
                            }
                            showApprovalStatus(item.getApprovalStatus(), item.getRejectionReason());
                            userReviewContainer.setVisibility(View.GONE);
                            ratingAccountIv.setVisibility(View.GONE);
                        } else {
                            chatBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    final Bundle bundle = new Bundle();
                                    bundle.putInt("seller_id", sellerData.getId());
                                    bundle.putInt("item_id", item.getId());
                                    if (sellerData.isCallEnabled()) {
                                        Analytics.triggerEvent(CALL_BTN_CLICKED, bundle, ItemActivity.this);
                                        Intent intent = new Intent(Intent.ACTION_DIAL);
                                        intent.setData(Uri.parse("tel:" + sellerData.getPhone()));
                                        startActivity(intent);
                                    } else {
                                        Analytics.triggerEvent(MPLACE_CHAT_BTN_CLICKED, bundle, ItemActivity.this);
                                        if (!TextUtils.isEmpty(dmId)) {
                                            ChatActivity.openForDm(ItemActivity.this, dmId, null, item.getName());
                                        } else {
                                            DmIntroBottomSheet.newInstance(String.valueOf(sellerData.getId()), sellerData.getName(), sellerData.getPhotoUrl(), sellerData.getPhone())
                                                    .show(getSupportFragmentManager(), null);
                                        }
                                    }
                                }
                            });
                            itemPvtInfoLayout.setVisibility(View.GONE);
                            userReviewContainer.setVisibility(View.VISIBLE);
                            ratingAccountIv.setVisibility(View.VISIBLE);
                        }
                        handleRecommendations(sellerData);
                        sellerShareContainer.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (!TextUtils.isEmpty(sellerData.getShareLink())) {
                                    Intent intent = new Intent(Intent.ACTION_SEND);
                                    intent.setType("text/plain");
                                    String msg = "Look at this amazing shop I found: ";
                                    if (item.getType() == Item.ITEM_SERVICE) {
                                        msg = "Hey, check out this great service provider: ";
                                    }
                                    intent.putExtra(Intent.EXTRA_TEXT, msg + sellerData.getShareLink());
                                    startActivity(Intent.createChooser(intent, "Share"));
                                }
                            }
                        });
                    }
                    if (item.getRatingData() != null) {
                        showMyRatingLayout(item.getRatingData());
                    }
                    float avgRating = 0F;
                    if (item.getTotalRatingsCount() > 0) {
                        avgRating = (float) item.getTotalRating() / item.getTotalRatingsCount();
                    }
                    avgRatingTv.setText(String.format("%.1f", avgRating));
                    ratingCountTv.setText(item.getTotalRatingsCount() + " ratings");
                    avgRatingBar.setRating(avgRating);
                    if (item.getUserRatingsList() != null) {
                        reviewsRecyclerView.setAdapter(new ReviewAdapter(GlideApp.with(ItemActivity.this), item.getUserRatingsList()));
                    }

                    itemShareContainer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!TextUtils.isEmpty(item.getShareLink()) && item.getApprovalStatus() == ITEM_APPROVED) {
                                Analytics.triggerEvent(AnalyticsEvents.SHARE_ITEM, ItemActivity.this);
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("text/plain");
                                String msg = "";
                                if (item.getType() == Item.ITEM_PRODUCT) {
                                    msg = "Hey! Look at this amazing item I found: ";
                                } else {
                                    msg = "Hey, here's a service you might be interested in: ";
                                }
                                intent.putExtra(Intent.EXTRA_TEXT, msg + item.getShareLink());
                                startActivity(Intent.createChooser(intent, "Share"));
                            } else {
                                Toast.makeText(ItemActivity.this, "Please wait for the item to be approved", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } else if (!isFinishing()) {
                    if (response.code() == 404) {
                        final Bundle bundle = new Bundle();
                        bundle.putInt("item_id", itemId);
                        Analytics.triggerEvent(AnalyticsEvents.ITEM_NOT_FOUND, bundle, ItemActivity.this);
                        Toast.makeText(ItemActivity.this, "Item Not Found", Toast.LENGTH_LONG).show();
                        itemProgressBar.setVisibility(View.GONE);
                    } else {
                        FirebaseCrashlytics.getInstance().recordException(new IllegalArgumentException("Item null for item ID: " + itemId));
                        Toast.makeText(ItemActivity.this, R.string.all_try_again, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Item> call, Throwable t) {
                Log.e(TAG, "onFailure: ");
                itemProgressBar.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ItemActivity.this, R.string.check_internet, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showApprovalStatus(int approvalStatus, @Nullable String reason) {
        switch (approvalStatus) {
            case Item.ITEM_PENDING_APPROVAL:
                approvalIconIv.setImageResource(R.drawable.ic_access_time_black_24dp);
                approvalStatusTv.setText("Pending Approval");
                approvalStatusTv.setTextColor(ContextCompat.getColor(this, R.color.black));
                break;
            case ITEM_APPROVED:
                approvalIconIv.setImageResource(R.drawable.ic_check_circle_black_24dp);
                approvalStatusTv.setText("Approved");
                approvalStatusTv.setTextColor(ContextCompat.getColor(this, R.color.black));
                break;
            case Item.ITEM_REJECTED:
                approvalIconIv.setImageResource(R.drawable.ic_cancel_red_24dp);
                approvalStatusTv.setText("Declined: " + reason);
                approvalStatusTv.setTextColor(ContextCompat.getColor(this, R.color.red));
                break;
        }
    }

    private void handleRecommendations(final SellerData sellerData) {
        recommendationCount = sellerData.getRecommendationCount();
        recommendationCountTv.setText(recommendationCount + " recommendations");
        isRecommended = sellerData.getIsRecommended();
        updateRecommendContainer();
        recommendBtnContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateRecommendation(sellerData.getId());
            }
        });
    }

    private void updateRecommendation(Integer sellerId) {
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

    private void syncDmId(SellerData sellerData) {
        sellerDmQuery = RealtimeDbHelper.getUserRef(FirebaseAuth.getInstance().getUid())
                .child("dms").orderByChild("profileId").equalTo(String.valueOf(sellerData.getId()));
        sellerDmIdValueEventListener = sellerDmQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final HashMap<String, Object> map = (HashMap<String, Object>) dataSnapshot.getValue();
                if (map != null) {
                    dmId = (String) map.keySet().toArray()[0];
                    if (serviceCatalogAdapter != null) {
                        serviceCatalogAdapter.updateDmId(dmId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void toggleServiceCatalog(boolean isShown, @Nullable Item item) {
        if (isShown && item != null && item.getServiceDataList() != null && !item.getServiceDataList().isEmpty()) {
            ArrayList<ServiceData> serviceDataList = item.getServiceDataList();
            serviceHintTv.setVisibility(View.VISIBLE);
            serviceRv.setVisibility(View.VISIBLE);
            serviceRv.setNestedScrollingEnabled(false);
            serviceRv.setLayoutManager(new LinearLayoutManager(this));
            serviceCatalogAdapter = new ServiceCatalogAdapter(this, item.getSellerData(), getSupportFragmentManager());
            serviceRv.setAdapter(serviceCatalogAdapter);
            for (ServiceData serviceData : serviceDataList) {
                serviceCatalogAdapter.addData(serviceData);
            }
        } else {
            serviceHintTv.setVisibility(View.GONE);
            serviceRv.setVisibility(View.GONE);
        }
    }

    private void setDpForRating() {
        getUserInfoRef(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ProfileInfo profileInfo = dataSnapshot.getValue(ProfileInfo.class);
                try {
                    GlideApp.with(ItemActivity.this)
                            .load(profileInfo == null ? "" : profileInfo.getThumbnail())
                            .circleCrop()
                            .placeholder(R.drawable.ic_account_circle_black_no_padding)
                            .error(R.drawable.ic_account_circle_black_no_padding)
                            .into(ratingAccountIv);
                } catch (IllegalArgumentException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sellerDmIdValueEventListener != null) {
            sellerDmQuery.removeEventListener(sellerDmIdValueEventListener);
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
