package in.lubble.app.feed_groups;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import in.lubble.app.R;
import in.lubble.app.models.FeedGroupData;

public class FeedGroupAdapter extends RecyclerView.Adapter<FeedGroupAdapter.MyViewHolder> {

    private List<FeedGroupData> feedGroupData;
    private Context context;

    public FeedGroupAdapter(Context context,List<FeedGroupData> feedGroupData) {
        this.feedGroupData = feedGroupData;
        this.context = context;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView groupNameTV;

        public MyViewHolder(View view) {
            super(view);
            groupNameTV = view.findViewById(R.id.feed_group_name_row);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.feed_groups_recyclerview_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.groupNameTV.setText(feedGroupData.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return feedGroupData.size();
    }
}
