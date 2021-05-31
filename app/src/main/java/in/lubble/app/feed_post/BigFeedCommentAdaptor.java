package in.lubble.app.feed_post;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.emoji.widget.EmojiTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Map;

import in.lubble.app.GlideRequests;
import in.lubble.app.R;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.ProfileInfo;
import in.lubble.app.utils.DateTimeUtils;
import io.getstream.core.models.Reaction;

public class BigFeedCommentAdaptor extends RecyclerView.Adapter<BigFeedCommentAdaptor.MyViewHolder> {

    private static final int MAX_LIST_COUNT = 2;
    private Context context;
    private GlideRequests glide;
    private List<Reaction> reactionList;

    public BigFeedCommentAdaptor(Context context, GlideRequests glide, List<Reaction> reactionList) {
        this.reactionList = reactionList;
        this.context = context;
        this.glide = glide;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private EmojiTextView commentTv;
        private TextView commentTimestampTv, commentUserNameTv;
        private ImageView commentProfilePicIv;

        public MyViewHolder(@NonNull View view) {
            super(view);
            commentTv = view.findViewById(R.id.comment_textView);
            commentUserNameTv = view.findViewById(R.id.comment_user_display_name);
            commentProfilePicIv = view.findViewById(R.id.iv_comment_profile_pic);
            commentTimestampTv = view.findViewById(R.id.tv_comment_timestamp);
        }
    }

    @NonNull
    @Override
    public BigFeedCommentAdaptor.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.post_expanded_comment_row, parent, false);
        return new BigFeedCommentAdaptor.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BigFeedCommentAdaptor.MyViewHolder holder, int position) {
        Reaction reaction = reactionList.get(position);
        Map<String, Object> activityMap = reaction.getActivityData();

        if(activityMap!=null && activityMap.containsKey("key"))
            holder.commentTv.setText(activityMap.get("text").toString());
        Object timestamp = reaction.getExtra().get("timestamp");
        if (timestamp != null && timestamp instanceof Long) {
            holder.commentTimestampTv.setText(DateTimeUtils.getHumanTimestamp((Long) timestamp));
        }

        String userId = reaction.getUserID();
        if (userId == null && reaction.getExtra() != null) {
            userId = String.valueOf(reaction.getExtra().get("userId"));
        }
        RealtimeDbHelper.getUserInfoRef(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final ProfileInfo profileInfo = snapshot.getValue(ProfileInfo.class);
                holder.commentUserNameTv.setText(profileInfo.getName());
                glide.load(profileInfo.getThumbnail())
                        .placeholder(R.drawable.ic_account_circle_grey_24dp)
                        .error(R.drawable.ic_account_circle_grey_24dp)
                        .circleCrop()
                        .into(holder.commentProfilePicIv);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return reactionList.size();
    }

}