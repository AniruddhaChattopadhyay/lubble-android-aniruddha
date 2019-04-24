package in.lubble.app.chat.collections;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;

public class CollectionActivity extends AppCompatActivity {

    private static final String TAG = "CollectionActivity";

    private static final String PARAM_COLLECTION = "CollectionActivity.PARAM_COLLECTION";

    private ProgressBar progressBar;
    private TextView titleTv;
    private TextView descTV;
    private ImageView imageIv;
    private RecyclerView placesRv;
    private CollectionsData collectionsData;

    public static void open(Context context, CollectionsData collectionsData) {
        final Intent intent = new Intent(context, CollectionActivity.class);
        intent.putExtra(PARAM_COLLECTION, collectionsData);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);

        Toolbar toolbar = findViewById(R.id.text_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar = findViewById(R.id.progressbar);
        titleTv = findViewById(R.id.tv_collection_title);
        imageIv = findViewById(R.id.iv_collection_hero);
        descTV = findViewById(R.id.tv_desc);
        placesRv = findViewById(R.id.rv_places);
        placesRv.setLayoutManager(new LinearLayoutManager(this));
        placesRv.setNestedScrollingEnabled(false);

        collectionsData = (CollectionsData) getIntent().getSerializableExtra(PARAM_COLLECTION);

        setTitle(collectionsData.getTitle());
        titleTv.setText(collectionsData.getTitle());
        descTV.setText(HtmlCompat.fromHtml(collectionsData.getDescription(), HtmlCompat.FROM_HTML_MODE_LEGACY));
        GlideApp.with(this).load(collectionsData.getImageUrl())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(imageIv);
        fetchPlaces(collectionsData.getPlaceIdList());

    }


    private void fetchPlaces(List<String> placeIdList) {
        progressBar.setVisibility(View.VISIBLE);
        String filterByFormula = "";
        for (String recordId : placeIdList) {
            filterByFormula = filterByFormula.concat("RECORD_ID()=\'" + recordId + "\',");
        }

        String url = "https://api.airtable.com/v0/appbhSWmy7ZS6UeTy/Places?filterByFormula=\"OR(" + filterByFormula + ")\"&view=Grid%20view";

        final Endpoints endpoints = ServiceGenerator.createAirtableService(Endpoints.class);
        endpoints.fetchPlaces(url).enqueue(new Callback<AirtablePlacesData>() {
            @Override
            public void onResponse(Call<AirtablePlacesData> call, Response<AirtablePlacesData> response) {
                final AirtablePlacesData airtableData = response.body();
                if (response.isSuccessful() && airtableData != null && !isFinishing()) {
                    progressBar.setVisibility(View.GONE);

                    placesRv.setAdapter(new CollectionPlacesAdapter(CollectionActivity.this, GlideApp.with(CollectionActivity.this), airtableData.getRecords()));

                } else {
                    if (!isFinishing()) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(CollectionActivity.this, R.string.all_try_again, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<AirtablePlacesData> call, Throwable t) {
                if (!isFinishing()) {
                    Toast.makeText(CollectionActivity.this, R.string.check_internet, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onFailure: ");
                    progressBar.setVisibility(View.GONE);
                }
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
