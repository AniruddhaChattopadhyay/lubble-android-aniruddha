package in.lubble.app.feed_user;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.feed_post.FeedPostActivity;
import in.lubble.app.services.FeedServices;
import in.lubble.app.utils.RoundedCornersTransformation;
import in.lubble.app.utils.UiUtils;
import io.getstream.core.exceptions.StreamException;
import io.getstream.core.models.EnrichedActivity;
import io.getstream.core.models.Reaction;

import static android.view.View.GONE;
import static in.lubble.app.utils.UiUtils.dpToPx;

public class FeedAdaptor extends RecyclerView.Adapter<FeedAdaptor.MyViewHolder> {

    private static final String TAG = "FeedAdaptor";
    private final List<EnrichedActivity> activityList;
    private final Context context;
    private final ReplyClickListener replyClickListener;
    private final HashMap<Integer, String> likedMap = new HashMap<>();
    private final String userId = FirebaseAuth.getInstance().getUid();

    public FeedAdaptor(Context context, List<EnrichedActivity> moviesList, ReplyClickListener replyClickListener) {
        this.activityList = moviesList;
        this.context = context;
        this.replyClickListener = replyClickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.feed_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        EnrichedActivity activity = activityList.get(position);
        String postDateDisplay = getPostDateDisplay(activity.getTime());
        Map<String, Object> extras = activity.getExtra();

        holder.photoContentIv.setVisibility(View.GONE);
        holder.groupNameTv.setVisibility(View.GONE);
        holder.lubbleNameTv.setVisibility(View.GONE);
        if (extras != null) {
            if (extras.containsKey("message")) {
                holder.textContentTv.setVisibility(View.VISIBLE);
                holder.textContentTv.setText(String.valueOf(extras.get("message")));
            }
            if (extras.containsKey("photoLink")) {
                holder.photoContentIv.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(extras.get("photoLink").toString())
                        .transform(new RoundedCornersTransformation(dpToPx(8), 0))
                        .into(holder.photoContentIv);
            }

            if (extras.containsKey("authorName")) {
                holder.authorNameTv.setText(extras.get("authorName").toString());
            }
            if (extras.containsKey("group")) {
                holder.groupNameTv.setVisibility(View.VISIBLE);
                holder.groupNameTv.setText(extras.get("group").toString());
            }
            if (extras.containsKey("lubble_id")) {
                holder.lubbleNameTv.setVisibility(View.VISIBLE);
                holder.lubbleNameTv.setText(extras.get("lubble_id").toString());
            }
        }
        Map<String, Object> actorMap = activity.getActor().getData();
        if (actorMap.containsKey("name")) {
            holder.authorNameTv.setText(String.valueOf(actorMap.get("name")));
            if (actorMap.containsKey("profile_picture")) {
                Glide.with(context)
                        .load(actorMap.get("profile_picture").toString())
                        .placeholder(R.drawable.ic_account_circle_black_no_padding)
                        .error(R.drawable.ic_account_circle_black_no_padding)
                        .circleCrop()
                        .into(holder.authorPhotoIv);
            }
        }
        holder.timePostedTv.setText(postDateDisplay);

        List<Reaction> userLikes = activity.getOwnReactions().get("like");
        if (userLikes != null && userLikes.size() > 0) {
            holder.likeIv.setImageResource(R.drawable.ic_favorite_24dp);
            likedMap.put(position, userLikes.get(0).getId());
        } else {
            holder.likeIv.setImageResource(R.drawable.ic_favorite_border_light);
            likedMap.remove(position);
        }

        handleReactionStats(activity, holder);
        initCommentRecyclerView(holder, activity);
        handleCommentEditText(activity, holder);

        holder.likeLayout.setOnClickListener(v -> toggleLike(holder, position));
        holder.commentLayout.setOnClickListener(v -> {
            replyClickListener.onReplyClicked(activity.getID(), position);
        });
        holder.replyStatsTv.setOnClickListener(v -> {
            FeedPostActivity.open(context, activity.getID());
        });
        holder.itemView.setOnClickListener(v -> {
            FeedPostActivity.open(context, activity.getID());
        });
    }

    private void handleCommentEditText(EnrichedActivity activity, MyViewHolder holder) {
        GlideApp.with(context)
                .load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl())
                .apply(new RequestOptions().override(UiUtils.dpToPx(24), UiUtils.dpToPx(24)))
                .circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE) //caches final image after transformations
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        holder.commentEdtText.setCompoundDrawablesWithIntrinsicBounds(resource, null, null, null);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        holder.commentEdtText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_account_circle_grey_24dp, 0, 0, 0);
                    }
                });
        holder.commentEdtText.setOnClickListener(v -> replyClickListener.onReplyClicked(activity.getID(), holder.getAdapterPosition()));
    }

    public interface ReplyClickListener {
        void onReplyClicked(String activityId, int position);
    }

    private void initCommentRecyclerView(MyViewHolder holder, EnrichedActivity activity) {
        List<Reaction> commentList = activity.getLatestReactions().get("comment");
        if (commentList != null && commentList.size() > 0) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
            holder.commentRecyclerView.setVisibility(View.VISIBLE);
            holder.viewAllRepliesTv.setVisibility(View.VISIBLE);
            holder.commentRecyclerView.setLayoutManager(layoutManager);
            holder.commentRecyclerView.setNestedScrollingEnabled(false);
            holder.viewAllRepliesTv.setOnClickListener(v -> {
                FeedPostActivity.open(context, activity.getID());
            });
            FeedCommentAdaptor adapter = new FeedCommentAdaptor(context, commentList);
            holder.commentRecyclerView.setAdapter(adapter);
        } else {
            holder.commentRecyclerView.setVisibility(GONE);
            holder.viewAllRepliesTv.setVisibility(GONE);
        }
    }

    private void handleReactionStats(EnrichedActivity enrichedActivity, MyViewHolder holder) {
        extractReactionCount(enrichedActivity, "like", holder.likeStatsTv, R.plurals.likes, 0);
        extractReactionCount(enrichedActivity, "comment", holder.replyStatsTv, R.plurals.replies, 0);
    }

    private void extractReactionCount(EnrichedActivity enrichedActivity, @NotNull String reaction, TextView statsTv, int stringRes, int change) {
        Number reactionNumber = enrichedActivity.getReactionCounts().get(reaction);
        if (reactionNumber == null) {
            reactionNumber = 0;
        }
        int reactionCount = reactionNumber.intValue();
        if (change != 0) {
            reactionCount += change;
            enrichedActivity.getReactionCounts().put(reaction, reactionCount);
        }
        if (reactionCount > 0) {
            statsTv.setVisibility(View.VISIBLE);
            statsTv.setText(reactionCount + " " + context.getResources().getQuantityString(stringRes, reactionCount));
        } else {
            statsTv.setVisibility(GONE);
        }
    }

    private void toggleLike(MyViewHolder holder, int position) {
        EnrichedActivity activity = activityList.get(position);
        if (!likedMap.containsKey(position)) {
            // like
            Reaction like = new Reaction.Builder()
                    .kind("like")
                    .id(userId)
                    .activityID(activity.getID())
                    .build();
            try {
                FeedServices.getTimelineClient().reactions().add(like).whenComplete((reaction, throwable) -> {
                    if (throwable != null) {
                        //todo
                    }
                });
                holder.likeIv.setImageResource(R.drawable.ic_favorite_24dp);
                likedMap.put(position, like.getId());
                extractReactionCount(activity, "like", holder.likeStatsTv, R.plurals.likes, 1);
            } catch (StreamException e) {
                e.printStackTrace();
                //todo
            }
        } else {
            // unlike
            try {
                FeedServices.getTimelineClient().reactions().delete(likedMap.get(position)).whenComplete((aVoid, throwable) -> {
                    if (throwable != null) {
                        //todo
                    }
                });
                holder.likeIv.setImageResource(R.drawable.ic_favorite_border_24dp);
                likedMap.remove(position);
                extractReactionCount(activity, "like", holder.likeStatsTv, R.plurals.likes, -1);
            } catch (StreamException e) {
                e.printStackTrace();
                //todo
            }
        }
    }

    private String getPostDateDisplay(Date timePosted) {
        Date timeNow = new Date(System.currentTimeMillis());
        long duration = timeNow.getTime() - timePosted.getTime();

        long diffInSeconds = TimeUnit.MILLISECONDS.toSeconds(duration);
        long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        long diffInHours = TimeUnit.MILLISECONDS.toHours(duration);
        long diffInDays = TimeUnit.MILLISECONDS.toDays(duration);

        if (diffInDays > 0) {
            return diffInDays + "d";
        } else if (diffInHours > 0) {
            return diffInHours + "hr";
        } else if (diffInMinutes > 0) {
            return diffInMinutes + "min";
        } else if (diffInSeconds > 0) {
            return "just now";
        }
        return "Just Now";
    }

    public void addUserReply(String activityId, Reaction reaction) {
        int pos = getActivityPosById(activityId);
        if (pos >= 0) {
            EnrichedActivity updatedActivity = activityList.get(pos);
            List<Reaction> latestCommentList = updatedActivity.getLatestReactions().get("comment");
            if (latestCommentList == null)
                latestCommentList = new ArrayList<>();
            latestCommentList.add(0, reaction);
            updatedActivity.getLatestReactions().put("comment", latestCommentList);
            notifyItemChanged(pos);
        }
    }

    private int getActivityPosById(String activityId) {
        for (int i = 0; i < activityList.size(); i++)
            if (activityId.equalsIgnoreCase(activityList.get(i).getID()))
                return i;
        return -1;
    }

    @Override
    public int getItemCount() {
        return activityList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView textContentTv;
        private ImageView photoContentIv;
        private ImageView authorPhotoIv;
        private TextView viewAllRepliesTv, authorNameTv, timePostedTv, groupNameTv, lubbleNameTv;
        private LinearLayout likeLayout;
        private TextView likeStatsTv, replyStatsTv;
        private ImageView likeIv;
        private int likeCount = 0;
        private LinearLayout commentLayout;
        private TextView commentEdtText;
        private RecyclerView commentRecyclerView;

        public MyViewHolder(View view) {
            super(view);
            textContentTv = view.findViewById(R.id.feed_text_content);
            photoContentIv = view.findViewById(R.id.feed_photo_content);
            authorNameTv = view.findViewById(R.id.feed_author_name);
            groupNameTv = view.findViewById(R.id.tv_group_name);
            lubbleNameTv = view.findViewById(R.id.tv_lubble_name);
            authorPhotoIv = view.findViewById(R.id.feed_author_photo);
            viewAllRepliesTv = view.findViewById(R.id.tv_view_all_replies);
            timePostedTv = view.findViewById(R.id.feed_post_timestamp);
            likeLayout = view.findViewById(R.id.cont_like);
            likeStatsTv = view.findViewById(R.id.tv_like_stats);
            replyStatsTv = view.findViewById(R.id.tv_reply_stats);
            likeIv = view.findViewById(R.id.like_imageview);
            commentLayout = view.findViewById(R.id.cont_reply);
            commentEdtText = view.findViewById(R.id.comment_edit_text);
            commentRecyclerView = view.findViewById(R.id.comment_recycler_view);
        }
    }

}
