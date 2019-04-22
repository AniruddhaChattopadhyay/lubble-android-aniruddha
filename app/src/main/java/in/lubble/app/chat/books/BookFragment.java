package in.lubble.app.chat.books;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import in.lubble.app.R;
import in.lubble.app.chat.books.dummy.DummyContent;
import in.lubble.app.chat.books.dummy.DummyContent.DummyItem;

public class BookFragment extends Fragment {

    private static final String TAG = "BookFragment";

    private static final int mColumnCount = 2;
    private OnListFragmentInteractionListener mListener;
    private RecyclerView booksRecyclerView;
    private LinearLayout addBookContainer;
    private LinearLayout addBook2Container;

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

        booksRecyclerView = view.findViewById(R.id.rv_books);
        addBookContainer = view.findViewById(R.id.container_add_book);
        addBook2Container = view.findViewById(R.id.container_add_book_bottom);

        booksRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), mColumnCount));
        booksRecyclerView.setAdapter(new BookAdapter(DummyContent.ITEMS, mListener));

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

        return view;
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
