package in.lubble.app.feed_user;

import android.content.Intent;
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

import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;

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
import static in.lubble.app.utils.FeedUtils.processTrackedPosts;

public class FeedFrag extends Fragment implements FeedAdaptor.FeedListener, ReplyListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "FeedFrag";

    private static final int REQUEST_CODE_NEW_POST = 800;
    private static final int REQ_CODE_POST_ACTIV = 226;

    private MaterialButton postBtn;
    private MaterialButton postQandABtn;
    private LinearLayout postBtnLL;
    private TextView emptyHintTv;
    private ShimmerRecyclerView feedRV;
    private final String userId = FirebaseAuth.getInstance().getUid();
    private FeedAdaptor adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayoutManager layoutManager;
    private FeedViewModel viewModel;

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
        postBtn = view.findViewById(R.id.btn_new_post);
        postQandABtn = view.findViewById(R.id.btn_QandA_new_post);
        postBtnLL = view.findViewById(R.id.post_btn_LL);
        emptyHintTv = view.findViewById(R.id.tv_empty_hint);
        feedRV = view.findViewById(R.id.feed_recyclerview);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_feed);

        postBtnLL.setVisibility(View.VISIBLE);
//        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) postButtonsRV.getLayoutParams();
//        if (getParentFragment() instanceof FeedCombinedFragment) {
//            lp.setMargins(0, 0, UiUtils.dpToPx(16), UiUtils.dpToPx(64));
//        } else {
//            lp.setMargins(0, 0, UiUtils.dpToPx(16), UiUtils.dpToPx(16));
//        }
//        postBtn.setLayoutParams(lp);

        layoutManager = new LinearLayoutManager(getContext());
        feedRV.setLayoutManager(layoutManager);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.colorAccent));

        postBtn.setOnClickListener(v -> {
            startActivityForResult(new Intent(getContext(), AddPostForFeed.class), REQUEST_CODE_NEW_POST);
            getActivity().overridePendingTransition(R.anim.slide_from_bottom_fast, R.anim.none);
        });
        postQandABtn.setOnClickListener(v->{
            Intent intent = new Intent(getContext(), AddPostForFeed.class);
            intent.putExtra(AddPostForFeed.QnAString,true);
            startActivityForResult(intent, REQUEST_CODE_NEW_POST);
        });

        getCredentials();

        return view;
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

            feedRV.setAdapter(adapter.withLoadStateAdapters(
                    new PagingLoadStateAdapter(() -> {
                        adapter.retry();
                        return null;
                    })
            ));
            feedRV.clearOnScrollListeners();
            feedRV.addOnScrollListener(scrollListener);
            viewModel.getDistinctLiveData().observe(this, visibleState -> {
                processTrackedPosts(adapter.snapshot().getItems(), visibleState, "timeline:" + FirebaseAuth.getInstance().getUid(), FeedFrag.class.getSimpleName());
            });
        }
        viewModel.loadPaginatedActivities(timelineFeed, 10).observe(this, pagingData -> {
            layoutManager.scrollToPosition(0);
            adapter.submitData(getViewLifecycleOwner().getLifecycle(), pagingData);
        });
        layoutManager.scrollToPosition(0);
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
                //show shimmer only on first load, not when user pulled to refresh
                feedRV.showShimmerAdapter();
            }
        } else {
            if (feedRV.getActualAdapter() != feedRV.getAdapter()) {
                // recycler view is currently holding shimmer adapter so hide it
                // without this condition pagination will break!!
                feedRV.hideShimmerAdapter();
            }
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
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
