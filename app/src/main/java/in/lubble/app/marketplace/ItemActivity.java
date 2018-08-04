package in.lubble.app.marketplace;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.chat.ChatActivity;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.marketplace.Item;
import in.lubble.app.models.marketplace.PhotoData;
import in.lubble.app.models.marketplace.SellerData;
import in.lubble.app.models.marketplace.ServiceData;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ItemActivity extends AppCompatActivity {

    private static final String TAG = "ItemActivity";
    private static final String PARAM_ITEM_ID = "PARAM_ITEM_ID";

    private ImageView imageIv;
    private ProgressBar itemProgressBar;
    private ProgressBar progressBar;
    private TextView titleTv;
    private TextView priceTv;
    private TextView mrpTv;
    private RelativeLayout itemPvtInfoLayout;
    private TextView editItemTv;
    private TextView viewCountTv;
    private TextView descTv;
    private TextView serviceHintTv;
    private RecyclerView serviceRv;
    private ImageView sellerIv;
    private TextView sellerNameTv;
    private TextView sellerBioTv;
    private RecyclerView sellerItemsRv;
    private TextView visitShopTv;
    private Button chatBtn;
    private ValueEventListener sellerDmIdValueEventListener;
    private Query sellerDmQuery;
    @Nullable
    private String dmId = null;

    private int itemId;

    public static void open(Context context, int itemId) {
        final Intent intent = new Intent(context, ItemActivity.class);
        intent.putExtra(PARAM_ITEM_ID, itemId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        itemProgressBar = findViewById(R.id.item_progress_bar);
        progressBar = findViewById(R.id.progress_bar);
        imageIv = findViewById(R.id.iv_item_image);
        titleTv = findViewById(R.id.tv_item_title);
        priceTv = findViewById(R.id.tv_price);
        mrpTv = findViewById(R.id.tv_mrp);
        itemPvtInfoLayout = findViewById(R.id.relativelayout_item_pvt_info);
        editItemTv = findViewById(R.id.tv_edit_item);
        viewCountTv = findViewById(R.id.tv_view_count);
        chatBtn = findViewById(R.id.btn_chat);
        descTv = findViewById(R.id.tv_item_desc);
        serviceHintTv = findViewById(R.id.tv_service_catalog_hint);
        serviceRv = findViewById(R.id.rv_service_catalog);

        sellerIv = findViewById(R.id.iv_seller_pic);
        sellerNameTv = findViewById(R.id.tv_seller_name);
        sellerBioTv = findViewById(R.id.tv_seller_bio);
        sellerItemsRv = findViewById(R.id.rv_items);
        visitShopTv = findViewById(R.id.tv_visit_shop);

        mrpTv.setPaintFlags(mrpTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        Toolbar toolbar = findViewById(R.id.text_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        itemId = getIntent().getIntExtra(PARAM_ITEM_ID, -1);
        if (itemId == -1) {
            throw new IllegalArgumentException("No ITEM ID passed");
        }

        fetchItemDetails();

        Analytics.triggerScreenEvent(this, this.getClass());
    }

    private void fetchItemDetails() {
        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        endpoints.fetchItemDetails(itemId).enqueue(new Callback<Item>() {
            @Override
            public void onResponse(Call<Item> call, Response<Item> response) {
                final Item item = response.body();
                itemProgressBar.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                if (item != null) {
                    titleTv.setText(item.getName());
                    priceTv.setText("₹ " + item.getSellingPrice());
                    mrpTv.setText(String.valueOf(item.getMrp()));
                    descTv.setText(item.getDescription());

                    if (item.getType() == Item.ITEM_SERVICE) {
                        toggleServiceCatalog(true, item.getServiceDataList());
                    } else {
                        toggleServiceCatalog(false, null);
                    }

                    final ArrayList<PhotoData> photoList = item.getPhotos();
                    if (photoList.size() > 0) {
                        GlideApp.with(ItemActivity.this)
                                .load(photoList.get(0).getUrl())
                                .thumbnail(0.1f)
                                .into(imageIv);
                    }
                    setTitle(item.getName());

                    final SellerData sellerData = item.getSellerData();
                    if (sellerData != null) {

                        sellerNameTv.setText(sellerData.getName());
                        sellerBioTv.setText(sellerData.getBio());
                        GlideApp.with(ItemActivity.this).load(sellerData.getPhotoUrl()).circleCrop().into(sellerIv);

                        sellerItemsRv.setLayoutManager(new GridLayoutManager(ItemActivity.this, 2));
                        final BigItemAdapter itemAdapter = new BigItemAdapter(GlideApp.with(ItemActivity.this));
                        sellerItemsRv.setAdapter(itemAdapter);
                        for (Item sellerItem : sellerData.getItemList()) {
                            itemAdapter.addData(sellerItem);
                        }
                        visitShopTv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ItemListActiv.open(ItemActivity.this, true, sellerData.getId());
                            }
                        });
                        syncDmId(sellerData);
                        chatBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (!TextUtils.isEmpty(dmId)) {
                                    ChatActivity.openForDm(ItemActivity.this, dmId, null);
                                } else {
                                    ChatActivity.openForEmptyDm(
                                            ItemActivity.this,
                                            String.valueOf(sellerData.getId()),
                                            sellerData.getName(),
                                            sellerData.getPhotoUrl()
                                    );
                                }
                            }
                        });
                        if (sellerData.getId() == LubbleSharedPrefs.getInstance().getSellerId()) {
                            itemPvtInfoLayout.setVisibility(View.VISIBLE);
                            editItemTv.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    NewItemActiv.open(ItemActivity.this, itemId);
                                }
                            });
                            if (LubbleSharedPrefs.getInstance().getIsViewCountEnabled()) {
                                viewCountTv.setVisibility(View.VISIBLE);
                                viewCountTv.setText("View Count: " + sellerData.getViewCount());
                            } else {
                                viewCountTv.setVisibility(View.GONE);
                            }
                        } else {
                            itemPvtInfoLayout.setVisibility(View.GONE);
                        }
                    }
                } else {
                    if (response.code() == 404) {
                        final Bundle bundle = new Bundle();
                        bundle.putInt("item_id", itemId);
                        Analytics.triggerEvent(AnalyticsEvents.ITEM_NOT_FOUND, bundle, ItemActivity.this);
                        Toast.makeText(ItemActivity.this, "Item Not Found", Toast.LENGTH_LONG).show();
                    } else {
                        Crashlytics.logException(new IllegalArgumentException("Item null for item ID: " + itemId));
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

    private void syncDmId(SellerData sellerData) {
        sellerDmQuery = RealtimeDbHelper.getUserRef(FirebaseAuth.getInstance().getUid())
                .child("dms").orderByChild("profileId").equalTo(String.valueOf(sellerData.getId()));
        sellerDmIdValueEventListener = sellerDmQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final HashMap<String, Object> map = (HashMap<String, Object>) dataSnapshot.getValue();
                if (map != null) {
                    dmId = (String) map.keySet().toArray()[0];
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void toggleServiceCatalog(boolean isShown, @Nullable ArrayList<ServiceData> serviceDataList) {
        if (isShown && serviceDataList != null && !serviceDataList.isEmpty()) {
            serviceHintTv.setVisibility(View.VISIBLE);
            serviceRv.setVisibility(View.VISIBLE);
            serviceRv.setNestedScrollingEnabled(false);
            serviceRv.setLayoutManager(new LinearLayoutManager(this));
            final ServiceCatalogAdapter adapter = new ServiceCatalogAdapter(this);
            serviceRv.setAdapter(adapter);
            for (ServiceData serviceData : serviceDataList) {
                adapter.addData(serviceData);
            }
        } else {
            serviceHintTv.setVisibility(View.GONE);
            serviceRv.setVisibility(View.GONE);
        }
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
