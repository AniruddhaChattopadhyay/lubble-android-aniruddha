package in.lubble.app.profile;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
import com.crashlytics.android.Crashlytics;
import com.freshchat.consumer.sdk.Freshchat;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;



import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import in.lubble.app.BuildConfig;
import in.lubble.app.GlideApp;
import in.lubble.app.GlideRequests;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.chat.ChatActivity;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.GroupData;
import in.lubble.app.models.ProfileData;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import in.lubble.app.referrals.ReferralActivity;
import in.lubble.app.utils.*;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static in.lubble.app.firebase.RealtimeDbHelper.getUserRef;
import static in.lubble.app.utils.ReferralUtils.generateBranchUrl;
import static in.lubble.app.utils.ReferralUtils.getReferralIntent;
import static in.lubble.app.utils.StringUtils.isValidString;

public class ProfileFrag extends Fragment implements AuthenticationListener{
    private static final String TAG = "ProfileFrag";
    private static final String ARG_USER_ID = "arg_user_id";

    private String insta_handle_cloud;
    private InstagramLoginState instagramLoginState;

    private View rootView;
    private String userId;
    private ImageView profilePicIv;
    private TextView userName;
    private TextView badgeTv;
    private TextView lubbleTv;
    private TextView userBio;
    private TextView editProfileTV;
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
    private ValueEventListener valueEventListener;
    @Nullable
    private ProfileData profileData;
    private String sharingUrl;
    private ProgressDialog sharingProgressDialog;
    private GroupsAdapter groupsAdapter;
    private ConstraintLayout statsContainer;

    private String token = null;
    private AppPreferences appPreferences = null;
    private AuthenticationDialog authenticationDialog = null;
    private Button instaBtn = null;

    ImageView genderIv;
    TextView genderTv;
    ImageView businessIv;
    TextView businessTv;
    ImageView educationIv;
    TextView educationTv;
    private boolean insta_link_status;
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
        instaBtn = rootView.findViewById(R.id.insta_btn_login);
        Log.d("database_uid",RealtimeDbHelper.getThisUserRef().toString());//whole link to the user we are logged in as
        Log.d("uid",userId);//user we are currently on the profile of
        Log.d("uid_this",FirebaseAuth.getInstance().getUid());//the user we are logged in as

        //if owner access his profile then the below code is executed
        if (userId.equalsIgnoreCase(FirebaseAuth.getInstance().getUid()))
        {
            instaBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    if(token!=null)
                    {
                        //insta_logout();
                        //if the owner has already linked insta then on click of the instaBtn BottomSheet will be called
                        CreateBottomSheetDialog();
                    }
                    else {
                        //if the owner has not linked insta then this part of the code is executed
                        authenticationDialog = new AuthenticationDialog(getContext(), ProfileFrag.this);
                        authenticationDialog.setCancelable(true);
                        authenticationDialog.show();
                    }
                }
            });
            appPreferences = new AppPreferences(getContext());
            token = appPreferences.getString(AppPreferences.TOKEN);
            if (token != null) {
                getUserInfoByAccessToken(token);
            }
        }
        //if a person open another persons profile page then below code is executed
        else
        {
            //placing the insta button correctly
            float dip_left = 280f;
            float dip_right = 14f;
            Resources r = getResources();
            float px_left = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    dip_left,
                    r.getDisplayMetrics()
            );
            float px_right = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    dip_right,
                    r.getDisplayMetrics()
            );
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) instaBtn.getLayoutParams();
            params.leftMargin = (int)px_left;
            params.rightMargin = (int) px_right;
            instaBtn.setLayoutParams(params);
            RealtimeDbHelper.getUserRef(userId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //checking whether firebase contains intagram info of this user
                    if (dataSnapshot.hasChild("instagram") ) {
                        DatabaseReference insta_linked = RealtimeDbHelper.getUserRef(userId).child("instagram").child("insta_linked");

                        insta_linked.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                insta_link_status = dataSnapshot.getValue(boolean.class);
                                //checking whether the person wants other to see his insta or not if yes then below code is executed setting up the instaBtn view
                                if(insta_link_status ) {
                                    DatabaseReference insta_handle_ref = RealtimeDbHelper.getUserRef(userId).child("instagram").child("insta_handle");

                                    insta_handle_ref.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            insta_handle_cloud = dataSnapshot.getValue(String.class);
                                            instaBtn.setText(insta_handle_cloud);
                                            instaBtn.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Uri uri = Uri.parse("http://instagram.com/_u/"+insta_handle_cloud);
                                                    Intent i= new Intent(Intent.ACTION_VIEW,uri);

                                                    i.setPackage("com.instagram.android");

                                                    try {
                                                        startActivity(i);
                                                    } catch (ActivityNotFoundException e) {

                                                        startActivity(new Intent(Intent.ACTION_VIEW,
                                                                Uri.parse("http://instagram.com/xxx")));
                                                    }
                                                }
                                            });
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                                else{
                                    instaBtn.setVisibility(View.INVISIBLE);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                    //if the person doesn't want others to see his instagram then instaBtn is set to invisible
                    else
                    {
                        instaBtn.setVisibility(View.INVISIBLE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }






        //*************************************************

        sharingProgressDialog = new ProgressDialog(getContext());
        generateBranchUrl(getContext(), linkCreateListener);



        if (userId.equalsIgnoreCase(FirebaseAuth.getInstance().getUid())) {
            coinsContainer.setVisibility(View.VISIBLE);
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
        syncGroups();
        fetchStats();

        coinsContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReferralActivity.open(requireContext());
            }
        });

        return rootView;
    }

    private void CreateBottomSheetDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
        View sheetView = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_view, null);
        LinearLayout goto_insta = sheetView.findViewById(R.id.goto_insta);;
        LinearLayout unlink_insta = sheetView.findViewById(R.id.unlink_insta);
        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();
        goto_insta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                {
                        Uri uri = Uri.parse("http://instagram.com/_u/"+insta_handle_cloud);
                        Intent i= new Intent(Intent.ACTION_VIEW,uri);

                        i.setPackage("com.instagram.android");

                        try {
                            startActivity(i);
                        } catch (ActivityNotFoundException e) {

                            startActivity(new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("http://instagram.com/xxx")));
                        }
                        bottomSheetDialog.dismiss();
                }
            }
        });

        unlink_insta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insta_logout();
                bottomSheetDialog.dismiss();
            }
        });
    }
    @Override
    public void onStart() {
        super.onStart();
        userRef = getUserRef(userId);
        fetchProfileFeed();
    }

    private void syncGroups() {
        RealtimeDbHelper.getLubbleGroupsRef().orderByChild("lastMessageTimestamp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final ArrayList<GroupData> groupDataList = new ArrayList<>();

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    final GroupData groupData = child.getValue(GroupData.class);
                    if (groupData.getMembers().containsKey(userId)) {
                        groupDataList.add(groupData);
                    }
                }
                Collections.reverse(groupDataList);
                groupsAdapter.addGroupList(groupDataList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
                Crashlytics.logException(new IllegalStateException(error.getMessage()));
                if (isAdded() && isVisible()) {
                    Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    };



    private class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.ViewHolder> {

        private ArrayList<GroupData> groupList = new ArrayList<>();
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
            final GroupData groupData = groupList.get(position);
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

        void addGroupList(ArrayList<GroupData> groupDataList) {
            groupList.addAll(groupDataList);
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
                        final GroupData groupData = groupList.get(getAdapterPosition());
                        ChatActivity.openForGroup(getContext(), groupData.getId(), false, null);
                    }
                });
            }
        }

    }

    private void fetchProfileFeed() {
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                profileData = dataSnapshot.getValue(ProfileData.class);
                if (profileData != null) {
                    userName.setText(profileData.getInfo().getName());
                    if (!TextUtils.isEmpty(profileData.getInfo().getBadge())) {
                        badgeTv.setVisibility(View.VISIBLE);
                        badgeTv.setText(profileData.getInfo().getBadge());
                    } else {
                        badgeTv.setVisibility(View.GONE);
                    }
                    RealtimeDbHelper.getLubbleRef().addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            lubbleTv.setText(dataSnapshot.child("title").getValue(String.class));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    if (isValidString(profileData.getBio())) {
                        userBio.setText(profileData.getBio());
                    } else if (userId.equalsIgnoreCase(FirebaseAuth.getInstance().getUid())) {
                        userBio.setText(R.string.edit_profile_to_add_bio);
                    } else {
                        userBio.setText(R.string.no_bio_text);
                    }
                    populateProfileDetails();
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
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        userRef.addValueEventListener(valueEventListener);
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
                    Crashlytics.log("referral leaderboard bad response");
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
        userRef.removeEventListener(valueEventListener);
    }

    //***********************************************************
    //insta integration code
    public void insta_login() {
//        Log.d("instalogin","************************************here at instalogin**********************");
//        RealtimeDbHelper.getThisUserRef().addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//                for (DataSnapshot snap: dataSnapshot.getChildren()) {
//                    if(snap.getKey().equals("instagram")) {
//                        Log.d("instalogin","found instagram");
//                        snap.getRef().removeValue();
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//            }
//        });
        RealtimeDbHelper.getThisUserRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Checking if our database already has the instagram information if not then in the else part we send the info obtained from the token from instagram
                if (dataSnapshot.hasChild("instagram")) {
                    try {
                        RealtimeDbHelper.getThisUserRef().child("instagram").child("insta_linked").setValue("true");
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    DatabaseReference insta_handle_ref = RealtimeDbHelper.getThisUserRef().child("instagram").child("insta_handle");
                    insta_handle_ref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            insta_handle_cloud = dataSnapshot.getValue(String.class);
                            instaBtn.setText(insta_handle_cloud);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
            }

                else
                {
                    Map<String, Object> instagram_update = new HashMap<>();
                    instagram_update.put("instagram", instagramLoginState);
                    RealtimeDbHelper.getThisUserRef().updateChildren(instagram_update);
                    instaBtn.setText(instagramLoginState.insta_handle);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void insta_logout() {
        instaBtn.setText("INSTAGRAM");

        token = null;
        appPreferences.clear();
        try {
            RealtimeDbHelper.getThisUserRef().child("instagram").child("insta_linked").setValue("false");
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onTokenReceived(String auth_token) {
        if (auth_token == null)
            return;
        appPreferences.putString(AppPreferences.TOKEN, auth_token);
        token = auth_token;
        getUserInfoByAccessToken(token);
    }

    private void getUserInfoByAccessToken(String token) {
        new RequestInstagramAPI().execute();
    }


    private class RequestInstagramAPI extends AsyncTask<Void, String, String> {

        @Override
        protected String doInBackground(Void... params) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(getResources().getString(R.string.get_user_info_url) + token);
            try {
                HttpResponse response = httpClient.execute(httpGet);
                HttpEntity httpEntity = response.getEntity();
                return EntityUtils.toString(httpEntity);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if (response != null) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Log.e("response", jsonObject.toString());
                    JSONObject jsonData = jsonObject.getJSONObject("data");
                    if (jsonData.has("id")) {
                        //сохранение данных пользователя
                        appPreferences.putString(AppPreferences.USER_ID, jsonData.getString("id"));
                        appPreferences.putString(AppPreferences.USER_NAME, jsonData.getString("username"));
                        appPreferences.putString(AppPreferences.PROFILE_PIC, jsonData.getString("profile_picture"));
                        instagramLoginState = new InstagramLoginState(jsonData.getString("username"),true);
                        Log.d("inside post req",jsonData.getString("username"));
                        //instagramLoginState = new InstagramLoginState(jsonData.getString("username"),jsonData.getString("profile_picture"));
                        insta_login();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                Toast toast = Toast.makeText(getContext(),"Instagram Linking failed!, please try again",Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }

}
