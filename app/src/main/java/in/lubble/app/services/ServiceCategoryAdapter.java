package in.lubble.app.services;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import in.lubble.app.GlideRequests;
import in.lubble.app.R;
import in.lubble.app.models.marketplace.Category;
import in.lubble.app.utils.RoundedCornersTransformation;
import in.lubble.app.utils.UiUtils;

import static in.lubble.app.utils.RoundedCornersTransformation.CornerType.TOP;

public class ServiceCategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "ServiceCategoryAdapter";

    private final List<Category> categoryList;
    private final GlideRequests glide;

    public ServiceCategoryAdapter(GlideRequests glide) {
        categoryList = new ArrayList<>();
        this.glide = glide;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ServiceCategoryAdapter.ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service_category, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        final ServiceCategoryAdapter.ViewHolder viewHolder = (ServiceCategoryAdapter.ViewHolder) holder;
        final Category category = categoryList.get(position);

        viewHolder.nameTv.setText(category.getName());

        final String picUrl = category.getIcon();
        viewHolder.itemPicProgressBar.setVisibility(View.GONE);
        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCornersTransformation(UiUtils.dpToPx(8), 0, TOP));
        glide.load(picUrl)
                .thumbnail(0.1f)
                .placeholder(R.drawable.rounded_rect_gray)
                .error(R.drawable.rounded_rect_gray)
                .apply(requestOptions)
                .into(viewHolder.itemIv);

    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public void addData(Category category) {
        categoryList.add(category);
        notifyDataSetChanged();
    }

    public void clear() {
        categoryList.clear();
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final View view;
        final ImageView itemIv;
        final ProgressBar itemPicProgressBar;
        final TextView nameTv;

        ViewHolder(View view) {
            super(view);
            this.view = view;
            itemIv = view.findViewById(R.id.iv_item);
            itemPicProgressBar = view.findViewById(R.id.progress_bar_item_pic);
            nameTv = view.findViewById(R.id.tv_name);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            ServiceCategoryDetailActiv.open(v.getContext(), categoryList.get(getAdapterPosition()).getId());
        }
    }


}
