package in.lubble.app.marketplace;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import in.lubble.app.BaseActivity;
import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.models.marketplace.Category;
import in.lubble.app.models.marketplace.Item;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;

public class CategoryChooserActiv extends BaseActivity {

    private static final String TAG = "CategoryChooserActiv";

    private static final String ARG_DEFAULT_TYPE = "ARG_DEFAULT_TYPE";

    private RecyclerView catsRv;
    private CategoryAdapter categoryAdapter;
    private ProgressBar progressBar;
    private int selectedItemType = Item.ITEM_PRODUCT;

    public static Intent getIntent(Context context, int type) {
        final Intent intent = new Intent(context, CategoryChooserActiv.class);
        intent.putExtra(ARG_DEFAULT_TYPE, type);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_category);

        Toolbar toolbar = findViewById(R.id.text_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.category);

        selectedItemType = getIntent().getIntExtra(ARG_DEFAULT_TYPE, Item.ITEM_PRODUCT);

        progressBar = findViewById(R.id.progress_bar);
        catsRv = findViewById(R.id.rv_cats);
        catsRv.setLayoutManager(new LinearLayoutManager(this));

        categoryAdapter = new CategoryAdapter(GlideApp.with(this), new CategorySelectedListener() {
            @Override
            public void onSelected(Category category) {
                final Intent intent = new Intent();
                intent.putExtra("cat_id", category.getId());
                intent.putExtra("cat_name", category.getName());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        catsRv.setAdapter(categoryAdapter);
        catsRv.addItemDecoration(new DividerItemDecoration(catsRv.getContext(), DividerItemDecoration.VERTICAL));

        fetchCategories();
    }

    private void fetchCategories() {
        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        endpoints.fetchCategories().enqueue(new Callback<ArrayList<Category>>() {
            @Override
            public void onResponse(Call<ArrayList<Category>> call, Response<ArrayList<Category>> response) {
                progressBar.setVisibility(View.GONE);
                final ArrayList<Category> categoryList = response.body();
                if (categoryList != null && categoryList.size() > 0) {
                    for (Category category : categoryList) {
                        if (category.getType() == selectedItemType) {
                            categoryAdapter.addData(category);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Category>> call, Throwable t) {
                Log.e(TAG, "onFailure: ");
                Toast.makeText(CategoryChooserActiv.this, R.string.check_internet, Toast.LENGTH_SHORT).show();
                finish();
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
