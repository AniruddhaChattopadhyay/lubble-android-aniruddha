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
import com.google.firebase.auth.FirebaseAuth;
import in.lubble.app.BaseActivity;
import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.chat.books.airtable_pojo.AirtableBooksData;
import in.lubble.app.chat.books.pojos.BookItem;
import in.lubble.app.chat.books.pojos.BooksData;
import in.lubble.app.chat.books.pojos.IndustryIdentifier;
import in.lubble.app.chat.books.pojos.VolumeInfo;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import in.lubble.app.utils.UiUtils;
import okhttp3.RequestBody;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.HashMap;

import static in.lubble.app.Constants.MEDIA_TYPE;
import static in.lubble.app.chat.books.BookFragment.BOOK_STATUS_AVAILABLE;

public class BookSearchActiv extends BaseActivity implements BookSelectedListener {

    private static final String TAG = "BookSearchActiv";

    private EditText searchEt;
    private ImageView searchIv;
    private TextView addedBooksTv;
    private RelativeLayout proceedContainer;
    private RelativeLayout uploadingBookContainer;
    private RelativeLayout addMoreContainer;
    private RecyclerView searchResultsRv;
    private ProgressBar progressBar;
    private int booksAdded = 0;

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
        uploadingBookContainer = findViewById(R.id.container_uploading_book);
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
        String url = "https://www.googleapis.com/books/v1/volumes?q=intitle:" + searchString + "&printType=books";

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
        uploadBook(bookItem);
        addMoreContainer.setVisibility(View.VISIBLE);
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

    private void uploadBook(BookItem bookItem) {
        uploadingBookContainer.setVisibility(View.VISIBLE);
        proceedContainer.setVisibility(View.GONE);

        HashMap<String, Object> params = new HashMap<>();
        final VolumeInfo volumeInfo = bookItem.getVolumeInfo();
        String isbn = "";

        for (IndustryIdentifier industryIdentifier : volumeInfo.getIndustryIdentifiers()) {
            if (industryIdentifier.getType().equalsIgnoreCase("ISBN_13")) {
                isbn = industryIdentifier.getIdentifier();
                break;
            }
        }

        HashMap<String, Object> fieldParams = new HashMap<>();
        fieldParams.put("id", bookItem.getId());
        fieldParams.put("Title", volumeInfo.getTitle());
        fieldParams.put("Author", volumeInfo.getAuthors().get(0));
        fieldParams.put("Photo", volumeInfo.getImageLinks().getThumbnail());
        fieldParams.put("Owner", FirebaseAuth.getInstance().getUid());
        fieldParams.put("Lubble", LubbleSharedPrefs.getInstance().requireLubbleId());
        fieldParams.put("Status", BOOK_STATUS_AVAILABLE);
        fieldParams.put("isbn", isbn);
        params.put("fields", fieldParams);
        RequestBody body = RequestBody.create(MEDIA_TYPE, new JSONObject(params).toString());

        String url = "https://api.airtable.com/v0/appbhSWmy7ZS6UeTy/Books";

        final Endpoints endpoints = ServiceGenerator.createAirtableService(Endpoints.class);
        endpoints.uploadNewBook(url, body).enqueue(new Callback<AirtableBooksData>() {
            @Override
            public void onResponse(Call<AirtableBooksData> call, Response<AirtableBooksData> response) {
                final AirtableBooksData airtableData = response.body();
                if (response.isSuccessful() && airtableData != null && !isFinishing()) {
                    booksAdded++;
                    uploadingBookContainer.setVisibility(View.GONE);
                    updateProceedVisibility();

                } else {
                    if (!isFinishing()) {
                        uploadingBookContainer.setVisibility(View.GONE);
                        updateProceedVisibility();
                        Toast.makeText(BookSearchActiv.this, R.string.all_try_again, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<AirtableBooksData> call, Throwable t) {
                if (!isFinishing()) {
                    Toast.makeText(BookSearchActiv.this, R.string.check_internet, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onFailure: ");
                    uploadingBookContainer.setVisibility(View.GONE);
                    updateProceedVisibility();
                }
            }
        });
    }

    private void updateProceedVisibility() {
        if (booksAdded > 0) {
            uploadingBookContainer.setVisibility(View.GONE);
            proceedContainer.setVisibility(View.VISIBLE);
            addMoreContainer.setVisibility(View.VISIBLE);
            final String bookCountStr = getResources().getQuantityString(R.plurals.book_count, booksAdded, booksAdded);
            addedBooksTv.setText("Added " + bookCountStr);
        } else {
            proceedContainer.setVisibility(View.GONE);
            addMoreContainer.setVisibility(View.GONE);
        }
    }
}
