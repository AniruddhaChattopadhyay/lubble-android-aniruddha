package in.lubble.app.chat;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import in.lubble.app.R;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.GroupData;
import in.lubble.app.models.ProfileInfo;
import in.lubble.app.models.UserGroupData;

import static in.lubble.app.firebase.RealtimeDbHelper.getUserGroupsRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getUserInfoRef;

public class BlockedChatsFrag extends Fragment implements OnBlockedChatClickListener {

    private OnBlockedChatClickListener mListener;
    private Query query;
    private RecyclerView recyclerView;
    private BlockedChatsAdapter adapter;
    private ImageView noBlocksIv;
    private TextView noBlocksTv;

    public BlockedChatsFrag() {
    }

    public static BlockedChatsFrag newInstance() {
        BlockedChatsFrag fragment = new BlockedChatsFrag();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_blocked_chat, container, false);

        // Set the adapter
        Context context = view.getContext();
        recyclerView = view.findViewById(R.id.rv_blocked_chats);
        noBlocksIv = view.findViewById(R.id.iv_no_block);
        noBlocksTv = view.findViewById(R.id.tv_no_block);

        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        adapter = new BlockedChatsAdapter(mListener);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchBlockedGroups();
    }

    private void fetchBlockedGroups() {
        query = RealtimeDbHelper.getDmsRef().orderByChild("lastMessageTimestamp");
        query.addChildEventListener(dmListener);
        getUserGroupsRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userGroupDataSnapshot : dataSnapshot.getChildren()) {
                    adapter.updateUserGroupData(userGroupDataSnapshot.getKey(), userGroupDataSnapshot.getValue(UserGroupData.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // all groups have been fetched by now
                if (adapter.getItemCount() == 0) {
                    noBlocksIv.setVisibility(View.VISIBLE);
                    noBlocksTv.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    noBlocksIv.setVisibility(View.GONE);
                    noBlocksTv.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private final ChildEventListener dmListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            final GroupData groupData = dataSnapshot.getValue(GroupData.class);
            if (groupData != null) {
                if (groupData.getMembers().containsKey(FirebaseAuth.getInstance().getUid())
                        && ((HashMap) groupData.getMembers().get(FirebaseAuth.getInstance().getUid())).get("blocked_status") != null) {
                    // blocked chat
                    groupData.setId(dataSnapshot.getKey());
                    groupData.setIsDm(true);
                    groupData.setIsPrivate(true);
                    if (TextUtils.isEmpty(groupData.getTitle())) {
                        for (String key : groupData.getMembers().keySet()) {
                            if (!key.equalsIgnoreCase(FirebaseAuth.getInstance().getUid())) {
                                getUserInfoRef(key).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        final ProfileInfo profileInfo = dataSnapshot.getValue(ProfileInfo.class);
                                        groupData.setTitle(profileInfo.getName());
                                        groupData.setThumbnail(profileInfo.getThumbnail());
                                        groupData.setIsDm(true);
                                        groupData.setIsPrivate(true);
                                        adapter.updateGroup(groupData);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        }
                    }
                    adapter.addToTop(groupData);
                }
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            final GroupData groupData = dataSnapshot.getValue(GroupData.class);
            if (groupData != null && groupData.isJoined()
                    && groupData.getMembers().get("blocked_status") == null) {
                groupData.setId(dataSnapshot.getKey());
                groupData.setIsDm(true);
                if (TextUtils.isEmpty(groupData.getTitle())) {
                    for (String key : groupData.getMembers().keySet()) {
                        if (!key.equalsIgnoreCase(FirebaseAuth.getInstance().getUid())) {
                            getUserInfoRef(key).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    final ProfileInfo profileInfo = dataSnapshot.getValue(ProfileInfo.class);
                                    groupData.setTitle(profileInfo.getName());
                                    groupData.setThumbnail(profileInfo.getThumbnail());
                                    groupData.setIsDm(true);
                                    groupData.setIsPrivate(true);
                                    adapter.updateGroup(groupData);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                }
                adapter.updateGroup(groupData);
            }
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            adapter.removeGroup(dataSnapshot.getKey());
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            final GroupData groupData = dataSnapshot.getValue(GroupData.class);
            if (groupData != null && groupData.isJoined() && groupData.getMembers().get("blocked_status") != null) {
                groupData.setId(dataSnapshot.getKey());
                groupData.setIsPrivate(true);
                groupData.setIsDm(true);
                adapter.updateGroupPos(groupData);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = this;
    }

    @Override
    public void onBlockedChatClicked(String dmId, String title, String thumbnail) {
        ChatActivity.openForDm(requireContext(), dmId, null, "");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}
