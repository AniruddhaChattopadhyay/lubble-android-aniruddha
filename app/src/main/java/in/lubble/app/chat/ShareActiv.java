package in.lubble.app.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import in.lubble.app.GlideApp;
import in.lubble.app.GlideRequests;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static in.lubble.app.firebase.RealtimeDbHelper.*;

public class ShareActiv extends AppCompatActivity {

    private static final String TAG = "ShareActiv";
    private static final String ARG_GROUP_ID = "ARG_GROUP_ID";

    private RecyclerView recyclerView;
    private Query query;
    private ValueEventListener valueEventListener;
    private ChatsAdapter chatsAdapter;
    private String groupIdToShare;

    public static void open(Context context, String groupIdToShare) {
        final Intent intent = new Intent(context, ShareActiv.class);
        intent.putExtra(ARG_GROUP_ID, groupIdToShare);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        recyclerView = findViewById(R.id.rv_share_chats);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatsAdapter = new ChatsAdapter(GlideApp.with(this));
        recyclerView.setAdapter(chatsAdapter);
        DividerItemDecoration itemDecor = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(itemDecor);

        groupIdToShare = getIntent().getStringExtra(ARG_GROUP_ID);

        syncGroups();
        syncUserDmIds();
        syncSellerDmIds();
    }

    private void syncGroups() {
        query = RealtimeDbHelper.getLubbleGroupsRef().orderByChild("lastMessageTimestamp");
        valueEventListener = new ValueEventListener() {
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
                chatsAdapter.addGroupList(groupDataList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        query.addListenerForSingleValueEvent(valueEventListener);
    }

    private void syncSellerDmIds() {
        getSellerRef().child(String.valueOf(LubbleSharedPrefs.getInstance().getSellerId())).child("dms").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    final String dmId = snapshot.getKey();
                    if (dmId != null) {
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
        getUserDmsRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    final String dmId = snapshot.getKey();
                    if (dmId != null) {
                        HashMap<String, Object> map = (HashMap<String, Object>) snapshot.getValue();
                        Long count = 0L;
                        if (map != null && map.containsKey("unreadCount")) {
                            count = (Long) map.get("unreadCount");
                        }
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
        getDmsRef().child(dmId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final DmData dmData = dataSnapshot.getValue(DmData.class);
                if (dmData != null) {
                    dmData.setId(dataSnapshot.getKey());

                    final GroupData dmGroupData = new GroupData();
                    dmGroupData.setId(dmData.getId());
                    dmGroupData.setLastMessage(dmData.getLastMessage());
                    dmGroupData.setLastMessageTimestamp(dmData.getLastMessageTimestamp());
                    dmGroupData.setIsPrivate(true);

                    final HashMap<String, Object> members = dmData.getMembers();
                    for (String profileId : members.keySet()) {
                        final String sellerId = String.valueOf(LubbleSharedPrefs.getInstance().getSellerId());
                        if (!FirebaseAuth.getInstance().getUid().equalsIgnoreCase(profileId) && !sellerId.equalsIgnoreCase(profileId)) {
                            final HashMap<String, Object> profileMap = (HashMap<String, Object>) members.get(profileId);
                            if (profileMap != null) {
                                final boolean isSeller = (boolean) profileMap.get("isSeller");
                                if (isSeller) {
                                    fetchSellerProfileFrom(profileId, dmGroupData);
                                } else {
                                    fetchProfileFrom(profileId, dmGroupData);
                                }
                            }
                        }
                    }
                }
            }

            private void fetchProfileFrom(String profileId, final GroupData dmGroupData) {
                getUserInfoRef(profileId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        HashMap<String, String> map = (HashMap<String, String>) dataSnapshot.getValue();
                        if (map != null) {
                            final ProfileInfo profileInfo = dataSnapshot.getValue(ProfileInfo.class);
                            if (profileInfo != null) {
                                profileInfo.setId(dataSnapshot.getRef().getParent().getKey()); // this works. Don't touch.
                                dmGroupData.setTitle(profileInfo.getName());
                                dmGroupData.setThumbnail(profileInfo.getThumbnail());
                                final UserGroupData userGroupData = new UserGroupData();
                                userGroupData.setJoined(true);
                                chatsAdapter.addGroup(dmGroupData, userGroupData);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            private synchronized void fetchSellerProfileFrom(String profileId, final GroupData dmGroupData) {
                getSellerRef().child(profileId).child("info").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final ProfileInfo profileInfo = dataSnapshot.getValue(ProfileInfo.class);
                        if (profileInfo != null) {
                            profileInfo.setId(dataSnapshot.getKey());
                            dmGroupData.setTitle(profileInfo.getName());
                            dmGroupData.setThumbnail(profileInfo.getThumbnail());
                            final UserGroupData userGroupData = new UserGroupData();
                            userGroupData.setJoined(true);
                            chatsAdapter.addGroup(dmGroupData, userGroupData);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder> {

        private ArrayList<GroupData> groupList = new ArrayList<>();
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
            final GroupData groupData = groupList.get(position);
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

        void addGroup(GroupData newGroupData, UserGroupData userGroupData) {
            if (!userGroupDataMap.containsKey(newGroupData.getId())) {
                // if not already present
                for (int i = 0; i < groupList.size(); i++) {
                    final GroupData groupData = groupList.get(i);
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

        void addGroupList(ArrayList<GroupData> groupDataList) {
            groupList.addAll(groupDataList);
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
                        final ChatData chatData = new ChatData();
                        if (groupIdToShare != null) {
                            chatData.setType(ChatData.GROUP);
                            chatData.setAttachedGroupId(groupIdToShare);
                        }
                        final GroupData groupData = groupList.get(getAdapterPosition());
                        if (groupData.isDm()) {
                            ChatActivity.openForDm(ShareActiv.this, groupData.getId(), null, null, chatData);
                        } else {
                            ChatActivity.openForGroup(ShareActiv.this, groupData.getId(), false, null, chatData);
                        }
                        finish();
                    }
                });
            }
        }

    }

}
