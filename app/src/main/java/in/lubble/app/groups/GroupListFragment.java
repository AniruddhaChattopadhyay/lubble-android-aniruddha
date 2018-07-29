package in.lubble.app.groups;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.chat.ChatActivity;
import in.lubble.app.models.DmData;
import in.lubble.app.models.GroupData;
import in.lubble.app.models.ProfileInfo;
import in.lubble.app.models.UserGroupData;

import static in.lubble.app.chat.ChatActivity.EXTRA_DM_ID;
import static in.lubble.app.chat.ChatActivity.EXTRA_GROUP_ID;
import static in.lubble.app.chat.ChatActivity.EXTRA_IS_JOINING;
import static in.lubble.app.chat.ChatActivity.EXTRA_RECEIVER_DP_URL;
import static in.lubble.app.chat.ChatActivity.EXTRA_RECEIVER_NAME;
import static in.lubble.app.firebase.RealtimeDbHelper.getDmsRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getLubbleGroupsRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getSellerRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getUserDmsRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getUserGroupsRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getUserInfoRef;

public class GroupListFragment extends Fragment implements OnListFragmentInteractionListener {

    private static final String TAG = "GroupListFragment";

    private OnListFragmentInteractionListener mListener;
    private GroupRecyclerAdapter adapter;
    private HashMap<Query, ValueEventListener> map = new HashMap<>();
    private HashMap<Query, ValueEventListener> dmListenersMap = new HashMap<>();
    private ChildEventListener joinedGroupListener;
    private ChildEventListener unjoinedGroupListener;
    private ChildEventListener userDmsListener;
    private RecyclerView groupsRecyclerView;
    private ProgressBar progressBar;
    private HashMap<String, Set<String>> groupInvitedByMap;
    private HashMap<String, UserGroupData> userGroupDataMap;

    public GroupListFragment() {
    }

    public static GroupListFragment newInstance() {
        return new GroupListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_item, container, false);
        Context context = view.getContext();

        groupsRecyclerView = view.findViewById(R.id.rv_groups);
        progressBar = view.findViewById(R.id.progressBar_groups);
        FloatingActionButton fab = view.findViewById(R.id.btn_create_group);
        groupInvitedByMap = new HashMap<>();
        userGroupDataMap = new HashMap<>();
        Analytics.triggerScreenEvent(getContext(), this.getClass());

        groupsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        adapter = new GroupRecyclerAdapter(mListener);
        groupsRecyclerView.setAdapter(adapter);
        DividerItemDecoration itemDecor = new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL);
        groupsRecyclerView.addItemDecoration(itemDecor);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), NewGroupActivity.class));
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        adapter.clearGroups();
        groupsRecyclerView.setVisibility(View.INVISIBLE);
        syncUserGroupIds();
        syncUserDmIds();
    }

    private void syncUserDmIds() {

        userDmsListener = getUserDmsRef().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                final String dmId = dataSnapshot.getKey();
                if (dmId != null) {
                    fetchDmFrom(dmId);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                /// TODO: 29/7/18 update unread count etc
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                /// TODO: 29/7/18
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fetchDmFrom(@NonNull final String dmId) {
        final ValueEventListener dmValueListener = getDmsRef().child(dmId).addValueEventListener(new ValueEventListener() {
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
                        if (!FirebaseAuth.getInstance().getUid().equalsIgnoreCase(profileId)) {
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
                                adapter.addGroup(dmGroupData, userGroupData);
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
                            adapter.addGroup(dmGroupData, userGroupData);
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
        dmListenersMap.put(getDmsRef().child(dmId), dmValueListener);
    }

    private void syncUserGroupIds() {
        // gets list of group IDs joined by the user
        joinedGroupListener = getUserGroupsRef().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                final UserGroupData userGroupData = dataSnapshot.getValue(UserGroupData.class);
                userGroupDataMap.put(dataSnapshot.getKey(), userGroupData);
                if (userGroupData.isJoined()) {
                    syncJoinedGroups(dataSnapshot.getKey());
                } else {
                    if (userGroupData.getInvitedBy() != null) {
                        groupInvitedByMap.put(dataSnapshot.getKey(), userGroupData.getInvitedBy().keySet());
                        syncInvitedGroups(dataSnapshot.getKey());
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                final UserGroupData userGroupData = dataSnapshot.getValue(UserGroupData.class);
                userGroupDataMap.put(dataSnapshot.getKey(), userGroupData);
                if (userGroupData.isJoined()) {
                    syncJoinedGroups(dataSnapshot.getKey());
                } else {
                    groupInvitedByMap.put(dataSnapshot.getKey(), userGroupData.getInvitedBy().keySet());
                    syncInvitedGroups(dataSnapshot.getKey());
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getKey() != null) {
                    // remove from list
                    adapter.removeGroup(dataSnapshot.getKey());
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        // single listener as this just needs to be called once after all joined groups have been synced.
        getUserGroupsRef().orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // all user groups have been synced now
                if (progressBar.getVisibility() == View.VISIBLE) {
                    progressBar.setVisibility(View.GONE);
                }
                ArrayList<String> list = new ArrayList<>();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    list.add(child.getKey());
                }
                syncAllPublicGroups(list);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void syncJoinedGroups(String groupId) {
        // get meta data of the groups joined by the user
        final ValueEventListener joinedGroupListener = getLubbleGroupsRef().child(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GroupData groupData = dataSnapshot.getValue(GroupData.class);

                final UserGroupData userGroupData = userGroupDataMap.get(dataSnapshot.getKey());
                if (groupData != null && groupData.getId() != null) {
                    adapter.addGroup(groupData, userGroupData);
                    groupsRecyclerView.scrollToPosition(0);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        map.put(getLubbleGroupsRef().child(groupId), joinedGroupListener);
    }

    private void syncInvitedGroups(final String groupId) {
        // get meta data of the groups joined by the user
        final ValueEventListener joinedGroupListener = getLubbleGroupsRef().child(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        GroupData groupData = dataSnapshot.getValue(GroupData.class);
                        if (groupData != null) {
                            if (groupData.getMembers().get(FirebaseAuth.getInstance().getUid()) == null) {
                                groupData.setInvitedBy(groupInvitedByMap.get(groupData.getId()));
                            }
                            final UserGroupData userGroupData = userGroupDataMap.get(dataSnapshot.getKey());
                            adapter.addGroup(groupData, userGroupData);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        map.put(getLubbleGroupsRef().child(groupId), joinedGroupListener);
    }

    private void syncAllPublicGroups(final ArrayList<String> joinedGroupIdList) {
        unjoinedGroupListener = getLubbleGroupsRef().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                final GroupData unJoinedGroup = dataSnapshot.getValue(GroupData.class);
                if (unJoinedGroup != null && unJoinedGroup.getId() != null && !joinedGroupIdList.contains(unJoinedGroup.getId()) && !unJoinedGroup.getIsPrivate()) {
                    adapter.addPublicGroup(unJoinedGroup);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        getLubbleGroupsRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                groupsRecyclerView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = this;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (joinedGroupListener != null) {
            getUserGroupsRef().removeEventListener(joinedGroupListener);
        }
        if (unjoinedGroupListener != null) {
            getLubbleGroupsRef().removeEventListener(unjoinedGroupListener);
        }
        if (userDmsListener != null) {
            getUserDmsRef().removeEventListener(userDmsListener);
        }
        for (Query query : map.keySet()) {
            query.removeEventListener(map.get(query));
        }
        for (Query query : dmListenersMap.keySet()) {
            query.removeEventListener(dmListenersMap.get(query));
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onListFragmentInteraction(String groupId, boolean isJoining) {
        final Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra(EXTRA_GROUP_ID, groupId);
        intent.putExtra(EXTRA_IS_JOINING, isJoining);
        startActivity(intent);
    }

    @Override
    public void onDmClick(String dmId, String name, String thumbnailUrl) {
        final Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra(EXTRA_DM_ID, dmId);
        intent.putExtra(EXTRA_RECEIVER_NAME, name);
        intent.putExtra(EXTRA_RECEIVER_DP_URL, thumbnailUrl);
        startActivity(intent);
    }
}
