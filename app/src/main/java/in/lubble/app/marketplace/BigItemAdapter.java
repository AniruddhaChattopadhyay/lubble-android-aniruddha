package in.lubble.app.marketplace;

import android.graphics.Paint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import in.lubble.app.GlideRequests;
import in.lubble.app.LubbleApp;
import in.lubble.app.R;
import in.lubble.app.models.marketplace.Item;
import in.lubble.app.models.marketplace.PhotoData;
import in.lubble.app.utils.RoundedCornersTransformation;
import in.lubble.app.utils.UiUtils;

import static in.lubble.app.models.marketplace.Item.ITEM_PRICING_PAID;

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
            viewHolder.itemPicProgressBar.setVisibility(View.GONE);
            RequestOptions requestOptions = new RequestOptions();
            requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCornersTransformation(UiUtils.dpToPx(8), 0));
            viewHolder.itemIv.setBackground(null);
            glide.load(photoList.get(0).getUrl())
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .thumbnail(0.1f)
                    .apply(requestOptions)
                    .into(viewHolder.itemIv);
        } else {
            viewHolder.itemPicProgressBar.setVisibility(View.VISIBLE);
            viewHolder.itemIv.setBackground(ContextCompat.getDrawable(LubbleApp.getAppContext(), R.drawable.rounded_rect_very_light_gray));
            glide.load("")
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
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
        final TextView savingTv;

        ViewHolder(View view) {
            super(view);
            this.view = view;
            itemIv = view.findViewById(R.id.iv_item);
            itemPicProgressBar = view.findViewById(R.id.progress_bar_item_pic);
            approvalStatusTv = view.findViewById(R.id.tv_approval_status);
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


}
