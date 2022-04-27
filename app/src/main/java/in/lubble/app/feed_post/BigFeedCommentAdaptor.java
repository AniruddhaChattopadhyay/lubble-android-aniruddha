package in.lubble.app.feed_post;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.animation.Animator;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.protobuf.Enum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
    private List<Boolean> toogleLikeList;
    private List<Reaction> newReactionList;
    private List<Integer> numberOfLikesList;

    public BigFeedCommentAdaptor(Context context, GlideRequests glide, List<Reaction> reactionList) {
        this.reactionList = reactionList;
        this.context = context;
        this.glide = glide;
        toogleLikeList = new ArrayList<>(Collections.nCopies(reactionList.size(), false));
        newReactionList = new ArrayList<>(Collections.nCopies(reactionList.size(), null));
        numberOfLikesList = new ArrayList<>(Collections.nCopies(reactionList.size(), 0));
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener {
        private EmojiTextView commentTv;
        private TextView commentTimestampTv, commentUserNameTv;
        private ImageView commentProfilePicIv;
        private TextView likeOnCommentTv;
        private ImageView likeOnCommentIv;
        private LottieAnimationView likeAnimation;
        private LinearLayout likeOnCommentLayout;
        private View touchView;
        private RelativeLayout commentSectionLayout;
        private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener(){
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
                if(touchView.getId() == R.id.cont_like_on_comment)
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
            boolean b = gestureDetector.onTouchEvent(motionEvent);
            return b;
        }

    }

    @NonNull
    @Override
    public BigFeedCommentAdaptor.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.post_expanded_comment_row, parent, false);
        return new BigFeedCommentAdaptor.MyViewHolder(itemView);
    }

    private void displayLikes(int numOfLikes,TextView likeOnCommentTv){
        String text = "";
        if(numOfLikes==0) {
            likeOnCommentTv.setText("like");
            return;
        }
        else if(numOfLikes==1)
            text = " like";
        else
            text = " likes";

        likeOnCommentTv.setText(Integer.toString(numOfLikes) + text);


    }
    private void toggleLike(ImageView likeIv, LottieAnimationView likeAnimation, TextView likeOnCommentTv, int position){
        Reaction reaction = reactionList.get(position);
        Map<String,List<Reaction>> reactionOwnChildren= reaction.getOwnChildren();
        String uid = FirebaseAuth.getInstance().getUid();
        CloudClient timelineClient = FeedServices.getTimelineClient();
        assert timelineClient!=null;
        int numOfLikes = numberOfLikesList.get(position);
        if(!toogleLikeList.get(position)){
            likeIv.setVisibility(View.GONE);
            likeAnimation.setVisibility(View.VISIBLE);
            likeAnimation.playAnimation();
            likeAnimation.addAnimatorListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {

                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            try {
                newReactionList.add(position,timelineClient.reactions().addChild(uid,"like", reaction.getId()).get());
            } catch (StreamException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            numOfLikes++;
            toogleLikeList.add(position,true);
        }
        else{
            likeAnimation.setVisibility(View.GONE);
            likeIv.setVisibility(View.VISIBLE);
            String reactionId = "";
            if(newReactionList.get(position)!=null)
                reactionId = newReactionList.get(position).getId();
            else if(reactionOwnChildren!=null && reactionOwnChildren.containsKey("like"))
                reactionId = Objects.requireNonNull(reactionOwnChildren.get("like")).get(0).getId();
            if(!reactionId.equals("")) {
                try {
                    timelineClient.reactions().delete(reactionId);
                } catch (StreamException e) {
                    e.printStackTrace();
                }
                numOfLikes = numOfLikes==0?0:numOfLikes-1;
                toogleLikeList.add(position,false);
            }
        }

        displayLikes(numOfLikes,likeOnCommentTv);
        numberOfLikesList.add(position,numOfLikes);

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull BigFeedCommentAdaptor.MyViewHolder holder, int position) {
        Reaction reaction = reactionList.get(position);
        Map<String, Object> activityMap = reaction.getActivityData();
        if (activityMap != null && activityMap.containsKey("text")) {
            holder.commentTv.setText(activityMap.get("text").toString());
            Object timestamp = reaction.getExtra().get("created_at");
            Map<String,List<Reaction>> reactionOwnChildren= reaction.getOwnChildren();
            if(reactionOwnChildren != null) {
                holder.likeAnimation.setVisibility(View.VISIBLE);
                holder.likeOnCommentIv.setVisibility(View.GONE);
                toogleLikeList.add(position,true);
            }
            else {
                toogleLikeList.add(position, false);
            }
            if(reaction.getChildrenCounts().containsKey("like")) {
                int likes = (int) reaction.getChildrenCounts().get("like");
                numberOfLikesList.add(position,likes);
                displayLikes(likes,holder.likeOnCommentTv);
            }

//            holder.likeOnCommentLayout.setOnClickListener(view -> {
//                toggleLike(holder.likeOnCommentIv,holder.likeAnimation,holder.likeOnCommentTv,position);
//            });

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