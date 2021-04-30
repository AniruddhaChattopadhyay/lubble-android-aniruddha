package in.lubble.app.feed_user;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

import in.lubble.app.R;
import io.getstream.core.models.Reaction;

public class FeedCommentAdaptor extends RecyclerView.Adapter<FeedCommentAdaptor.MyViewHolder>{

    private Context context;
    private List<Reaction> reactionList;
    FeedCommentAdaptor(Context context, List<Reaction> reactionList){
        this.reactionList = reactionList;
        this.context = context;
    }
    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView commentTV;
        private TextView commentUserNameTv;
        private ImageView commentUserPicIv;
        public MyViewHolder(@NonNull View view) {
            super(view);
            commentTV = view.findViewById(R.id.comment_textView);
            commentUserNameTv = view.findViewById(R.id.comment_user_display_name);
            commentUserPicIv = view.findViewById(R.id.comment_user_display_pic);
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
        Map<String,Object> map = reactionList.get(position).getActivityData();
        holder.commentTV.setText(map.get("text").toString());
        holder.commentUserNameTv.setText(map.get("userId").toString());
    }

    @Override
    public int getItemCount() {
        return reactionList.size();
    }

}
