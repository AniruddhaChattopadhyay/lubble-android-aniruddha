package in.lubble.app.marketplace;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;

import org.json.JSONException;
import org.json.JSONObject;

import in.lubble.app.BaseActivity;
import in.lubble.app.BuildConfig;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;

public class SearchActivity extends BaseActivity implements CompletionHandler {

    private static final String TAG = "SearchActivity";

    private EditText searchEt;
    private RecyclerView searchResultsRv;
    private SearchResultsAdapter adapter;

    public static void open(Context context) {
        context.startActivity(new Intent(context, SearchActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Analytics.triggerScreenEvent(this, this.getClass());

        searchEt = findViewById(R.id.et_search);
        searchResultsRv = findViewById(R.id.rv_search_results);
        searchResultsRv.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        searchResultsRv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new SearchResultsAdapter(this);
        searchResultsRv.setAdapter(adapter);

        Client client = new Client("IIVL0B0EIY", "12ac422e05119422ec03224a9da738a7");
        final Index index = client.getIndex(BuildConfig.DEBUG ? "dev_mplace" : "prod_mplace");

        try {
            index.addObjectAsync(new JSONObject()
                    .put("title", "Occasions")
                    .put("id", 1994)
                    .put("entity", "seller"), null);
            index.addObjectAsync(new JSONObject()
                    .put("title", "Bakery")
                    .put("id", 1995)
                    .put("entity", "category"), null);
            index.addObjectAsync(new JSONObject()
                    .put("title", "Rainbow Cake")
                    .put("id", 1996)
                    .put("entity", "item"), null);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {

                    index.searchAsync(new Query("jimmie"), SearchActivity.this);

                } else {
                    adapter.clearAll();
                }
            }
        });
    }

    @Override
    public void requestCompleted(@Nullable JSONObject jsonObject, @Nullable AlgoliaException e) {
        if (jsonObject != null) {
            Log.d(TAG, "requestCompleted: " + jsonObject.toString());
            ItemSearchData itemSearchData = new ItemSearchData();
            itemSearchData.setId(jsonObject.optInt("id"));
            itemSearchData.setName(jsonObject.optString("title"));

            //todo adapter.addData(itemSearchDataList);
            //if (itemSearchDataList.isEmpty()) {
            //    // failed search
            //    final Bundle bundle = new Bundle();
            //    bundle.putString("search_term", s.toString());
            //    Analytics.triggerEvent(FAILED_SEARCH, bundle, SearchActivity.this);
            //}
        }
    }
}
