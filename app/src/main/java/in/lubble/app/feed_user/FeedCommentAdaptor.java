package in.lubble.app.feed_user;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

import in.lubble.app.R;
import io.getstream.core.models.Reaction;

public class FeedCommentAdaptor extends RecyclerView.Adapter<FeedCommentAdaptor.MyViewHolder> {

    private static final int MAX_LIST_COUNT = 2;
    private Context context;
    private List<Reaction> reactionList;

    FeedCommentAdaptor(Context context, List<Reaction> reactionList) {
        this.reactionList = reactionList;
        this.context = context;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView commentTV;
        private TextView commentUserNameTv;

        public MyViewHolder(@NonNull View view) {
            super(view);
            commentTV = view.findViewById(R.id.comment_textView);
            commentUserNameTv = view.findViewById(R.id.comment_user_display_name);
        }
    }

    @NonNull
    @Override
    public FeedCommentAdaptor.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.feed_comment_list_row, parent, false);

        return new FeedCommentAdaptor.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedCommentAdaptor.MyViewHolder holder, int position) {
        Reaction reaction = reactionList.get(position);
        Map<String, Object> activityMap = reaction.getActivityData();

        holder.commentTV.setText(activityMap.get("text").toString());
        holder.commentUserNameTv.setText(String.valueOf(reaction.getUserData().getData().get("name")));
    }

    @Override
    public int getItemCount() {
        return Math.min(reactionList.size(), MAX_LIST_COUNT);
    }

}
