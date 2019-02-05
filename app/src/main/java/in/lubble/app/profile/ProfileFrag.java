package in.lubble.app.profile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import in.lubble.app.*;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.chat.ChatActivity;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.GroupData;
import in.lubble.app.models.ProfileData;
import in.lubble.app.utils.*;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;

import java.util.ArrayList;
import java.util.Collections;

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
    private TextView locality;
    private TextView userBio;
    private TextView editProfileTV;
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
        locality = rootView.findViewById(R.id.tv_locality);
        userBio = rootView.findViewById(R.id.tv_bio);
        editProfileTV = rootView.findViewById(R.id.tv_editProfile);
        userGroupsRv = rootView.findViewById(R.id.rv_user_groups);
        referralCard = rootView.findViewById(R.id.card_referral);
        inviteBtn = rootView.findViewById(R.id.btn_invite);
        logoutTv = rootView.findViewById(R.id.tv_logout);
        progressBar = rootView.findViewById(R.id.progressBar_profile);
        TextView versionTv = rootView.findViewById(R.id.tv_version_name);
        RelativeLayout feedbackView = rootView.findViewById(R.id.feedback_container);

        sharingProgressDialog = new ProgressDialog(getContext());
        generateBranchUrl(getContext(), linkCreateListener);

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
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"ayush@mittalsoft.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "Lubble Feedback");
                intent.putExtra(Intent.EXTRA_TEXT, "Thanks for taking out time to write to us!\n\n" +
                        "We're here to listen & improve Lubble for you, please write your feedback below:\n\n\n\n");
                Intent mailer = Intent.createChooser(intent, "Choose an email app");
                startActivity(mailer);
            }
        });

        groupsAdapter = new GroupsAdapter(GlideApp.with(requireContext()));
        userGroupsRv.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        userGroupsRv.setAdapter(groupsAdapter);
        syncGroups();
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        userRef = getUserRef(userId);
        fetchProfileFeed();
        fetchDevMenu();
    }

    private void syncGroups() {
        RealtimeDbHelper.getLubbleGroupsRef().orderByChild("lastMessageTimestamp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final ArrayList<GroupData> groupDataList = new ArrayList<>();

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    final GroupData groupData = child.getValue(GroupData.class);
                    if (groupData.getMembers().containsKey(FirebaseAuth.getInstance().getUid())) {
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
                groupDpIv = itemView.findViewById(R.id.iv_group_pic);
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
                    if (!TextUtils.isEmpty(profileData.getLocality())) {
                        locality.setText(profileData.getLocality());
                        locality.setVisibility(View.VISIBLE);
                    } else {
                        locality.setVisibility(View.GONE);
                    }
                    if (isValidString(profileData.getBio())) {
                        userBio.setText(profileData.getBio());
                    } else if (userId.equalsIgnoreCase(FirebaseAuth.getInstance().getUid())) {
                        userBio.setText(R.string.edit_profile_to_add_bio);
                    } else {
                        userBio.setText(R.string.no_bio_text);
                    }
                    if (userId.equalsIgnoreCase(FirebaseAuth.getInstance().getUid())) {
                        editProfileTV.setVisibility(View.VISIBLE);
                        referralCard.setVisibility(View.VISIBLE);
                        logoutTv.setVisibility(View.VISIBLE);
                    } else {
                        editProfileTV.setVisibility(View.GONE);
                        referralCard.setVisibility(View.GONE);
                        logoutTv.setVisibility(View.GONE);
                    }
                    GlideApp.with(getContext())
                            .load(profileData.getProfilePic())
                            .error(R.drawable.ic_account_circle_black_no_padding)
                            .placeholder(R.drawable.circle)
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

    private void fetchDevMenu() {
        RealtimeDbHelper.getDevRef().child(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (isAdded() && isVisible()) {
                    if (dataSnapshot.getValue() != null && dataSnapshot.getValue(Boolean.class)) {
                        toggleDevMenu(true);
                    } else {
                        toggleDevMenu(false);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void toggleDevMenu(boolean isDev) {
        Spinner envoSpinner = rootView.findViewById(R.id.spinner_envo);
        envoSpinner.setVisibility(isDev ? View.VISIBLE : View.GONE);
        String[] envos = new String[3];
        envos[0] = "Select Environment...";
        envos[1] = "DEV";
        envos[2] = "STAGING";
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, envos);

        envoSpinner.setAdapter(adapter);
        envoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 1) {
                    LubbleSharedPrefs.getInstance().setLubbleId("DEV");
                    UserUtils.logout(getActivity());
                } else if (position == 2) {
                    LubbleSharedPrefs.getInstance().setLubbleId("STAGING");
                    UserUtils.logout(getActivity());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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

}
