package in.lubble.app.feed_groups.SingleGroupFeed;

import static android.app.Activity.RESULT_OK;
import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;
import static in.lubble.app.Constants.MEDIA_TYPE;
import static in.lubble.app.feed_groups.SingleGroupFeed.GroupFeedFragPermissionsDispatcher.startSharingPostWithPermissionCheck;
import static in.lubble.app.firebase.RealtimeDbHelper.getThisUserFeedIntroRef;
import static in.lubble.app.utils.FeedUtils.processTrackedPosts;
import static in.lubble.app.utils.FileUtils.showStoragePermRationale;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.emoji.widget.EmojiTextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LoadState;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.feed_post.FeedPostActivity;
import in.lubble.app.feed_user.AddPostForFeed;
import in.lubble.app.feed_user.FeedAdaptor;
import in.lubble.app.feed_user.FeedPostComparator;
import in.lubble.app.feed_user.PagingLoadStateAdapter;
import in.lubble.app.feed_user.ReplyBottomSheetDialogFrag;
import in.lubble.app.feed_user.ReplyListener;
import in.lubble.app.models.FeedGroupData;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import in.lubble.app.profile.StatusBottomSheetFragment;
import in.lubble.app.receivers.ShareSheetReceiver;
import in.lubble.app.services.FeedServices;
import in.lubble.app.utils.FeedUtils;
import in.lubble.app.utils.FeedViewModel;
import in.lubble.app.utils.FullScreenImageActivity;
import in.lubble.app.utils.FullScreenVideoActivity;
import in.lubble.app.utils.UiUtils;
import in.lubble.app.utils.VisibleState;
import in.lubble.app.widget.PostReplySmoothScroller;
import io.getstream.cloud.CloudClient;
import io.getstream.cloud.CloudFlatFeed;
import io.getstream.core.exceptions.StreamException;
import io.getstream.core.models.EnrichedActivity;
import io.getstream.core.models.FollowRelation;
import io.getstream.core.models.Reaction;
import io.getstream.core.options.Limit;
import io.getstream.core.options.Offset;
import okhttp3.RequestBody;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@RuntimePermissions()
public class GroupFeedFrag extends Fragment implements FeedAdaptor.FeedListener, ReplyListener, SwipeRefreshLayout.OnRefreshListener {

    private MaterialButton postBtn;
    private MaterialButton postQandABtn;
    private LinearLayout postBtnLl;
    private RecyclerView feedRV;
    private ProgressBar joinGroupProgressBar;
    private EmojiTextView joinGroupTv;
    private TextView emptyHintTv;
    private static final int REQUEST_CODE_NEW_POST = 800;
    private static final int REQ_CODE_POST_ACTIV = 226;
    private static final String FEED_NAME_BUNDLE = "FEED_NAME";
    private String feedName = null;
    private View rootView;
    private final String userId = FirebaseAuth.getInstance().getUid();
    private SwipeRefreshLayout swipeRefreshLayout;
    private FeedAdaptor adapter;
    private LinearLayoutManager layoutManager;
    private FeedViewModel viewModel;

    public GroupFeedFrag() {
        // Required empty public constructor
    }

    public static GroupFeedFrag newInstance(String feedName) {
        GroupFeedFrag fragment = new GroupFeedFrag();
        Bundle args = new Bundle();
        args.putString(FEED_NAME_BUNDLE, feedName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            feedName = getArguments().getString(FEED_NAME_BUNDLE);
            if (feedName.toLowerCase(Locale.ROOT).startsWith("introductions")) {
                feedName = feedName + "-" + LubbleSharedPrefs.getInstance().getLubbleId();
            }
            viewModel = new ViewModelProvider(this).get(FeedViewModel.class);
        }
        Analytics.triggerScreenEvent(requireContext(), this.getClass());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_single_group_feed, container, false);
        joinGroupTv = rootView.findViewById(R.id.tv_join_group);
        emptyHintTv = rootView.findViewById(R.id.tv_empty_hint);
        postBtn = rootView.findViewById(R.id.btn_new_post);
        postQandABtn = rootView.findViewById(R.id.btn_QandA_new_post);
        postBtnLl = rootView.findViewById(R.id.post_btn_LL);
        feedRV = rootView.findViewById(R.id.feed_recyclerview);
        joinGroupProgressBar = rootView.findViewById(R.id.progressbar_joining);
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_feed);

        postBtn.setOnClickListener(v -> {
            startActivityForResult(new Intent(getContext(), AddPostForFeed.class), REQUEST_CODE_NEW_POST);
        });
        postQandABtn.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddPostForFeed.class);
            intent.putExtra(AddPostForFeed.ARG_POST_TYPE, AddPostForFeed.TYPE_QNA);
            startActivityForResult(intent, REQUEST_CODE_NEW_POST);
        });

        layoutManager = new LinearLayoutManager(getContext());
        feedRV.setLayoutManager(layoutManager);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.colorAccent));

        getCredentials();
        return rootView;
    }

    void getCredentials() {
        swipeRefreshLayout.setRefreshing(true);
        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        Call<Endpoints.StreamCredentials> call = endpoints.getStreamCredentials(feedName);//feedName
        call.enqueue(new Callback<Endpoints.StreamCredentials>() {
            @Override
            public void onResponse(Call<Endpoints.StreamCredentials> call, Response<Endpoints.StreamCredentials> response) {
                if (response.isSuccessful() && isAdded()) {
                    //Toast.makeText(getContext(), R.string.upload_success, Toast.LENGTH_SHORT).show();
                    assert response.body() != null;
                    final Endpoints.StreamCredentials credentials = response.body();
                    try {
                        FeedServices.init(credentials.getApi_key(), credentials.getUser_token());
                        initRecyclerView(false);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(getContext(), R.string.all_try_again, Toast.LENGTH_SHORT).show();
                    }

                } else if (isAdded()) {
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(getContext(), R.string.all_try_again, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Endpoints.StreamCredentials> call, Throwable t) {
                if (isAdded()) {
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initRecyclerView(boolean isRefresh) {
        CloudFlatFeed groupFeed;
        if (feedName.toLowerCase(Locale.ROOT).startsWith("introductions")) {
            groupFeed = FeedServices.client.flatFeed("group_locality", feedName);
        } else {
            groupFeed = FeedServices.client.flatFeed("group", feedName);
        }

        if (adapter == null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = displayMetrics.widthPixels;
            int height = displayMetrics.heightPixels; //height of RV, excluding toolbar & bottom nav
            adapter = new FeedAdaptor(new FeedPostComparator());
            adapter.setVars(getContext(), width, height, GlideApp.with(this), this);

            feedRV.setAdapter(adapter.withLoadStateAdapters(
                    new PagingLoadStateAdapter(() -> {
                        adapter.retry();
                        return null;
                    })
            ));
            feedRV.clearOnScrollListeners();
            feedRV.addOnScrollListener(scrollListener);
            viewModel.getDistinctLiveData().observe(this, visibleState -> {
                processTrackedPosts(adapter.snapshot().getItems(), visibleState, "group:" + feedName, GroupFeedFrag.class.getSimpleName());
            });
        }
        String algo = isRefresh ? null : "lbl_" + LubbleSharedPrefs.getInstance().getLubbleId();
        viewModel.loadPaginatedActivities(groupFeed, 10, algo).observe(this, pagingData -> {
            adapter.submitData(getViewLifecycleOwner().getLifecycle(), pagingData);
        });
        layoutManager.scrollToPosition(0);

        try {
            checkGroupJoinedStatus(groupFeed);
        } catch (StreamException e) {
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    /*
    Check if user follows this group feed
     */
    private void checkGroupJoinedStatus(CloudFlatFeed groupFeed) throws StreamException {
        CloudClient timelineClient = FeedServices.getTimelineClient();
        if (timelineClient != null) {
            CloudFlatFeed userTimelineFeed = timelineClient.flatFeed("timeline", userId);
            try {
                List<FollowRelation> followed = userTimelineFeed.getFollowed(new Limit(1), new Offset(0), groupFeed.getID()).get();
                if (!followed.isEmpty()) {
                    // joined
                    joinGroupTv.setVisibility(View.GONE);
                    postBtnLl.setVisibility(View.VISIBLE);
                    if (getActivity() != null && getActivity() instanceof GroupFeedActivity) {
                        ((GroupFeedActivity) getActivity()).toggleContextMenu(true);
                    }
                } else {
                    // not joined
                    joinGroupTv.setVisibility(View.VISIBLE);
                    postBtnLl.setVisibility(View.GONE);
                    ((GroupFeedActivity) getActivity()).toggleContextMenu(false);
                    joinGroupTv.setOnClickListener(v -> {
                        joinGroup(groupFeed);
                    });
                }
            } catch (InterruptedException | ExecutionException e) {
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), R.string.all_something_wrong_try_again, Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
                e.printStackTrace();
            }
        } else {
            requireActivity().finish();
        }
    }

    @Override
    public void onRefreshLoading(@NotNull LoadState refresh) {
        if (refresh == LoadState.Loading.INSTANCE) {
            if (!swipeRefreshLayout.isRefreshing()) {
                //show loader only on first load, not when user pulled to refresh
                swipeRefreshLayout.setRefreshing(true);
            }
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (dy > 0) {
                UiUtils.animateSlideDownHide(getContext(), postBtnLl);
            } else {
                UiUtils.animateSlideUpShow(getContext(), postBtnLl);
            }
            int firstCompletelyVisibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
            int lastCompletelyVisibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition();
            VisibleState visibleState;
            if (firstCompletelyVisibleItemPosition == NO_POSITION && lastCompletelyVisibleItemPosition == NO_POSITION) {
                // no post is completely visible: either 2 posts are partially visible or just 1 long post
                // in both cases count the impression of both or the one single post
                visibleState = new VisibleState(layoutManager.findFirstVisibleItemPosition(),
                        layoutManager.findLastVisibleItemPosition());
            } else {
                visibleState = new VisibleState(firstCompletelyVisibleItemPosition,
                        lastCompletelyVisibleItemPosition);
            }
            viewModel.onScrolled(visibleState);
        }
    };

    public void joinGroup(CloudFlatFeed groupFeed) {
        joinGroupTv.setText("");
        joinGroupProgressBar.setVisibility(View.VISIBLE);
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("groupFeedId", groupFeed.getUserID());
            RequestBody body = RequestBody.create(MEDIA_TYPE, jsonObject.toString());
            Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
            Call<Void> call = endpoints.addGroupForUser(body);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful() && isAdded()) {
                        Snackbar.make(rootView, "Joined Group!", Snackbar.LENGTH_SHORT).show();
                        joinGroupTv.setVisibility(View.GONE);
                        postBtnLl.setVisibility(View.VISIBLE);
                        joinGroupProgressBar.setVisibility(View.GONE);
                        if (getActivity() != null && getActivity() instanceof GroupFeedActivity) {
                            ((GroupFeedActivity) getActivity()).toggleContextMenu(true);
                        }
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    FirebaseCrashlytics.getInstance().recordException(t);
                    if (isAdded()) {
                        joinGroupTv.setText("✨ JOIN GROUP");
                        joinGroupProgressBar.setVisibility(View.GONE);
                        String text = getString(R.string.all_something_wrong_try_again);
                        if (t.getMessage() != null) {
                            text = "Failed: " + t.getMessage();
                        }
                        Snackbar.make(rootView, text, Snackbar.LENGTH_SHORT).show();
                    }
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(e);
            joinGroupProgressBar.setVisibility(View.GONE);
            Snackbar.make(rootView, R.string.all_something_wrong_try_again, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRefresh() {
        initRecyclerView(true);
        Analytics.triggerEvent(AnalyticsEvents.FEED_REFRESHED, requireContext());
    }

    @Override
    public void onReplyClicked(String activityId, String foreignId, String postActorUid, int position) {
        postBtnLl.setVisibility(View.GONE);
        ReplyBottomSheetDialogFrag replyBottomSheetDialogFrag = ReplyBottomSheetDialogFrag.newInstance(activityId, foreignId, postActorUid);
        replyBottomSheetDialogFrag.show(getChildFragmentManager(), null);
        RecyclerView.SmoothScroller smoothScroller = new PostReplySmoothScroller(feedRV.getContext());
        smoothScroller.setTargetPosition(position);
        feedRV.getLayoutManager().startSmoothScroll(smoothScroller);
    }

    @Override
    public void onReplied(String activityId, String foreignId, Reaction reaction) {
        postBtnLl.setVisibility(View.VISIBLE);
        adapter.addUserReply(activityId, reaction);
        Analytics.triggerFeedEngagement(foreignId, "comment", 10, "group:" + feedName, GroupFeedFrag.class.getSimpleName());
    }

    @Override
    public void onImageClicked(String imgPath, ImageView imageView) {
        FullScreenImageActivity.open(getActivity(), requireContext(), imgPath, imageView, null, R.drawable.ic_cancel_black_24dp);
    }

    @Override
    public void onVideoClicked(String vidPath) {
        FullScreenVideoActivity.open(getActivity(), requireContext(), vidPath, "", "");
    }

    @Override
    public void onLiked(String foreignID) {
        Analytics.triggerFeedEngagement(foreignID, "like", 5, "group:" + feedName, GroupFeedFrag.class.getSimpleName());
    }

    @Override
    public void openPostActivity(@NotNull String activityId) {
        startActivityForResult(FeedPostActivity.getIntent(requireContext(), activityId), REQ_CODE_POST_ACTIV);
    }

    @Override
    public void openGroupFeed(@NotNull FeedGroupData feedGroupData) {
        //do nothing here
    }

    @Override
    public void showEmptyView(boolean show) {
        if (show) {
            emptyHintTv.setVisibility(View.VISIBLE);
            emptyHintTv.setText("Be the first to post here!");
            emptyHintTv.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_add_circle_black_24dp, 0, 0);
        } else {
            emptyHintTv.setVisibility(View.GONE);
        }
    }

    @Override
    public void onShareClicked(EnrichedActivity activity, Map<String, Object> extras) {
        startSharingPostWithPermissionCheck(GroupFeedFrag.this, activity, extras);
    }

    @Override
    public void onBadgeClicked(String name) {
        Analytics.triggerEvent(AnalyticsEvents.CLICK_ON_OTHERS_STATUS, requireContext());
        View dialogView = requireActivity().getLayoutInflater().inflate(R.layout.bottom_sheet_for_status_redirect, null);
        final BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), R.style.RoundedBottomSheetDialog);
        TextView tv = dialogView.findViewById(R.id.status_redirect_tv);
        tv.setText("You are viewing " + name + "'s badge. Set your badge from your profile or here \uD83D\uDC47");
        Button btn = dialogView.findViewById(R.id.status_redirect_btn);
        btn.setOnClickListener(v -> {
            Analytics.triggerEvent(AnalyticsEvents.CLICK_ON_SET_STATUS_FROM_OTHERS_STATUS, requireContext());
            StatusBottomSheetFragment statusBottomSheetFragment = new StatusBottomSheetFragment(getView());
            statusBottomSheetFragment.show(((AppCompatActivity) requireContext()).getSupportFragmentManager(), statusBottomSheetFragment.getTag());
            dialog.dismiss();
        });
        dialog.setContentView(dialogView);
        dialog.show();
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public void startSharingPost(EnrichedActivity activity, Map<String, Object> extras) {
        FeedUtils.requestPostShareIntent(GlideApp.with(this), activity, extras, this::startShareFlow);
    }

    private void startShareFlow(Intent sharingIntent) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                requireContext(), 21,
                new Intent(requireContext(), ShareSheetReceiver.class),
                PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            requireContext().startActivity(Intent.createChooser(sharingIntent, requireContext().getString(R.string.refer_share_title), pendingIntent.getIntentSender()));
        } else {
            requireContext().startActivity(Intent.createChooser(sharingIntent, requireContext().getString(R.string.refer_share_title)));
        }
        Bundle bundle = new Bundle();
        bundle.putString("feed_id", sharingIntent.getStringExtra("FEED_POST_ID"));
        Analytics.triggerEvent(AnalyticsEvents.POST_SHARED, requireContext());
    }


    @Override
    public void onDismissed() {
        postBtnLl.setVisibility(View.VISIBLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_NEW_POST && resultCode == RESULT_OK) {
            // Posted a new post -> refresh list
            onRefresh();
            getThisUserFeedIntroRef().setValue(Boolean.TRUE);
            if (data != null && data.hasExtra("post_medium")) {
                showPostSuccessSnackbar(data);
            }
        } else if (requestCode == REQ_CODE_POST_ACTIV && resultCode == RESULT_OK) {
            //Returned from individual post -> refresh list to update reactions
            onRefresh();
        }
    }

    private void showPostSuccessSnackbar(@NonNull Intent data) {
        String postMedium = data.getStringExtra("post_medium");
        String text = "Posted Successfully!";
        if (postMedium.equalsIgnoreCase("img")) {
            text = "Uploading Photo...";
        } else if (postMedium.equalsIgnoreCase("vid")) {
            text = "Uploading Video...";
        }
        Snackbar snackbar = Snackbar.make(requireView(), text, Snackbar.LENGTH_SHORT);
        snackbar.setAnchorView(postBtnLl);
        snackbar.show();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (adapter != null) {
            adapter.pauseAllVideos();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        LubbleSharedPrefs.getInstance().setReplyBottomSheet(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (adapter != null) {
            adapter.clearAllVideos();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        GroupFeedFragPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showRationaleForCamera(final PermissionRequest request) {
        showStoragePermRationale(getContext(), request);
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showDeniedForCamera() {
        Toast.makeText(getContext(), R.string.storage_perm_denied_text, Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showNeverAskForCamera() {
        Toast.makeText(getContext(), R.string.storage_perm_never_text, Toast.LENGTH_LONG).show();
    }

}