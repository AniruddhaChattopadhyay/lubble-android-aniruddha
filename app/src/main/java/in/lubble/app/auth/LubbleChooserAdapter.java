package in.lubble.app.auth;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.lubble.app.R;
import in.lubble.app.utils.DialogInterface;

public class LubbleChooserAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "ServiceCategoryAdapter";

    private final List<LocationsData> lubbleList;
    private DialogInterface callback;

    public LubbleChooserAdapter(DialogInterface callback) {
        this.lubbleList = new ArrayList<>();
        this.callback = callback;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_simple_text, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        final ViewHolder viewHolder = (ViewHolder) holder;
        final LocationsData lubbleData = lubbleList.get(position);

        viewHolder.nameTv.setText(lubbleData.getLubbleName());

    }

    @Override
    public int getItemCount() {
        return lubbleList.size();
    }

    public void addData(LocationsData lubbleData) {
        lubbleList.add(lubbleData);
        notifyDataSetChanged();
    }

    public void clear() {
        lubbleList.clear();
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final View view;
        final TextView nameTv;

        ViewHolder(View view) {
            super(view);
            this.view = view;
            nameTv = view.findViewById(R.id.tv_text);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            callback.onClick(lubbleList.get(getAdapterPosition()));
        }
    }

}
