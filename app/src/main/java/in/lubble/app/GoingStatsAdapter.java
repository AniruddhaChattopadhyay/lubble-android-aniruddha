package in.lubble.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import in.lubble.app.models.EventData;

public class GoingStatsAdapter extends RecyclerView.Adapter<GoingStatsAdapter.MyViewHolder> {
    private List<GoingStatsModel> goingStatsModelList;
    private Context context;

    GoingStatsAdapter(Context context, List<GoingStatsModel> goingStatsModelList) {
        this.goingStatsModelList = goingStatsModelList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_going_stats, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        GoingStatsModel goingStatsModel = goingStatsModelList.get(position);
        GlideApp.with(context).
                load(goingStatsModel.getUrl())
                .placeholder(R.drawable.ic_account_circle_black_no_padding)
                .error(R.drawable.ic_account_circle_black_no_padding)
                .circleCrop()
                .into(holder.profilePicture);
        holder.nameTextView.setText(goingStatsModel.getName());
        if (goingStatsModel.getStatus() == EventData.GOING)
            holder.statsTextView.setText("Going");
        else if (goingStatsModel.getStatus() == EventData.MAYBE)
            holder.statsTextView.setText("Maybe");
        else
            holder.statsTextView.setText("Not Going");
    }

    @Override
    public int getItemCount() {
        return goingStatsModelList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePicture;
        TextView nameTextView, statsTextView;

        MyViewHolder(View v) {
            super(v);
            profilePicture = v.findViewById(R.id.goingStatsImg);
            nameTextView = v.findViewById(R.id.goingStatsName);
            statsTextView = v.findViewById(R.id.goingStatsTv);

        }
    }
}
