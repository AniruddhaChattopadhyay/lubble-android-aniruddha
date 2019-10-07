package in.lubble.app.chat.books;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import in.lubble.app.BaseActivity;
import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.models.airtable_pojo.AirtableBooksData;
import in.lubble.app.models.airtable_pojo.AirtableBooksRecord;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyBooksActivity extends BaseActivity {

    private static final String TAG = "MyBooksActivity";
    static final String ARG_SELECT_BOOK = "MyBooksActivity.ARG_SELECT_BOOK";
    static final String SELECTED_BOOK_RECORD = "MyBooksActivity.SELECTED_BOOK_RECORD";

    private RecyclerView recyclerView;
    private Button addBooksBtn;
    private ProgressBar progressBar;
    private ImageView noBookIv;
    private TextView noBookTv;
    private boolean toSelectBook;
    private MyBooksSelectedListener myBooksSelectedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_books);

        Toolbar toolbar = findViewById(R.id.text_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("My Books");

        progressBar = findViewById(R.id.progressbar_my_books);
        noBookIv = findViewById(R.id.iv_no_book);
        noBookTv = findViewById(R.id.tv_no_book);
        recyclerView = findViewById(R.id.rv_my_books);
        addBooksBtn = findViewById(R.id.btn_add_books);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Analytics.triggerScreenEvent(this, this.getClass());

        toSelectBook = getIntent().getBooleanExtra(ARG_SELECT_BOOK, false);

        myBooksSelectedListener = new MyBooksSelectedListener() {
            @Override
            public void onBookSelected(AirtableBooksRecord airtableBooksRecord) {
                final Intent intent = new Intent();
                intent.putExtra(SELECTED_BOOK_RECORD, airtableBooksRecord);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        };

        addBooksBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(MyBooksActivity.this, BookSearchActiv.class);
                intent.putExtra(ARG_SELECT_BOOK, false);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        fetchMyBooks();
    }

    private void fetchMyBooks() {
        progressBar.setVisibility(View.VISIBLE);
        String filterByFormula = "";
        filterByFormula = filterByFormula.concat("Owner=\'" + FirebaseAuth.getInstance().getUid() + "\'");

        String url = "https://api.airtable.com/v0/appbhSWmy7ZS6UeTy/Books?filterByFormula=AND(" + filterByFormula + ")&view=Grid%20view";

        final Endpoints endpoints = ServiceGenerator.createAirtableService(Endpoints.class);
        endpoints.fetchBooks(url).enqueue(new Callback<AirtableBooksData>() {
            @Override
            public void onResponse(Call<AirtableBooksData> call, Response<AirtableBooksData> response) {
                final AirtableBooksData airtableData = response.body();
                if (response.isSuccessful() && airtableData != null && !isFinishing()) {
                    progressBar.setVisibility(View.GONE);
                    if (airtableData.getRecords().size() > 0) {
                        recyclerView.setVisibility(View.VISIBLE);
                        noBookIv.setVisibility(View.GONE);
                        noBookTv.setVisibility(View.GONE);
                        recyclerView.setAdapter(new MyBooksAdapter(airtableData.getRecords(), GlideApp.with(MyBooksActivity.this), toSelectBook ? myBooksSelectedListener : null));
                    } else {
                        recyclerView.setVisibility(View.GONE);
                        noBookIv.setVisibility(View.VISIBLE);
                        noBookTv.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (!isFinishing()) {
                        progressBar.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.GONE);
                        noBookIv.setVisibility(View.GONE);
                        noBookTv.setVisibility(View.GONE);
                        Toast.makeText(MyBooksActivity.this, R.string.all_try_again, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<AirtableBooksData> call, Throwable t) {
                if (!isFinishing()) {
                    Toast.makeText(MyBooksActivity.this, R.string.check_internet, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onFailure: ");
                    progressBar.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                    noBookIv.setVisibility(View.GONE);
                    noBookTv.setVisibility(View.GONE);
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                }
            }
        });
    }

    public interface MyBooksSelectedListener {
        public void onBookSelected(AirtableBooksRecord airtableBooksRecord);
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
