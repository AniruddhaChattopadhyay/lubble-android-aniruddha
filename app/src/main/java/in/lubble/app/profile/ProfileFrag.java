package in.lubble.app.profile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
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
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.freshchat.consumer.sdk.Freshchat;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import in.lubble.app.BuildConfig;
import in.lubble.app.GlideApp;
import in.lubble.app.GlideRequests;
import in.lubble.app.LubbleApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.chat.ChatActivity;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.GroupInfoData;
import in.lubble.app.models.ProfileData;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import in.lubble.app.referrals.ReferralActivity;
import in.lubble.app.utils.FragUtils;
import in.lubble.app.utils.FullScreenImageActivity;
import in.lubble.app.utils.RoundedCornersTransformation;
import in.lubble.app.utils.UiUtils;
import in.lubble.app.utils.UserUtils;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static in.lubble.app.analytics.AnalyticsEvents.NEW_DM_CLICKED;
import static in.lubble.app.firebase.RealtimeDbHelper.getUserRef;
import static in.lubble.app.utils.ReferralUtils.generateBranchUrl;
import static in.lubble.app.utils.ReferralUtils.getReferralIntent;
import static in.lubble.app.utils.StringUtils.isValidString;

public class ProfileFrag extends Fragment {
    private static final String TAG = "ProfileFrag";
    private static final String ARG_USER_ID = "arg_user_id";

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
    private CardView referralCard;
    private DatabaseReference userRef;
    private DatabaseReference dmRef;
    @Nullable
    private ProfileData profileData;
    private String sharingUrl;
    private ProgressDialog sharingProgressDialog;
    private GroupsAdapter groupsAdapter;
    private ConstraintLayout statsContainer;

    private ImageView genderIv;
    private TextView genderTv;
    private ImageView businessIv;
    private TextView businessTv;
    private ImageView educationIv;
    private TextView educationTv;

    private int profileView;


    public ProfileFrag() {
        // Required empty public constructor
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);

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
        userGroupsRv = rootView.findViewById(R.id.rv_user_groups);
        referralCard = rootView.findViewById(R.id.card_referral);
        inviteBtn = rootView.findViewById(R.id.btn_invite);
        coinsContainer = rootView.findViewById(R.id.container_current_coins);
        coinsTv = rootView.findViewById(R.id.tv_total_coins);
        statsContainer = rootView.findViewById(R.id.container_stats);
        logoutTv = rootView.findViewById(R.id.tv_logout);
        progressBar = rootView.findViewById(R.id.progressBar_profile);
        TextView versionTv = rootView.findViewById(R.id.tv_version_name);
        RelativeLayout feedbackView = rootView.findViewById(R.id.feedback_container);

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
                    StatusBottomSheetFragment statusBottomSheetFragment = new StatusBottomSheetFragment(rootView);
                    statusBottomSheetFragment.show(getFragmentManager(), statusBottomSheetFragment.getTag());
                }
            });
        }

        inviteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onInviteClicked();
            }
        });

        logoutTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Analytics.triggerLogoutEvent(getContext());
                UserUtils.logout(getActivity());
            }
        });

        versionTv.setVisibility(FirebaseAuth.getInstance().getUid().equalsIgnoreCase(userId) ? View.VISIBLE : View.GONE);
        feedbackView.setVisibility(FirebaseAuth.getInstance().getUid().equalsIgnoreCase(userId) ? View.VISIBLE : View.GONE);

        versionTv.setText(BuildConfig.VERSION_NAME);
        feedbackView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Freshchat.showConversations(requireContext());
            }
        });

        groupsAdapter = new GroupsAdapter(GlideApp.with(requireContext()));
        userGroupsRv.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        userGroupsRv.setAdapter(groupsAdapter);
        fetchStats();

        coinsContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReferralActivity.open(requireContext());
            }
        });

        return rootView;
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

    private void syncGroups(ProfileData profileData) {
        // fetch token first
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        mUser.getIdToken(false)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String idToken = task.getResult().getToken();
                        HashMap<String, Object> groupsMap = profileData.getLubbles().get(LubbleSharedPrefs.getInstance().getLubbleId()).get("groups");
                        groupsAdapter.clear();
                        for (String groupId : groupsMap.keySet()) {
                            fetchGroupInfo(groupId, idToken);
                        }
                    } else {
                        Toast.makeText(getContext(), "Failed to fetch access token", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchGroupInfo(String groupId, String idToken) {
        final Endpoints endpoints = ServiceGenerator.createFirebaseService(Endpoints.class);
        Call<GroupInfoData> groupDataCall = endpoints.fetchGroupInfo(LubbleSharedPrefs.getInstance().requireLubbleId(), groupId, idToken);
        groupDataCall.enqueue(new Callback<GroupInfoData>() {
            @Override
            public void onResponse(@NotNull Call<GroupInfoData> call, @NotNull Response<GroupInfoData> response) {
                if (response.isSuccessful() && isAdded()) {
                    final GroupInfoData groupInfoData = response.body();
                    if (groupInfoData != null && !groupInfoData.getIsPrivate() && !groupInfoData.getIsDm()) {
                        groupsAdapter.addGroupInfo(groupInfoData);
                    }
                } else if (isAdded()) {
                    Toast.makeText(getContext(), "error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GroupInfoData> call, Throwable t) {
                if (isAdded() && isVisible()) {
                    Toast.makeText(getContext(), R.string.check_internet, Toast.LENGTH_SHORT).show();
                }
            }
        });
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

    private class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.ViewHolder> {

        private ArrayList<GroupInfoData> groupList = new ArrayList<>();
        private GlideRequests glideApp;

        GroupsAdapter(GlideRequests glideApp) {
            this.glideApp = glideApp;
        }

        @Override
        public GroupsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new GroupsAdapter.ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(GroupsAdapter.ViewHolder holder, int position) {
            final GroupInfoData groupData = groupList.get(position);
            RequestOptions requestOptions = new RequestOptions();
            requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCornersTransformation(UiUtils.dpToPx(8), 0));
            glideApp.load(groupData.getThumbnail())
                    .placeholder(R.drawable.rounded_rect_gray)
                    .error(R.drawable.rounded_rect_gray)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .apply(requestOptions)
                    .into(holder.groupDpIv);
            holder.groupNameTv.setText(groupData.getTitle());
        }

        @Override
        public int getItemCount() {
            return groupList.size();
        }

        void addGroupList(ArrayList<GroupInfoData> groupDataList) {
            groupList.addAll(groupDataList);
            notifyDataSetChanged();
        }

        void addGroupInfo(GroupInfoData groupInfoData) {
            groupList.add(groupInfoData);
            notifyDataSetChanged();
        }

        public void clear() {
            groupList.clear();
            notifyDataSetChanged();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            final ImageView groupDpIv;
            final TextView groupNameTv;
            final TextView groupMemberTv;

            ViewHolder(LayoutInflater inflater, ViewGroup parent) {
                super(inflater.inflate(R.layout.item_user_group, parent, false));
                groupDpIv = itemView.findViewById(R.id.iv_wheretonight_pic);
                groupNameTv = itemView.findViewById(R.id.tv_group_title);
                groupMemberTv = itemView.findViewById(R.id.tv_member_hint);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final GroupInfoData groupData = groupList.get(getAdapterPosition());
                        ChatActivity.openForGroup(getContext(), groupData.getId(), false, null);
                    }
                });
            }
        }
    }

    private void fetchProfileFeed() {
        progressBar.setVisibility(View.VISIBLE);
        userRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e("firebase", "Error getting data", task.getException());
            } else {
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
                        if (lubbleId.equalsIgnoreCase(LubbleSharedPrefs.getInstance().getLubbleId())) {
                            // profile is of the same lubble as current user, show chat groups
                            syncGroups(profileData);
                        }
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
                        referralCard.setVisibility(View.VISIBLE);
                        logoutTv.setVisibility(View.VISIBLE);
                    } else {
                        editProfileTV.setVisibility(View.GONE);
                        referralCard.setVisibility(View.GONE);
                        logoutTv.setVisibility(View.GONE);
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
                final UserProfileData userProfileData = response.body();
                if (response.isSuccessful() && userProfileData != null && isAdded() && isVisible()) {
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

    private void onInviteClicked() {
        final Intent referralIntent = getReferralIntent(getContext(), sharingUrl, sharingProgressDialog, linkCreateListener);
        if (referralIntent != null) {
            startActivity(Intent.createChooser(referralIntent, getString(R.string.refer_share_title)));
            Analytics.triggerEvent(AnalyticsEvents.REFERRAL_PROFILE_SHARE, getContext());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (dmRef != null && dmValueEventListener != null) {
            dmRef.removeEventListener(dmValueEventListener);
        }
    }

}
