package in.lubble.app.chat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import in.lubble.app.BaseActivity;
import in.lubble.app.GlideApp;
import in.lubble.app.GlideRequests;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.MainActivity;
import in.lubble.app.R;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.ChatData;
import in.lubble.app.models.DmData;
import in.lubble.app.models.GroupData;
import in.lubble.app.models.GroupInfoData;
import in.lubble.app.models.ProfileInfo;
import in.lubble.app.models.UserGroupData;
import in.lubble.app.utils.FileUtils;

import static in.lubble.app.firebase.RealtimeDbHelper.getDmsRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getLubbleGroupInfoRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getSellerRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getUserDmsRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getUserGroupsRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getUserInfoRef;
import static in.lubble.app.utils.FileUtils.getFileFromInputStreamUri;

public class ShareActiv extends BaseActivity {

    public enum ShareType {
        GROUP, EVENT
    }

    private static final String TAG = "ShareActiv";
    private static final String ARG_SHARE_ID = "ARG_SHARE_ID";
    private static final String ARG_TYPE = "ARG_TYPE";

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private Query query;
    private ValueEventListener valueEventListener;
    private ChatsAdapter chatsAdapter;
    private String shareId;
    private ShareType shareType;
    private final ChatData chatDataToSend = new ChatData();
    private Uri mediaUri;
    private int queryCounter = 0;
    private HashMap<String, UserGroupData> userGroupDataMap;

    public static void open(Context context, String groupIdToShare, ShareType shareType) {
        final Intent intent = new Intent(context, ShareActiv.class);
        intent.putExtra(ARG_SHARE_ID, groupIdToShare);
        intent.putExtra(ARG_TYPE, shareType);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        recyclerView = findViewById(R.id.rv_share_chats);
        progressBar = findViewById(R.id.progressbar);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatsAdapter = new ChatsAdapter(GlideApp.with(this));
        recyclerView.setAdapter(chatsAdapter);
        DividerItemDecoration itemDecor = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(itemDecor);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (FirebaseAuth.getInstance().getUid() == null) {
            // user is trying to share from gallery but isnt not logged in
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finishAffinity();
            return;
        }

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent);
            } else if (type.startsWith("image/")) {
                handleSendImage(intent);
            } else if (type.startsWith("video/")) {
                handleSendVideo(intent);
            }
            else if (type.startsWith("application/pdf")) {
                handleSendPdf(intent);
            }
        } else {
            // Handle other intents
            shareId = getIntent().getStringExtra(ARG_SHARE_ID);
            shareType = (ShareType) getIntent().getSerializableExtra(ARG_TYPE);
        }

        userGroupDataMap = new HashMap<>();
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        queryCounter = 0;
        syncUserGroupIds();
        syncUserDmIds();
        syncSellerDmIds();
    }

    private void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (!TextUtils.isEmpty(sharedText)) {
            chatDataToSend.setMessage(sharedText);
        }
    }

    private void handleSendImage(Intent intent) {
        Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            // make a local copy of the img becoz our file upload service will lose auth for this URI
            // since the service is not part of same context/process
            File localImgFile = getFileFromInputStreamUri(this, imageUri);
            this.mediaUri = Uri.fromFile(localImgFile);
            // look for any attached text caption
            String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (!TextUtils.isEmpty(sharedText)) {
                chatDataToSend.setMessage(sharedText);
            }
        }
    }

    private void handleSendVideo(Intent intent) {
        Uri videoUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);

        String extension = FileUtils.getFileExtension(this, videoUri);
        if (extension != null && extension.contains("mov")) {
            Toast.makeText(getApplicationContext(), "Unsupported File Type", Toast.LENGTH_LONG).show();
            finish();
        } else {
            File localVidFile = getFileFromInputStreamUri(this, videoUri);
            this.mediaUri = Uri.fromFile(localVidFile);
            // look for any attached text caption
            String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (!TextUtils.isEmpty(sharedText)) {
                chatDataToSend.setMessage(sharedText);
            }
        }
    }

    private void handleSendPdf(Intent intent) {
        Uri pdfUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (pdfUri != null) {
            // make a local copy of the img becoz our file upload service will lose auth for this URI
            // since the service is not part of same context/process
            File localImgFile = getFileFromInputStreamUri(this, pdfUri);
            this.mediaUri = Uri.fromFile(localImgFile);
        }
    }

    private void syncUserGroupIds() {
        // gets list of group IDs joined by the user
        getUserGroupsRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    final UserGroupData userGroupData = snapshot.getValue(UserGroupData.class);
                    if (userGroupData != null) {
                        userGroupDataMap.put(snapshot.getKey(), userGroupData);
                        if (userGroupData.isJoined()) {
                            queryCounter++;
                            syncJoinedGroups(snapshot.getKey());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                FirebaseCrashlytics.getInstance().recordException(error.toException());
            }
        });
    }


    private void syncJoinedGroups(String groupId) {
        // get meta data of the groups joined by the user
        getLubbleGroupInfoRef(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GroupInfoData groupData = dataSnapshot.getValue(GroupInfoData.class);
                String groupId = dataSnapshot.getRef().getParent().getKey();
                final UserGroupData userGroupData = userGroupDataMap.get(groupId);
                if (groupData != null && userGroupData != null) {
                    groupData.setId(groupId);
                    chatsAdapter.addGroup(groupData, userGroupData);
                }
                queryCounter--;
                checkAllChatsSynced();
            }

            @Override
            public void onCancelled(@NotNull DatabaseError error) {
                FirebaseCrashlytics.getInstance().recordException(error.toException());
            }
        });
    }

    private void syncSellerDmIds() {
        getSellerRef().child(String.valueOf(LubbleSharedPrefs.getInstance().getSellerId())).child("dms").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    final String dmId = snapshot.getKey();
                    if (dmId != null) {
                        queryCounter++;
                        fetchDmFrom(dmId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void syncUserDmIds() {
        getUserDmsRef(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    final String dmId = snapshot.getKey();
                    if (dmId != null) {
                        queryCounter++;
                        fetchDmFrom(dmId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fetchDmFrom(@NonNull final String dmId) {
        final String sellerIdStr = String.valueOf(LubbleSharedPrefs.getInstance().getSellerId());
        getDmsRef().child(dmId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final DmData dmData = dataSnapshot.getValue(DmData.class);
                if (dmData != null) {
                    dmData.setId(dataSnapshot.getKey());

                    final GroupInfoData dmGroupData = new GroupInfoData();
                    dmGroupData.setId(dmData.getId());
                    dmGroupData.setLastMessage(dmData.getLastMessage());
                    dmGroupData.setLastMessageTimestamp(dmData.getLastMessageTimestamp());
                    dmGroupData.setIsPrivate(true);
                    dmGroupData.setIsDm(true);

                    final UserGroupData userGroupData = new UserGroupData();
                    final HashMap<String, Object> members = dmData.getMembers();
                    for (String memberUid : members.keySet()) {
                        if (!memberUid.equalsIgnoreCase(FirebaseAuth.getInstance().getUid())
                                && !memberUid.equalsIgnoreCase(sellerIdStr)) {
                            // Other user
                            HashMap otherMemberMap = (HashMap) members.get(memberUid);
                            if (otherMemberMap != null) {
                                final String name = String.valueOf(otherMemberMap.get("name"));
                                final String dp = String.valueOf(otherMemberMap.get("profilePic"));
                                dmGroupData.setTitle(name);
                                dmGroupData.setThumbnail(dp);

                            }
                        } else {
                            // this user/seller
                            HashMap thisMemberMap = (HashMap) members.get(memberUid);
                            if (thisMemberMap.containsKey("joinedTimestamp") && (Long) thisMemberMap.get("joinedTimestamp") > 0) {
                                userGroupData.setJoined(true);
                            }
                        }
                        chatsAdapter.addGroup(dmGroupData, userGroupData);
                    }
                }
                queryCounter--;
                checkAllChatsSynced();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkAllChatsSynced() {
        if (queryCounter == 0) {
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder> {

        private ArrayList<GroupInfoData> groupList = new ArrayList<>();
        private GlideRequests glideApp;
        // <GroupID, UserGroupData>
        private final HashMap<String, UserGroupData> userGroupDataMap;

        ChatsAdapter(GlideRequests glideApp) {
            this.glideApp = glideApp;
            this.userGroupDataMap = new HashMap<>();
        }

        @Override
        public ChatsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ChatsAdapter.ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(ChatsAdapter.ViewHolder holder, int position) {
            final GroupInfoData groupData = groupList.get(position);
            glideApp.load(groupData.getThumbnail())
                    .placeholder(R.drawable.ic_circle_group_24dp)
                    .error(R.drawable.ic_circle_group_24dp)
                    .circleCrop()
                    .into(holder.groupDpIv);
            holder.groupNameTv.setText(groupData.getTitle());
            holder.groupDescTv.setText(groupData.getDescription());
            if (groupData.getIsPrivate()) {
                holder.lockIv.setVisibility(View.VISIBLE);
            } else {
                holder.lockIv.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return groupList.size();
        }

        void addGroup(GroupInfoData newGroupData, UserGroupData userGroupData) {
            if (!userGroupDataMap.containsKey(newGroupData.getId())) {
                // if not already present
                for (int i = 0; i < groupList.size(); i++) {
                    final GroupInfoData groupData = groupList.get(i);
                    if (newGroupData.getLastMessageTimestamp() >= groupData.getLastMessageTimestamp()) {
                        groupList.add(i, newGroupData);
                        userGroupDataMap.put(newGroupData.getId(), userGroupData);
                        break;
                    }
                }
                if (!userGroupDataMap.containsKey(newGroupData.getId())) {
                    // wasn't placed, add to last
                    groupList.add(newGroupData);
                    userGroupDataMap.put(newGroupData.getId(), userGroupData);
                }
                notifyDataSetChanged();
            }
        }

        void addGroupList(ArrayList<GroupInfoData> groupDataList) {
            groupList.addAll(groupDataList);
            notifyDataSetChanged();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            final ImageView groupDpIv;
            final ImageView lockIv;
            final TextView groupNameTv;
            final TextView groupDescTv;

            ViewHolder(LayoutInflater inflater, ViewGroup parent) {
                super(inflater.inflate(R.layout.item_chat_share, parent, false));
                groupDpIv = itemView.findViewById(R.id.iv_group_dp);
                lockIv = itemView.findViewById(R.id.iv_lock_icon);
                groupNameTv = itemView.findViewById(R.id.tv_group_name);
                groupDescTv = itemView.findViewById(R.id.tv_group_desc);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (shareType == ShareType.GROUP && shareId != null) {
                            chatDataToSend.setType(ChatData.GROUP);
                            chatDataToSend.setAttachedGroupId(shareId);
                        } else if (shareType == ShareType.EVENT && shareId != null) {
                            chatDataToSend.setType(ChatData.EVENT);
                            chatDataToSend.setAttachedGroupId(shareId);
                        }
                        final GroupInfoData groupData = groupList.get(getBindingAdapterPosition());
                        if (groupData.getIsDm()) {
                            ChatActivity.openForDm(ShareActiv.this, groupData.getId(), null, null, chatDataToSend, mediaUri);
                        } else {
                            ChatActivity.openForGroup(ShareActiv.this, groupData.getId(), false, null, chatDataToSend, mediaUri);
                        }
                        finish();
                    }
                });
            }
        }

    }

}
