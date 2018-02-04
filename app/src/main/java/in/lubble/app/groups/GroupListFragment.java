package in.lubble.app.groups;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import in.lubble.app.R;
import in.lubble.app.chat.ChatActivity;
import in.lubble.app.models.GroupData;

import static in.lubble.app.Constants.DEFAULT_LUBBLE;
import static in.lubble.app.chat.ChatActivity.EXTRA_GROUP_ID;

public class GroupListFragment extends Fragment implements OnListFragmentInteractionListener {

    private static final String TAG = "GroupListFragment";

    private OnListFragmentInteractionListener mListener;
    private GroupRecyclerAdapter adapter;

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

        RecyclerView groupsRecyclerView = view.findViewById(R.id.rv_groups);
        Button newGroupBtn = view.findViewById(R.id.btn_create_group);

        groupsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        adapter = new GroupRecyclerAdapter(mListener);
        groupsRecyclerView.setAdapter(adapter);

        syncUserGroupIds();

        newGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), NewGroupActivity.class));
            }
        });

        return view;
    }

    private void syncUserGroupIds() {
        // gets list of group IDs joined by the user
        final DatabaseReference userGroupsRef = FirebaseDatabase.getInstance().getReference("users/" + FirebaseAuth.getInstance().getUid()
                + "/lubbles/" + DEFAULT_LUBBLE + "/groups");
        userGroupsRef.addChildEventListener(new ChildEventListener() {
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
        userGroupsRef.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
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
        final Query query = FirebaseDatabase.getInstance().getReference("lubbles/" + DEFAULT_LUBBLE
                + "/groups").orderByKey().equalTo(groupId);

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                GroupData groupData = dataSnapshot.getValue(GroupData.class);
                adapter.addGroup(groupData);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                GroupData groupData = dataSnapshot.getValue(GroupData.class);
                adapter.updateGroup(groupData);
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

    private void syncAllPublicGroups(final ArrayList<String> joinedGroupIdList) {
        FirebaseDatabase.getInstance().getReference("lubbles/" + DEFAULT_LUBBLE
                + "/groups").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<GroupData> allGroupList = new ArrayList<>();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    allGroupList.add(child.getValue(GroupData.class));
                }
                for (GroupData groupData : allGroupList) {
                    boolean isDuplicate = false;
                    for (String joinedId : joinedGroupIdList) {
                        if (groupData.getId().equalsIgnoreCase(joinedId)) {
                            isDuplicate = true;
                            break;
                        }
                    }
                    if (!isDuplicate && !groupData.getIsPrivate()) {
                        groupData.setJoined(false);
                        adapter.addGroup(groupData);
                    }
                }
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
