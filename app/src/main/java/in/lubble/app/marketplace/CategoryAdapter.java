package in.lubble.app.marketplace;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import in.lubble.app.GlideRequests;
import in.lubble.app.R;
import in.lubble.app.models.marketplace.Category;

public class CategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "CategoryAdapter";

    private final List<Category> categoryList;
    private CategorySelectedListener categorySelectedListener;
    private GlideRequests glide;

    public CategoryAdapter(GlideRequests glide, CategorySelectedListener categorySelectedListener) {
        categoryList = new ArrayList<>();
        this.categorySelectedListener = categorySelectedListener;
        this.glide = glide;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CategoryAdapter.ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        final CategoryAdapter.ViewHolder viewHolder = (CategoryAdapter.ViewHolder) holder;

        final Category category = categoryList.get(position);

        viewHolder.nameTv.setText(category.getHumanReadableName());

        glide.load(category.getIcon()).into(viewHolder.iconIv);

    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public void addData(Category category) {
        categoryList.add(category);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final ImageView iconIv;
        final TextView nameTv;

        ViewHolder(View view) {
            super(view);
            iconIv = view.findViewById(R.id.iv_icon);
            nameTv = view.findViewById(R.id.tv_label);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            categorySelectedListener.onSelected(categoryList.get(getAdapterPosition()));
        }
    }

}
