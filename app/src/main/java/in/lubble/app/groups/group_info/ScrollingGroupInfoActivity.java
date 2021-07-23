package in.lubble.app.groups.group_info;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.lubble.app.BaseActivity;
import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.MainActivity;
import in.lubble.app.R;
import in.lubble.app.chat.ShareActiv;
import in.lubble.app.chat.SnoozeGroupBottomSheet;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.GroupData;
import in.lubble.app.models.ProfileData;
import in.lubble.app.notifications.SnoozedGroupsSharedPrefs;
import in.lubble.app.user_search.UserSearchActivity;
import in.lubble.app.utils.CompleteListener;
import in.lubble.app.utils.FullScreenImageActivity;
import in.lubble.app.utils.NotifUtils;

import static in.lubble.app.firebase.RealtimeDbHelper.getLubbleGroupsRef;
import static in.lubble.app.utils.UiUtils.dpToPx;

public class ScrollingGroupInfoActivity extends BaseActivity {

    private static final String EXTRA_GROUP_ID = "GroupInfoActivity_GroupId";

    private String groupId;
    private ProgressBar dpProgressBar;
    private ProgressBar groupMembersProgressBar;
    private ImageView groupIv;
    private TextView descTv;
    private ImageView privacyIcon;
    private TextView privacyTv;
    private LinearLayout inviteMembersContainer;
    private LinearLayout shareGroupContainer;
    private RecyclerView recyclerView;
    private TextView leaveGroupTV, snoozeNotifsTv;
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
        groupMembersProgressBar = findViewById(R.id.prgressbar_group_members);
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
        snoozeNotifsTv = findViewById(R.id.tv_snooze_notifs);

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

        muteSwitch.setChecked(NotifUtils.isGroupSnoozed(groupId));

        muteSwitch.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    toggleMuteNotifs();
                }
                return true;
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
        if (NotifUtils.isGroupSnoozed(groupId)) {
            SnoozedGroupsSharedPrefs.getInstance().getPreferences().edit().remove(groupId).apply();
            muteSwitch.setChecked(false);
            Toast.makeText(this, R.string.unmuted, Toast.LENGTH_SHORT).show();
        } else {
            SnoozeGroupBottomSheet.newInstance(groupId, "group_info", new CompleteListener() {
                @Override
                public void onComplete(boolean isSuccess) {
                    if (!isFinishing()) {
                        muteSwitch.setChecked(isSuccess);
                    }
                }
            }).show(getSupportFragmentManager(), null);
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

            List<Map.Entry> memberEntryList = new ArrayList<>(groupData.getMembers().entrySet());
            adapter.clear();
            fetchAllGroupUsers();
            for (Map.Entry entry : memberEntryList) {
                final HashMap map = (HashMap) entry.getValue();
                if (map.get("admin") == Boolean.TRUE) {
                    adapter.addAdminId((String) entry.getKey());
                }
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

    private void fetchAllGroupUsers() {
        groupMembersProgressBar.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference("users").orderByChild("lubbles/" + LubbleSharedPrefs.getInstance().requireLubbleId()
                + "/groups/" + groupId + "/joined").equalTo(true)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!isFinishing()) {
                            adapter.clear();
                            groupMembersProgressBar.setVisibility(View.GONE);
                            for (DataSnapshot child : dataSnapshot.getChildren()) {
                                if (!(child.getValue() instanceof Boolean)) {
                                    final ProfileData profileData = child.getValue(ProfileData.class);
                                    if (profileData != null && profileData.getInfo() != null && !profileData.getIsDeleted()) {
                                        profileData.setId(child.getKey());
                                        profileData.getInfo().setId(child.getKey());
                                        adapter.addProfile(profileData.getInfo());
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

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
