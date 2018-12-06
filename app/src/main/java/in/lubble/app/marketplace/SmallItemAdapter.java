package in.lubble.app.marketplace;

import android.graphics.Paint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import in.lubble.app.GlideRequests;
import in.lubble.app.R;
import in.lubble.app.models.marketplace.Item;
import in.lubble.app.models.marketplace.PhotoData;

import java.util.ArrayList;
import java.util.List;

import static in.lubble.app.models.marketplace.Item.ITEM_PRICING_PAID;

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
            viewHolder.savingTv.setVisibility(View.GONE);
            if (item.getType() == Item.ITEM_SERVICE) {
                final Integer startingPrice = item.getStartingPrice() == null ? item.getSellingPrice() : item.getStartingPrice();
                if (item.getPricingOption() == ITEM_PRICING_PAID) {
                    viewHolder.priceTv.setText("₹" + startingPrice + " onwards");
                } else {
                    viewHolder.priceTv.setText("Request Price");
                }
                viewHolder.mrpTv.setVisibility(View.GONE);
                if ((item.getDealPercent() > 0)) {
                    viewHolder.savingTv.setVisibility(View.VISIBLE);
                    viewHolder.savingTv.setText(item.getDealPercent() + "%\nOFF");
                }
            } else {
                if (item.getPricingOption() == ITEM_PRICING_PAID) {
                    if (item.getDealPrice() > 0) {
                        viewHolder.priceTv.setText("₹" + item.getDealPrice());
                    } else {
                        viewHolder.priceTv.setText("₹" + item.getSellingPrice());
                    }
                    if (item.getMrp().equals(item.getSellingPrice())) {
                        viewHolder.mrpTv.setVisibility(View.GONE);
                    } else {
                        viewHolder.mrpTv.setVisibility(View.VISIBLE);
                        viewHolder.mrpTv.setText("₹" + item.getMrp());
                        viewHolder.mrpTv.setPaintFlags(viewHolder.mrpTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    }
                    if (!TextUtils.isEmpty(item.getSavingPercentText())) {
                        viewHolder.savingTv.setVisibility(View.VISIBLE);
                        viewHolder.savingTv.setText(item.getSavingPercentText());
                    } else {
                        viewHolder.savingTv.setVisibility(View.GONE);
                    }
                } else {
                    viewHolder.priceTv.setText("Request Price");
                    viewHolder.mrpTv.setVisibility(View.GONE);
                }
            }

            final ArrayList<PhotoData> photoList = item.getPhotos();
            if (photoList.size() > 0) {
                glide.load(photoList.get(0).getUrl())
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(viewHolder.itemIv);
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
        final TextView savingTv;

        ViewHolder(View view) {
            super(view);
            itemIv = view.findViewById(R.id.iv_item);
            nameTv = view.findViewById(R.id.tv_name);
            priceTv = view.findViewById(R.id.tv_price);
            mrpTv = view.findViewById(R.id.tv_mrp);
            savingTv = view.findViewById(R.id.tv_saving_text);
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
