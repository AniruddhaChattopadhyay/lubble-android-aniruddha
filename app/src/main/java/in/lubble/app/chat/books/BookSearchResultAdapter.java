package in.lubble.app.chat.books;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import in.lubble.app.GlideRequests;
import in.lubble.app.R;
import in.lubble.app.models.pojos.BookItem;
import in.lubble.app.models.pojos.VolumeInfo;

public class BookSearchResultAdapter extends RecyclerView.Adapter<BookSearchResultAdapter.BookViewHolder> {

    private final List<BookItem> bookItemList;
    private final BookSelectedListener mListener;
    private final GlideRequests glideApp;

    public BookSearchResultAdapter(List<BookItem> items, GlideRequests glideApp, BookSelectedListener listener) {
        bookItemList = items;
        mListener = listener;
        this.glideApp = glideApp;
    }

    @Override
    public BookViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_serp_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final BookViewHolder holder, int position) {
        final BookItem bookItem = bookItemList.get(position);
        holder.mItem = bookItem;

        final VolumeInfo volumeInfo = bookItem.getVolumeInfo();
        if (volumeInfo != null && volumeInfo.getAuthors() != null) {
            holder.titleTv.setText(volumeInfo.getTitle());
            holder.authorTv.setText(volumeInfo.getAuthors().size() > 0 ? volumeInfo.getAuthors().get(0) : "");
            holder.giveThisTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mListener) {
                        mListener.onBookSelected(holder.mItem);
                    }
                }
            });
            if (volumeInfo.getImageLinks() != null && volumeInfo.getImageLinks().getThumbnail() != null) {
                glideApp.load(volumeInfo.getImageLinks().getThumbnail()).diskCacheStrategy(DiskCacheStrategy.NONE).into(holder.bookIv);
            }
            holder.mView.setVisibility(View.VISIBLE);
        } else {
            holder.mView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return bookItemList.size();
    }

    class BookViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final ImageView bookIv;
        final TextView titleTv;
        final TextView authorTv;
        final TextView giveThisTv;
        BookItem mItem;

        BookViewHolder(View view) {
            super(view);
            mView = view;
            bookIv = view.findViewById(R.id.iv_serp_book);
            titleTv = view.findViewById(R.id.tv_book_title);
            authorTv = view.findViewById(R.id.tv_book_author);
            giveThisTv = view.findViewById(R.id.tv_give_this);
        }

    }
}
