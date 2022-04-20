package in.lubble.app.feed_post;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.emoji.widget.EmojiTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import in.lubble.app.GlideRequests;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.chat.CustomURLSpan;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.ProfileInfo;
import in.lubble.app.profile.ProfileActivity;
import in.lubble.app.services.FeedServices;
import in.lubble.app.utils.DateTimeUtils;
import io.getstream.cloud.CloudClient;
import io.getstream.core.LookupKind;
import io.getstream.core.exceptions.StreamException;
import io.getstream.core.models.Reaction;
import io.getstream.core.options.Limit;

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
        private TextView likeOnCommentTv;

        public MyViewHolder(@NonNull View view) {
            super(view);
            commentTv = view.findViewById(R.id.comment_textView);
            commentUserNameTv = view.findViewById(R.id.comment_user_display_name);
            commentProfilePicIv = view.findViewById(R.id.iv_comment_profile_pic);
            commentTimestampTv = view.findViewById(R.id.tv_comment_timestamp);
            likeOnCommentTv = view.findViewById(R.id.like_on_comment_tv);
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
        String uid = FirebaseAuth.getInstance().getUid();
        final String[] numberOfLikes = {"0"};
        final Reaction[] newReaction = {null};
        final boolean[] toogleLike = {false};
        if (activityMap != null && activityMap.containsKey("text")) {
            holder.commentTv.setText(activityMap.get("text").toString());
            Object timestamp = reaction.getExtra().get("created_at");
            Map<String,List<Reaction>> reactionOwnChildren= reaction.getOwnChildren();
            if(reactionOwnChildren != null) {
                toogleLike[0] = true;
                holder.likeOnCommentTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_24dp, 0, 0, 0);
            }
            if(reaction.getChildrenCounts().containsKey("like")) {
                numberOfLikes[0] = Integer.toString((Integer) reaction.getChildrenCounts().get("like"));
                holder.likeOnCommentTv.setText(numberOfLikes[0] + " likes");
            }

            holder.likeOnCommentTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CloudClient timelineClient = FeedServices.getTimelineClient();
                    assert timelineClient!=null;
                    if(!toogleLike[0]){
                        holder.likeOnCommentTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_24dp,0,0,0);
                        try {
                            newReaction[0] = timelineClient.reactions().addChild(uid,"like", reaction.getId()).get();
                        } catch (StreamException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        numberOfLikes[0] = Integer.toString(Integer.parseInt(numberOfLikes[0]) + 1);
                        holder.likeOnCommentTv.setText(numberOfLikes[0] + " likes");
                        toogleLike[0] = true;
                    }
                    else{
                        holder.likeOnCommentTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_border_light,0,0,0);
                        String reactionId = "";
                        if(newReaction[0]!=null)
                            reactionId = newReaction[0].getId();
                        else if(reactionOwnChildren!=null && reactionOwnChildren.containsKey("like"))
                            reactionId = Objects.requireNonNull(reactionOwnChildren.get("like")).get(0).getId();

                        if(!reactionId.equals("")) {
                            try {
                                timelineClient.reactions().delete(reactionId);
                            } catch (StreamException e) {
                                e.printStackTrace();
                            }
                            int numOfLikes = Integer.parseInt(numberOfLikes[0]);
                            numOfLikes = numOfLikes==0?0:numOfLikes-1;
                            numberOfLikes[0] = Integer.toString(numOfLikes);
                            toogleLike[0] = false;
                        }
                    }
                    holder.likeOnCommentTv.setText(numberOfLikes[0] + " likes");
                }
            });

            if (timestamp instanceof String && !TextUtils.isEmpty(String.valueOf(timestamp))) {
                holder.commentTimestampTv.setText(DateTimeUtils.getHumanTimestamp((String) timestamp));
            }

            final String userId;
            if (reaction.getUserID() == null && reaction.getExtra() != null) {
                userId = String.valueOf(reaction.getExtra().get("userId"));
            } else {
                userId = reaction.getUserID();
            }

            Linkify.addLinks(holder.commentTv, Linkify.ALL);
            CustomURLSpan.clickifyTextView(holder.commentTv, () -> {
                Bundle bundle = new Bundle();
                bundle.putString("post_id", reaction.getActivityID());
                bundle.putString("commenter_id", userId);
                Analytics.triggerEvent(AnalyticsEvents.COMMENT_LINK_CLICKED, bundle, context);
            });
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
            holder.commentProfilePicIv.setOnClickListener(v ->
                    ProfileActivity.open(context, userId)
            );
        }
    }

    @Override
    public int getItemCount() {
        return reactionList.size();
    }

}