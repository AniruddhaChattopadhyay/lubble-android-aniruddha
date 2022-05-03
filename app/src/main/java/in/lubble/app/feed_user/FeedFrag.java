package in.lubble.app.feed_user;

import static android.app.Activity.RESULT_OK;
import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE;
import static in.lubble.app.feed_user.FeedFragPermissionsDispatcher.startSharingPostWithPermissionCheck;
import static in.lubble.app.firebase.RealtimeDbHelper.getThisUserFeedIntroRef;
import static in.lubble.app.utils.FeedUtils.processTrackedPosts;
import static in.lubble.app.utils.FileUtils.showStoragePermRationale;
import static in.lubble.app.utils.UiUtils.determineYOffset;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LoadState;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import in.lubble.app.GlideApp;
import in.lubble.app.GlideRequests;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.MainActivity;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.feed_groups.SingleGroupFeed.GroupFeedActivity;
import in.lubble.app.feed_post.FeedPostActivity;
import in.lubble.app.models.FeedGroupData;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import in.lubble.app.receivers.ShareSheetReceiver;
import in.lubble.app.services.FeedServices;
import in.lubble.app.utils.FeedUtils;
import in.lubble.app.utils.FeedViewModel;
import in.lubble.app.utils.FullScreenImageActivity;
import in.lubble.app.utils.FullScreenVideoActivity;
import in.lubble.app.utils.UiUtils;
import in.lubble.app.utils.VisibleState;
import in.lubble.app.widget.PostReplySmoothScroller;
import io.getstream.cloud.CloudFlatFeed;
import io.getstream.core.models.EnrichedActivity;
import io.getstream.core.models.Reaction;
import it.sephiroth.android.library.xtooltip.ClosePolicy;
import it.sephiroth.android.library.xtooltip.Tooltip;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@RuntimePermissions
public class FeedFrag extends Fragment implements FeedAdaptor.FeedListener, ReplyListener, SwipeRefreshLayout.OnRefreshListener, JoinedGroupsStoriesAdapter.JoinedGroupsListener {

    private static final String TAG = "FeedFrag";

    private static final int REQUEST_CODE_NEW_POST = 800;
    private static final int REQ_CODE_POST_ACTIV = 226;

    private MaterialButton postBtn, postQandABtn, startIntroBtn;
    private LinearLayout postBtnLL;
    private MaterialCardView introMcv;
    private ImageView introCloseIv;
    private TextView emptyHintTv, introTitleTv, introSubtitleTv;
    private RecyclerView feedRV;
    private final String userId = FirebaseAuth.getInstance().getUid();
    private FeedAdaptor adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayoutManager layoutManager;
    private FeedViewModel viewModel;
    private LinearLayout joinedGroupStroriesLL;
    private RecyclerView joinedGroupStoriesRV;
    ArrayList<FeedGroupData> feedGroupDataList;
    private ValueEventListener feedIntroRefListener;
    private View fragmentContainer;
    private boolean isIntroStarted;

    public FeedFrag() {
        // Required empty public constructor
    }

    public static FeedFrag newInstance() {
        FeedFrag fragment = new FeedFrag();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(FeedViewModel.class);
        Analytics.triggerScreenEvent(requireContext(), this.getClass());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_feed, container, false);

        if (getParentFragment() instanceof FeedCombinedFragment) {
            ((FeedCombinedFragment) getParentFragment()).setRefreshListener(this);
        } else if (getActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).setRefreshListener(this);
        }

        if (getParentFragment() instanceof FeedCombinedFragment && getParentFragment().getView() != null) {
            View parentFragView = getParentFragment().getView();
            postBtnLL = parentFragView.findViewById(R.id.post_btn_LL);
            postBtn = parentFragView.findViewById(R.id.btn_new_post);
            postQandABtn = parentFragView.findViewById(R.id.btn_QandA_new_post);
        } else {
            postBtnLL = view.findViewById(R.id.post_btn_LL);
            postBtn = view.findViewById(R.id.btn_new_post);
            postQandABtn = view.findViewById(R.id.btn_QandA_new_post);
        }
        emptyHintTv = view.findViewById(R.id.tv_empty_hint);
        fragmentContainer = view.findViewById(R.id.fragment_container);
        introMcv = view.findViewById(R.id.mcv_intro);
        introCloseIv = view.findViewById(R.id.iv_intro_close);
        introTitleTv = view.findViewById(R.id.tv_intro_title);
        introSubtitleTv = view.findViewById(R.id.tv_intro_subtitle);
        startIntroBtn = view.findViewById(R.id.btn_start_intro);
        feedRV = view.findViewById(R.id.feed_recyclerview);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_feed);
        joinedGroupStoriesRV = view.findViewById(R.id.joined_groups_stories_recycler_view);
        joinedGroupStroriesLL = view.findViewById(R.id.ll_joined_groups_stories);
        postBtnLL.setVisibility(View.VISIBLE);
        layoutManager = new LinearLayoutManager(getContext());
        feedRV.setLayoutManager(layoutManager);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.colorAccent));

        postBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddPostForFeed.class);
            if (isIntroStarted) {
                intent.putExtra(AddPostForFeed.ARG_POST_TYPE, AddPostForFeed.TYPE_INTRO);
            }
            startActivityForResult(intent, REQUEST_CODE_NEW_POST);
            getActivity().overridePendingTransition(R.anim.slide_from_bottom_fast, R.anim.none);
        });
        postQandABtn.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddPostForFeed.class);
            intent.putExtra(AddPostForFeed.ARG_POST_TYPE, AddPostForFeed.TYPE_QNA);
            startActivityForResult(intent, REQUEST_CODE_NEW_POST);
        });

        getCredentials();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        //showIntroCard();
    }

    private void showIntroCard() {
        feedIntroRefListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Object value = snapshot.getValue();
                if (value == Boolean.TRUE) {
                    //intro done
                    introMcv.setVisibility(View.GONE);
                } else {
                    //value is null or false -> show intro card
                    introMcv.setVisibility(View.VISIBLE);
                    Analytics.triggerEvent(AnalyticsEvents.FEED_INTRO_SHOWN, requireContext());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                introMcv.setVisibility(View.GONE);
            }
        };
        getThisUserFeedIntroRef().addValueEventListener(feedIntroRefListener);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String name = (currentUser != null && currentUser.getDisplayName() != null) ? currentUser.getDisplayName().split(" ")[0] : "Neighbour";
        introTitleTv.setText(String.format("Welcome %s!", name));
        introSubtitleTv.setText(String.format("First things first: Let's introduce you to everyone in %s!\n\nWe'll help you write an awesome intro \uD83D\uDC47", LubbleSharedPrefs.getInstance().getLubbleName()));
        startIntroBtn.setOnClickListener(v -> {
            Analytics.triggerEvent(AnalyticsEvents.FEED_INTRO_START, requireContext());
            introNewPostBtn();
        });
        introCloseIv.setOnClickListener(v -> {
            Analytics.triggerEvent(AnalyticsEvents.FEED_INTRO_CANCELLED, requireContext());
            getThisUserFeedIntroRef().setValue(Boolean.TRUE);
            introMcv.setVisibility(View.GONE);
        });
    }

    private void introNewPostBtn() {
        isIntroStarted = true;
        Tooltip tooltip = new Tooltip.Builder(requireContext())
                .anchor(postBtn, 0, determineYOffset(requireActivity()), false)
                .closePolicy(ClosePolicy.Companion.getTOUCH_ANYWHERE_NO_CONSUME())
                .showDuration(10000)
                .overlay(true)
                .floatingAnimation(Tooltip.Animation.Companion.getDEFAULT())
                .styleId(R.style.BlueTooltipLayout)
                .text("Tap \"NEW POST\" button")
                .create();
        tooltip.show(postBtn, Tooltip.Gravity.TOP, false);
    }

    private void initJoinedGroupRecyclerView() {
        Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        Call<List<FeedGroupData>> call = endpoints.getFeedGroupList();
        call.enqueue(new Callback<List<FeedGroupData>>() {
            @Override
            public void onResponse(@NotNull Call<List<FeedGroupData>> call, @NotNull Response<List<FeedGroupData>> response) {
                feedGroupDataList = (ArrayList<FeedGroupData>) response.body();
                if (response.isSuccessful() && isAdded() && feedGroupDataList != null && !feedGroupDataList.isEmpty()) {
                    LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
                    joinedGroupStroriesLL.setVisibility(View.VISIBLE);
                    joinedGroupStoriesRV.setLayoutManager(layoutManager);
                    JoinedGroupsStoriesAdapter adapter = new JoinedGroupsStoriesAdapter(getContext(), feedGroupDataList, FeedFrag.this);
                    joinedGroupStoriesRV.setAdapter(adapter);
                } else if (isAdded()) {
                    Toast.makeText(getContext(), R.string.all_try_again, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<FeedGroupData>> call, Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), R.string.all_something_wrong_try_again, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity && !(getParentFragment() instanceof FeedCombinedFragment)) {
            ((MainActivity) requireActivity()).toggleSearchInToolbar(false);
            ((MainActivity) requireActivity()).toggleChatInToolbar(true);
        }
    }

    void getCredentials() {
        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        String feedUserToken = LubbleSharedPrefs.getInstance().getFeedUserToken();
        String feedApiKey = LubbleSharedPrefs.getInstance().getFeedApiKey();
        if (TextUtils.isEmpty(feedApiKey) || TextUtils.isEmpty(feedUserToken)) {
            Call<Endpoints.StreamCredentials> call = endpoints.getStreamCredentials(userId);
            call.enqueue(new Callback<Endpoints.StreamCredentials>() {
                @Override
                public void onResponse(Call<Endpoints.StreamCredentials> call, Response<Endpoints.StreamCredentials> response) {
                    if (response.isSuccessful()) {
                        //Toast.makeText(getContext(), R.string.upload_success, Toast.LENGTH_SHORT).show();
                        assert response.body() != null;
                        final Endpoints.StreamCredentials credentials = response.body();
                        try {
                            FeedServices.initTimelineClient(credentials.getApi_key(), credentials.getUser_token());
                            if (isAdded()) {
                                initRecyclerView(false);
                                initJoinedGroupRecyclerView();
                            }
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(getContext(), R.string.all_try_again, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Endpoints.StreamCredentials> call, Throwable t) {
                    if (isAdded()) {
                        Toast.makeText(getContext(), R.string.all_try_again, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            initRecyclerView(false);
            initJoinedGroupRecyclerView();
        }

    }

    private void initRecyclerView(boolean isRefresh) {
        CloudFlatFeed timelineFeed = FeedServices.getTimelineClient().flatFeed("timeline", userId);
        if (adapter == null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = displayMetrics.widthPixels;
            int height = displayMetrics.heightPixels; //height of RV, excluding toolbar & bottom nav
            adapter = new FeedAdaptor(new FeedPostComparator());
            adapter.setVars(getContext(), width, height, GlideApp.with(this), this);

            viewModel.getDistinctLiveData().observe(getViewLifecycleOwner(), visibleState -> {
                processTrackedPosts(adapter.snapshot().getItems(), visibleState, "timeline:" + FirebaseAuth.getInstance().getUid(), FeedFrag.class.getSimpleName());
            });
        }
        adapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
        feedRV.setAdapter(adapter.withLoadStateAdapters(
                new PagingLoadStateAdapter(() -> {
                    adapter.retry();
                    return null;
                })
        ));
        feedRV.clearOnScrollListeners();
        feedRV.addOnScrollListener(scrollListener);
        String algo = isRefresh ? null : "lbl_" + LubbleSharedPrefs.getInstance().getLubbleId();
        viewModel.loadPaginatedActivities(timelineFeed, 10, algo).observe(getViewLifecycleOwner(), pagingData -> {
            adapter.submitData(getViewLifecycleOwner().getLifecycle(), pagingData);
        });
        //layoutManager.scrollToPosition(0);
    }

    @Override
    public void openPostActivity(@NotNull String activityId) {
        startActivityForResult(FeedPostActivity.getIntent(requireContext(), activityId), REQ_CODE_POST_ACTIV);
    }

    @Override
    public void openGroupFeed(@NotNull FeedGroupData feedGroupData) {
        GroupFeedActivity.open(requireContext(), feedGroupData.getFeedName());
    }

    @Override
    public void showEmptyView(boolean show) {
        if (show) {
            emptyHintTv.setVisibility(View.VISIBLE);
            emptyHintTv.setText("Join groups to view their posts here");
        } else {
            emptyHintTv.setVisibility(View.GONE);
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
        int state = SCROLL_STATE_IDLE;

        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            state = newState;
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (dy > 0) {
                UiUtils.animateSlideDownHide(getContext(), postBtnLL);
            } else {
                UiUtils.animateSlideUpShow(getContext(), postBtnLL);
            }
            VisibleState visibleState = new VisibleState(layoutManager.findFirstCompletelyVisibleItemPosition(),
                    layoutManager.findLastCompletelyVisibleItemPosition());
            viewModel.onScrolled(visibleState);
        }
    };

    @Override
    public void onReplyClicked(String activityId, String foreignId, String postActorUid, int position) {
        postBtnLL.setVisibility(View.GONE);
        ReplyBottomSheetDialogFrag replyBottomSheetDialogFrag = ReplyBottomSheetDialogFrag.newInstance(activityId, foreignId, postActorUid);
        replyBottomSheetDialogFrag.show(getChildFragmentManager(), null);
        RecyclerView.SmoothScroller smoothScroller = new PostReplySmoothScroller(feedRV.getContext());
        smoothScroller.setTargetPosition(position);
        feedRV.getLayoutManager().startSmoothScroll(smoothScroller);
    }

    @Override
    public void onExploreClicked() {
        if (getParentFragment() instanceof FeedCombinedFragment) {
            // koramangala, SVR -> open 2nd top tab
            ((FeedCombinedFragment) getParentFragment()).setCurrentTabPos(1);
        } else if (getActivity() instanceof MainActivity) {
            // all else -> open 2nd bottom tab
            ((MainActivity) requireActivity()).setSelectedNavPos(R.id.navigation_feed_groups);
        }
    }

    @Override
    public void onRefresh() {
        initRecyclerView(true);
        initJoinedGroupRecyclerView();
        Analytics.triggerEvent(AnalyticsEvents.FEED_REFRESHED, requireContext());
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
        Analytics.triggerFeedEngagement(foreignID, "like", 5, "timeline:" + userId, FeedFrag.class.getSimpleName());
    }

    @Override
    public void onReplied(String activityId, String foreignId, Reaction reaction) {
        postBtnLL.setVisibility(View.VISIBLE);
        adapter.addUserReply(activityId, reaction);
        Analytics.triggerFeedEngagement(foreignId, "comment", 10, "timeline:" + userId, FeedFrag.class.getSimpleName());
    }

    @Override
    public void onShareClicked(EnrichedActivity activity, Map<String, Object> extras) {
        startSharingPostWithPermissionCheck(FeedFrag.this, activity, extras);
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
        Analytics.triggerEvent(AnalyticsEvents.POST_SHARED, requireContext());
    }

    @Override
    public void onDismissed() {
        postBtnLL.setVisibility(View.VISIBLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_NEW_POST && resultCode == RESULT_OK) {
            // Posted a new post -> refresh list
            initRecyclerView(true);
            getThisUserFeedIntroRef().setValue(Boolean.TRUE);
            introMcv.setVisibility(View.GONE);
            isIntroStarted = false;
            if (data != null && data.hasExtra("post_medium")) {
                showPostSuccessSnackbar(data);
            }
        } else if (requestCode == REQ_CODE_POST_ACTIV && resultCode == RESULT_OK) {
            //Returned from individual post -> refresh list to update reactions
            initRecyclerView(false);
        }
    }

    private void showPostSuccessSnackbar(@NonNull Intent data) {
        String groupTitle = data.getStringExtra("group_title");
        String feedName = data.getStringExtra("feed_name");
        String postMedium = data.getStringExtra("post_medium");
        String text = "Posted Successfully!";
        if (postMedium.equalsIgnoreCase("img")) {
            text = "Uploading Photo...";
        } else if (postMedium.equalsIgnoreCase("vid")) {
            text = "Uploading Video...";
        }
        Snackbar snackbar = Snackbar.make(fragmentContainer, text, Snackbar.LENGTH_SHORT);
        snackbar.setAnchorView(postBtnLL);
        if (groupTitle != null && groupTitle.toLowerCase(Locale.ROOT).startsWith("introduction")) {
            text += " View " + groupTitle + " group";
            snackbar.setText(text);
            snackbar.setAction("View", v -> {
                Analytics.triggerEvent(AnalyticsEvents.FEED_INTRO_GROUP_VIA_SNACKBAR, requireContext());
                GroupFeedActivity.open(requireContext(), feedName);
            });
        }
        snackbar.show();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).toggleChatInToolbar(false);
        }
        if (adapter != null) {
            adapter.pauseAllVideos();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        LubbleSharedPrefs.getInstance().setReplyBottomSheet(null);
        if (feedIntroRefListener != null) {
            getThisUserFeedIntroRef().removeEventListener(feedIntroRefListener);
        }
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
        FeedFragPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
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
