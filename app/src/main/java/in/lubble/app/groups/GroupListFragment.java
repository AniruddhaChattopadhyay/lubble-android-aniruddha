package in.lubble.app.groups;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import in.lubble.app.R;
import in.lubble.app.chat.ChatActivity;
import in.lubble.app.models.GroupData;

import static in.lubble.app.chat.ChatActivity.EXTRA_GROUP_ID;
import static in.lubble.app.firebase.RealtimeDbHelper.getLubbleGroupsRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getUserGroupsRef;

public class GroupListFragment extends Fragment implements OnListFragmentInteractionListener {

    private static final String TAG = "GroupListFragment";

    private OnListFragmentInteractionListener mListener;
    private GroupRecyclerAdapter adapter;
    private HashMap<Query, ChildEventListener> map = new HashMap<>();
    private ChildEventListener joinedGroupListener;
    private ChildEventListener unjoinedGroupListener;
    private RecyclerView groupsRecyclerView;

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
        FloatingActionButton fab = view.findViewById(R.id.btn_create_group);

        groupsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        adapter = new GroupRecyclerAdapter(mListener);
        groupsRecyclerView.setAdapter(adapter);
        DividerItemDecoration itemDecor = new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL);
        groupsRecyclerView.addItemDecoration(itemDecor);

        syncUserGroupIds();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), NewGroupActivity.class));
            }
        });

        return view;
    }

    private void syncUserGroupIds() {
        // gets list of group IDs joined by the user
        joinedGroupListener = getUserGroupsRef().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                syncJoinedGroups(dataSnapshot.getKey());
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
        // single listener as this just needs to be called once after all joined groups have been synced.
        getUserGroupsRef().orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // all user groups have been synced now
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
        final ChildEventListener joinedGroupListener = getLubbleGroupsRef().orderByKey().equalTo(groupId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                GroupData groupData = dataSnapshot.getValue(GroupData.class);
                adapter.addGroup(groupData);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                GroupData groupData = dataSnapshot.getValue(GroupData.class);
                adapter.updateGroup(groupData);
                groupsRecyclerView.scrollToPosition(0);
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
        map.put(getLubbleGroupsRef().orderByKey().equalTo(groupId), joinedGroupListener);
    }

    private void syncAllPublicGroups(final ArrayList<String> joinedGroupIdList) {
        unjoinedGroupListener = getLubbleGroupsRef().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                final GroupData unjoinedGroup = dataSnapshot.getValue(GroupData.class);
                if (!joinedGroupIdList.contains(unjoinedGroup.getId()) && !unjoinedGroup.getIsPrivate()) {
                    unjoinedGroup.setJoined(false);
                    adapter.addGroup(unjoinedGroup);
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
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = this;
    }

    @Override
    public void onPause() {
        super.onPause();
        getUserGroupsRef().removeEventListener(joinedGroupListener);
        getLubbleGroupsRef().removeEventListener(unjoinedGroupListener);
        for (Query query : map.keySet()) {
            query.removeEventListener(map.get(query));
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onListFragmentInteraction(GroupData groupData) {
        final Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra(EXTRA_GROUP_ID, groupData.getId());
        startActivity(intent);
    }

}
