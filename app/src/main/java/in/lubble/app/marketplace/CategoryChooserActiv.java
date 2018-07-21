package in.lubble.app.marketplace;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import java.util.ArrayList;

import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.models.marketplace.Category;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryChooserActiv extends AppCompatActivity {

    private static final String TAG = "CategoryChooserActiv";
    private RecyclerView catsRv;
    private CategoryAdapter categoryAdapter;

    public static Intent getIntent(Context context) {
        return new Intent(context, CategoryChooserActiv.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_category);

        Toolbar toolbar = findViewById(R.id.text_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.category);

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
                final ArrayList<Category> categoryList = response.body();
                if (categoryList != null && categoryList.size() > 0) {
                    for (Category category : categoryList) {
                        categoryAdapter.addData(category);
                    }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Category>> call, Throwable t) {
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
