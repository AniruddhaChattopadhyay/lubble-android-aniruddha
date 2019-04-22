package in.lubble.app.chat.books;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import in.lubble.app.BaseActivity;
import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.chat.books.pojos.BookItem;
import in.lubble.app.chat.books.pojos.BooksData;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import in.lubble.app.utils.UiUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.HashMap;

public class BookSearchActiv extends BaseActivity implements BookSelectedListener {

    private static final String TAG = "BookSearchActiv";

    private EditText searchEt;
    private ImageView searchIv;
    private TextView addedBooksTv;
    private RelativeLayout proceedContainer;
    private RelativeLayout addMoreContainer;
    private RecyclerView searchResultsRv;
    private ProgressBar progressBar;
    private HashMap<String, BookItem> selectedBooksMap = new HashMap<>();

    public static void open(Context context) {
        context.startActivity(new Intent(context, BookSearchActiv.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_search);

        searchEt = findViewById(R.id.et_search);
        searchIv = findViewById(R.id.iv_search_btn);
        progressBar = findViewById(R.id.progressbar_book_search);
        proceedContainer = findViewById(R.id.container_proceed);
        addMoreContainer = findViewById(R.id.container_add_more);
        addedBooksTv = findViewById(R.id.tv_added_books);
        searchResultsRv = findViewById(R.id.rv_book_search_results);
        searchResultsRv.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        searchResultsRv.setLayoutManager(new LinearLayoutManager(this));

        searchEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch();
                    return true;
                }
                return false;
            }
        });

        searchIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performSearch();
            }
        });

        proceedContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void performSearch() {
        progressBar.setVisibility(View.VISIBLE);
        addMoreContainer.setVisibility(View.GONE);
        UiUtils.hideKeyboard(BookSearchActiv.this);

        String searchString = searchEt.getText().toString();
        searchString = searchString.replace(" ", "+");
        String url = "https://www.googleapis.com/books/v1/volumes?q=" + searchString + "+intitle&printType=books";

        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        endpoints.searchBooks(url).enqueue(new Callback<BooksData>() {
            @Override
            public void onResponse(Call<BooksData> call, Response<BooksData> response) {
                final BooksData booksData = response.body();
                if (response.isSuccessful() && booksData != null && !isFinishing()) {
                    progressBar.setVisibility(View.GONE);
                    searchResultsRv.setAdapter(new BookSearchResultAdapter(booksData.getItems(),
                            GlideApp.with(BookSearchActiv.this), BookSearchActiv.this));
                } else {
                    if (!isFinishing()) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(BookSearchActiv.this, R.string.all_try_again, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<BooksData> call, Throwable t) {
                if (!isFinishing()) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(BookSearchActiv.this, R.string.check_internet, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onFailure: ");
                }
            }
        });
    }

    @Override
    public void onBookSelected(BookItem bookItem) {
        selectedBooksMap.put(bookItem.getId(), bookItem);
        updateProceedVisibility();
        searchEt.setText("");
        searchResultsRv.setAdapter(null);
        searchEt.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.showSoftInput(searchEt, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        }, 1000);
        searchEt.requestFocus();
    }

    private void updateProceedVisibility() {
        if (selectedBooksMap.size() > 0) {
            proceedContainer.setVisibility(View.VISIBLE);
            addMoreContainer.setVisibility(View.VISIBLE);
            final String bookCountStr = getResources().getQuantityString(R.plurals.book_count, selectedBooksMap.size(), selectedBooksMap.size());
            addedBooksTv.setText("Added " + bookCountStr);
        } else {
            proceedContainer.setVisibility(View.GONE);
            addMoreContainer.setVisibility(View.GONE);
        }
    }
}
