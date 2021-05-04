package in.lubble.app.feed_post;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import in.lubble.app.R;
import in.lubble.app.feed_user.FeedCommentAdaptor;
import in.lubble.app.services.FeedServices;
import in.lubble.app.utils.DateTimeUtils;
import io.getstream.core.LookupKind;
import io.getstream.core.exceptions.StreamException;
import io.getstream.core.models.EnrichedActivity;
import io.getstream.core.models.Reaction;
import io.getstream.core.options.EnrichmentFlags;
import io.getstream.core.options.Filter;
import io.getstream.core.options.Limit;

public class FeedPostFrag extends Fragment {

    private static final String ARG_POST_ID = "LBL_ARG_POST_ID";

    private String postId;

    private TextView textContentTv;
    private ImageView photoContentIv;
    private ImageView authorPhotoIv;
    private TextView authorNameTv, timePostedTv;
    private LinearLayout likeLayout;
    private TextView likeStatsTv, replyStatsTv;
    private ImageView likeIv;
    private LinearLayout commentLayout;
    private ImageView postCommentBtn;
    private EditText commentEdtText;
    private RecyclerView commentRecyclerView;

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

        textContentTv = view.findViewById(R.id.feed_text_content);
        photoContentIv = view.findViewById(R.id.feed_photo_content);
        authorNameTv = view.findViewById(R.id.feed_author_name);
        authorPhotoIv = view.findViewById(R.id.feed_author_photo);
        timePostedTv = view.findViewById(R.id.feed_post_timestamp);
        likeLayout = view.findViewById(R.id.cont_like);
        likeStatsTv = view.findViewById(R.id.tv_like_stats);
        likeIv = view.findViewById(R.id.like_imageview);
        commentLayout = view.findViewById(R.id.cont_reply);
        postCommentBtn = view.findViewById(R.id.comment_post_btn);
        commentEdtText = view.findViewById(R.id.comment_edit_text);
        commentRecyclerView = view.findViewById(R.id.comment_recycler_view);
        replyStatsTv = view.findViewById(R.id.tv_reply_stats);

        postId = requireArguments().getString(ARG_POST_ID);

        if (postId == null) {
            throw new IllegalArgumentException("Missing ARG_POST_ID for FeedPostFrag");
        }

        fetchPost();

        return view;
    }

    private void fetchPost() {
        try {
            List<EnrichedActivity> postList = FeedServices.getTimelineClient().flatFeed("timeline")
                    .getEnrichedActivities(
                            new Limit(1), new Filter().idLessThanEqual(postId),
                            new EnrichmentFlags()
                                    .withReactionCounts()
                                    .withOwnReactions()
                    ).join();
            if (!postList.isEmpty()) {
                EnrichedActivity enrichedActivity = postList.get(0);
                Map<String, Object> extrasMap = enrichedActivity.getExtra();
                if (extrasMap != null && extrasMap.containsKey("message")) {
                    textContentTv.setVisibility(View.VISIBLE);
                    textContentTv.setText(String.valueOf(extrasMap.get("message")));
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
                handleLikes(enrichedActivity);

                List<Reaction> userLikes = enrichedActivity.getOwnReactions().get("like");
                if (userLikes != null && userLikes.size() > 0) {
                    likeIv.setImageResource(R.drawable.ic_favorite_24dp);
                } else {
                    likeIv.setImageResource(R.drawable.ic_favorite_border_light);
                }

                handleReactionStats(enrichedActivity);
            } else {
                Toast.makeText(getContext(), "Post not found", Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        } catch (StreamException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            e.printStackTrace();
        }
    }

    private void handleReactionStats(EnrichedActivity enrichedActivity) {
        if (enrichedActivity.getReactionCounts().containsKey("like")) {
            int likeCount = enrichedActivity.getReactionCounts().get("like").intValue();
            if (likeCount > 0) {
                likeStatsTv.setVisibility(View.VISIBLE);
                likeStatsTv.setText(likeCount + " " + getResources().getQuantityString(R.plurals.likes, likeCount));
            }
        }
        if (enrichedActivity.getReactionCounts().containsKey("comment")) {
            int replyCount = enrichedActivity.getReactionCounts().get("comment").intValue();
            if (replyCount > 0) {
                replyStatsTv.setVisibility(View.VISIBLE);
                replyStatsTv.setText(replyCount + " " + getResources().getQuantityString(R.plurals.replies, replyCount));
            }
        }
    }

    private void initCommentRecyclerView(EnrichedActivity activity) {
        try {
            List<Reaction> reactions = FeedServices.getTimelineClient().reactions()
                    .filter(LookupKind.ACTIVITY, activity.getID(), "comment")
                    .get();
            if (reactions.size() > 0) {
                commentRecyclerView.setVisibility(View.VISIBLE);
                LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                commentRecyclerView.setVisibility(View.VISIBLE);
                commentRecyclerView.setLayoutManager(layoutManager);
                commentRecyclerView.setNestedScrollingEnabled(false);
                FeedCommentAdaptor adapter = new FeedCommentAdaptor(getContext(), reactions);
                commentRecyclerView.setAdapter(adapter);
            } else {
                commentRecyclerView.setVisibility(View.GONE);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (StreamException e) {
            e.printStackTrace();
        }
    }

    private void handleLikes(EnrichedActivity activity) {
        likeLayout.setOnClickListener(v -> {
            List<Reaction> userLikes = activity.getOwnReactions().get("like");
            if (userLikes != null && userLikes.size() > 0) {
                try {
                    FeedServices.getTimelineClient().reactions().delete(userLikes.get(0).getId()).join();
                    likeIv.setImageResource(R.drawable.ic_favorite_border_24dp);
                    handleReactionStats(activity);
                    //likeStatsTv.setText(Integer.toString(likeCount - 1));
                } catch (StreamException e) {
                    e.printStackTrace();
                }
            } else {
                Reaction like = new Reaction.Builder()
                        .kind("like")
                        .activityID(activity.getID())
                        .build();
                try {
                    like = FeedServices.getTimelineClient().reactions().add(like).get();
                    handleReactionStats(activity);
                    //likeStatsTv.setText(Integer.toString(likeCount + 1));
                    likeIv.setImageResource(R.drawable.ic_favorite_24dp);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (StreamException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}