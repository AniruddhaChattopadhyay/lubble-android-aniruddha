package in.lubble.app.user_search;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.database.*;
import in.lubble.app.GlideApp;
import in.lubble.app.LubbleApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.GroupData;
import in.lubble.app.models.ProfileData;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static in.lubble.app.firebase.RealtimeDbHelper.getLubbleGroupsRef;
import static in.lubble.app.utils.ReferralUtils.generateBranchUrl;
import static in.lubble.app.utils.ReferralUtils.getReferralIntentForGroup;

public class UserSearchFrag extends Fragment implements OnUserSelectedListener {

    private static final String TAG = "UserSearchFrag";
    private OnUserSelectedListener mListener;
    private static final String ARG_LUBBLE_ID = "UserSearchFrag_ARG_LUBBLE_ID";
    private static final String ARG_GROUP_ID = "UserSearchFrag_ARG_GROUP_ID";

    private String lubbleId;
    private String groupId;
    private String sharingUrl;
    private Button sendBtn;
    private RecyclerView usersRecyclerView;
    private UserAdapter userAdapter;
    private SelectedUserAdapter selectedUserAdapter;

    private LinearLayout copyLinkContainer;
    private LinearLayout fbContainer;
    private LinearLayout whatsappContainer;
    private LinearLayout moreContainer;
    private ProgressDialog sharingProgressDialog;

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
        copyLinkContainer = view.findViewById(R.id.container_copy_link);
        fbContainer = view.findViewById(R.id.container_fb);
        whatsappContainer = view.findViewById(R.id.container_whatsapp);
        moreContainer = view.findViewById(R.id.container_more);

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

                    sharingProgressDialog = new ProgressDialog(getContext());
                    generateBranchUrl(getContext(), linkCreateListener);
                    initClickHandlers(groupData);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    final Branch.BranchLinkCreateListener linkCreateListener = new Branch.BranchLinkCreateListener() {
        @Override
        public void onLinkCreate(String url, BranchError error) {
            if (url != null) {
                Log.d(TAG, "got my Branch link to share: " + url);
                sharingUrl = url;
                if (sharingProgressDialog != null && sharingProgressDialog.isShowing()) {
                    sharingProgressDialog.dismiss();
                }
            } else {
                Log.e(TAG, "Branch onLinkCreate: " + error.getMessage());
                Crashlytics.logException(new IllegalStateException(error.getMessage()));
                if (isAdded() && isVisible()) {
                    Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    private void initClickHandlers(final GroupData groupData) {
        copyLinkContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent referralIntent = getReferralIntentForGroup(getContext(), sharingUrl, sharingProgressDialog, groupData, linkCreateListener);
                if (referralIntent != null) {
                    ClipboardManager clipboard = (ClipboardManager) LubbleApp.getAppContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("lubble_share_link", sharingUrl);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(requireContext(), "LINK COPIED", Toast.LENGTH_SHORT).show();
                    Analytics.triggerEvent(AnalyticsEvents.REFERRAL_COPY_LINK, getContext());
                } else {
                    Toast.makeText(requireContext(), "RETRY", Toast.LENGTH_SHORT).show();
                }
            }
        });
        fbContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent referralIntent = getReferralIntentForGroup(getContext(), sharingUrl, sharingProgressDialog, groupData, linkCreateListener);
                if (referralIntent != null) {
                    boolean facebookAppFound = false;
                    List<ResolveInfo> matches = getContext().getPackageManager().queryIntentActivities(referralIntent, 0);
                    for (ResolveInfo info : matches) {
                        if (info.activityInfo.packageName.toLowerCase().startsWith("com.facebook.katana")) {
                            referralIntent.setPackage(info.activityInfo.packageName);
                            facebookAppFound = true;
                            break;
                        }
                    }
                    if (!facebookAppFound) {
                        String sharerUrl = "https://www.facebook.com/sharer/sharer.php?u=" + sharingUrl;
                        referralIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(sharerUrl));
                    }
                    startActivity(referralIntent);
                    Analytics.triggerEvent(AnalyticsEvents.REFERRAL_FB_SHARE, getContext());
                }
            }
        });
        whatsappContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent referralIntent = getReferralIntentForGroup(getContext(), sharingUrl, sharingProgressDialog, groupData, linkCreateListener);
                if (referralIntent != null) {
                    PackageManager pm = getContext().getPackageManager();
                    try {
                        PackageInfo info = pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
                        //Check if package exists or not. If not then code
                        //in catch block will be called
                        referralIntent.setPackage("com.whatsapp");

                        startActivity(Intent.createChooser(referralIntent, getString(R.string.refer_share_title)));

                        Analytics.triggerEvent(AnalyticsEvents.REFERRAL_WA_SHARE, getContext());

                    } catch (PackageManager.NameNotFoundException e) {
                        Toast.makeText(getContext(), "You don't have WhatsApp! Please share it with another app", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        moreContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent referralIntent = getReferralIntentForGroup(getContext(), sharingUrl, sharingProgressDialog, groupData, linkCreateListener);
                if (referralIntent != null) {
                    startActivity(Intent.createChooser(referralIntent, getString(R.string.refer_share_title)));
                    Analytics.triggerEvent(AnalyticsEvents.REFERRAL_MORE_SHARE, getContext());
                }
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
