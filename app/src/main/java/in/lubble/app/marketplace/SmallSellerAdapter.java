package in.lubble.app.marketplace;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import in.lubble.app.models.marketplace.SellerData;
import in.lubble.app.utils.RoundedCornersTransformation;
import in.lubble.app.utils.UiUtils;

public class SmallSellerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "SmallItemAdapter";
    private static final int TYPE_ITEM = 172;
    private static final int TYPE_VIEW_ALL = 506;

    private final List<SellerData> sellerList;
    private final GlideRequests glide;
    private int categoryId;

    public SmallSellerAdapter(GlideRequests glide) {
        sellerList = new ArrayList<>();
        this.glide = glide;
    }

    @Override
    public int getItemViewType(int position) {
        if (sellerList.get(position) == null) {
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
            final SmallSellerAdapter.ViewHolder viewHolder = (SmallSellerAdapter.ViewHolder) holder;

            final SellerData sellerData = sellerList.get(position);

            viewHolder.nameTv.setText(sellerData.getName());
            viewHolder.savingTv.setVisibility(View.GONE);

            RequestOptions requestOptions = new RequestOptions();
            requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCornersTransformation(UiUtils.dpToPx(8), 0));
            if (!TextUtils.isEmpty(sellerData.getPhotoUrl())) {
                viewHolder.sellerIv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                viewHolder.sellerIv.setBackground(null);
                glide.load(sellerData.getPhotoUrl())
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .placeholder(R.drawable.gradient_black_trans)
                        .error(R.drawable.gradient_black_trans)
                        .apply(requestOptions)
                        .into(viewHolder.sellerIv);
            } else {
                viewHolder.sellerIv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                viewHolder.sellerIv.setBackground(ContextCompat.getDrawable(LubbleApp.getAppContext(), R.drawable.rounded_rect_very_light_gray));
                glide.load(R.drawable.ic_shop)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(viewHolder.sellerIv);
            }

        } else {
            // nothing to do here..
        }
    }

    @Override
    public int getItemCount() {
        return sellerList.size();
    }

    void addSeller(SellerData seller) {
        sellerList.add(seller);
        notifyDataSetChanged();
    }

    void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final ImageView sellerIv;
        final TextView nameTv;
        final TextView distanceTV;
        final TextView savingTv;

        ViewHolder(View view) {
            super(view);
            sellerIv = view.findViewById(R.id.iv_seller);
            nameTv = view.findViewById(R.id.tv_name);
            distanceTV = view.findViewById(R.id.tv_distance);
            savingTv = view.findViewById(R.id.tv_saving_text);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            ItemListActiv.open(v.getContext(), true, sellerList.get(getAdapterPosition()).getId());
        }
    }

    class ViewAllViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ViewAllViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            ItemListActiv.open(v.getContext(), false, categoryId);
        }
    }


}
