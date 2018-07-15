package in.lubble.app.marketplace;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.models.marketplace.Item;
import in.lubble.app.models.marketplace.PhotoData;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ItemActivity extends AppCompatActivity {

    private static final String TAG = "ItemActivity";
    private static final String PARAM_ITEM_ID = "PARAM_ITEM_ID";

    private ImageView imageIv;
    private TextView titleTv;
    private TextView priceTv;
    private TextView mrpTv;
    private TextView descTv;

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

        imageIv = findViewById(R.id.iv_item_image);
        titleTv = findViewById(R.id.tv_item_title);
        priceTv = findViewById(R.id.tv_price);
        mrpTv = findViewById(R.id.tv_mrp);
        descTv = findViewById(R.id.tv_item_desc);

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
                if (item != null) {
                    titleTv.setText(item.getName());
                    priceTv.setText("₹ " + item.getSellingPrice());
                    mrpTv.setText(String.valueOf(item.getMrp()));
                    descTv.setText(item.getDescription());

                    final ArrayList<PhotoData> photoList = item.getPhotos();
                    if (photoList.size() > 0) {
                        GlideApp.with(ItemActivity.this)
                                .load(photoList.get(0).getUrl())
                                .into(imageIv);
                    }
                    setTitle(item.getName());
                }
            }

            @Override
            public void onFailure(Call<Item> call, Throwable t) {
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
