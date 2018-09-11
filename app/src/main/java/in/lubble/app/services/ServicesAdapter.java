package in.lubble.app.services;

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
import in.lubble.app.marketplace.ItemActivity;
import in.lubble.app.models.marketplace.Item;
import in.lubble.app.models.marketplace.PhotoData;

public class ServicesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "ServiceCategoryAdapter";

    private final List<Item> itemList;
    private final GlideRequests glide;

    public ServicesAdapter(GlideRequests glide) {
        itemList = new ArrayList<>();
        this.glide = glide;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ServicesAdapter.ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        final ServicesAdapter.ViewHolder viewHolder = (ServicesAdapter.ViewHolder) holder;
        final Item item = itemList.get(position);

        viewHolder.nameTv.setText(item.getName());

        final ArrayList<PhotoData> photoList = item.getPhotos();
        if (photoList.size() > 0) {
            glide.load(photoList.get(0).getUrl())
                    .thumbnail(0.1f)
                    .circleCrop()
                    .into(viewHolder.iconIv);
        } else {
            glide.load(R.drawable.blue_circle)
                    .circleCrop()
                    .into(viewHolder.iconIv);
        }

        if (item.getDealPercent() > 0) {
            viewHolder.dealTv.setVisibility(View.VISIBLE);
            viewHolder.dealTv.setText(item.getDealPercent() + "% off");
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

    public void clear() {
        itemList.clear();
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final View view;
        final ImageView iconIv;
        final TextView nameTv;
        final TextView dealTv;

        ViewHolder(View view) {
            super(view);
            this.view = view;
            iconIv = view.findViewById(R.id.iv_service_icon);
            nameTv = view.findViewById(R.id.tv_service_name);
            dealTv = view.findViewById(R.id.tv_deal);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            v.getContext().startActivity(ItemActivity.getIntent(v.getContext(), itemList.get(getAdapterPosition()).getId()));
        }
    }


}
