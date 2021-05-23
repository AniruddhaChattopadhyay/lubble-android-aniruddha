package in.lubble.app.feed_user;

import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.emoji.widget.EmojiTextView;
import androidx.paging.LoadState;
import androidx.paging.LoadStateAdapter;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.curios.textformatter.FormatText;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import in.lubble.app.GlideRequests;
import in.lubble.app.LubbleApp;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.chat.CustomURLSpan;
import in.lubble.app.feed_groups.SingleGroupFeed.GroupFeedActivity;
import in.lubble.app.models.FeedGroupData;
import in.lubble.app.profile.ProfileActivity;
import in.lubble.app.receivers.ShareSheetReceiver;
import in.lubble.app.services.FeedServices;
import in.lubble.app.utils.FeedUtils;
import in.lubble.app.utils.RoundedCornersTransformation;
import in.lubble.app.utils.UiUtils;
import io.getstream.core.exceptions.StreamException;
import io.getstream.core.models.EnrichedActivity;
import io.getstream.core.models.FeedID;
import io.getstream.core.models.Reaction;
import me.saket.bettermovementmethod.BetterLinkMovementMethod;

import static android.view.View.GONE;
import static in.lubble.app.utils.UiUtils.dpToPx;

public class FeedAdaptor extends PagingDataAdapter<EnrichedActivity, FeedAdaptor.MyViewHolder> {

    private static final String TAG = "FeedAdaptor";
    private Context context;
    private int itemWidth, displayHeight;
    private FeedListener feedListener;
    private GlideRequests glide;
    private final HashMap<Integer, String> likedMap = new HashMap<>();
    private final String userId = FirebaseAuth.getInstance().getUid();

    public FeedAdaptor(@NotNull DiffUtil.ItemCallback<EnrichedActivity> diffCallback) {
        super(diffCallback);
    }

    public void setVars(Context context, int displayWidth, int displayHeight, GlideRequests glide, FeedListener feedListener) {
        this.context = context;
        this.glide = glide;
        this.feedListener = feedListener;
        this.itemWidth = displayWidth - UiUtils.dpToPx(32);
        this.displayHeight = displayHeight - UiUtils.dpToPx(172);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.feed_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        EnrichedActivity activity = getItem(position);
        if (activity == null) {
            return;
        }
        String postDateDisplay = getPostDateDisplay(activity.getTime());
        Map<String, Object> extras = activity.getExtra();

        holder.photoContentIv.setVisibility(View.GONE);
        holder.groupNameTv.setVisibility(View.GONE);
        holder.lubbleNameTv.setVisibility(View.GONE);
        if (extras != null) {
            holder.textContentTv.setVisibility(View.VISIBLE);
            final String message = String.valueOf(extras.get("message") == null ? "" : extras.get("message"));
            holder.textContentTv.setText(FormatText.boldAndItalics(message));
            holder.textContentTv.setLinkTextColor(ContextCompat.getColor(context, R.color.colorAccent));

            Linkify.addLinks(holder.textContentTv, Linkify.ALL);
            BetterLinkMovementMethod betterLinkMovementMethod = BetterLinkMovementMethod.newInstance().setOnLinkClickListener((textView, url) -> {
                Bundle bundle = new Bundle();
                bundle.putString("group_id", String.valueOf(extras.get("group")));
                bundle.putString("post_id", activity.getID());
                bundle.putString("author_uid", activity.getActor().getID());
                Analytics.triggerEvent(AnalyticsEvents.POST_LINK_CLICKED, bundle, context);
                return false;
            }).setOnLinkLongClickListener((textView, url) -> {
                Bundle bundle = new Bundle();
                bundle.putString("group_id", String.valueOf(extras.get("group")));
                bundle.putString("post_id", activity.getID());
                bundle.putString("author_uid", activity.getActor().getID());
                Analytics.triggerEvent(AnalyticsEvents.POST_LINK_LONG_CLICKED, bundle, context);

                ClipboardManager clipboard = (ClipboardManager) LubbleApp.getAppContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("lubble_feed_copied_url", url);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show();
                return true;
            });
            holder.textContentTv.setMovementMethod(betterLinkMovementMethod);

            CustomURLSpan.clickifyTextView(holder.textContentTv, () -> {
            });
            if (extras.containsKey("aspectRatio") && extras.get("aspectRatio") instanceof Double) {
                float aspectRatio = ((Double) extras.get("aspectRatio")).floatValue();
                if (aspectRatio > 0) {
                    holder.photoContentIv.setVisibility(View.VISIBLE);
                    holder.photoContentIv.setMaxHeight(View.VISIBLE);
                    float targetHeight = Math.min(displayHeight - holder.itemView.getMeasuredHeight(), itemWidth / aspectRatio);
                    ViewGroup.LayoutParams lp = holder.photoContentIv.getLayoutParams();
                    lp.height = Math.round(targetHeight);
                    holder.photoContentIv.setLayoutParams(lp);
                    holder.photoContentIv.setBackgroundColor(ContextCompat.getColor(context, R.color.md_grey_200));
                }
            }
            if (extras.containsKey("photoLink")) {
                holder.photoContentIv.setVisibility(View.VISIBLE);
                String photoLink = extras.get("photoLink").toString();
                glide
                        .load(photoLink)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                holder.photoContentIv.setBackgroundResource(0);//removes bg
                                return false;
                            }
                        })
                        .transform(new RoundedCornersTransformation(dpToPx(8), 0))
                        .into(holder.photoContentIv);
                holder.photoContentIv.setOnClickListener(v -> feedListener.onImageClicked(photoLink, holder.photoContentIv));
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
            if (extras.containsKey("group") && extras.containsKey("lubble_id")) {
                String groupFeedName = extras.get("group").toString() + '_' + extras.get("lubble_id");
                holder.groupNameTv.setOnClickListener(v -> {
                    FeedGroupData feedGroupData = new FeedGroupData(extras.get("group").toString(), groupFeedName, extras.get("lubble_id").toString());
                    GroupFeedActivity.open(context, feedGroupData);
                });
            }
        }
        Map<String, Object> actorMap = activity.getActor().getData();
        if (actorMap.containsKey("name")) {
            holder.authorNameTv.setText(String.valueOf(actorMap.get("name")));
            if (actorMap.containsKey("profile_picture") && actorMap.get("profile_picture") != null) {
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
        handleLinkPreview(activity, holder);

        holder.likeLayout.setOnClickListener(v -> toggleLike(holder, position));
        holder.commentLayout.setOnClickListener(v -> {
            feedListener.onReplyClicked(activity.getID(), activity.getForeignID(), activity.getActor().getID(), position);
        });
        holder.replyStatsTv.setOnClickListener(v -> {
            feedListener.openPostActivity(activity.getID());
        });
        holder.itemView.setOnClickListener(v -> {
            feedListener.openPostActivity(activity.getID());
        });
        holder.textContentTv.setOnClickListener(v -> {
            feedListener.openPostActivity(activity.getID());
        });
        holder.authorPhotoIv.setOnClickListener(v -> {
            ProfileActivity.open(context, activity.getActor().getID());
        });
        holder.shareLayout.setOnClickListener(v -> {
            FeedUtils.requestPostShareIntent(glide, activity, extras, this::startShareFlow);
        });
    }

    private void startShareFlow(Intent sharingIntent) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 21,
                new Intent(context, ShareSheetReceiver.class),
                PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            context.startActivity(Intent.createChooser(sharingIntent, context.getString(R.string.refer_share_title), pendingIntent.getIntentSender()));
        } else {
            context.startActivity(Intent.createChooser(sharingIntent, context.getString(R.string.refer_share_title)));
        }
        Analytics.triggerEvent(AnalyticsEvents.POST_SHARED, context);
    }

    private void handleLinkPreview(EnrichedActivity activity, MyViewHolder holder) {
        if (activity.getExtra().containsKey("linkUrl")) {
            String linkUrl = ((String) activity.getExtra().get("linkUrl")).toLowerCase();
            if (!TextUtils.isEmpty(linkUrl)) {
                holder.linkPreviewContainer.setVisibility(View.VISIBLE);
                if (activity.getExtra().containsKey("linkTitle")) {
                    holder.linkTitleTv.setText((String) activity.getExtra().get("linkTitle"));
                }
                if (activity.getExtra().containsKey("linkDesc")) {
                    holder.linkDescTv.setText((String) activity.getExtra().get("linkDesc"));
                }
                if (activity.getExtra().containsKey("linkPicUrl")) {
                    String linkPicUrl = (String) activity.getExtra().get("linkPicUrl");
                    glide
                            .load(linkPicUrl)
                            .error(R.drawable.ic_public_black_24dp)
                            .placeholder(R.drawable.ic_public_black_24dp)
                            .transform(new RoundedCornersTransformation(dpToPx(8), 0))
                            .into(holder.linkImageIv);
                }
                holder.linkPreviewContainer.setOnClickListener(v -> {
                    CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
                    intentBuilder.setToolbarColor(ContextCompat.getColor(context, R.color.colorAccent));
                    intentBuilder.setSecondaryToolbarColor(ContextCompat.getColor(context, R.color.dk_colorAccent));
                    intentBuilder.enableUrlBarHiding();
                    intentBuilder.setShowTitle(true);
                    CustomTabsIntent customTabsIntent = intentBuilder.build();
                    try {
                        customTabsIntent.launchUrl(context, Uri.parse(linkUrl));
                    } catch (ActivityNotFoundException e) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(linkUrl));
                        context.startActivity(i);
                    }
                });
            } else {
                holder.linkPreviewContainer.setVisibility(GONE);
            }
        } else {
            holder.linkPreviewContainer.setVisibility(GONE);
        }
    }

    private void handleCommentEditText(EnrichedActivity activity, MyViewHolder holder) {
        glide
                .load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl())
                .apply(new RequestOptions().override(dpToPx(24), dpToPx(24)))
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
        holder.commentEdtText.setOnClickListener(v -> feedListener.onReplyClicked(activity.getID(), activity.getForeignID(), activity.getActor().getID(), holder.getAdapterPosition()));
    }

    public interface FeedListener {
        void onReplyClicked(String activityId, String foreignId, String postActorUid, int position);

        void onImageClicked(String imgPath, ImageView imageView);

        void onLiked(String foreignID);

        void onRefreshLoading(@NotNull LoadState refresh);

        void openPostActivity(@NotNull String activityId);
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
                feedListener.openPostActivity(activity.getID());
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
        EnrichedActivity activity = getItem(position);
        if (!likedMap.containsKey(position)) {
            // like
            Reaction like = new Reaction.Builder()
                    .kind("like")
                    .id(userId + activity.getID())
                    .activityID(activity.getID())
                    .build();
            try {
                String notificationUserFeedId = "notification:" + activity.getActor().getID();
                FeedServices.getTimelineClient().reactions().add(like, new FeedID(notificationUserFeedId)).whenComplete((reaction, throwable) -> {
                    if (throwable != null) {
                        //todo
                    }
                });
                holder.likeIv.setImageResource(R.drawable.ic_favorite_24dp);
                likedMap.put(position, like.getId());
                extractReactionCount(activity, "like", holder.likeStatsTv, R.plurals.likes, 1);
                feedListener.onLiked(activity.getForeignID());
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

    public ConcatAdapter withLoadStateAdapters(LoadStateAdapter footer) {
        addLoadStateListener(combinedLoadStates -> {
            footer.setLoadState(combinedLoadStates.getAppend());
            feedListener.onRefreshLoading(combinedLoadStates.getRefresh());
            return null;
        });
        return new ConcatAdapter(FeedAdaptor.this, footer);
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
            EnrichedActivity updatedActivity = getItem(pos);
            List<Reaction> latestCommentList = updatedActivity.getLatestReactions().get("comment");
            if (latestCommentList == null)
                latestCommentList = new ArrayList<>();
            latestCommentList.add(0, reaction);
            updatedActivity.getLatestReactions().put("comment", latestCommentList);
            notifyItemChanged(pos);
        }
    }

    private int getActivityPosById(String activityId) {
        for (int i = 0; i < getItemCount(); i++)
            if (activityId.equalsIgnoreCase(getItem(i).getID()))
                return i;
        return -1;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private EmojiTextView textContentTv;
        private ImageView photoContentIv;
        private ImageView authorPhotoIv, linkImageIv;
        private TextView viewAllRepliesTv, authorNameTv, timePostedTv, groupNameTv, lubbleNameTv, linkTitleTv, linkDescTv;
        private LinearLayout likeLayout, shareLayout;
        private TextView likeStatsTv, replyStatsTv;
        private ImageView likeIv;
        private LinearLayout commentLayout;
        private TextView commentEdtText;
        private RecyclerView commentRecyclerView;
        private RelativeLayout linkPreviewContainer;

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
            shareLayout = view.findViewById(R.id.cont_share);
            likeStatsTv = view.findViewById(R.id.tv_like_stats);
            replyStatsTv = view.findViewById(R.id.tv_reply_stats);
            likeIv = view.findViewById(R.id.like_imageview);
            commentLayout = view.findViewById(R.id.cont_reply);
            commentEdtText = view.findViewById(R.id.comment_edit_text);
            commentRecyclerView = view.findViewById(R.id.comment_recycler_view);
            linkPreviewContainer = view.findViewById(R.id.cont_link_preview);
            linkImageIv = view.findViewById(R.id.iv_link_image);
            linkTitleTv = view.findViewById(R.id.tv_link_title);
            linkDescTv = view.findViewById(R.id.tv_link_desc);
            ImageView moreMenuIv = view.findViewById(R.id.iv_more_menu);
            moreMenuIv.setVisibility(GONE);
        }
    }

}
