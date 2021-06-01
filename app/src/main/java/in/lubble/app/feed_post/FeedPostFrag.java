package in.lubble.app.feed_post;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.emoji.widget.EmojiTextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.curios.textformatter.FormatText;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import in.lubble.app.BuildConfig;
import in.lubble.app.GlideApp;
import in.lubble.app.LubbleApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import in.lubble.app.profile.ProfileActivity;
import in.lubble.app.receivers.ShareSheetReceiver;
import in.lubble.app.services.FeedServices;
import in.lubble.app.utils.DateTimeUtils;
import in.lubble.app.utils.FeedUtils;
import in.lubble.app.utils.FullScreenImageActivity;
import in.lubble.app.utils.RoundedCornersTransformation;
import in.lubble.app.utils.UiUtils;
import in.lubble.app.widget.ReplyEditText;
import io.getstream.analytics.beans.Content;
import io.getstream.core.LookupKind;
import io.getstream.core.exceptions.StreamException;
import io.getstream.core.models.EnrichedActivity;
import io.getstream.core.models.FeedID;
import io.getstream.core.models.Reaction;
import io.getstream.core.options.EnrichmentFlags;
import io.getstream.core.options.Filter;
import io.getstream.core.options.Limit;
import me.saket.bettermovementmethod.BetterLinkMovementMethod;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;
import static in.lubble.app.Constants.MEDIA_TYPE;
import static in.lubble.app.utils.UiUtils.dpToPx;

public class FeedPostFrag extends Fragment {

    private static final String ARG_POST_ID = "LBL_ARG_POST_ID";
    private static final int ACTION_PROMOTE_POST = 572;
    private static final int ACTION_DELETE_POST = 464;

    private String postId;

    private ProgressBar progressBar, replyProgressBar;
    private EmojiTextView textContentTv;
    private ImageView photoContentIv;
    private ImageView authorPhotoIv;
    private TextView authorNameTv, timePostedTv, groupNameTv, lubbleNameTv;
    private LinearLayout likeLayout, shareLayout;
    private TextView likeTv, replyTv, noRepliesHelpTextTv, linkTitleTv, linkDescTv;
    private ImageView likeIv, replyIv, linkImageIv, moreMenuIv;
    private LinearLayout commentLayout;
    private ReplyEditText replyEt;
    private ShimmerRecyclerView commentRecyclerView;
    @Nullable
    private String likeReactionId = null;
    private final String userId = FirebaseAuth.getInstance().getUid();
    private BottomSheetBehavior sheetBehavior;
    private RelativeLayout linkPreviewContainer;

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
        shareLayout = view.findViewById(R.id.cont_share);
        likeTv = view.findViewById(R.id.tv_like);
        likeIv = view.findViewById(R.id.like_imageview);
        commentLayout = view.findViewById(R.id.cont_reply);
        commentRecyclerView = view.findViewById(R.id.comment_recycler_view);
        noRepliesHelpTextTv = view.findViewById(R.id.tv_no_replies_help_text);
        replyTv = view.findViewById(R.id.tv_reply);
        LinearLayout replyBottomSheet = view.findViewById(R.id.bottomsheet_reply);
        replyEt = view.findViewById(R.id.et_reply);
        replyIv = view.findViewById(R.id.iv_reply);
        replyProgressBar = view.findViewById(R.id.progressbar_reply);
        linkPreviewContainer = view.findViewById(R.id.cont_link_preview);
        linkImageIv = view.findViewById(R.id.iv_link_image);
        linkTitleTv = view.findViewById(R.id.tv_link_title);
        linkDescTv = view.findViewById(R.id.tv_link_desc);
        moreMenuIv = view.findViewById(R.id.iv_more_menu);

        sheetBehavior = BottomSheetBehavior.from(replyBottomSheet);
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
                                        textContentTv.setMaxLines(100);
                                        textContentTv.setText(FormatText.boldAndItalics(String.valueOf(extras.get("message"))));
                                        textContentTv.setLinkTextColor(ContextCompat.getColor(requireContext(), R.color.colorAccent));

                                        Linkify.addLinks(textContentTv, Linkify.ALL);

                                        BetterLinkMovementMethod betterLinkMovementMethod = BetterLinkMovementMethod.newInstance().setOnLinkClickListener((textView, url) -> {
                                            Bundle bundle = new Bundle();
                                            bundle.putString("group_id", String.valueOf(extras.get("group")));
                                            bundle.putString("post_id", enrichedActivity.getID());
                                            bundle.putString("author_uid", enrichedActivity.getActor().getID());
                                            Analytics.triggerEvent(AnalyticsEvents.POST_LINK_CLICKED, bundle, requireContext());
                                            return false;
                                        }).setOnLinkLongClickListener((textView, url) -> {
                                            Bundle bundle = new Bundle();
                                            bundle.putString("group_id", String.valueOf(extras.get("group")));
                                            bundle.putString("post_id", enrichedActivity.getID());
                                            bundle.putString("author_uid", enrichedActivity.getActor().getID());
                                            Analytics.triggerEvent(AnalyticsEvents.POST_LINK_LONG_CLICKED, bundle, requireContext());

                                            ClipboardManager clipboard = (ClipboardManager) LubbleApp.getAppContext().getSystemService(Context.CLIPBOARD_SERVICE);
                                            ClipData clip = ClipData.newPlainText("lubble_feed_copied_url", url);
                                            clipboard.setPrimaryClip(clip);
                                            Toast.makeText(requireContext(), "Copied!", Toast.LENGTH_SHORT).show();
                                            return true;
                                        });
                                        textContentTv.setMovementMethod(betterLinkMovementMethod);
                                    }
                                    if (extras.containsKey("photoLink")) {
                                        photoContentIv.setVisibility(View.VISIBLE);
                                        String photoLink = extras.get("photoLink").toString();
                                        Glide.with(requireContext())
                                                .load(photoLink)
                                                .transform(new RoundedCornersTransformation(dpToPx(8), 0))
                                                .into(photoContentIv);
                                        photoContentIv.setOnClickListener(v ->
                                                FullScreenImageActivity.open(getActivity(), requireContext(), photoLink, photoContentIv, null, R.drawable.ic_cancel_black_24dp));
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
                                    if (actorMap.get("profile_picture") != null) {
                                        Glide.with(requireContext())
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
                                    likeIv.setImageResource(R.drawable.ic_favorite_border_24dp);
                                    likeReactionId = null;
                                }

                                sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                                likeLayout.setOnClickListener(v -> toggleLike(enrichedActivity));
                                commentLayout.setOnClickListener(v -> {
                                    replyEt.requestFocus();
                                    UiUtils.showKeyboard(requireContext(), replyEt);
                                });
                                authorPhotoIv.setOnClickListener(v -> {
                                    ProfileActivity.open(requireContext(), enrichedActivity.getActor().getID());
                                });
                                handleReactionStats(enrichedActivity);
                                handleReplyBottomSheet(enrichedActivity);
                                handleLinkPreview(enrichedActivity);
                                trackPostImpression(enrichedActivity);

                                shareLayout.setOnClickListener(v -> {
                                    FeedUtils.requestPostShareIntent(GlideApp.with(requireContext()),
                                            enrichedActivity, extras,
                                            this::startShareFlow);
                                });

                                moreMenuIv.setOnClickListener(v -> {
                                    openMorePopupMenu(enrichedActivity.getID(), enrichedActivity.getActor().getID(), extras);
                                });
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

    private void openMorePopupMenu(String activityId, String actorId, Map<String, Object> extras) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), moreMenuIv);

        popupMenu.getMenuInflater().inflate(R.menu.menu_post_more, popupMenu.getMenu());
        if (BuildConfig.DEBUG || userId.equalsIgnoreCase(LubbleSharedPrefs.getInstance().getSupportUid())) {
            popupMenu.getMenu().add(0, ACTION_PROMOTE_POST, popupMenu.getMenu().size(), "Promote Post");
        } else {
            popupMenu.getMenu().removeItem(ACTION_PROMOTE_POST);
        }
        if (BuildConfig.DEBUG || userId.equalsIgnoreCase(actorId) || userId.equalsIgnoreCase(LubbleSharedPrefs.getInstance().getSupportUid())) {
            popupMenu.getMenu().add(0, ACTION_DELETE_POST, popupMenu.getMenu().size(), "Delete Post");
        } else {
            popupMenu.getMenu().removeItem(ACTION_DELETE_POST);
        }
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getItemId() == ACTION_PROMOTE_POST) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Promote Post?")
                        .setMessage("Are you sure?")
                        .setIcon(R.drawable.ic_star_shine)
                        .setPositiveButton("Promote", (dialog, which) -> promotePost(activityId, extras))
                        .setNegativeButton(R.string.all_cancel, (dialog, which) -> dialog.cancel())
                        .show();
            } else if (menuItem.getItemId() == ACTION_DELETE_POST) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Delete Post?")
                        .setMessage("Are you sure? Once deleted, this post will be gone forever.\n\nThis action cannot be undone.")
                        .setIcon(R.drawable.ic_cancel_red_24dp)
                        .setPositiveButton(R.string.all_cancel, (dialog, which) -> dialog.cancel())
                        .setNeutralButton("Delete Post", (dialog, which) -> deletePost(activityId))
                        .show();
            }
            return true;
        });
        popupMenu.show();
    }

    private void deletePost(String activityId) {
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setTitle("Deleting post");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("activityId", activityId);
        } catch (JSONException e) {
            e.printStackTrace();
            Snackbar.make(getView(), e.getMessage() == null ? "JSON error" : e.getMessage(), Snackbar.LENGTH_SHORT).show();
            progressDialog.dismiss();
            return;
        }
        RequestBody body = RequestBody.create(MEDIA_TYPE, jsonObject.toString());
        endpoints.deletePost(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                if (response.isSuccessful() && isAdded()) {
                    progressDialog.dismiss();
                    Toast.makeText(LubbleApp.getAppContext(), "Post Deleted!", Toast.LENGTH_SHORT).show();
                    getActivity().setResult(Activity.RESULT_OK);
                    getActivity().finish();
                } else {
                    if (isAdded()) {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), response.message() == null ? getString(R.string.check_internet) : response.message(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {
                if (isAdded()) {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), t.getMessage() == null ? getString(R.string.check_internet) : t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void promotePost(String activityId, Map<String, Object> extras) {
        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("feedGroupName", extras.get("group").toString() + "_" + extras.get("lubble_id").toString());
            jsonObject.put("activityId", activityId);
        } catch (JSONException e) {
            e.printStackTrace();
            Snackbar.make(getView(), e.getMessage() == null ? "JSON error" : e.getMessage(), Snackbar.LENGTH_SHORT).show();
            return;
        }
        RequestBody body = RequestBody.create(MEDIA_TYPE, jsonObject.toString());
        endpoints.promotePost(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                if (response.isSuccessful() && isAdded()) {
                    Snackbar.make(getView(), "Promoted!", Snackbar.LENGTH_SHORT).show();
                } else {
                    if (isAdded()) {
                        Toast.makeText(getContext(), response.message() == null ? getString(R.string.check_internet) : response.message(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(getContext(), t.getMessage() == null ? getString(R.string.check_internet) : t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void trackPostImpression(EnrichedActivity enrichedActivity) {
        ArrayList<Content> contentList = new ArrayList<>();
        contentList.add(new Content.ContentBuilder()
                .withForeignId(enrichedActivity.getForeignID())
                .withAttribute("actor", userId)
                .build());
        Analytics.triggerFeedImpression(contentList, null, FeedPostFrag.class.getSimpleName());
    }

    private void handleReplyBottomSheet(EnrichedActivity enrichedActivity) {
        String activityId = enrichedActivity.getID();

        replyEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                final String inputString = editable.toString();
                if (editable.length() > 0 && inputString.trim().length() > 0) {
                    replyIv.setAlpha(1f);
                } else {
                    replyIv.setAlpha(0.4f);
                }
            }
        });

        String replyText = LubbleSharedPrefs.getInstance().getReplyBottomSheet();
        if (!TextUtils.isEmpty(replyText)) {
            replyEt.append(replyText);
            replyIv.setAlpha(1f);
        }

        replyIv.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(replyEt.getText().toString())) {
                postComment(activityId, enrichedActivity.getForeignID(), enrichedActivity.getActor().getID());
            } else {
                Toast.makeText(getContext(), "Reply can't be empty", Toast.LENGTH_LONG).show();
            }
        });

        GlideApp.with(requireContext())
                .load(LubbleSharedPrefs.getInstance().getProfilePicUrl())
                .apply(new RequestOptions().override(UiUtils.dpToPx(24), UiUtils.dpToPx(24)))
                .circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE) //caches final image after transformations
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        replyEt.setCompoundDrawablesWithIntrinsicBounds(resource, null, null, null);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        replyEt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_account_circle_grey_24dp, 0, 0, 0);
                    }
                });
    }

    private void postComment(String activityId, String foreignId, String postActorUid) {
        try {
            replyIv.setVisibility(View.GONE);
            replyProgressBar.setVisibility(View.VISIBLE);
            String replyText = replyEt.getText().toString().trim();
            Reaction comment = new Reaction.Builder()
                    .kind("comment")
                    .userID(userId)
                    .activityID(activityId)
                    .extraField("text", replyText)
                    .extraField("timestamp", System.currentTimeMillis())
                    .build();
            String notificationUserFeedId = "notification:" + postActorUid;
            FeedServices.getTimelineClient().reactions().add(comment, new FeedID(notificationUserFeedId)).whenComplete((reaction, throwable) -> {
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        replyProgressBar.setVisibility(View.GONE);
                        if (throwable != null) {
                            Toast.makeText(getContext(), "Reply Failed!", Toast.LENGTH_SHORT).show();
                            replyIv.setVisibility(View.VISIBLE);
                            replyProgressBar.setVisibility(View.GONE);
                        } else {
                            replyEt.clearFocus();
                            replyEt.setText("");
                            LubbleSharedPrefs.getInstance().setReplyBottomSheet(null);
                            fetchPost();
                            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                            replyEt.hideIme();
                            Analytics.triggerFeedEngagement(foreignId, "comment", 10, null, FeedPostFrag.class.getSimpleName());
                        }
                    });
                }
            });
        } catch (StreamException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            replyIv.setVisibility(View.VISIBLE);
            replyProgressBar.setVisibility(View.GONE);
        }
    }

    private void handleLinkPreview(EnrichedActivity activity) {
        linkPreviewContainer.setVisibility(GONE);
        if (activity.getExtra().containsKey("linkUrl")) {
            String linkUrl = ((String) activity.getExtra().get("linkUrl")).toLowerCase();
            if (!TextUtils.isEmpty(linkUrl)) {
                linkPreviewContainer.setVisibility(View.VISIBLE);
                if (activity.getExtra().containsKey("linkTitle")) {
                    linkTitleTv.setText((String) activity.getExtra().get("linkTitle"));
                }
                if (activity.getExtra().containsKey("linkDesc")) {
                    linkDescTv.setText((String) activity.getExtra().get("linkDesc"));
                }
                if (activity.getExtra().containsKey("linkPicUrl")) {
                    String linkPicUrl = (String) activity.getExtra().get("linkPicUrl");
                    GlideApp.with(requireContext())
                            .load(linkPicUrl)
                            .error(R.drawable.ic_public_black_24dp)
                            .placeholder(R.drawable.ic_public_black_24dp)
                            .transform(new RoundedCornersTransformation(dpToPx(8), 0))
                            .into(linkImageIv);
                }
                linkPreviewContainer.setOnClickListener(v -> {
                    CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
                    intentBuilder.setToolbarColor(ContextCompat.getColor(requireContext(), R.color.colorAccent));
                    intentBuilder.setSecondaryToolbarColor(ContextCompat.getColor(requireContext(), R.color.dk_colorAccent));
                    intentBuilder.enableUrlBarHiding();
                    intentBuilder.setShowTitle(true);
                    CustomTabsIntent customTabsIntent = intentBuilder.build();
                    try {
                        customTabsIntent.launchUrl(requireContext(), Uri.parse(linkUrl));
                    } catch (ActivityNotFoundException e) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(linkUrl));
                        startActivity(i);
                    }
                });
            }
        }
    }

    private void handleReactionStats(EnrichedActivity enrichedActivity) {
        extractReactionCount(enrichedActivity, "like", likeTv, 0);
        extractReactionCount(enrichedActivity, "comment", replyTv, 0);
    }

    private void extractReactionCount(EnrichedActivity enrichedActivity, @NotNull String reaction, TextView statsTv, int change) {
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
            statsTv.setText(String.valueOf(reactionCount));
        } else {
            statsTv.setText("");
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
                                        BigFeedCommentAdaptor adapter = new BigFeedCommentAdaptor(getContext(), GlideApp.with(requireContext()), reactions);
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
                    .id(userId + enrichedActivity.getID())
                    .activityID(enrichedActivity.getID())
                    .build();
            try {
                String notificationUserFeedId = "notification:" + enrichedActivity.getActor().getID();
                ;
                FeedServices.getTimelineClient().reactions().add(like, new FeedID(notificationUserFeedId)).whenComplete((reaction, throwable) -> {
                    if (throwable != null) {
                        //todo
                    }
                });
                likeIv.setImageResource(R.drawable.ic_favorite_24dp);
                likeReactionId = like.getId();
                extractReactionCount(enrichedActivity, "like", likeTv, 1);
                Analytics.triggerFeedEngagement(enrichedActivity.getForeignID(), "like", 5, null, FeedPostFrag.class.getSimpleName());
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
                extractReactionCount(enrichedActivity, "like", likeTv, -1);
            } catch (StreamException e) {
                e.printStackTrace();
                //todo
            }
        }
    }

    private void startShareFlow(Intent sharingIntent) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                requireContext(), 21,
                new Intent(requireContext(), ShareSheetReceiver.class),
                PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            startActivity(Intent.createChooser(sharingIntent, getString(R.string.refer_share_title), pendingIntent.getIntentSender()));
        } else {
            startActivity(Intent.createChooser(sharingIntent, getString(R.string.refer_share_title)));
        }
        Analytics.triggerEvent(AnalyticsEvents.POST_SHARED, requireContext());
    }

}