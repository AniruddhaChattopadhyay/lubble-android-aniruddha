package in.lubble.app.profile;

import static android.app.Activity.RESULT_OK;
import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;
import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE;

import android.Manifest;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LoadState;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import in.lubble.app.BuildConfig;
import in.lubble.app.GlideApp;
import in.lubble.app.GlideRequests;
import in.lubble.app.LubbleApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.MainActivity;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.chat.ChatActivity;
import in.lubble.app.explore.ExploreActiv;
import in.lubble.app.feed_groups.SingleGroupFeed.GroupFeedActivity;
import in.lubble.app.feed_post.FeedPostActivity;
import in.lubble.app.feed_user.FeedAdaptor;
import in.lubble.app.feed_user.FeedCombinedFragment;
import in.lubble.app.feed_user.FeedFrag;
import in.lubble.app.feed_user.FeedPostComparator;
import in.lubble.app.feed_user.JoinedGroupsStoriesAdapter;
import in.lubble.app.feed_user.PagingLoadStateAdapter;
import in.lubble.app.feed_user.ReplyBottomSheetDialogFrag;
import in.lubble.app.feed_user.ReplyListener;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.FeedGroupData;
import in.lubble.app.models.ProfileData;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import in.lubble.app.receivers.ShareSheetReceiver;
import in.lubble.app.referrals.ReferralActivity;
import in.lubble.app.services.FeedServices;
import in.lubble.app.utils.FeedUtils;
import in.lubble.app.utils.FeedViewModel;
import in.lubble.app.utils.FragUtils;
import in.lubble.app.utils.FullScreenImageActivity;
import in.lubble.app.utils.FullScreenVideoActivity;
import in.lubble.app.utils.RoundedCornersTransformation;
import in.lubble.app.utils.UiUtils;
import in.lubble.app.utils.UserUtils;
import in.lubble.app.utils.VisibleState;
import in.lubble.app.widget.PostReplySmoothScroller;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import io.getstream.cloud.CloudFlatFeed;
import io.getstream.core.models.EnrichedActivity;
import io.getstream.core.models.Reaction;
import permissions.dispatcher.NeedsPermission;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static in.lubble.app.analytics.AnalyticsEvents.NEW_DM_CLICKED;
import static in.lubble.app.firebase.RealtimeDbHelper.getThisUserFeedIntroRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getUserRef;
import static in.lubble.app.utils.FeedUtils.processTrackedPosts;
import static in.lubble.app.utils.ReferralUtils.generateBranchUrl;
import static in.lubble.app.utils.ReferralUtils.getReferralIntent;
import static in.lubble.app.utils.StringUtils.isValidString;

import org.jetbrains.annotations.NotNull;

public class ProfileFrag extends Fragment implements FeedAdaptor.FeedListener, ReplyListener,SwipeRefreshLayout.OnRefreshListener,JoinedGroupsStoriesAdapter.JoinedGroupsListener  {
    private static final String TAG = "ProfileFrag";
    private static final String ARG_USER_ID = "arg_user_id";
    private static final int REQ_CODE_POST_ACTIV = 226;

    private View rootView;
    private String userId;
    private ImageView profilePicIv;
    private TextView userName;
    private TextView badgeTv;
    private TextView lubbleTv;
    private TextView userBio;
    private TextView editProfileTV;
    private MaterialButton msgBtn;
    private MaterialButton statusBtn;
    private TextView invitedTv;
    private TextView likesTv;
    private LinearLayout coinsContainer;
    private TextView coinsTv;
    private RecyclerView userGroupsRv;
    private Button inviteBtn;
    private TextView logoutTv;
    private ProgressBar progressBar;
//    private CardView referralCard;
    private DatabaseReference userRef;
    private DatabaseReference dmRef;
    @Nullable
    private ProfileData profileData;
    private String sharingUrl;
    private ProgressDialog sharingProgressDialog;
//    private GroupsAdapter groupsAdapter;
    private ConstraintLayout statsContainer;
    private ImageView genderIv;
    private TextView genderTv;
    private ImageView businessIv;
    private TextView businessTv;
    private ImageView educationIv;
    private TextView educationTv;
    private int profileView;
    private UserProfileData userProfileData;
    private RecyclerView feedRV;
    private FeedAdaptor adapter;
    private FeedViewModel viewModel;
    private LinearLayoutManager layoutManager;
    private LinearLayout postBtnLL;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView joinedGroupStoriesRV;
    private LinearLayout joinedGroupStroriesLL;
    private ArrayList<FeedGroupData> feedGroupDataList;


    public ProfileFrag() {
        //Required empty public constructor
    }

    public static ProfileFrag newInstance(String profileId) {
        ProfileFrag fragment = new ProfileFrag();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, profileId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userId = FirebaseAuth.getInstance().getUid();
        if (getArguments() != null && !TextUtils.isEmpty(getArguments().getString(ARG_USER_ID))) {
            userId = getArguments().getString(ARG_USER_ID);
        }
        viewModel = new ViewModelProvider(this).get(FeedViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        postBtnLL = rootView.findViewById(R.id.post_btn_LL);
        profilePicIv = rootView.findViewById(R.id.iv_profilePic);
        userName = rootView.findViewById(R.id.tv_name);
        badgeTv = rootView.findViewById(R.id.tv_badge);
        lubbleTv = rootView.findViewById(R.id.tv_lubble);
        userBio = rootView.findViewById(R.id.tv_bio);
        editProfileTV = rootView.findViewById(R.id.tv_editProfile);
        msgBtn = rootView.findViewById(R.id.btn_msg);
        statusBtn = rootView.findViewById(R.id.btn_status);
        invitedTv = rootView.findViewById(R.id.tv_invited);
        likesTv = rootView.findViewById(R.id.tv_likes);
        genderIv = rootView.findViewById(R.id.iv_gender);
        genderTv = rootView.findViewById(R.id.tv_gender);
        businessIv = rootView.findViewById(R.id.iv_business);
        businessTv = rootView.findViewById(R.id.tv_business);
        educationIv = rootView.findViewById(R.id.iv_education);
        educationTv = rootView.findViewById(R.id.tv_education);
        coinsContainer = rootView.findViewById(R.id.container_current_coins);
        coinsTv = rootView.findViewById(R.id.tv_total_coins);
        statsContainer = rootView.findViewById(R.id.container_stats);
        progressBar = rootView.findViewById(R.id.progressBar_profile);
        layoutManager = new LinearLayoutManager(getContext());
        feedRV = rootView.findViewById(R.id.feed_recyclerview);
        feedRV.setLayoutManager(layoutManager);
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_feed);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.colorAccent));
        joinedGroupStoriesRV = rootView.findViewById(R.id.joined_groups_stories_recycler_view);
        joinedGroupStroriesLL = rootView.findViewById(R.id.ll_joined_groups_stories);

        Bundle bundle = new Bundle();
        bundle.putString("profile_uid", userId);
        Analytics.triggerScreenEvent(getContext(), this.getClass(), bundle);

        Log.d("database_uid", RealtimeDbHelper.getThisUserRef().toString());//whole link to the user we are logged in as
        Log.d("uid", userId);//user we are currently on the profile of
        Log.d("uid_this", FirebaseAuth.getInstance().getUid());//the user we are logged in as

        sharingProgressDialog = new ProgressDialog(getContext());
        generateBranchUrl(getContext(), linkCreateListener);
        if (userId.equalsIgnoreCase(FirebaseAuth.getInstance().getUid())) {
            coinsContainer.setVisibility(View.VISIBLE);
            msgBtn.setVisibility(View.GONE);
        }
        profilePicIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String profilePicUrl;
                if (profileData != null) {
                    profilePicUrl = profileData.getProfilePic();
                    if (isValidString(profilePicUrl)) {
                        String uploadPath = null;
                        if (userId.equalsIgnoreCase(FirebaseAuth.getInstance().getUid())) {
                            uploadPath = "user_profile/" + FirebaseAuth.getInstance().getUid();
                        }
                        FullScreenImageActivity.open(getActivity(), getContext(), profilePicUrl, profilePicIv, uploadPath, R.drawable.ic_account_circle_black_no_padding);
                    }
                }
            }
        });

        editProfileTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragUtils.addFrag(getFragmentManager(), R.id.frameLayout_fragContainer, EditProfileFrag.newInstance());
            }
        });

        if (userId.equals(FirebaseAuth.getInstance().getUid())) {
            statusBtn.setVisibility(View.VISIBLE);

            statusBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    StatusBottomSheetFragment statusBottomSheetFragment = new StatusBottomSheetFragment(() -> {
                        Snackbar.make(getView(), "Badge updated!", Snackbar.LENGTH_LONG).show();
                        fetchProfileFeed();
                    });
                    statusBottomSheetFragment.show(getFragmentManager(), statusBottomSheetFragment.getTag());
                }
            });
        }

//        groupsAdapter = new GroupsAdapter(GlideApp.with(requireContext()));
        fetchStats();

        coinsContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReferralActivity.open(requireContext());
            }
        });

        initFeedRecyclerView(true);
        initJoinedGroupRecyclerView();
        return rootView;
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

    private void initJoinedGroupRecyclerView() {
        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        endpoints.fetchUserProfile(userId).enqueue(new Callback<UserProfileData>() {
            @Override
            public void onResponse(Call<UserProfileData> call, Response<UserProfileData> response) {
                userProfileData = response.body();
                if (response.isSuccessful() && userProfileData != null && isAdded()) {
                    LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
                    joinedGroupStroriesLL.setVisibility(View.VISIBLE);
                    joinedGroupStoriesRV.setLayoutManager(layoutManager);
                    feedGroupDataList = (ArrayList<FeedGroupData>) userProfileData.getJoinedGroups();
                    JoinedGroupsStoriesAdapter adapter = new JoinedGroupsStoriesAdapter(getContext(), feedGroupDataList, ProfileFrag.this);
                    joinedGroupStoriesRV.setAdapter(adapter);

                } else if (isAdded() && isVisible()) {
                    FirebaseCrashlytics.getInstance().log("referral leaderboard bad response");
                    Toast.makeText(getContext(), R.string.all_try_again, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserProfileData> call, Throwable t) {
                if (isAdded() && isVisible()) {
                    Log.e(TAG, "onFailure: ");
                    Toast.makeText(getContext(), R.string.check_internet, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void initFeedRecyclerView(boolean isRefresh) {
        CloudFlatFeed timelineFeed = FeedServices.getTimelineClient().flatFeed("user", userId);
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
    public void onStart() {
        super.onStart();
        userRef = getUserRef(userId);
        fetchProfileFeed();
        if (!userId.equalsIgnoreCase(FirebaseAuth.getInstance().getUid())) {
            userRef.runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                    if (currentData == null) {
                        return Transaction.success(currentData);
                    } else {
                        if (currentData.hasChild("profileViews")) {
                            profileView = currentData.child("profileViews").getValue(Integer.class);
                        } else {
                            profileView = 0;
                        }
                    }
                    profileView += 1;
                    userRef.child("profileViews").setValue(profileView);
                    return Transaction.success(currentData);
                }

                @Override
                public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {

                }
            });
        }
    }
    final Branch.BranchLinkCreateListener linkCreateListener = new Branch.BranchLinkCreateListener() {
        @Override
        public void onLinkCreate(String url, BranchError error) {
            if (url != null) {
                Log.d(TAG, "got my Branch link to share: " + url);
                sharingUrl = url;
                if (sharingProgressDialog != null && sharingProgressDialog.isShowing()) {
                    sharingProgressDialog.dismiss();
                }
            } else {
                Log.e(TAG, "Branch onLinkCreate: " + error.getMessage());
                FirebaseCrashlytics.getInstance().recordException(new IllegalStateException(error.getMessage()));
                if (isAdded() && isVisible()) {
                    Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    @Override
    public void onRefresh() {
        initFeedRecyclerView(true);
        initJoinedGroupRecyclerView();
        Analytics.triggerEvent(AnalyticsEvents.FEED_REFRESHED, requireContext());
    }


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

    }

    @Override
    public void onShareClicked(EnrichedActivity activity, Map<String, Object> extras) {
        startSharingPostProfile(activity, extras);
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
    public void startSharingPostProfile(EnrichedActivity activity, Map<String, Object> extras) {
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
        Analytics.triggerEvent(AnalyticsEvents.POST_SHARED, bundle, requireContext());
    }

    @Override
    public void onExploreClicked() {
        ExploreActiv.open(getContext(), false);
    }


    private void fetchProfileFeed() {
        progressBar.setVisibility(View.VISIBLE);
        userRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e("firebase", "Error getting data", task.getException());
            } else if (isAdded()) {
                profileData = task.getResult().getValue(ProfileData.class);
                if (profileData != null && profileData.getInfo() != null) {
                    userName.setText(profileData.getInfo().getName());
                    for (String lubbleId : profileData.getLubbles().keySet()) {
                        RealtimeDbHelper.getLubbleInfoRef(lubbleId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                lubbleTv.setText(dataSnapshot.child("title").getValue(String.class));
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                        break;
                    }
                    if (!TextUtils.isEmpty(profileData.getInfo().getBadge())) {
                        badgeTv.setVisibility(View.VISIBLE);
                        badgeTv.setText(profileData.getInfo().getBadge());
                    } else {
                        badgeTv.setVisibility(View.GONE);
                    }

                    if (isValidString(profileData.getBio())) {
                        userBio.setText(profileData.getBio());
                    } else if (userId.equalsIgnoreCase(FirebaseAuth.getInstance().getUid())) {
                        userBio.setText(R.string.edit_profile_to_add_bio);
                    } else {
                        userBio.setText(R.string.no_bio_text);
                    }
                    populateProfileDetails();
                    msgBtn.setEnabled(false);
                    msgBtn.setText("LOADING...");
                    syncDms();
                    if (userId.equalsIgnoreCase(FirebaseAuth.getInstance().getUid())) {
                        editProfileTV.setVisibility(View.VISIBLE);
                    } else {
                        editProfileTV.setVisibility(View.GONE);
                    }
                    likesTv.setText(String.valueOf(profileData.getLikes()));
                    coinsTv.setText(String.valueOf(profileData.getCoins()));
                    GlideApp.with(getContext())
                            .load(profileData.getProfilePic())
                            .error(R.drawable.ic_account_circle_black_no_padding)
                            .apply(new RequestOptions()
                                    .placeholder(R.drawable.ic_account_circle_black_no_padding)
                                    .dontAnimate().skipMemoryCache(true))
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    progressBar.setVisibility(View.GONE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    progressBar.setVisibility(View.GONE);
                                    return false;
                                }
                            })
                            .circleCrop()
                            .into(profilePicIv);
                } else {
                    if (getActivity() != null) {
                        Toast.makeText(LubbleApp.getAppContext(), "Error loading profile, plz retry", Toast.LENGTH_SHORT).show();
                        getActivity().finish();
                    }
                }
            }
        });
    }

    private void syncDms() {
        dmRef = RealtimeDbHelper.getUserDmsRef(userId);
        dmRef.orderByChild("profileId").equalTo(FirebaseAuth.getInstance().getUid()).addValueEventListener(dmValueEventListener);
    }

    private final ValueEventListener dmValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
            boolean dmExists = false;
            if (dataSnapshot.getChildrenCount() > 0) {
                dmExists = true;
            }
            msgBtn.setText("MESSAGE");
            msgBtn.setEnabled(true);
            setMsgBtnClickListener(dmExists, dataSnapshot);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    public void setMsgBtnClickListener(boolean dmExists, @NonNull DataSnapshot dataSnapshot) {
        msgBtn.setOnClickListener(v -> {
            if (dmExists) {
                ChatActivity.openForDm(requireContext(), dataSnapshot.getChildren().iterator().next().getKey(), null, null);
            } else if (profileData != null && profileData.getInfo() != null) {
                String userName = profileData.getInfo().getName();
                if (!profileData.getIsDmEnabled()) {
                    UiUtils.showBottomSheetAlertLight(requireContext(), getLayoutInflater(),
                            userName + " has disabled message requests",
                            "Nobody can start private chats with " + userName + " as they have disabled new message requests.\n\nOnly they can start new chats with others.",
                            R.drawable.ic_baseline_privacy_tip_24, getString(R.string.all_ok), null
                    );
                } else {
                    DmIntroBottomSheet.newInstance(userId, userName, profileData.getInfo().getThumbnail(), null).show(getChildFragmentManager(), null);
                    Analytics.triggerEvent(NEW_DM_CLICKED, getContext());
                }
            } else {
                Toast.makeText(requireContext(), R.string.all_something_wrong_try_again, Toast.LENGTH_SHORT).show();
                fetchProfileFeed();
            }
        });
    }

    private void populateProfileDetails() {
        if (!TextUtils.isEmpty(profileData.getGenderText())) {
            genderTv.setText(profileData.getGenderText());
            if (profileData.getIsAgePublic()) {
                genderTv.append(", " + profileData.getAge());
            }
            genderIv.setVisibility(View.VISIBLE);
            genderTv.setVisibility(View.VISIBLE);
        } else {
            genderIv.setVisibility(View.GONE);
            genderTv.setVisibility(View.GONE);
        }

        String companyText = profileData.getJobTitle();
        if (!TextUtils.isEmpty(profileData.getCompany())) {
            if (!TextUtils.isEmpty(profileData.getJobTitle())) {
                companyText += " @ ";
            }
            companyText += profileData.getCompany();
        }
        if (!TextUtils.isEmpty(companyText)) {
            businessTv.setText(companyText);
            businessIv.setVisibility(View.VISIBLE);
            businessTv.setVisibility(View.VISIBLE);
        } else {
            businessIv.setVisibility(View.GONE);
            businessTv.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(profileData.getSchool())) {
            educationTv.setText(profileData.getSchool());
            educationIv.setVisibility(View.VISIBLE);
            educationTv.setVisibility(View.VISIBLE);
        } else {
            educationIv.setVisibility(View.GONE);
            educationTv.setVisibility(View.GONE);
        }

    }

    private void fetchStats() {
        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        endpoints.fetchUserProfile(userId).enqueue(new Callback<UserProfileData>() {
            @Override
            public void onResponse(Call<UserProfileData> call, Response<UserProfileData> response) {
                userProfileData = response.body();
                if (response.isSuccessful() && userProfileData != null && isAdded()) {
//                    groupsAdapter.addGroupList(userProfileData.getJoinedGroups());
                    invitedTv.setText(String.valueOf(userProfileData.getReferrals()));
                } else if (isAdded() && isVisible()) {
                    FirebaseCrashlytics.getInstance().log("referral leaderboard bad response");
                    Toast.makeText(getContext(), R.string.all_try_again, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserProfileData> call, Throwable t) {
                if (isAdded() && isVisible()) {
                    Log.e(TAG, "onFailure: ");
                    Toast.makeText(getContext(), R.string.check_internet, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public void onStop() {
        super.onStop();
        if (dmRef != null && dmValueEventListener != null) {
            dmRef.removeEventListener(dmValueEventListener);
        }
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
        if (requestCode == REQ_CODE_POST_ACTIV && resultCode == RESULT_OK) {
            //Returned from individual post -> refresh list to update reactions
            initFeedRecyclerView(false);
        }
    }
}
