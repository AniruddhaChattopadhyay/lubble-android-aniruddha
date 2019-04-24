package in.lubble.app.chat.books;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.chat.books.airtable_pojo.AirtableBooksData;
import in.lubble.app.chat.books.airtable_pojo.AirtableBooksRecord;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

import static in.lubble.app.chat.books.MyBooksActivity.ARG_SELECT_BOOK;

public class BookFragment extends Fragment {

    private static final String TAG = "BookFragment";

    private OnListFragmentInteractionListener mListener;
    private RecyclerView booksRecyclerView;
    private LinearLayout bookStatsContainer;
    private LinearLayout addBookContainer;
    private LinearLayout addBook2Container;
    private ImageView noBookIv;
    private TextView noBookTv;
    private TextView bookCountTv;
    private ProgressBar progressBar;
    private int myBooks = -1;

    public BookFragment() {
    }

    public static BookFragment newInstance() {
        BookFragment fragment = new BookFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_book, container, false);

        noBookIv = view.findViewById(R.id.iv_no_book);
        noBookTv = view.findViewById(R.id.tv_no_book);
        bookStatsContainer = view.findViewById(R.id.container_book_stats);
        bookCountTv = view.findViewById(R.id.tv_book_count);
        booksRecyclerView = view.findViewById(R.id.rv_books);
        addBookContainer = view.findViewById(R.id.container_add_book);
        addBook2Container = view.findViewById(R.id.container_add_book_bottom);
        progressBar = view.findViewById(R.id.progressbar_books);

        booksRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        addBookContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addBooks();
            }
        });
        addBook2Container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addBooks();
            }
        });

        mListener = new OnListFragmentInteractionListener() {
            @Override
            public void onListFragmentInteraction(AirtableBooksRecord airtableBooksRecord) {
                BookCheckoutActiv.open(requireContext(), airtableBooksRecord, myBooks);
            }
        };

        fetchBooks();

        DividerItemDecoration itemDecor = new DividerItemDecoration(getContext(), GridLayoutManager.VERTICAL);
        booksRecyclerView.addItemDecoration(itemDecor);

        bookStatsContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(requireContext(), MyBooksActivity.class);
                intent.putExtra(ARG_SELECT_BOOK, false);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchBooks();
    }

    private void fetchBooks() {
        progressBar.setVisibility(View.VISIBLE);
        String filterByFormula = "";
        filterByFormula = filterByFormula.concat("Lubble=\'" + LubbleSharedPrefs.getInstance().requireLubbleId() + "\',");
        filterByFormula = filterByFormula.concat("Borrower=\'\'");

        String url = "https://api.airtable.com/v0/appbhSWmy7ZS6UeTy/Books?filterByFormula=AND(" + filterByFormula + ")&view=Grid%20view";

        final Endpoints endpoints = ServiceGenerator.createAirtableService(Endpoints.class);
        endpoints.fetchBooks(url).enqueue(new Callback<AirtableBooksData>() {
            @Override
            public void onResponse(Call<AirtableBooksData> call, Response<AirtableBooksData> response) {
                final AirtableBooksData airtableData = response.body();
                if (response.isSuccessful() && airtableData != null && isAdded() && isVisible()) {
                    progressBar.setVisibility(View.GONE);
                    if (airtableData.getRecords().size() > 0) {
                        booksRecyclerView.setVisibility(View.VISIBLE);
                        noBookIv.setVisibility(View.GONE);
                        noBookTv.setVisibility(View.GONE);

                        final List<AirtableBooksRecord> publicBooksList = new ArrayList<>();
                        for (AirtableBooksRecord record : airtableData.getRecords()) {
                            if (!record.getFields().getOwner().equalsIgnoreCase(FirebaseAuth.getInstance().getUid())) {
                                publicBooksList.add(record);
                            }
                        }

                        booksRecyclerView.setAdapter(new BookAdapter(publicBooksList, GlideApp.with(requireContext()), mListener));
                        calcMyBooks(airtableData.getRecords());
                    } else {
                        booksRecyclerView.setVisibility(View.GONE);
                        noBookIv.setVisibility(View.VISIBLE);
                        noBookTv.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (isAdded() && isVisible()) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), R.string.all_try_again, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<AirtableBooksData> call, Throwable t) {
                if (isAdded() && isVisible()) {
                    Toast.makeText(requireContext(), R.string.check_internet, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onFailure: ");
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    private void calcMyBooks(List<AirtableBooksRecord> records) {
        myBooks = 0;
        for (AirtableBooksRecord booksRecord : records) {
            if (booksRecord.getFields().getOwner().equalsIgnoreCase(FirebaseAuth.getInstance().getUid())) {
                myBooks++;
            }
        }
        bookCountTv.setText(String.valueOf(myBooks));
    }

    private void addBooks() {
        BookSearchActiv.open(requireContext());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            /*throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");*/
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(AirtableBooksRecord airtableBooksRecord);
    }
}
