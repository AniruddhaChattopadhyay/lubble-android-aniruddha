package in.lubble.app.feed;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.radiobutton.MaterialRadioButton;

import java.util.List;

import in.lubble.app.R;

public class GroupSelectionAdapter extends RecyclerView.Adapter<GroupSelectionAdapter.GroupSelectionViewHolder> {

    private int lastCheckedPos = 0;
    private List<String> stringList;

    public GroupSelectionAdapter(List<String> stringList) {
        this.stringList = stringList;
    }

    @NonNull
    @Override
    public GroupSelectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group_selection, parent, false);
        return new GroupSelectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupSelectionViewHolder holder, int position) {
        String s = stringList.get(position);

        holder.titleTv.setText(s);

        holder.selectionRb.setChecked(position == lastCheckedPos);
        holder.titleTv.setOnClickListener(v -> holder.selectionRb.performClick());
        holder.selectionRb.setOnClickListener(v -> {
            int copyOfLastCheckedPosition = lastCheckedPos;
            lastCheckedPos = holder.getAdapterPosition();
            notifyItemChanged(copyOfLastCheckedPosition);
            notifyItemChanged(lastCheckedPos);
        });

    }

    @Override
    public int getItemCount() {
        return stringList.size();
    }

    public static class GroupSelectionViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView titleTv;
        final MaterialRadioButton selectionRb;

        GroupSelectionViewHolder(View view) {
            super(view);
            mView = view;
            titleTv = view.findViewById(R.id.tv_group_name);
            selectionRb = view.findViewById(R.id.rb_group_selection);
        }

    }

}
