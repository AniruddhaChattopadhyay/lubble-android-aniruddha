package in.lubble.app.user_search;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.*;
import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.GroupData;
import in.lubble.app.models.ProfileData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static in.lubble.app.firebase.RealtimeDbHelper.getLubbleGroupsRef;

public class UserSearchFrag extends Fragment implements OnUserSelectedListener {

    private OnUserSelectedListener mListener;
    private static final String ARG_LUBBLE_ID = "UserSearchFrag_ARG_LUBBLE_ID";
    private static final String ARG_GROUP_ID = "UserSearchFrag_ARG_GROUP_ID";

    private String lubbleId;
    private String groupId;
    private Button sendBtn;
    private RecyclerView usersRecyclerView;
    private UserAdapter userAdapter;
    private SelectedUserAdapter selectedUserAdapter;

    public UserSearchFrag() {
    }

    public static UserSearchFrag newInstance(String lubbleId, String groupId) {
        UserSearchFrag fragment = new UserSearchFrag();
        Bundle args = new Bundle();
        args.putString(ARG_LUBBLE_ID, lubbleId);
        args.putString(ARG_GROUP_ID, groupId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            lubbleId = getArguments().getString(ARG_LUBBLE_ID);
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

        userAdapter = new UserAdapter(mListener, GlideApp.with(getContext()));
        usersRecyclerView.setAdapter(userAdapter);
        fetchAllLubbleUsers();
        fetchGroupUsers();

        selectedUsersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), OrientationHelper.HORIZONTAL, false));
        selectedUserAdapter = new SelectedUserAdapter(mListener, GlideApp.with(getContext()));
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
                                if (isAdded() && isVisible()) {
                                    Toast.makeText(getContext(), R.string.invites_sent, Toast.LENGTH_SHORT).show();
                                    getActivity().finish();
                                }
                            }
                        });
                    }
                } else {
                    Toast.makeText(getContext(), R.string.invite_select_users, Toast.LENGTH_SHORT).show();
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
        FirebaseDatabase.getInstance().getReference("users").orderByChild("lubbles/" + LubbleSharedPrefs.getInstance().requireLubbleId()).startAt("")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        userAdapter.clear();
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            if (!(child.getValue() instanceof Boolean)) {
                                final ProfileData profileData = child.getValue(ProfileData.class);
                                if (profileData != null && profileData.getInfo() != null && !profileData.getIsDeleted()) {
                                    profileData.setId(child.getKey());
                                    profileData.getInfo().setId(profileData.getId());
                                    userAdapter.addMemberProfile(profileData.getInfo());
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
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
        selectedUserAdapter.removeAllListeners();
    }
}
