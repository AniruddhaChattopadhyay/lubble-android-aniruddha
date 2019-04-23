package in.lubble.app.chat.books;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import in.lubble.app.BaseActivity;
import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.chat.books.airtable_pojo.AirtableBooksFields;
import in.lubble.app.chat.books.airtable_pojo.AirtableBooksRecord;

import static in.lubble.app.chat.books.MyBooksActivity.ARG_SELECT_BOOK;
import static in.lubble.app.chat.books.MyBooksActivity.SELECTED_BOOK_RECORD;

public class BookCheckoutActiv extends BaseActivity {

    private static final String TAG = "BookCheckoutActiv";
    private static final int REQUEST_MY_BOOK = 163;

    private static final String ARG_BOOK_DATA = "BookCheckoutActiv.ARG_BOOK_DATA";
    private static final String ARG_MY_BOOK_COUNT = "BookCheckoutActiv.ARG_MY_BOOK_COUNT";

    private ImageView selectedBookIv;
    private ImageView giveBookIv;
    private TextView bookTitleTv;
    private TextView giveBookTitleTv;
    private TextView bookAuthorTv;
    private TextView giveBookAuthorTv;
    private TextView giveBookChangeTv;
    private LinearLayout addBookContainer;
    private RelativeLayout bookGiveContainer;
    private Button addressBtn;
    private AirtableBooksRecord airtableBooksRecord;
    private int myBookCount = -1;

    public static void open(Context context, AirtableBooksRecord airtableBooksRecord, int myBooks) {
        final Intent intent = new Intent(context, BookCheckoutActiv.class);
        intent.putExtra(ARG_BOOK_DATA, airtableBooksRecord);
        intent.putExtra(ARG_MY_BOOK_COUNT, myBooks);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activ_book_checkout);

        Toolbar toolbar = findViewById(R.id.text_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Book Exchange");

        selectedBookIv = findViewById(R.id.iv_selected_book);
        bookTitleTv = findViewById(R.id.tv_book_title);
        bookAuthorTv = findViewById(R.id.tv_book_author);
        addBookContainer = findViewById(R.id.container_add_book);
        bookGiveContainer = findViewById(R.id.container_book_give);
        giveBookIv = findViewById(R.id.iv_give_selected_book);
        giveBookTitleTv = findViewById(R.id.tv_give_book_title);
        giveBookAuthorTv = findViewById(R.id.tv_give_book_author);
        giveBookChangeTv = findViewById(R.id.tv_give_change);
        addressBtn = findViewById(R.id.btn_address);

        if (!getIntent().hasExtra(ARG_BOOK_DATA)) {
            Log.e(TAG, "onCreate: ARG_BOOK_DATA missing");
            finish();
            return;
        }

        airtableBooksRecord = (AirtableBooksRecord) getIntent().getSerializableExtra(ARG_BOOK_DATA);
        myBookCount = getIntent().getIntExtra(ARG_MY_BOOK_COUNT, -1);

        final AirtableBooksFields booksFields = airtableBooksRecord.getFields();
        GlideApp.with(this).load(booksFields.getPhoto()).diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(selectedBookIv);
        bookTitleTv.setText(booksFields.getTitle());
        bookAuthorTv.setText(booksFields.getAuthor());

        addBookContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myBookCount == 0) {
                    final Intent intent = new Intent(BookCheckoutActiv.this, BookSearchActiv.class);
                    intent.putExtra(ARG_SELECT_BOOK, true);
                    startActivityForResult(intent, REQUEST_MY_BOOK);
                } else {
                    final Intent intent = new Intent(BookCheckoutActiv.this, MyBooksActivity.class);
                    intent.putExtra(ARG_SELECT_BOOK, true);
                    startActivityForResult(intent, REQUEST_MY_BOOK);
                }
            }
        });

        giveBookChangeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(BookCheckoutActiv.this, MyBooksActivity.class);
                intent.putExtra(ARG_SELECT_BOOK, true);
                startActivityForResult(intent, REQUEST_MY_BOOK);
            }
        });

        addressBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddressChooserActiv.open(BookCheckoutActiv.this);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MY_BOOK && resultCode == Activity.RESULT_OK) {
            bookGiveContainer.setVisibility(View.VISIBLE);
            addBookContainer.setVisibility(View.GONE);

            AirtableBooksRecord airtableBooksRecord = (AirtableBooksRecord) data.getSerializableExtra(SELECTED_BOOK_RECORD);
            final AirtableBooksFields bookFields = airtableBooksRecord.getFields();
            GlideApp.with(this).load(bookFields.getPhoto()).diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(giveBookIv);
            giveBookTitleTv.setText(bookFields.getTitle());
            giveBookAuthorTv.setText(bookFields.getAuthor());
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
