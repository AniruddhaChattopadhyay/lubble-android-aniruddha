package in.lubble.app.feed_groups.SingleGroupFeed;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.emoji.widget.EmojiTextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.feed_user.AddPostForFeed;
import in.lubble.app.feed_user.FeedAdaptor;
import in.lubble.app.feed_user.ReplyBottomSheetDialogFrag;
import in.lubble.app.feed_user.ReplyListener;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import in.lubble.app.services.FeedServices;
import in.lubble.app.utils.FullScreenImageActivity;
import in.lubble.app.utils.TrackingViewModel;
import in.lubble.app.utils.VisibleState;
import io.getstream.cloud.CloudFlatFeed;
import io.getstream.core.exceptions.StreamException;
import io.getstream.core.models.EnrichedActivity;
import io.getstream.core.models.FollowRelation;
import io.getstream.core.models.Reaction;
import io.getstream.core.options.EnrichmentFlags;
import io.getstream.core.options.Limit;
import io.getstream.core.options.Offset;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;
import static in.lubble.app.Constants.MEDIA_TYPE;
import static in.lubble.app.utils.FeedUtils.processTrackedPosts;

public class SingleGroupFeed extends Fragment implements FeedAdaptor.FeedListener, ReplyListener, SwipeRefreshLayout.OnRefreshListener {

    private ExtendedFloatingActionButton postBtn;
    private ShimmerRecyclerView feedRV;
    private ProgressBar joinGroupProgressBar;
    private EmojiTextView joinGroupTv;
    private List<EnrichedActivity> activities = null;
    private static final int REQUEST_CODE_POST = 800;
    private static final String FEED_NAME_BUNDLE = "FEED_NAME";
    private String feedName = null;
    private View rootView;
    private final String userId = FirebaseAuth.getInstance().getUid();
    private SwipeRefreshLayout swipeRefreshLayout;
    private FeedAdaptor adapter;
    private LinearLayoutManager layoutManager;
    private TrackingViewModel viewModel;

    public SingleGroupFeed() {
        // Required empty public constructor
    }

    public static SingleGroupFeed newInstance(String feedName) {
        SingleGroupFeed fragment = new SingleGroupFeed();
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
            viewModel = new ViewModelProvider(this).get(TrackingViewModel.class);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_single_group_feed, container, false);
        joinGroupTv = rootView.findViewById(R.id.tv_join_group);
        postBtn = rootView.findViewById(R.id.btn_new_post);
        feedRV = rootView.findViewById(R.id.feed_recyclerview);
        joinGroupProgressBar = rootView.findViewById(R.id.progressbar_joining);
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_feed);

        postBtn.setOnClickListener(v -> {
            startActivityForResult(new Intent(getContext(), AddPostForFeed.class), REQUEST_CODE_POST);
        });

        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        feedRV.setLayoutManager(layoutManager);
        feedRV.showShimmerAdapter();
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.colorAccent));

        getCredentials();
        return rootView;
    }

    void getCredentials() {
        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        Call<Endpoints.StreamCredentials> call = endpoints.getStreamCredentials(feedName);//feedName
        call.enqueue(new Callback<Endpoints.StreamCredentials>() {
            @Override
            public void onResponse(Call<Endpoints.StreamCredentials> call, Response<Endpoints.StreamCredentials> response) {
                if (response.isSuccessful()) {
                    //Toast.makeText(getContext(), R.string.upload_success, Toast.LENGTH_SHORT).show();
                    assert response.body() != null;
                    final Endpoints.StreamCredentials credentials = response.body();
                    try {
                        FeedServices.init(credentials.getApi_key(), credentials.getUser_token());

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
                Toast.makeText(getContext(), R.string.all_try_again, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initRecyclerView() throws StreamException {
        CloudFlatFeed groupFeed = FeedServices.client.flatFeed("group", feedName);
        activities = groupFeed
                .getEnrichedActivities(new Limit(25),
                        new EnrichmentFlags()
                                .withReactionCounts()
                                .withOwnReactions()
                                .withRecentReactions())
                .join();
        if (feedRV.getActualAdapter() != feedRV.getAdapter()) {
            // recycler view is currently holding shimmer adapter so hide it
            feedRV.hideShimmerAdapter();
        }

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        adapter = new FeedAdaptor(getContext(), activities, width, GlideApp.with(this), this);
        feedRV.setAdapter(adapter);
        feedRV.clearOnScrollListeners();
        feedRV.addOnScrollListener(scrollListener);
        viewModel.getDistinctLiveData().observe(this, visibleState -> {
            processTrackedPosts(activities, visibleState, groupFeed.getID().toString(), SingleGroupFeed.class.getSimpleName());
        });

        CloudFlatFeed userTimelineFeed = FeedServices.getTimelineClient().flatFeed("timeline", FeedServices.uid);
        // Check if user follows this group feed
        try {
            List<FollowRelation> followed = userTimelineFeed.getFollowed(new Limit(1), new Offset(0), groupFeed.getID()).get();
            if (!followed.isEmpty()) {
                // joined
                joinGroupTv.setVisibility(View.GONE);
                postBtn.setVisibility(View.VISIBLE);
                if (getActivity() != null && getActivity() instanceof GroupFeedActivity) {
                    ((GroupFeedActivity) getActivity()).toggleContextMenu(true);
                }
            } else {
                // not joined
                joinGroupTv.setVisibility(View.VISIBLE);
                postBtn.setVisibility(View.GONE);

                joinGroupTv.setOnClickListener(v -> {
                    joinGroup(groupFeed, userTimelineFeed);
                });
            }
        } catch (InterruptedException | ExecutionException e) {
            if (getActivity() != null) {
                Toast.makeText(getActivity(), R.string.all_something_wrong_try_again, Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
            e.printStackTrace();
        }
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

    private void joinGroup(CloudFlatFeed groupFeed, CloudFlatFeed userTimelineFeed) {
        try {
            userTimelineFeed.follow(groupFeed).join();
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
                            postBtn.setVisibility(View.VISIBLE);
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
        } catch (StreamException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            e.printStackTrace();
        }
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
    public void onReplyClicked(String activityId, String foreignId, int position) {
        postBtn.setVisibility(View.GONE);
        ReplyBottomSheetDialogFrag replyBottomSheetDialogFrag = ReplyBottomSheetDialogFrag.newInstance(activityId, foreignId);
        replyBottomSheetDialogFrag.show(getChildFragmentManager(), null);
    }

    @Override
    public void onReplied(String activityId, String foreignId, Reaction reaction) {
        postBtn.setVisibility(View.VISIBLE);
        adapter.addUserReply(activityId, reaction);
        Analytics.triggerFeedEngagement(foreignId, "comment", 10, "group:" + feedName, SingleGroupFeed.class.getSimpleName());
    }

    @Override
    public void onImageClicked(String imgPath, ImageView imageView) {
        FullScreenImageActivity.open(getActivity(), requireContext(), imgPath, imageView, null, R.drawable.ic_cancel_black_24dp);
    }

    @Override
    public void onLiked(String foreignID) {
        Analytics.triggerFeedEngagement(foreignID, "like", 5, "group:" + feedName, SingleGroupFeed.class.getSimpleName());
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
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        LubbleSharedPrefs.getInstance().setReplyBottomSheet(null);
    }

}