package in.lubble.app.chat.books;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import in.lubble.app.GlideRequests;
import in.lubble.app.R;
import in.lubble.app.chat.books.airtable_pojo.AirtableBooksFields;
import in.lubble.app.chat.books.airtable_pojo.AirtableBooksRecord;

import java.util.List;

public class MyBooksAdapter extends RecyclerView.Adapter<MyBooksAdapter.MyBookViewHolder> {

    private final List<AirtableBooksRecord> bookRecordsList;
    private final MyBooksActivity.MyBooksSelectedListener mListener;
    private GlideRequests glide;

    public MyBooksAdapter(List<AirtableBooksRecord> items, GlideRequests glide, MyBooksActivity.MyBooksSelectedListener listener) {
        bookRecordsList = items;
        mListener = listener;
        this.glide = glide;
    }

    @Override
    public MyBookViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_book, parent, false);
        return new MyBookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyBookViewHolder holder, int position) {
        final AirtableBooksFields airtableBook = bookRecordsList.get(position).getFields();
        holder.mItem = bookRecordsList.get(position);
        holder.titleTv.setText(airtableBook.getTitle());
        holder.authorTv.setText(airtableBook.getAuthor());
        glide.load(airtableBook.getPhoto()).diskCacheStrategy(DiskCacheStrategy.NONE).into(holder.picIv);

        holder.giveThisTv.setVisibility(mListener != null ? View.VISIBLE : View.GONE);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onBookSelected(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookRecordsList.size();
    }

    public class MyBookViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final ImageView picIv;
        final TextView titleTv;
        final TextView authorTv;
        final TextView giveThisTv;
        AirtableBooksRecord mItem;

        MyBookViewHolder(View view) {
            super(view);
            mView = view;
            picIv = view.findViewById(R.id.iv_book_cover);
            titleTv = view.findViewById(R.id.tv_book_title);
            authorTv = view.findViewById(R.id.tv_book_author);
            giveThisTv = view.findViewById(R.id.tv_give_this);
        }

    }
}
