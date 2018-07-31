package in.lubble.app.marketplace;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.lubble.app.GlideRequests;
import in.lubble.app.R;
import in.lubble.app.models.marketplace.Item;
import in.lubble.app.models.marketplace.PhotoData;

public class BigItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private static final String TAG = "MsgReceiptAdapter";

    private final List<Item> itemList;
    private final GlideRequests glide;

    public BigItemAdapter(GlideRequests glide) {
        itemList = new ArrayList<>();
        this.glide = glide;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BigItemAdapter.ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.big_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        final BigItemAdapter.ViewHolder viewHolder = (BigItemAdapter.ViewHolder) holder;
        final Item item = itemList.get(position);

        viewHolder.nameTv.setText(item.getName());
        viewHolder.priceTv.setText("₹ " + item.getSellingPrice());

        final ArrayList<PhotoData> photoList = item.getPhotos();
        if (photoList.size() > 0) {
            viewHolder.itemPicProgressBar.setVisibility(View.GONE);
            glide.load(photoList.get(0).getUrl())
                    .thumbnail(0.1f)
                    .into(viewHolder.itemIv);
        } else {
            viewHolder.itemPicProgressBar.setVisibility(View.VISIBLE);
            glide.load("")
                    .thumbnail(0.1f)
                    .into(viewHolder.itemIv);
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void addData(Item item) {
        itemList.add(item);
        notifyDataSetChanged();
    }

    public void updateItemPic(int itemId, @NonNull String downloadUrl) {
        final Item itemToUpdate = new Item();
        itemToUpdate.setId(itemId);
        final int index = itemList.indexOf(itemToUpdate);
        if (index != -1) {
            final Item item = itemList.get(index);

            ArrayList<PhotoData> photoList = new ArrayList<>();
            final PhotoData photoData = new PhotoData();
            photoData.setUrl(downloadUrl);
            photoList.add(photoData);

            item.setPhotos(photoList);
            notifyItemChanged(index);
        }
    }

    public void clear() {
        itemList.clear();
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final ImageView itemIv;
        final ProgressBar itemPicProgressBar;
        final TextView nameTv;
        final TextView priceTv;

        ViewHolder(View view) {
            super(view);
            itemIv = view.findViewById(R.id.iv_item);
            itemPicProgressBar = view.findViewById(R.id.progress_bar_item_pic);
            nameTv = view.findViewById(R.id.tv_name);
            priceTv = view.findViewById(R.id.tv_price);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            ItemActivity.open(v.getContext(), itemList.get(getAdapterPosition()).getId());
        }
    }


}
