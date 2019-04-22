package in.lubble.app.chat.books;

import android.content.Context;
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
import in.lubble.app.chat.books.dummy.DummyContent.DummyItem;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;

public class BookFragment extends Fragment {

    private static final String TAG = "BookFragment";

    public static final String BOOK_STATUS_AVAILABLE = "AVAILABLE";
    public static final String BOOK_STATUS_BORROWED = "BORROWED";

    private static final int mColumnCount = 2;
    private OnListFragmentInteractionListener mListener;
    private RecyclerView booksRecyclerView;
    private LinearLayout addBookContainer;
    private LinearLayout addBook2Container;
    private ImageView noBookIv;
    private TextView noBookTv;
    private TextView bookCountTv;
    private ProgressBar progressBar;

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
        fetchBooks();

        DividerItemDecoration itemDecor = new DividerItemDecoration(getContext(), GridLayoutManager.VERTICAL);
        booksRecyclerView.addItemDecoration(itemDecor);

        return view;
    }

    private void fetchBooks() {
        progressBar.setVisibility(View.VISIBLE);
        String filterByFormula = "";
        filterByFormula = filterByFormula.concat("Lubble=\'" + LubbleSharedPrefs.getInstance().requireLubbleId() + "\',");
        filterByFormula = filterByFormula.concat("Status=\'" + BOOK_STATUS_AVAILABLE + "\'");

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
                        booksRecyclerView.setAdapter(new BookAdapter(airtableData.getRecords(), GlideApp.with(requireContext()), mListener));
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
        int myBooks = 0;
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(DummyItem item);
    }
}
