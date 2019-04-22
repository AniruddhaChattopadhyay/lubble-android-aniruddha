package in.lubble.app.chat.books;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import in.lubble.app.R;
import in.lubble.app.chat.books.BookFragment.OnListFragmentInteractionListener;
import in.lubble.app.chat.books.dummy.DummyContent.DummyItem;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private final List<DummyItem> mValues;
    private final OnListFragmentInteractionListener mListener;

    public BookAdapter(List<DummyItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public BookViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final BookViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.titleTv.setText(mValues.get(position).id);
        holder.authorTv.setText(mValues.get(position).content);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
                BookCheckoutActiv.open(v.getContext());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class BookViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView titleTv;
        public final TextView authorTv;
        public DummyItem mItem;

        public BookViewHolder(View view) {
            super(view);
            mView = view;
            titleTv = view.findViewById(R.id.tv_book_title);
            authorTv = view.findViewById(R.id.tv_book_author);
        }

    }
}
