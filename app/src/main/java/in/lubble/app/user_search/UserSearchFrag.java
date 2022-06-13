package in.lubble.app.user_search;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
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

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.lubble.app.GlideApp;
import in.lubble.app.LubbleApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.chat.ShareActiv;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.GroupData;
import in.lubble.app.models.ProfileData;
import in.lubble.app.models.ProfileInfo;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import in.lubble.app.receivers.ShareSheetReceiver;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static in.lubble.app.utils.ReferralUtils.generateBranchUrlForGroup;
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
    private EditText searchEt;
    private RecyclerView usersRecyclerView;
    private UserAdapter userAdapter;
    private SelectedUserAdapter selectedUserAdapter;

    private LinearLayout copyLinkContainer;
    private LinearLayout fbContainer;
    private LinearLayout whatsappContainer;
    private LinearLayout moreContainer;
    private ProgressDialog sharingProgressDialog;

    private LinearLayout sendGroupContainer;
    private LinearLayout inviteLinksContainer;
    private View selectedMembersDiv;

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
        searchEt = view.findViewById(R.id.et_user_search);
        inviteLinksContainer = view.findViewById(R.id.container_invite_links);
        copyLinkContainer = view.findViewById(R.id.container_copy_link);
        selectedMembersDiv = view.findViewById(R.id.div_selected_members);
        sendGroupContainer = view.findViewById(R.id.container_send);
        fbContainer = view.findViewById(R.id.container_fb);
        whatsappContainer = view.findViewById(R.id.container_whatsapp);
        moreContainer = view.findViewById(R.id.container_more);

        usersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        Analytics.triggerScreenEvent(getContext(), this.getClass());

        userAdapter = new UserAdapter(mListener, GlideApp.with(getContext()), groupId);
        usersRecyclerView.setAdapter(userAdapter);
        fetchAllUsers();

        selectedUsersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
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
                                    Analytics.triggerEvent(AnalyticsEvents.REFERRAL_INVITE_MEMBERS, getContext());
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

        handleSearch();

        sendGroupContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareActiv.open(requireContext(), groupId, ShareActiv.ShareType.GROUP);
            }
        });

        return view;
    }

    private void handleSearch() {
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
                if (s.length() != 0) {
                    inviteLinksContainer.setVisibility(View.GONE);
                } else if (selectedUserAdapter.getItemCount() == 0) {
                    inviteLinksContainer.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void fetchAllUsers() {
        // fetch token first
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        mUser.getIdToken(false)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String idToken = task.getResult().getToken();
                        fetchLubbleMembers(idToken);
                        fetchGroupUsers(idToken);
                    } else {
                        Toast.makeText(getContext(), "Failed to fetch access token", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchLubbleMembers(String idToken) {
        final Endpoints endpoints = ServiceGenerator.createFirebaseService(Endpoints.class);
        Call<JsonObject> lubbleMembersCall =
                endpoints.fetchLubbleMembers("\"lubbles/" + LubbleSharedPrefs.getInstance().requireLubbleId() + "\"", "\"\"", idToken);
        lubbleMembersCall.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                if (response.isSuccessful() && isAdded()) {
                    final JsonObject responseJson = response.body();
                    Gson gson = new Gson();
                    for (Map.Entry<String, JsonElement> entry : responseJson.entrySet()) {
                        ProfileData profileData = gson.fromJson(entry.getValue(), ProfileData.class);
                        final ProfileInfo profileInfo = profileData.getInfo();
                        if (profileInfo != null && profileInfo.getName() != null && !profileData.getIsDeleted()) {
                            profileInfo.setId(entry.getKey());
                            userAdapter.addMemberProfile(profileInfo);
                        }
                    }
                } else if (isAdded()) {
                    Toast.makeText(getContext(), "error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                if (isAdded() && isVisible()) {
                    Toast.makeText(getContext(), R.string.check_internet, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchGroupUsers(String idToken) {
        final Endpoints endpoints = ServiceGenerator.createFirebaseService(Endpoints.class);
        Call<GroupData> groupDataCall = endpoints.fetchGroupData(LubbleSharedPrefs.getInstance().requireLubbleId(), groupId, idToken);
        groupDataCall.enqueue(new Callback<GroupData>() {
            @Override
            public void onResponse(@NotNull Call<GroupData> call, @NotNull Response<GroupData> response) {
                if (response.isSuccessful() && isAdded()) {
                    final GroupData groupData = response.body();
                    if (groupData != null && getContext() != null) {
                        final ArrayList<String> groupMembersList = new ArrayList<>(groupData.getMembers().keySet());
                        final HashMap<String, Boolean> groupMembersMap = new HashMap<>();
                        for (String uid : groupMembersList) {
                            groupMembersMap.put(uid, true);
                        }
                        userAdapter.addGroupMembersList(groupMembersMap);

                        sharingProgressDialog = new ProgressDialog(getContext());
                        groupData.setId(groupId);
                        generateBranchUrlForGroup(getContext(), linkCreateListener, groupData);
                        initClickHandlers(groupData);
                    }
                } else if (isAdded()) {
                    Toast.makeText(getContext(), "error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GroupData> call, Throwable t) {
                if (isAdded() && isVisible()) {
                    Toast.makeText(getContext(), R.string.check_internet, Toast.LENGTH_SHORT).show();
                }
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
                FirebaseCrashlytics.getInstance().recordException(new IllegalStateException(error.getMessage()));
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
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(
                            getContext(), 21,
                            new Intent(getContext(), ShareSheetReceiver.class),
                            PendingIntent.FLAG_UPDATE_CURRENT);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                        startActivity(Intent.createChooser(referralIntent, getString(R.string.refer_share_title), pendingIntent.getIntentSender()));
                    } else {
                        startActivity(Intent.createChooser(referralIntent, getString(R.string.refer_share_title)));
                    }
                    Analytics.triggerEvent(AnalyticsEvents.REFERRAL_MORE_SHARE, getContext());
                }
            }
        });
    }

    @Override
    public void onUserSelected(String uid) {
        selectedUserAdapter.addUser(uid);
        inviteLinksContainer.setVisibility(View.GONE);
        selectedMembersDiv.setVisibility(View.VISIBLE);
        sendBtn.setVisibility(View.VISIBLE);
        searchEt.setText("");
    }

    @Override
    public void onUserDeSelected(String uid) {
        selectedUserAdapter.removeUser(uid);
        userAdapter.deselectUser(uid);
        if (selectedUserAdapter.getItemCount() == 0) {
            inviteLinksContainer.setVisibility(View.VISIBLE);
            selectedMembersDiv.setVisibility(View.GONE);
            sendBtn.setVisibility(View.GONE);
        }
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
