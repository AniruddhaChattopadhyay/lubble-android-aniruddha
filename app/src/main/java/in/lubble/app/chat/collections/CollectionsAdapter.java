package in.lubble.app.chat.collections;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import in.lubble.app.GlideRequests;
import in.lubble.app.R;
import in.lubble.app.utils.RoundedCornersTransformation;

import java.util.List;

import static in.lubble.app.utils.UiUtils.dpToPx;

public class CollectionsAdapter extends RecyclerView.Adapter<CollectionsAdapter.CollectionViewHolder> {

    private List<CollectionRecordData> collectionDataList;
    private final GlideRequests glide;

    public CollectionsAdapter(GlideRequests glide, List<CollectionRecordData> collectionDataList) {
        this.collectionDataList = collectionDataList;
        this.glide = glide;
    }

    @Override
    public CollectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_collection, parent, false);
        return new CollectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CollectionViewHolder holder, int position) {
        final CollectionsData collectionsData = collectionDataList.get(position).getCollectionsData();

        holder.nameTv.setText(collectionsData.getTitle());
        holder.captionTv.setText(collectionsData.getCaption());
        glide.load(collectionsData.getImageUrl())
                .transform(new RoundedCornersTransformation(dpToPx(8), 0))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(holder.collectionIv);

    }

    public void clear() {
        collectionDataList.clear();
    }

    @Override
    public int getItemCount() {
        return collectionDataList.size();
    }

    class CollectionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final View mView;
        final ImageView collectionIv;
        final TextView newTv;
        final TextView nameTv;
        final TextView captionTv;

        public CollectionViewHolder(View view) {
            super(view);
            mView = view;
            collectionIv = view.findViewById(R.id.iv_collection);
            newTv = view.findViewById(R.id.tv_new);
            nameTv = view.findViewById(R.id.tv_name);
            captionTv = view.findViewById(R.id.tv_caption);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            CollectionActivity.open(v.getContext(), collectionDataList.get(getAdapterPosition()).getCollectionsData());
        }
    }
}
