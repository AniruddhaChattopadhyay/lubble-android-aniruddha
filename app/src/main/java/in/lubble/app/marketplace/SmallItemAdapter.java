package in.lubble.app.marketplace;

import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.lubble.app.GlideRequests;
import in.lubble.app.R;
import in.lubble.app.models.marketplace.Item;
import in.lubble.app.models.marketplace.PhotoData;

public class SmallItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "SmallItemAdapter";
    private static final int TYPE_ITEM = 172;
    private static final int TYPE_VIEW_ALL = 506;

    private final List<Item> itemList;
    private final GlideRequests glide;

    public SmallItemAdapter(GlideRequests glide) {
        itemList = new ArrayList<>();
        this.glide = glide;
    }

    @Override
    public int getItemViewType(int position) {
        if (itemList.get(position) == null) {
            return TYPE_VIEW_ALL;
        } else {
            return TYPE_ITEM;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            return new ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.small_item, parent, false));
        } else {
            return new ViewAllViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.view_all_item, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof ViewHolder) {
            final SmallItemAdapter.ViewHolder viewHolder = (SmallItemAdapter.ViewHolder) holder;

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
                viewHolder.mrpTv.setVisibility(View.VISIBLE);
                viewHolder.mrpTv.setText("₹" + item.getMrp());
                viewHolder.mrpTv.setPaintFlags(viewHolder.mrpTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }

            final ArrayList<PhotoData> photoList = item.getPhotos();
            if (photoList.size() > 0) {
                glide.load(photoList.get(0).getUrl()).into(viewHolder.itemIv);
            }
        } else {
            // nothing to do here..
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

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final ImageView itemIv;
        final TextView nameTv;
        final TextView priceTv;
        final TextView mrpTv;

        ViewHolder(View view) {
            super(view);
            itemIv = view.findViewById(R.id.iv_item);
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

    class ViewAllViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ViewAllViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            ItemListActiv.open(v.getContext(), false, itemList.get(0).getCategory().getId());
        }
    }


}
