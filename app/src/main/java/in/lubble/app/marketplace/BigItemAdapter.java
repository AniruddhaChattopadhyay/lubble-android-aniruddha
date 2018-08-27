package in.lubble.app.marketplace;

import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
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

    private static final String TAG = "BigItemAdapter";

    private final List<Item> itemList;
    private final GlideRequests glide;
    private boolean isForSellerDash;

    public BigItemAdapter(GlideRequests glide, boolean isForSellerDash) {
        itemList = new ArrayList<>();
        this.glide = glide;
        this.isForSellerDash = isForSellerDash;
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
        if (item.getType() == Item.ITEM_SERVICE) {
            final Integer startingPrice = item.getStartingPrice() == null ? item.getSellingPrice() : item.getStartingPrice();
            if (startingPrice < 0) {
                viewHolder.priceTv.setText("Request Price");
            } else {
                viewHolder.priceTv.setText("₹" + startingPrice + " onwards");
            }

            viewHolder.mrpTv.setVisibility(View.GONE);
        } else {
            viewHolder.priceTv.setText("₹" + item.getSellingPrice());
            if (item.getMrp().equals(item.getSellingPrice())) {
                viewHolder.mrpTv.setVisibility(View.GONE);
            } else {
                viewHolder.mrpTv.setVisibility(View.VISIBLE);
                viewHolder.mrpTv.setText("₹" + item.getMrp());
                viewHolder.mrpTv.setPaintFlags(viewHolder.mrpTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
        }

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

        if (isForSellerDash) {
            switch (item.getApprovalStatus()) {
                case Item.ITEM_PENDING_APPROVAL:
                    viewHolder.approvalStatusTv.setVisibility(View.VISIBLE);
                    viewHolder.approvalStatusTv.setText("Pending Approval");
                    viewHolder.approvalStatusTv.setBackgroundColor(ContextCompat.getColor(viewHolder.view.getContext(), R.color.colorAccent));
                    break;
                case Item.ITEM_APPROVED:
                    viewHolder.approvalStatusTv.setVisibility(View.GONE);
                    break;
                case Item.ITEM_REJECTED:
                    viewHolder.approvalStatusTv.setVisibility(View.VISIBLE);
                    viewHolder.approvalStatusTv.setText("Declined");
                    viewHolder.approvalStatusTv.setBackgroundColor(ContextCompat.getColor(viewHolder.view.getContext(), R.color.red));
                    break;
            }
        } else {
            viewHolder.approvalStatusTv.setVisibility(View.GONE);
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
        final View view;
        final ImageView itemIv;
        final TextView approvalStatusTv;
        final ProgressBar itemPicProgressBar;
        final TextView nameTv;
        final TextView priceTv;
        final TextView mrpTv;

        ViewHolder(View view) {
            super(view);
            this.view = view;
            itemIv = view.findViewById(R.id.iv_item);
            itemPicProgressBar = view.findViewById(R.id.progress_bar_item_pic);
            approvalStatusTv = view.findViewById(R.id.tv_approval_status);
            nameTv = view.findViewById(R.id.tv_name);
            priceTv = view.findViewById(R.id.tv_price);
            mrpTv = view.findViewById(R.id.tv_mrp);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            v.getContext().startActivity(ItemActivity.getIntent(v.getContext(), itemList.get(getAdapterPosition()).getId()));
        }
    }


}
