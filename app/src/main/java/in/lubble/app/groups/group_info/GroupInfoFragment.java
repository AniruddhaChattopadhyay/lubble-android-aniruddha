package in.lubble.app.groups.group_info;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.MainActivity;
import in.lubble.app.R;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.GroupData;
import in.lubble.app.notifications.MutedChatsSharedPrefs;
import in.lubble.app.user_search.UserSearchActivity;
import in.lubble.app.utils.FullScreenImageActivity;

import static in.lubble.app.firebase.RealtimeDbHelper.getLubbleGroupsRef;
import static in.lubble.app.utils.UiUtils.dpToPx;
import static in.lubble.app.utils.UserUtils.getLubbleId;

public class GroupInfoFragment extends Fragment {
    private static final String ARG_GROUP_ID = "GroupInfoFragment_GroupId";
    private String groupId;
    private ProgressBar dpProgressBar;
    private ImageView groupIv;
    private TextView titleTv;
    private TextView descTv;
    private ImageView privacyIcon;
    private TextView privacyTv;
    private LinearLayout inviteMembersContainer;
    private RecyclerView recyclerView;
    private TextView leaveGroupTV;
    private RelativeLayout muteNotifsContainer;
    private SwitchCompat muteSwitch;
    private GroupMembersAdapter adapter;

    public GroupInfoFragment() {
        // Required empty public constructor
    }

    public static GroupInfoFragment newInstance(String groupId) {
        GroupInfoFragment fragment = new GroupInfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_GROUP_ID, groupId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            groupId = getArguments().getString(ARG_GROUP_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_group_info, container, false);

        dpProgressBar = view.findViewById(R.id.progressBar_groupInfo);
        groupIv = view.findViewById(R.id.iv_group_image);
        titleTv = view.findViewById(R.id.tv_group_title);
        descTv = view.findViewById(R.id.tv_group_desc);
        privacyIcon = view.findViewById(R.id.iv_privacy_icon);
        privacyTv = view.findViewById(R.id.tv_privacy);
        inviteMembersContainer = view.findViewById(R.id.linearLayout_invite_container);
        recyclerView = view.findViewById(R.id.rv_group_members);
        leaveGroupTV = view.findViewById(R.id.tv_leave_group);
        muteNotifsContainer = view.findViewById(R.id.mute_container);
        muteSwitch = view.findViewById(R.id.switch_mute);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new GroupMembersAdapter();
        recyclerView.setAdapter(adapter);
        groupIv.setOnClickListener(null);

        leaveGroupTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmationDialog();
            }
        });

        boolean isMuted = MutedChatsSharedPrefs.getInstance().getPreferences().getBoolean(groupId, false);
        muteSwitch.setChecked(isMuted);

        muteSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleMuteNotifs();
            }
        });
        muteNotifsContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleMuteNotifs();
            }
        });

        LubbleSharedPrefs.getInstance().setIsGroupInfoOpened(true);

        return view;
    }

    private void toggleMuteNotifs() {
        boolean isMuted = MutedChatsSharedPrefs.getInstance().getPreferences().getBoolean(groupId, false);
        if (isMuted) {
            MutedChatsSharedPrefs.getInstance().getPreferences().edit().remove(groupId).apply();
            muteSwitch.setChecked(false);
            Toast.makeText(getContext(), "UN-MUTED", Toast.LENGTH_SHORT).show();
        } else {
            MutedChatsSharedPrefs.getInstance().getPreferences().edit().putBoolean(groupId, true).apply();
            muteSwitch.setChecked(true);
            Toast.makeText(getContext(), "MUTED", Toast.LENGTH_SHORT).show();
        }
    }

    private void showConfirmationDialog() {
        final AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
        alertDialog.setTitle("Are you sure?");
        alertDialog.setMessage("You will no longer be a part of this group");
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Leave Group", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                leaveGroup();
            }
        });
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private void leaveGroup() {
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Leaving Group");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(RealtimeDbHelper.getUserGroupPath() + "/" + groupId, null);
        childUpdates.put(
                RealtimeDbHelper.getLubbleGroupPath() + "/" + groupId + "/members/" + FirebaseAuth.getInstance().getUid(),
                null
        );

        FirebaseDatabase.getInstance().getReference().updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (isAdded()) {
                    progressDialog.dismiss();
                    final Intent intent = new Intent(getContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    getActivity().finishAffinity();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        syncGroupInfo();
    }

    private void syncGroupInfo() {
        getLubbleGroupsRef().child(groupId)
                .addValueEventListener(groupInfoEventListener);
    }

    final ValueEventListener groupInfoEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            final GroupData groupData = dataSnapshot.getValue(GroupData.class);

            titleTv.setText(groupData.getTitle());
            descTv.setText(groupData.getDescription());
            GlideApp.with(getContext())
                    .load(groupData.getProfilePic())
                    .placeholder(R.drawable.circle)
                    .error(R.drawable.ic_group_24dp)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            groupIv.setPadding(dpToPx(56), dpToPx(56), dpToPx(56), dpToPx(56));
                            dpProgressBar.setVisibility(View.GONE);
                            groupIv.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    openDpInFullScreen(groupData);
                                }
                            });
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            dpProgressBar.setVisibility(View.GONE);
                            groupIv.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    openDpInFullScreen(groupData);
                                }
                            });
                            return false;
                        }
                    })
                    .into(groupIv);

            List<Map.Entry> memberEntryList = new ArrayList<Map.Entry>(groupData.getMembers().entrySet());
            adapter.clear();
            adapter.addAllMembers(memberEntryList);

            toggleLeaveGroupVisibility(groupData);
            toggleMemberElements(groupData.isJoined());

            privacyIcon.setImageResource(groupData.getIsPrivate() ? R.drawable.ic_lock_black_24dp : R.drawable.ic_public_black_24dp);
            privacyTv.setText(groupData.getIsPrivate() ? "Private Group" : "Public Group");
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private void openDpInFullScreen(GroupData groupData) {
        FullScreenImageActivity.open(getActivity(),
                getContext(),
                groupData.getProfilePic(),
                groupIv,
                "lubbles/" + getLubbleId() + "/groups/" + groupId,
                R.drawable.ic_circle_group_24dp);
    }

    private void toggleMemberElements(boolean isJoined) {
        muteNotifsContainer.setVisibility(isJoined ? View.VISIBLE : View.GONE);
        if (isJoined) {
            inviteMembersContainer.setAlpha(1f);
            inviteMembersContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UserSearchActivity.newInstance(getContext(), groupId);
                }
            });
        } else {
            inviteMembersContainer.setAlpha(0.5f);
            inviteMembersContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(), "Join the group to invite people", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void toggleLeaveGroupVisibility(final GroupData groupData) {
        final String defaultGroupId = LubbleSharedPrefs.getInstance().getDefaultGroupId();
        if (groupId.equalsIgnoreCase(defaultGroupId)) {
            leaveGroupTV.setVisibility(View.GONE);
        } else {
            if (groupData != null && groupData.isJoined()) {
                leaveGroupTV.setVisibility(View.VISIBLE);
            } else {
                leaveGroupTV.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getLubbleGroupsRef().child(groupId).removeEventListener(groupInfoEventListener);
    }
}
