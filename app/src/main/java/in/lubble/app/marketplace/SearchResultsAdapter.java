package in.lubble.app.marketplace;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import in.lubble.app.R;

public class SearchResultsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "SearchResultsAdapter";

    private final List<ItemSearchData> itemSearchDataList;
    private Context context;

    public SearchResultsAdapter(Context context) {
        this.context = context;
        itemSearchDataList = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SearchResultsAdapter.ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_result, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        final SearchResultsAdapter.ViewHolder viewHolder = (SearchResultsAdapter.ViewHolder) holder;

        final ItemSearchData itemSearchData = itemSearchDataList.get(position);

        viewHolder.itemNameTv.setText(itemSearchData.getName());
        viewHolder.entityTv.setText("in " + itemSearchData.getEntity());

    }

    @Override
    public int getItemCount() {
        return itemSearchDataList.size();
    }

    public void addData(ArrayList<ItemSearchData> searchResultsList) {
        itemSearchDataList.clear();
        itemSearchDataList.addAll(searchResultsList);
        notifyDataSetChanged();
    }

    public void clearAll() {
        itemSearchDataList.clear();
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final View view;
        final TextView itemNameTv, entityTv;

        ViewHolder(View view) {
            super(view);
            this.view = view;
            itemNameTv = view.findViewById(R.id.tv_item_name);
            entityTv = view.findViewById(R.id.tv_entity);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            final ItemSearchData itemSearchData = itemSearchDataList.get(getAdapterPosition());
            switch (itemSearchData.getEntity().toLowerCase()) {
                case "seller":
                    ItemListActiv.open(context, true, itemSearchData.getId());
                    break;
                case "item":
                    context.startActivity(ItemActivity.getIntent(context, itemSearchData.getId()));
                    break;
                case "category":
                    ItemListActiv.open(context, false, itemSearchData.getId());
                    break;
            }
        }
    }

}
