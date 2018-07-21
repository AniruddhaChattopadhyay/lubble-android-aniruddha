package in.lubble.app.marketplace;

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
import in.lubble.app.models.marketplace.Category;

public class ColoredChipsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "ColoredChipsAdapter";

    private final List<Category> list;
    private final GlideRequests glide;

    public ColoredChipsAdapter(GlideRequests glide) {
        list = new ArrayList<>();
        this.glide = glide;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ColoredChipsAdapter.ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chip, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        final ColoredChipsAdapter.ViewHolder viewHolder = (ColoredChipsAdapter.ViewHolder) holder;

        final Category category = list.get(position);

        viewHolder.nameTv.setText(category.getName());

        glide.load(category.getIcon())
                .into(viewHolder.iconIv);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void addData(Category category) {
        list.add(category);
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
            ItemListActiv.open(v.getContext(), false, list.get(getAdapterPosition()).getId());
        }
    }


}
