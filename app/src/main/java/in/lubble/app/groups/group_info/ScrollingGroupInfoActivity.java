package in.lubble.app.groups.group_info;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import in.lubble.app.*;
import in.lubble.app.R;
import in.lubble.app.chat.ShareActiv;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.GroupData;
import in.lubble.app.models.ProfileInfo;
import in.lubble.app.notifications.MutedChatsSharedPrefs;
import in.lubble.app.user_search.UserSearchActivity;
import in.lubble.app.utils.FullScreenImageActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static in.lubble.app.firebase.RealtimeDbHelper.getLubbleGroupsRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getUserInfoRef;
import static in.lubble.app.utils.UiUtils.dpToPx;

public class ScrollingGroupInfoActivity extends BaseActivity {

    private static final String EXTRA_GROUP_ID = "GroupInfoActivity_GroupId";

    private String groupId;
    private ProgressBar dpProgressBar;
    private ImageView groupIv;
    private TextView descTv;
    private ImageView privacyIcon;
    private TextView privacyTv;
    private LinearLayout inviteMembersContainer;
    private LinearLayout shareGroupContainer;
    private RecyclerView recyclerView;
    private TextView leaveGroupTV;
    private RelativeLayout muteNotifsContainer;
    private SwitchCompat muteSwitch;
    private GroupMembersAdapter adapter;

    public static void open(Context context, String groupId) {
        final Intent intent = new Intent(context, ScrollingGroupInfoActivity.class);
        intent.putExtra(EXTRA_GROUP_ID, groupId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling_group_info);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("");

        groupId = getIntent().getStringExtra(EXTRA_GROUP_ID);

        dpProgressBar = findViewById(R.id.progressBar_groupInfo);
        groupIv = findViewById(R.id.iv_group_image);
        descTv = findViewById(R.id.tv_group_desc);
        privacyIcon = findViewById(R.id.iv_privacy_icon);
        privacyTv = findViewById(R.id.tv_privacy);
        inviteMembersContainer = findViewById(R.id.linearLayout_invite_container);
        shareGroupContainer = findViewById(R.id.linearLayout_share_group);
        recyclerView = findViewById(R.id.rv_group_members);
        leaveGroupTV = findViewById(R.id.tv_leave_group);
        muteNotifsContainer = findViewById(R.id.mute_container);
        muteSwitch = findViewById(R.id.switch_mute);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GroupMembersAdapter(GlideApp.with(this));
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
    }

    private void toggleMuteNotifs() {
        boolean isMuted = MutedChatsSharedPrefs.getInstance().getPreferences().getBoolean(groupId, false);
        if (isMuted) {
            MutedChatsSharedPrefs.getInstance().getPreferences().edit().remove(groupId).apply();
            muteSwitch.setChecked(false);
            Toast.makeText(this, R.string.unmuted, Toast.LENGTH_SHORT).show();
        } else {
            MutedChatsSharedPrefs.getInstance().getPreferences().edit().putBoolean(groupId, true).apply();
            muteSwitch.setChecked(true);
            Toast.makeText(this, R.string.muted, Toast.LENGTH_SHORT).show();
        }
    }

    private void showConfirmationDialog() {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getString(R.string.all_are_you_sure));
        alertDialog.setMessage(getString(R.string.leave_group_desc));
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.leave_group_title), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                leaveGroup();
            }
        });
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.all_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private void leaveGroup() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.leaving_group_title));
        progressDialog.setMessage(getString(R.string.all_please_wait));
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
                if (!isFinishing() && !isDestroyed()) {
                    progressDialog.dismiss();
                    final Intent intent = new Intent(ScrollingGroupInfoActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finishAffinity();
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
            setTitle(groupData.getTitle());
            descTv.setText(groupData.getDescription());
            GlideApp.with(ScrollingGroupInfoActivity.this)
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
            for (Map.Entry entry : memberEntryList) {
                final HashMap map = (HashMap) entry.getValue();
                if (map.get("admin") == Boolean.TRUE) {
                    adapter.addAdminId((String) entry.getKey());
                }
                fetchProfileInfo((String) entry.getKey());
            }

            toggleLeaveGroupVisibility(groupData);
            toggleMemberElements(groupData.isJoined());

            privacyIcon.setImageResource(groupData.getIsPrivate() ? R.drawable.ic_lock_black_24dp : R.drawable.ic_public_black_24dp);
            privacyTv.setText(groupData.getIsPrivate() ? getString(R.string.private_group) : getString(R.string.public_group));
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private void fetchProfileInfo(String uid) {
        getUserInfoRef(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final ProfileInfo profileInfo = dataSnapshot.getValue(ProfileInfo.class);
                if (profileInfo != null) {
                    profileInfo.setId(dataSnapshot.getRef().getParent().getKey()); // this works. Don't touch.
                    adapter.addProfile(profileInfo);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void openDpInFullScreen(GroupData groupData) {
        FullScreenImageActivity.open(this,
                this,
                groupData.getProfilePic(),
                groupIv,
                "lubbles/" + LubbleSharedPrefs.getInstance().requireLubbleId() + "/groups/" + groupId,
                R.drawable.ic_circle_group_24dp);
    }

    private void toggleMemberElements(boolean isJoined) {
        muteNotifsContainer.setVisibility(isJoined ? View.VISIBLE : View.GONE);
        if (isJoined) {
            inviteMembersContainer.setAlpha(1f);
            shareGroupContainer.setAlpha(1f);
            inviteMembersContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UserSearchActivity.newInstance(ScrollingGroupInfoActivity.this, groupId);
                }
            });
            shareGroupContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ShareActiv.open(ScrollingGroupInfoActivity.this, groupId, ShareActiv.ShareType.GROUP);
                }
            });
        } else {
            inviteMembersContainer.setAlpha(0.5f);
            shareGroupContainer.setAlpha(0.5f);
            inviteMembersContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(ScrollingGroupInfoActivity.this, R.string.joing_group_to_invite, Toast.LENGTH_SHORT).show();
                }
            });
            shareGroupContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(ScrollingGroupInfoActivity.this, R.string.joing_group_to_share, Toast.LENGTH_SHORT).show();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        getLubbleGroupsRef().child(groupId).removeEventListener(groupInfoEventListener);
    }

}
