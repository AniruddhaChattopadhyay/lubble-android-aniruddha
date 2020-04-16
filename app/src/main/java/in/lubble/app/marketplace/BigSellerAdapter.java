package in.lubble.app.marketplace;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.ArrayList;
import java.util.List;

import in.lubble.app.Constants;
import in.lubble.app.GlideRequests;
import in.lubble.app.R;
import in.lubble.app.models.marketplace.SellerData;
import in.lubble.app.utils.UiUtils;

public class BigSellerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "BigItemAdapter";

    private final List<SellerData> sellerList;
    private final GlideRequests glide;

    public BigSellerAdapter(GlideRequests glide) {
        sellerList = new ArrayList<>();
        this.glide = glide;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BigSellerAdapter.ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.big_seller, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        final BigSellerAdapter.ViewHolder viewHolder = (BigSellerAdapter.ViewHolder) holder;
        final SellerData sellerData = sellerList.get(position);

        viewHolder.nameTv.setText(sellerData.getName());
        viewHolder.savingTv.setVisibility(View.GONE);

        if (!TextUtils.isEmpty(sellerData.getPhotoUrl())) {
            viewHolder.itemIv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            glide.load(sellerData.getPhotoUrl())
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .thumbnail(0.1f)
                    .placeholder(UiUtils.getCircularProgressDrawable(viewHolder.view.getContext()))
                    .into(viewHolder.itemIv);
        } else {
            viewHolder.itemIv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            glide.load(FirebaseRemoteConfig.getInstance().getString(Constants.DEFAULT_SHOP_PIC))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .thumbnail(0.1f)
                    .placeholder(UiUtils.getCircularProgressDrawable(viewHolder.view.getContext()))
                    .into(viewHolder.itemIv);
        }

        if ((sellerData.getDealPercent() > 0)) {
            viewHolder.savingTv.setVisibility(View.VISIBLE);
            viewHolder.savingTv.setText(sellerData.getDealPercent() + "%\nOFF");
        } else {
            viewHolder.savingTv.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(sellerData.getSubtitle())) {
            viewHolder.approvalStatusTv.setVisibility(View.VISIBLE);
            viewHolder.approvalStatusTv.setText(sellerData.getSubtitle());
        } else {
            viewHolder.approvalStatusTv.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return sellerList.size();
    }

    public void addData(SellerData sellerData) {
        sellerList.add(sellerData);
        notifyDataSetChanged();
    }

    public void clear() {
        sellerList.clear();
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final View view;
        final ImageView itemIv;
        final TextView approvalStatusTv;
        final TextView nameTv;
        final TextView mrpTv;
        final TextView savingTv;

        ViewHolder(View view) {
            super(view);
            this.view = view;
            itemIv = view.findViewById(R.id.iv_item);
            approvalStatusTv = view.findViewById(R.id.tv_header);
            nameTv = view.findViewById(R.id.tv_name);
            mrpTv = view.findViewById(R.id.tv_seller_subtitle);
            savingTv = view.findViewById(R.id.tv_saving_text);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            ItemListActiv.open(v.getContext(), true, sellerList.get(getAdapterPosition()).getId());
        }
    }


}
