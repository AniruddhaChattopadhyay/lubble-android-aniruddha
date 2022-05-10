package in.lubble.app.feed_post;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.emoji.widget.EmojiTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import io.getstream.core.exceptions.StreamException;
import io.getstream.core.models.FeedID;
import io.getstream.core.models.Reaction;

public class BigFeedCommentAdaptor extends RecyclerView.Adapter<BigFeedCommentAdaptor.MyViewHolder> {

    private final Context context;
    private final GlideRequests glide;
    private final List<Reaction> reactionList;
    private final HashMap<String, Integer> likeCountMap = new HashMap<>();

    public BigFeedCommentAdaptor(Context context, GlideRequests glide, List<Reaction> reactionList) {
        this.reactionList = reactionList;
        this.context = context;
        this.glide = glide;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener {
        private final EmojiTextView commentTv;
        private final TextView commentTimestampTv;
        private final TextView commentUserNameTv;
        private final ImageView commentProfilePicIv;
        private final TextView likeOnCommentTv;
        private final ImageView likeOnCommentIv;
        private final LottieAnimationView likeAnimation;
        private final LinearLayout likeOnCommentLayout;
        private View touchView;
        private final RelativeLayout commentSectionLayout;
        private final GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                toggleLike(likeOnCommentIv, likeAnimation, likeOnCommentTv, getBindingAdapterPosition());
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (getBindingAdapterPosition() == NO_POSITION) {
                    Toast.makeText(context, R.string.all_try_again, Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (touchView.getId() == R.id.cont_like_on_comment)
                    toggleLike(likeOnCommentIv, likeAnimation, likeOnCommentTv, getBindingAdapterPosition());
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                return true;
            }
        });

        public MyViewHolder(@NonNull View view) {
            super(view);
            commentTv = view.findViewById(R.id.comment_textView);
            commentUserNameTv = view.findViewById(R.id.comment_user_display_name);
            commentProfilePicIv = view.findViewById(R.id.iv_comment_profile_pic);
            commentTimestampTv = view.findViewById(R.id.tv_comment_timestamp);
            likeOnCommentTv = view.findViewById(R.id.tv_like);
            likeAnimation = view.findViewById(R.id.anim_comment_like);
            likeOnCommentIv = view.findViewById(R.id.like_imageview);
            likeOnCommentLayout = view.findViewById(R.id.cont_like_on_comment);
            commentSectionLayout = view.findViewById(R.id.comment_layout);
            commentSectionLayout.setOnTouchListener(this);
            likeOnCommentLayout.setOnTouchListener(this);
            commentTv.setOnTouchListener(this);

        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            touchView = view;
            return gestureDetector.onTouchEvent(motionEvent);
        }

    }

    @NonNull
    @Override
    public BigFeedCommentAdaptor.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.post_expanded_comment_row, parent, false);
        return new BigFeedCommentAdaptor.MyViewHolder(itemView);
    }

    private void displayLikes(int numOfLikes, TextView likeOnCommentTv) {
        String text = "";
        if (numOfLikes == 0) {
            likeOnCommentTv.setText("Double tap to Like");
            return;
        } else if (numOfLikes == 1)
            text = " Like ";
        else
            text = " Likes";
        likeOnCommentTv.setText(numOfLikes + text);
    }

    private void toggleLike(ImageView likeIv, LottieAnimationView likeAnimation, TextView likeOnCommentTv, int position) {
        try {
            Reaction reaction = reactionList.get(position);
            String uid = FirebaseAuth.getInstance().getUid();
            CloudClient timelineClient = FeedServices.getTimelineClient();
            int updatedCount;
            if (likeIv.getVisibility() == View.VISIBLE) {
                // like the comment
                likeIv.setVisibility(View.GONE);
                likeAnimation.setVisibility(View.VISIBLE);
                likeAnimation.playAnimation();
                updatedCount = updateLikeCount(reaction, true);
                try {
                    Reaction childReaction = Reaction.builder()
                            .id(reaction.getId() + "-like-" + uid)
                            .kind("like").parent(reaction.getId())
                            .build();
                    String notifUserFeedId = "notification:" + reaction.getUserID();
                    timelineClient.reactions().addChild(uid, reaction.getId(), childReaction, new FeedID(notifUserFeedId));
                } catch (StreamException e) {
                    e.printStackTrace();
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            } else {
                //un-like the comment
                likeAnimation.setVisibility(View.GONE);
                likeIv.setVisibility(View.VISIBLE);
                updatedCount = updateLikeCount(reaction, false);
                try {
                    timelineClient.reactions().delete(reaction.getId() + "-like-" + uid);
                } catch (StreamException e) {
                    e.printStackTrace();
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            }
            displayLikes(updatedCount, likeOnCommentTv);
        } catch (Exception e) {
            Toast.makeText(context, "Failed! Try again", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    private int updateLikeCount(Reaction reaction, boolean toIncrement) {
        Number likeNumber = reaction.getChildrenCounts().get("like");
        int numOfLikes = 0;
        if (likeNumber != null)
            numOfLikes = likeNumber.intValue();
        Integer cachedLikeCount = likeCountMap.get(reaction.getId());
        int updatedCount;
        if (toIncrement) {
            if (cachedLikeCount != null) {
                updatedCount = cachedLikeCount + 1;
            } else {
                updatedCount = numOfLikes + 1;
            }
        } else {
            if (cachedLikeCount != null) {
                updatedCount = cachedLikeCount == 0 ? 0 : cachedLikeCount - 1;
            } else {
                updatedCount = numOfLikes == 0 ? 0 : numOfLikes - 1;
            }
        }
        likeCountMap.put(reaction.getId(), updatedCount);
        return updatedCount;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull BigFeedCommentAdaptor.MyViewHolder holder, int position) {
        Reaction reaction = reactionList.get(position);
        Map<String, Object> activityMap = reaction.getActivityData();
        if (activityMap != null && activityMap.containsKey("text")) {
            holder.commentTv.setText(activityMap.get("text").toString());
            Object timestamp = reaction.getExtra().get("created_at");
            Map<String, List<Reaction>> reactionOwnChildren = reaction.getOwnChildren();
            if (reactionOwnChildren != null) {
                holder.likeAnimation.setVisibility(View.VISIBLE);
                holder.likeOnCommentIv.setVisibility(View.GONE);
            } else {
                holder.likeAnimation.setVisibility(View.GONE);
                holder.likeOnCommentIv.setVisibility(View.VISIBLE);
            }
            if (reaction.getChildrenCounts().containsKey("like")) {
                int likes = (int) reaction.getChildrenCounts().get("like");
                displayLikes(likes, holder.likeOnCommentTv);
            } else {
                displayLikes(0, holder.likeOnCommentTv);
            }

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