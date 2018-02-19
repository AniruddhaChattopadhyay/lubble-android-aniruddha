package in.lubble.app.user_search;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import in.lubble.app.R;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.GroupData;

import static in.lubble.app.firebase.RealtimeDbHelper.getLubbleGroupsRef;

public class UserSearchFrag extends Fragment implements OnUserSelectedListener {

    private OnUserSelectedListener mListener;
    private static final String ARG_LUBBLE_ID = "UserSearchFrag_ARG_LUBBLE_ID";
    private static final String ARG_GROUP_ID = "UserSearchFrag_ARG_GROUP_ID";

    private int lubbleId;
    private String groupId;
    private RecyclerView usersRecyclerView;
    private UserAdapter userAdapter;
    private SelectedUserAdapter selectedUserAdapter;

    public UserSearchFrag() {
    }

    public static UserSearchFrag newInstance(int lubbleId, String groupId) {
        UserSearchFrag fragment = new UserSearchFrag();
        Bundle args = new Bundle();
        args.putInt(ARG_LUBBLE_ID, lubbleId);
        args.putString(ARG_GROUP_ID, groupId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            lubbleId = getArguments().getInt(ARG_LUBBLE_ID);
            groupId = getArguments().getString(ARG_GROUP_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_search, container, false);

        usersRecyclerView = view.findViewById(R.id.rv_users);
        RecyclerView selectedUsersRecyclerView = view.findViewById(R.id.rv_selected_users);

        usersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fetchAllLubbleUsers();

        selectedUsersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), OrientationHelper.HORIZONTAL, false));
        selectedUserAdapter = new SelectedUserAdapter(mListener);
        selectedUsersRecyclerView.setAdapter(selectedUserAdapter);

        return view;
    }

    private void fetchAllLubbleUsers() {
        RealtimeDbHelper.getLubbleMembersRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> userList = new ArrayList<>();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    userList.add(child.getKey());
                }
                userAdapter = new UserAdapter(userList, mListener);
                usersRecyclerView.setAdapter(userAdapter);
                fetchGroupUsers();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void fetchGroupUsers() {
        //keep this as singleEventListener
        getLubbleGroupsRef().child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final GroupData groupData = dataSnapshot.getValue(GroupData.class);
                if (groupData != null) {
                    final ArrayList<String> groupMembersList = new ArrayList<>(groupData.getMembers().keySet());
                    final HashMap<String, Boolean> groupMembersMap = new HashMap<>();
                    for (String uid : groupMembersList) {
                        groupMembersMap.put(uid, true);
                    }
                    userAdapter.addGroupMembersList(groupMembersMap);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onUserSelected(String uid) {
        selectedUserAdapter.addUser(uid);
    }

    @Override
    public void onUserDeSelected(String uid) {
        selectedUserAdapter.removeUser(uid);
        userAdapter.deselectUser(uid);
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

}
