package in.lubble.app.feed_user;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
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
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.MainActivity;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.feed_groups.SingleGroupFeed.GroupFeedActivity;
import in.lubble.app.feed_post.FeedPostActivity;
import in.lubble.app.models.FeedGroupData;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import in.lubble.app.services.FeedServices;
import in.lubble.app.utils.FeedViewModel;
import in.lubble.app.utils.FullScreenImageActivity;
import in.lubble.app.utils.UiUtils;
import in.lubble.app.utils.VisibleState;
import in.lubble.app.widget.PostReplySmoothScroller;
import io.getstream.cloud.CloudFlatFeed;
import io.getstream.core.models.Reaction;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;
import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE;
import static in.lubble.app.utils.FeedUtils.processTrackedPosts;

public class FeedFrag extends Fragment implements FeedAdaptor.FeedListener, ReplyListener, SwipeRefreshLayout.OnRefreshListener, JoinedGroupsStoriesAdapter.JoinedGroupsListener {

    private static final String TAG = "FeedFrag";

    private static final int REQUEST_CODE_NEW_POST = 800;
    private static final int REQ_CODE_POST_ACTIV = 226;

    private MaterialButton postBtn;
    private MaterialButton postQandABtn;
    private LinearLayout postBtnLL;
    private TextView emptyHintTv;
    private RecyclerView feedRV;
    private final String userId = FirebaseAuth.getInstance().getUid();
    private FeedAdaptor adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayoutManager layoutManager;
    private FeedViewModel viewModel;
    private LinearLayout joinedGroupStroriesLL;
    private RecyclerView joinedGroupStoriesRV;
    ArrayList<FeedGroupData> feedGroupDataList;

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
        if (getParentFragment() instanceof FeedCombinedFragment) {
            ((FeedCombinedFragment) getParentFragment()).setRefreshListener(this);
        } else if (getActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).setRefreshListener(this);
        }
        Analytics.triggerScreenEvent(requireContext(), this.getClass());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_feed, container, false);

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
            startActivityForResult(new Intent(getContext(), AddPostForFeed.class), REQUEST_CODE_NEW_POST);
            getActivity().overridePendingTransition(R.anim.slide_from_bottom_fast, R.anim.none);
        });
        postQandABtn.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddPostForFeed.class);
            intent.putExtra(AddPostForFeed.QnAString, true);
            startActivityForResult(intent, REQUEST_CODE_NEW_POST);
        });

        getCredentials();

        return view;
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
                    final ViewTreeObserver observer = joinedGroupStroriesLL.getViewTreeObserver();
                    observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            int heightOfLayout = joinedGroupStroriesLL.getHeight();
                            if (observer.isAlive()) {
                                observer.removeOnGlobalLayoutListener(this);
                            }
                        }
                    });
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
                            initRecyclerView();
                            initJoinedGroupRecyclerView();
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
            initRecyclerView();
            initJoinedGroupRecyclerView();
        }

    }

    private void initRecyclerView() {
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
        viewModel.loadPaginatedActivities(timelineFeed, 10).observe(getViewLifecycleOwner(), pagingData -> {
            //layoutManager.scrollToPosition(0);
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
        GroupFeedActivity.open(requireContext(), feedGroupData);
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
        initRecyclerView();
    }

    @Override
    public void onImageClicked(String imgPath, ImageView imageView) {
        FullScreenImageActivity.open(getActivity(), requireContext(), imgPath, imageView, null, R.drawable.ic_cancel_black_24dp);
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
    public void onDismissed() {
        postBtnLL.setVisibility(View.VISIBLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_NEW_POST && resultCode == RESULT_OK) {
            initRecyclerView();
        } else if (requestCode == REQ_CODE_POST_ACTIV && resultCode == RESULT_OK) {
            //refresh list
            initRecyclerView();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).toggleChatInToolbar(false);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        LubbleSharedPrefs.getInstance().setReplyBottomSheet(null);
    }
}
