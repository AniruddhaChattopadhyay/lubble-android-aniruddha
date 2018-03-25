package in.lubble.app.user_search;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import in.lubble.app.R;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.GroupData;
import in.lubble.app.models.ProfileInfo;

import static in.lubble.app.firebase.RealtimeDbHelper.getLubbleGroupsRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getUserInfoRef;

public class UserSearchFrag extends Fragment implements OnUserSelectedListener {

    private OnUserSelectedListener mListener;
    private static final String ARG_LUBBLE_ID = "UserSearchFrag_ARG_LUBBLE_ID";
    private static final String ARG_GROUP_ID = "UserSearchFrag_ARG_GROUP_ID";

    private int lubbleId;
    private String groupId;
    private Button sendBtn;
    private RecyclerView usersRecyclerView;
    private UserAdapter userAdapter;
    private SelectedUserAdapter selectedUserAdapter;
    private ValueEventListener lubbleMembersListener;
    private HashMap<DatabaseReference, ValueEventListener> map = new HashMap<>();

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
        sendBtn = view.findViewById(R.id.btn_send);
        RecyclerView selectedUsersRecyclerView = view.findViewById(R.id.rv_selected_users);
        EditText searchEt = view.findViewById(R.id.et_user_search);

        usersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        userAdapter = new UserAdapter(mListener);
        usersRecyclerView.setAdapter(userAdapter);
        fetchAllLubbleUsers();

        selectedUsersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), OrientationHelper.HORIZONTAL, false));
        selectedUserAdapter = new SelectedUserAdapter(mListener);
        selectedUsersRecyclerView.setAdapter(selectedUserAdapter);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<String> selectedUidList = selectedUserAdapter.getSelectedUidList();
                if (selectedUidList.size() > 0) {
                    final DatabaseReference inviteesRef = RealtimeDbHelper.getUserGroupsRef().child(groupId).child("invitees");
                    for (String uid : selectedUidList) {
                        //inviteesRef.child(uid).setValue(Boolean.TRUE);
                        inviteesRef.child(uid).setValue(Boolean.TRUE, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                Toast.makeText(getContext(), "Invites Sent", Toast.LENGTH_SHORT).show();
                                getActivity().finish();
                            }
                        });
                    }
                } else {
                    Toast.makeText(getContext(), "Please select users to invite", Toast.LENGTH_SHORT).show();
                }
            }
        });

        handleSearch(searchEt);

        return view;
    }

    private void handleSearch(EditText searchEt) {
        searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // filter your list from your input
                userAdapter.getFilter().filter(s.toString());
            }
        });
    }

    private void fetchAllLubbleUsers() {
        lubbleMembersListener = RealtimeDbHelper.getLubbleMembersRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> userList = new ArrayList<>();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    userList.add(child.getKey());
                }/*
                */
                fetchGroupUsers();
                fetchAllLubbleMembersProfile(userList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void fetchAllLubbleMembersProfile(ArrayList<String> userList) {
        for (String uid : userList) {
            ValueEventListener membersProfileListener = getUserInfoRef(uid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final ProfileInfo profileInfo = dataSnapshot.getValue(ProfileInfo.class);
                    if (profileInfo != null) {
                        profileInfo.setId(dataSnapshot.getRef().getParent().getKey()); // this works. Don't touch.
                        userAdapter.addMemberProfile(profileInfo);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            map.put(getUserInfoRef(uid), membersProfileListener);
        }
    }

    private void fetchGroupUsers() {
        // keep this as singleEventListener to avoid over complications
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

    @Override
    public void onPause() {
        super.onPause();
        RealtimeDbHelper.getLubbleMembersRef().removeEventListener(lubbleMembersListener);
        for (Query query : map.keySet()) {
            query.removeEventListener(map.get(query));
        }
        userAdapter.removeAllListeners();
        selectedUserAdapter.removeAllListeners();
    }
}
