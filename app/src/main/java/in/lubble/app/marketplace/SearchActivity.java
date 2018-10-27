package in.lubble.app.marketplace;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import in.lubble.app.BaseActivity;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.database.DbSingleton;

import java.util.ArrayList;

public class SearchActivity extends BaseActivity {

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
                    final ArrayList<ItemSearchData> itemSearchDataList = DbSingleton.getInstance().readAllItemSearchData(s.toString());
                    adapter.addData(itemSearchDataList);
                } else {
                    adapter.clearAll();
                }
            }
        });
    }
}
