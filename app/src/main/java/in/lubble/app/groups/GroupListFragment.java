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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import in.lubble.app.R;
import in.lubble.app.chat.ChatActivity;
import in.lubble.app.models.GroupData;

import static in.lubble.app.Constants.DEFAULT_LUBBLE;

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

        groupsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        adapter = new GroupRecyclerAdapter(mListener);
        groupsRecyclerView.setAdapter(adapter);

        syncUserGroupIds();

        return view;
    }

    private void syncUserGroupIds() {
        // gets list of group IDs joined by the user
        FirebaseDatabase.getInstance().getReference("users/" + FirebaseAuth.getInstance().getUid()
                + "/lubbles/" + DEFAULT_LUBBLE + "/groups").addChildEventListener(new ChildEventListener() {
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
    }

    private void syncJoinedGroups(String groupId) {
        // get meta data of the groups joined by the user
        final Query query = FirebaseDatabase.getInstance().getReference("lubbles/" + DEFAULT_LUBBLE
                + "/groups").orderByKey().equalTo(groupId);

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                GroupData groupData = dataSnapshot.getValue(GroupData.class);
                groupData.setId(dataSnapshot.getKey());
                adapter.addGroup(groupData);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                GroupData groupData = dataSnapshot.getValue(GroupData.class);
                groupData.setId(dataSnapshot.getKey());
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

    private void syncAllPublicGroups() {
        FirebaseDatabase.getInstance().getReference("lubbles/" + DEFAULT_LUBBLE
                + "/groups").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                adapter.addGroup(dataSnapshot.getValue(GroupData.class));
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onListFragmentInteraction(GroupData groupData) {
        final Intent intent = new Intent(getContext(), ChatActivity.class);
        startActivity(intent);
    }

}
