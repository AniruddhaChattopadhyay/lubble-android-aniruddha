package in.lubble.app.feed_post;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import in.lubble.app.R;
import in.lubble.app.feed_user.FeedCommentAdaptor;
import in.lubble.app.services.FeedServices;
import in.lubble.app.utils.DateTimeUtils;
import in.lubble.app.utils.RoundedCornersTransformation;
import io.getstream.core.LookupKind;
import io.getstream.core.exceptions.StreamException;
import io.getstream.core.models.EnrichedActivity;
import io.getstream.core.models.Reaction;
import io.getstream.core.options.EnrichmentFlags;
import io.getstream.core.options.Filter;
import io.getstream.core.options.Limit;

import static android.view.View.GONE;
import static in.lubble.app.utils.UiUtils.dpToPx;

public class FeedPostFrag extends Fragment {

    private static final String ARG_POST_ID = "LBL_ARG_POST_ID";

    private String postId;

    private ProgressBar progressBar;
    private TextView textContentTv;
    private ImageView photoContentIv;
    private ImageView authorPhotoIv;
    private TextView authorNameTv, timePostedTv, groupNameTv, lubbleNameTv;
    private LinearLayout likeLayout;
    private TextView likeStatsTv, replyStatsTv, noRepliesHelpTextTv;
    private ImageView likeIv;
    private LinearLayout commentLayout;
    private ImageView postCommentBtn;
    private EditText commentEdtText;
    private ShimmerRecyclerView commentRecyclerView;
    @Nullable
    private String likeReactionId = null;
    private final String userId = FirebaseAuth.getInstance().getUid();

    public static FeedPostFrag newInstance(String postId) {
        FeedPostFrag feedPostFrag = new FeedPostFrag();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_POST_ID, postId);
        feedPostFrag.setArguments(bundle);
        return feedPostFrag;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.feed_post_fragment, container, false);

        progressBar = view.findViewById(R.id.progressbar_post);
        textContentTv = view.findViewById(R.id.feed_text_content);
        photoContentIv = view.findViewById(R.id.feed_photo_content);
        authorNameTv = view.findViewById(R.id.feed_author_name);
        groupNameTv = view.findViewById(R.id.tv_group_name);
        lubbleNameTv = view.findViewById(R.id.tv_lubble_name);
        authorPhotoIv = view.findViewById(R.id.feed_author_photo);
        timePostedTv = view.findViewById(R.id.feed_post_timestamp);
        likeLayout = view.findViewById(R.id.cont_like);
        likeStatsTv = view.findViewById(R.id.tv_like_stats);
        likeIv = view.findViewById(R.id.like_imageview);
        commentLayout = view.findViewById(R.id.cont_reply);
        postCommentBtn = view.findViewById(R.id.comment_post_btn);
        commentEdtText = view.findViewById(R.id.comment_edit_text);
        commentRecyclerView = view.findViewById(R.id.comment_recycler_view);
        noRepliesHelpTextTv = view.findViewById(R.id.tv_no_replies_help_text);
        replyStatsTv = view.findViewById(R.id.tv_reply_stats);

        postId = requireArguments().getString(ARG_POST_ID);
        commentRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        if (postId == null) {
            throw new IllegalArgumentException("Missing ARG_POST_ID for FeedPostFrag");
        }

        fetchPost();

        return view;
    }

    private void fetchPost() {
        try {
            progressBar.setVisibility(View.VISIBLE);
            commentRecyclerView.showShimmerAdapter();
            FeedServices.getTimelineClient().flatFeed("timeline")
                    .getEnrichedActivities(
                            new Limit(1), new Filter().idLessThanEqual(postId),
                            new EnrichmentFlags()
                                    .withReactionCounts()
                                    .withOwnReactions()
                    ).whenComplete((postList, throwable) -> {
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        if (throwable == null) {
                            if (!postList.isEmpty()) {
                                EnrichedActivity enrichedActivity = postList.get(0);
                                Map<String, Object> extras = enrichedActivity.getExtra();

                                if (extras != null) {
                                    if (extras.containsKey("message")) {
                                        textContentTv.setVisibility(View.VISIBLE);
                                        textContentTv.setText(String.valueOf(extras.get("message")));
                                    }
                                    if (extras.containsKey("photoLink")) {
                                        photoContentIv.setVisibility(View.VISIBLE);
                                        Glide.with(requireContext())
                                                .load(extras.get("photoLink").toString())
                                                .transform(new RoundedCornersTransformation(dpToPx(8), 0))
                                                .into(photoContentIv);
                                    }

                                    if (extras.containsKey("authorName")) {
                                        authorNameTv.setText(extras.get("authorName").toString());
                                    }
                                    if (extras.containsKey("group")) {
                                        groupNameTv.setVisibility(View.VISIBLE);
                                        groupNameTv.setText(extras.get("group").toString());
                                    }
                                    if (extras.containsKey("lubble_id")) {
                                        lubbleNameTv.setVisibility(View.VISIBLE);
                                        lubbleNameTv.setText(extras.get("lubble_id").toString());
                                    }
                                }
                                Map<String, Object> actorMap = enrichedActivity.getActor().getData();
                                if (actorMap.containsKey("name")) {
                                    authorNameTv.setText(String.valueOf(actorMap.get("name")));
                                    if (actorMap.containsKey("profile_picture")) {
                                        Glide.with(getContext())
                                                .load(actorMap.get("profile_picture").toString())
                                                .placeholder(R.drawable.ic_account_circle_black_no_padding)
                                                .error(R.drawable.ic_account_circle_black_no_padding)
                                                .circleCrop()
                                                .into(authorPhotoIv);
                                    }
                                }
                                timePostedTv.setText(DateTimeUtils.getHumanTimestampWithTime(enrichedActivity.getTime().getTime()));
                                initCommentRecyclerView(enrichedActivity);

                                List<Reaction> userLikes = enrichedActivity.getOwnReactions().get("like");
                                if (userLikes != null && userLikes.size() > 0) {
                                    likeIv.setImageResource(R.drawable.ic_favorite_24dp);
                                    likeReactionId = userLikes.get(0).getId();
                                } else {
                                    likeIv.setImageResource(R.drawable.ic_favorite_border_light);
                                    likeReactionId = null;
                                }

                                likeLayout.setOnClickListener(v -> toggleLike(enrichedActivity));
                                handleReactionStats(enrichedActivity);
                            } else {
                                Toast.makeText(getContext(), "Post not found", Toast.LENGTH_SHORT).show();
                                getActivity().finish();
                            }
                        } else {
                            Toast.makeText(getContext(), "Error: " + throwable.getCause(), Toast.LENGTH_SHORT).show();
                            getActivity().finish();
                        }
                    });
                }
            });

        } catch (StreamException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            e.printStackTrace();
        }
    }

    private void handleReactionStats(EnrichedActivity enrichedActivity) {
        extractReactionCount(enrichedActivity, "like", likeStatsTv, R.plurals.likes, 0);
        extractReactionCount(enrichedActivity, "comment", replyStatsTv, R.plurals.replies, 0);
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
            statsTv.setText(reactionCount + " " + getContext().getResources().getQuantityString(stringRes, reactionCount));
        } else {
            statsTv.setVisibility(GONE);
        }
    }

    private void initCommentRecyclerView(EnrichedActivity activity) {
        try {
            FeedServices.getTimelineClient().reactions()
                    .filter(LookupKind.ACTIVITY, activity.getID(), new Limit(50), "comment")
                    .whenComplete((reactions, throwable) -> {
                        if (isAdded() && getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                if (throwable == null) {
                                    commentRecyclerView.setVisibility(View.VISIBLE);
                                    noRepliesHelpTextTv.setVisibility(View.GONE);
                                    if (commentRecyclerView.getActualAdapter() != commentRecyclerView.getAdapter()) {
                                        // recycler view is currently holding shimmer adapter so hide it
                                        commentRecyclerView.hideShimmerAdapter();
                                    }
                                    if (reactions.size() > 0) {
                                        commentRecyclerView.setNestedScrollingEnabled(false);
                                        FeedCommentAdaptor adapter = new FeedCommentAdaptor(getContext(), reactions);
                                        commentRecyclerView.setAdapter(adapter);
                                    } else {
                                        commentRecyclerView.setVisibility(View.GONE);
                                        noRepliesHelpTextTv.setVisibility(View.VISIBLE);
                                    }
                                } else {
                                    if (getView() != null) {
                                        Snackbar.make(getView(), "Failed to load replies: " + throwable.getCause(), Snackbar.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });

        } catch (StreamException e) {
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(e);
            if (getView() != null) {
                Snackbar.make(getView(), "Failed to load replies: " + e.getCause(), Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private void toggleLike(EnrichedActivity enrichedActivity) {
        if (likeReactionId == null) {
            // like
            Reaction like = new Reaction.Builder()
                    .kind("like")
                    .id(userId)
                    .activityID(enrichedActivity.getID())
                    .build();
            try {
                FeedServices.getTimelineClient().reactions().add(like).whenComplete((reaction, throwable) -> {
                    if (throwable != null) {
                        //todo
                    }
                });
                likeIv.setImageResource(R.drawable.ic_favorite_24dp);
                likeReactionId = like.getId();
                extractReactionCount(enrichedActivity, "like", likeStatsTv, R.plurals.likes, 1);
            } catch (StreamException e) {
                e.printStackTrace();
                //todo
            }
        } else {
            // unlike
            try {
                FeedServices.getTimelineClient().reactions().delete(likeReactionId).whenComplete((aVoid, throwable) -> {
                    if (throwable != null) {
                        //todo
                    }
                });
                likeIv.setImageResource(R.drawable.ic_favorite_border_24dp);
                likeReactionId = null;
                extractReactionCount(enrichedActivity, "like", likeStatsTv, R.plurals.likes, -1);
            } catch (StreamException e) {
                e.printStackTrace();
                //todo
            }
        }
    }

}