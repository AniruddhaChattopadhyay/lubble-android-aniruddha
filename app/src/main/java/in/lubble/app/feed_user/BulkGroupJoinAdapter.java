package in.lubble.app.feed_user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import in.lubble.app.R;
import in.lubble.app.models.FeedGroupData;

public class BulkGroupJoinAdapter extends RecyclerView.Adapter<BulkGroupJoinAdapter.BulkGroupJoinViewHolder> {

    private HashSet<Integer> lastCheckedPosSet = new HashSet<>();
    private final List<FeedGroupData> stringList;
    private final List<FeedGroupData> stringListCopy;

    public BulkGroupJoinAdapter(List<FeedGroupData> stringList) {
        this.stringList = stringList;
        stringListCopy = new ArrayList<>();
        stringListCopy.addAll(stringList);
    }

    public static class BulkGroupJoinViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView titleTv;
        final MaterialCheckBox selectionRb;

        BulkGroupJoinViewHolder(View view) {
            super(view);
            mView = view;
            titleTv = view.findViewById(R.id.tv_group_name);
            selectionRb = view.findViewById(R.id.rb_group_selection);
        }

    }



    @NonNull
    @Override
    public BulkGroupJoinAdapter.BulkGroupJoinViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bulk_group_join_rv_row, parent, false);
        return new BulkGroupJoinViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BulkGroupJoinAdapter.BulkGroupJoinViewHolder holder, int position) {

        FeedGroupData feedGroupData = stringList.get(position);

        holder.titleTv.setText(feedGroupData.getName());

        holder.selectionRb.setChecked(lastCheckedPosSet.contains(position));
        holder.titleTv.setOnClickListener(v -> holder.selectionRb.performClick());
        holder.selectionRb.setOnClickListener(v -> {
            if(lastCheckedPosSet.contains(position)){
                lastCheckedPosSet.remove(position);
            }
            else{
                lastCheckedPosSet.add(position);
            }
            notifyItemChanged(position);
        });
    }

    HashSet<Integer> getLastCheckedPosSet() {
        return lastCheckedPosSet;
    }

    public void filter(String text) {
        stringList.clear();
        if (text.isEmpty()) {
            stringList.addAll(stringListCopy);
        } else {
            text = text.toLowerCase();
            for (FeedGroupData groupData : stringListCopy) {
                if (groupData.getName().toLowerCase().contains(text) || groupData.getName().toLowerCase().contains(text)) {
                    stringList.add(groupData);
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return stringList.size();
    }


}
