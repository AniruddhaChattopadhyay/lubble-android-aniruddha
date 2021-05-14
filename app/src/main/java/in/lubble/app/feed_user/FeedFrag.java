package in.lubble.app.feed_user;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.net.MalformedURLException;

import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import in.lubble.app.services.FeedServices;
import in.lubble.app.utils.FullScreenImageActivity;
import in.lubble.app.utils.TrackingViewModel;
import in.lubble.app.utils.VisibleState;
import in.lubble.app.widget.PostReplySmoothScroller;
import io.getstream.core.exceptions.StreamException;
import io.getstream.core.models.Reaction;
import io.getstream.core.options.EnrichmentFlags;
import io.getstream.core.options.Limit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;
import static in.lubble.app.utils.FeedUtils.processTrackedPosts;

public class FeedFrag extends Fragment implements FeedAdaptor.FeedListener, ReplyListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "FeedFrag";

    private ExtendedFloatingActionButton postBtn;
    private ShimmerRecyclerView feedRV;
    private static final int REQUEST_CODE_POST = 800;
    private final String userId = FirebaseAuth.getInstance().getUid();
    private FeedAdaptor adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayoutManager layoutManager;
    private TrackingViewModel viewModel;

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
        viewModel = new ViewModelProvider(this).get(TrackingViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_feed, container, false);
        postBtn = view.findViewById(R.id.btn_new_post);
        feedRV = view.findViewById(R.id.feed_recyclerview);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_feed);

        postBtn.setVisibility(View.VISIBLE);

        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        feedRV.setLayoutManager(layoutManager);
        feedRV.showShimmerAdapter();
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.colorAccent));

        postBtn.setOnClickListener(v -> {
            startActivityForResult(new Intent(getContext(), AddPostForFeed.class), REQUEST_CODE_POST);
            getActivity().overridePendingTransition(R.anim.slide_from_bottom_fast, R.anim.none);
        });
        try {
            getCredentials();
        } catch (StreamException e) {
            e.printStackTrace();
        }
        return view;
    }

    void getCredentials() throws StreamException {
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
                        } catch (MalformedURLException | StreamException e) {
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


    private void initRecyclerView() throws StreamException {
        FeedServices.getTimelineClient().flatFeed("timeline", FeedServices.uid)
                .getEnrichedActivities(new Limit(25),
                        new EnrichmentFlags()
                                .withReactionCounts()
                                .withOwnReactions()
                                .withRecentReactions()
                )
                .whenComplete((enrichedActivities, throwable) -> {
                    if (getActivity() != null && !getActivity().isFinishing()) {
                        getActivity().runOnUiThread(() -> {
                            if (throwable != null) {
                                //todo show retry option with error msg
                                return;
                            }
                            if (feedRV.getActualAdapter() != feedRV.getAdapter()) {
                                // recycler view is currently holding shimmer adapter so hide it
                                feedRV.hideShimmerAdapter();
                            }
                            if (swipeRefreshLayout.isRefreshing()) {
                                swipeRefreshLayout.setRefreshing(false);
                            }

                            DisplayMetrics displayMetrics = new DisplayMetrics();
                            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                            int width = displayMetrics.widthPixels;

                            adapter = new FeedAdaptor(getContext(), enrichedActivities, width,
                                    GlideApp.with(this),
                                    this);
                            feedRV.setAdapter(adapter);
                            feedRV.clearOnScrollListeners();
                            feedRV.addOnScrollListener(scrollListener);
                            viewModel.getDistinctLiveData().observe(this, visibleState -> {
                                processTrackedPosts(enrichedActivities, visibleState, "timeline:" + FeedServices.uid, FeedFrag.class.getSimpleName());
                            });
                        });
                    }
                });
    }

    RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            VisibleState visibleState = new VisibleState(layoutManager.findFirstCompletelyVisibleItemPosition(),
                    layoutManager.findLastCompletelyVisibleItemPosition());
            viewModel.onScrolled(visibleState);
        }
    };

    @Override
    public void onReplyClicked(String activityId, String foreignId, int position) {
        postBtn.setVisibility(View.GONE);
        ReplyBottomSheetDialogFrag replyBottomSheetDialogFrag = ReplyBottomSheetDialogFrag.newInstance(activityId, foreignId);
        replyBottomSheetDialogFrag.show(getChildFragmentManager(), null);
        RecyclerView.SmoothScroller smoothScroller = new PostReplySmoothScroller(feedRV.getContext());
        smoothScroller.setTargetPosition(position);
        feedRV.getLayoutManager().startSmoothScroll(smoothScroller);
    }

    @Override
    public void onRefresh() {
        try {
            initRecyclerView();
        } catch (StreamException e) {
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(e);
        }
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
        postBtn.setVisibility(View.VISIBLE);
        adapter.addUserReply(activityId, reaction);
        Analytics.triggerFeedEngagement(foreignId, "comment", 10, "timeline:" + userId, FeedFrag.class.getSimpleName());
    }

    @Override
    public void onDismissed() {
        postBtn.setVisibility(View.VISIBLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_POST && resultCode == RESULT_OK) {
            try {
                initRecyclerView();
            } catch (StreamException e) {
                e.printStackTrace();
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        LubbleSharedPrefs.getInstance().setReplyBottomSheet(null);
    }
}
