package in.lubble.app.chat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import in.lubble.app.Constants;
import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.UploadFileService;
import in.lubble.app.chat.chat_info.MsgInfoActivity;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.groups.group_info.ScrollingGroupInfoActivity;
import in.lubble.app.models.ChatData;
import in.lubble.app.models.GroupData;
import in.lubble.app.models.ProfileInfo;
import in.lubble.app.models.UserGroupData;
import in.lubble.app.network.LinkMetaAsyncTask;
import in.lubble.app.network.LinkMetaListener;
import in.lubble.app.utils.AppNotifUtils;
import in.lubble.app.utils.DateTimeUtils;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

import static android.app.Activity.RESULT_OK;
import static in.lubble.app.firebase.RealtimeDbHelper.getCreateOrJoinGroupRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getDmMessagesRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getLubbleGroupsRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getMessagesRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getUserInfoRef;
import static in.lubble.app.models.ChatData.LINK;
import static in.lubble.app.models.ChatData.REPLY;
import static in.lubble.app.models.ChatData.SYSTEM;
import static in.lubble.app.models.ChatData.UNREAD;
import static in.lubble.app.utils.FileUtils.createImageFile;
import static in.lubble.app.utils.FileUtils.getFileFromInputStreamUri;
import static in.lubble.app.utils.FileUtils.getPickImageIntent;
import static in.lubble.app.utils.FileUtils.showStoragePermRationale;
import static in.lubble.app.utils.NotifUtils.deleteUnreadMsgsForGroupId;
import static in.lubble.app.utils.StringUtils.extractFirstLink;
import static in.lubble.app.utils.StringUtils.isValidString;
import static in.lubble.app.utils.UiUtils.showBottomSheetAlert;

@RuntimePermissions
public class ChatFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "ChatFragment";
    private static final int REQUEST_CODE_IMG = 789;
    private static final String KEY_GROUP_ID = "CHAT_GROUP_ID";
    private static final String KEY_MSG_ID = "CHAT_MSG_ID";
    private static final String KEY_IS_JOINING = "KEY_IS_JOINING";
    private static final String KEY_DM_ID = "KEY_DM_ID";
    private static final String KEY_RECEIVER_ID = "KEY_RECEIVER_ID";
    private static final String KEY_RECEIVER_NAME = "KEY_RECEIVER_NAME";
    private static final String KEY_RECEIVER_DP_URL = "KEY_RECEIVER_DP_URL";

    @Nullable
    private GroupData groupData;
    private RelativeLayout joinContainer;
    private TextView joinDescTv;
    private Button joinBtn;
    private TextView declineTv;
    private RelativeLayout composeContainer;
    private Group linkMetaContainer;
    private RecyclerView chatRecyclerView;
    private EditText newMessageEt;
    private ImageView sendBtn;
    private ImageView attachMediaBtn;
    private TextView linkTitle;
    private TextView linkDesc;
    private ImageView linkCancel;
    @Nullable
    private DatabaseReference groupReference;
    @Nullable
    private DatabaseReference messagesReference;
    private String currentPhotoPath;
    @Nullable
    private String groupId;
    @Nullable
    private String dmId;
    @Nullable
    private String receiverId;
    @Nullable
    private String receiverName;
    @Nullable
    private String receiverDpUrl;
    @Nullable
    private String msgIdToOpen;
    private boolean isJoining;
    private ChildEventListener msgChildListener;
    private ValueEventListener groupInfoListener;
    private HashMap<String, ProfileInfo> groupMembersMap;
    private String prevUrl = "";
    private boolean foundFirstUnreadMsg;
    private RelativeLayout bottomContainer;
    private View pvtSystemMsg;
    private ProgressDialog joiningProgressDialog;
    @Nullable
    private ValueEventListener bottomBarListener;
    @Nullable
    private String replyMsgId = null;
    @Nullable
    private Parcelable recyclerViewState;
    private ChatAdapter chatAdapter;

    public ChatFragment() {
        // Required empty public constructor
    }

    public static ChatFragment newInstance(@Nullable String groupId, boolean isJoining, @Nullable String msgId, @Nullable String dmId,
                                           @Nullable String receiverId, @Nullable String receiverName, @Nullable String receiverDpUrl) {

        Bundle args = new Bundle();
        args.putString(KEY_GROUP_ID, groupId);
        args.putString(KEY_MSG_ID, msgId);
        args.putBoolean(KEY_IS_JOINING, isJoining);
        args.putString(KEY_DM_ID, dmId);
        args.putString(KEY_RECEIVER_ID, receiverId);
        args.putString(KEY_RECEIVER_NAME, receiverName);
        args.putString(KEY_RECEIVER_DP_URL, receiverDpUrl);
        ChatFragment fragment = new ChatFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        groupId = getArguments().getString(KEY_GROUP_ID);
        msgIdToOpen = getArguments().getString(KEY_MSG_ID);
        dmId = getArguments().getString(KEY_DM_ID);
        receiverId = getArguments().getString(KEY_RECEIVER_ID);
        receiverName = getArguments().getString(KEY_RECEIVER_NAME);
        receiverDpUrl = getArguments().getString(KEY_RECEIVER_DP_URL);
        isJoining = getArguments().getBoolean(KEY_IS_JOINING);

        if (groupId != null) {
            groupReference = getLubbleGroupsRef().child(groupId);
            messagesReference = getMessagesRef().child(groupId);
        } else if (dmId != null) {
            // todo groupReference = getLubbleGroupsRef().child(groupId);
            messagesReference = getDmMessagesRef().child(dmId);
        } else if (receiverId != null) {

        } else {
            throw new RuntimeException("khuch to params dega bhai?");
        }
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        registerMediaUploadCallback();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_chat, container, false);

        composeContainer = view.findViewById(R.id.compose_container);
        joinContainer = view.findViewById(R.id.relativeLayout_join_container);
        joinDescTv = view.findViewById(R.id.tv_join_desc);
        joinBtn = view.findViewById(R.id.btn_join);
        declineTv = view.findViewById(R.id.tv_decline);
        chatRecyclerView = view.findViewById(R.id.rv_chat);
        newMessageEt = view.findViewById(R.id.et_new_message);
        sendBtn = view.findViewById(R.id.iv_send_btn);
        attachMediaBtn = view.findViewById(R.id.iv_attach);
        linkMetaContainer = view.findViewById(R.id.group_link_meta);
        linkTitle = view.findViewById(R.id.tv_link_title);
        linkDesc = view.findViewById(R.id.tv_link_desc);
        linkCancel = view.findViewById(R.id.iv_link_cancel);
        bottomContainer = view.findViewById(R.id.bottom_container);
        pvtSystemMsg = view.findViewById(R.id.view_pvt_sys_msg);

        groupMembersMap = new HashMap<>();

        if (isJoining) {
            showJoiningDialog();
        }
        sendBtn.setEnabled(false);
        setupTogglingOfSendBtn();
        sendBtn.setOnClickListener(this);
        attachMediaBtn.setOnClickListener(this);
        joinBtn.setOnClickListener(this);
        declineTv.setOnClickListener(this);
        linkCancel.setOnClickListener(this);

        return view;
    }

    private void showJoiningDialog() {
        joiningProgressDialog = new ProgressDialog(getContext());
        joiningProgressDialog.setTitle(getString(R.string.joining_group));
        joiningProgressDialog.setMessage(getString(R.string.all_please_wait));
        joiningProgressDialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();

        syncGroupInfo();
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        chatRecyclerView.setLayoutManager(layoutManager);
        chatAdapter = new ChatAdapter(
                getActivity(),
                getContext(),
                groupId,
                chatRecyclerView,
                this,
                GlideApp.with(getContext()));
        chatRecyclerView.setAdapter(chatAdapter);
        if (messagesReference != null) {
            msgChildListener = msgListener(messagesReference);
        }

        deleteUnreadMsgsForGroupId(groupId, getContext());
        AppNotifUtils.deleteAppNotif(getContext(), groupId);
        foundFirstUnreadMsg = false;
        chatRecyclerView.setOnFlingListener(new RecyclerView.OnFlingListener() {
            @Override
            public boolean onFling(int velocityX, int velocityY) {
                recyclerViewState = null;
                msgIdToOpen = null;
                return false;
            }
        });
        chatAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int msgCount = chatAdapter.getItemCount();
                int lastVisiblePosition =
                        layoutManager.findLastCompletelyVisibleItemPosition();
                if (recyclerViewState != null) {
                    chatRecyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
                    if (lastVisiblePosition != -1 && (positionStart >= (msgCount - 1) &&
                            lastVisiblePosition == (positionStart - 1))) {
                        // If the user is at the bottom of the list, scroll to the bottom
                        // of the list to show the newly added message.
                        recyclerViewState = null;
                        chatRecyclerView.scrollToPosition(positionStart);
                    }
                } else if (msgIdToOpen != null) {
                    final int indexOfChatMsg = chatAdapter.getIndexOfChatMsg(msgIdToOpen);
                    if (indexOfChatMsg != -1) {
                        chatRecyclerView.scrollToPosition(indexOfChatMsg);
                        chatAdapter.setPosToFlash(indexOfChatMsg);
                        if (lastVisiblePosition != -1 && (positionStart >= (msgCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                            // If the user is at the bottom of the list, scroll to the bottom
                            // of the list to show the newly added message.
                            msgIdToOpen = null;
                            chatRecyclerView.scrollToPosition(positionStart);
                        }
                    }
                } else {
                    // If the recycler view is initially being loaded
                    if (lastVisiblePosition == -1 && !foundFirstUnreadMsg) {
                        final int pos = msgCount - 1;
                        final ChatData chatMsg = chatAdapter.getChatMsgAt(pos);
                        if (chatMsg.getReadReceipts().get(FirebaseAuth.getInstance().getUid()) == null) {
                            // unread msg found
                            foundFirstUnreadMsg = true;
                            final ChatData unreadChatData = new ChatData();
                            unreadChatData.setType(UNREAD);
                            chatAdapter.addChatData(pos, unreadChatData);
                            chatRecyclerView.scrollToPosition(pos - 1);
                        } else {
                            // all msgs read, scroll to last msg
                            chatRecyclerView.scrollToPosition(positionStart);
                        }
                    } else if (lastVisiblePosition != -1 && (positionStart >= (msgCount - 1) &&
                            lastVisiblePosition == (positionStart - 1))) {
                        // If the user is at the bottom of the list, scroll to the bottom
                        // of the list to show the newly added message.
                        chatRecyclerView.scrollToPosition(positionStart);
                    } else if (isValidString(chatAdapter.getChatMsgAt(positionStart).getAuthorUid()) &&
                            chatAdapter.getChatMsgAt(positionStart).getAuthorUid().equalsIgnoreCase(FirebaseAuth.getInstance().getUid())) {
                        chatRecyclerView.scrollToPosition(positionStart);
                    }
                }
            }
        });

        chatRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    int position = chatAdapter.getItemCount() - 1;
                    if (position != -1) {
                        // scrollToPosition() doesn't work here. why?
                        chatRecyclerView.smoothScrollToPosition(position);
                    }
                }
            }
        });

        resetActionBar();
    }

    private void resetActionBar() {
        final ActionMode actionMode = ((AppCompatActivity) getContext()).startSupportActionMode(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.menu_chat, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });
        if (actionMode != null) {
            actionMode.finish();
        }
    }

    private void syncGroupInfo() {
        if (!TextUtils.isEmpty(receiverName)) {
            chatRecyclerView.setVisibility(View.VISIBLE);
            ((ChatActivity) getActivity()).setGroupMeta(receiverName, receiverDpUrl, true);
        } else if (groupReference != null) {
            groupInfoListener = groupReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    groupData = dataSnapshot.getValue(GroupData.class);
                    // fetchMembersProfile(groupData.getMembers()); to be used for tagging
                    if (groupData != null) {
                        if (!groupData.isJoined() && groupData.getIsPrivate()) {
                            chatRecyclerView.setVisibility(View.GONE);
                            pvtSystemMsg.setVisibility(View.VISIBLE);
                            ((TextView) pvtSystemMsg.findViewById(R.id.tv_system_msg)).setText(R.string.pvt_group_msgs_hidden);
                        } else {
                            chatRecyclerView.setVisibility(View.VISIBLE);
                            pvtSystemMsg.setVisibility(View.GONE);
                        }
                        ((ChatActivity) getActivity()).setGroupMeta(groupData.getTitle(), groupData.getThumbnail(), groupData.getIsPrivate());
                        resetUnreadCount();
                        showBottomBar(groupData);
                        showPublicGroupWarning();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void fetchMembersProfile(HashMap<String, Object> membersMap) {
        for (String uid : membersMap.keySet()) {
            ValueEventListener valueEventListener = getUserInfoRef(uid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final ProfileInfo profileInfo = dataSnapshot.getValue(ProfileInfo.class);
                    if (profileInfo != null) {
                        profileInfo.setId(dataSnapshot.getRef().getParent().getKey()); // this works. Don't touch.
                        groupMembersMap.put(dataSnapshot.getRef().getParent().getKey(), profileInfo);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void showBottomBar(final GroupData groupData) {
        if (!isJoining) {
            bottomContainer.setVisibility(View.VISIBLE);
        }

        bottomBarListener = RealtimeDbHelper.getUserGroupsRef().child(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (groupData.isJoined()) {
                    composeContainer.setVisibility(View.VISIBLE);
                    joinContainer.setVisibility(View.GONE);
                    if (joiningProgressDialog != null && isJoining) {
                        bottomContainer.setVisibility(View.VISIBLE);
                        joiningProgressDialog.dismiss();
                        isJoining = false;
                    }
                } else {
                    final UserGroupData userGroupData = dataSnapshot.getValue(UserGroupData.class);
                    if (userGroupData != null && userGroupData.getInvitedBy() != null && userGroupData.getInvitedBy().size() != 0) {
                        final HashMap<String, Boolean> invitedBy = userGroupData.getInvitedBy();
                        String inviter = (String) invitedBy.keySet().toArray()[0];
                        RealtimeDbHelper.getUserInfoRef(inviter).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (isAdded() && isVisible()) {
                                    final ProfileInfo profileInfo = dataSnapshot.getValue(ProfileInfo.class);
                                    joinDescTv.setText(String.format(getString(R.string.invited_by), profileInfo.getName()));
                                    declineTv.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        composeContainer.setVisibility(View.GONE);
                        joinContainer.setVisibility(View.VISIBLE);
                    } else {
                        joinDescTv.setText(R.string.join_group_to_chat);
                        declineTv.setVisibility(View.GONE);
                        composeContainer.setVisibility(View.GONE);
                        joinContainer.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showPublicGroupWarning() {
        if (!LubbleSharedPrefs.getInstance().getIsDefaultGroupInfoShown() && groupId.equalsIgnoreCase(Constants.DEFAULT_GROUP)) {
            RealtimeDbHelper.getLubbleRef().addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String lubbleName = dataSnapshot.child("title").getValue(String.class);
                    showBottomSheetAlert(getContext(), getLayoutInflater(),
                            String.format(getString(R.string.lubble_group_warning_title), lubbleName),
                            String.format(getString(R.string.lubble_group_warning_subtitle), lubbleName),
                            R.drawable.ic_public_black_24dp, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    LubbleSharedPrefs.getInstance().setIsDefaultGroupInfoShown(true);
                                    if (!LubbleSharedPrefs.getInstance().getIsPublicGroupInfoShown()) {
                                        LubbleSharedPrefs.getInstance().setShowPvtGroupInfo(true);
                                    }
                                }
                            });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        } else if (groupData != null && !LubbleSharedPrefs.getInstance().getIsPublicGroupInfoShown()
                && !groupData.getIsPrivate() && !groupId.equalsIgnoreCase(Constants.DEFAULT_GROUP)) {
            showBottomSheetAlert(getContext(), getLayoutInflater(),
                    getString(R.string.public_group_warning_title),
                    getString(R.string.public_group_warning_subtitle),
                    R.drawable.ic_public_black_24dp, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            LubbleSharedPrefs.getInstance().setIsPublicGroupInfoShown(true);
                            if (!LubbleSharedPrefs.getInstance().getIsDefaultGroupInfoShown()) {
                                LubbleSharedPrefs.getInstance().setShowPvtGroupInfo(true);
                            }
                        }
                    });
        }
    }

    private void resetUnreadCount() {
        if (groupData != null && groupData.isJoined()) {
            RealtimeDbHelper.getUserGroupsRef().child(groupId)
                    .child("unreadCount").setValue(0);
        }
    }

    private ChildEventListener msgListener(@NonNull DatabaseReference messagesReference) {
        return messagesReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildAdded: ");
                final ChatData chatData = dataSnapshot.getValue(ChatData.class);
                if (chatData != null) {
                    Log.d(TAG, "onChildAdded: " + dataSnapshot.getKey());
                    checkAndInsertDate(chatData);
                    chatData.setId(dataSnapshot.getKey());
                    chatAdapter.addChatData(chatData);
                    sendReadReceipt(chatData);
                }
            }

            private void checkAndInsertDate(ChatData chatData) {
                final int lastPos = chatAdapter.getItemCount() - 1;
                ChatData lastMsg = null;
                if (lastPos > -1) {
                    lastMsg = chatAdapter.getChatMsgAt(lastPos);
                }
                if (lastMsg == null || !DateTimeUtils.getDateFromLong(lastMsg.getCreatedTimestamp())
                        .equalsIgnoreCase(DateTimeUtils.getDateFromLong(chatData.getCreatedTimestamp()))) {
                    // different date, insert date divider
                    final ChatData dateChatData = new ChatData();
                    dateChatData.setMessage(DateTimeUtils.getDateFromLong(chatData.getCreatedTimestamp()));
                    dateChatData.setType(SYSTEM);
                    final HashMap<String, Long> readMap = new HashMap<>();
                    readMap.put(FirebaseAuth.getInstance().getUid(), 0L);
                    dateChatData.setReadReceipts(readMap);
                    chatAdapter.addChatData(dateChatData);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildChanged: ");
                final ChatData chatData = dataSnapshot.getValue(ChatData.class);
                if (chatData != null) {
                    chatData.setId(dataSnapshot.getKey());
                    chatAdapter.updateChatData(chatData);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved: ");
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendReadReceipt(ChatData chatData) {
        if (chatData.getReadReceipts().get(FirebaseAuth.getInstance().getUid()) == null) {
            // either GROUP or DM id should be non-null if a msg exists
            getMessagesRef().child(groupId == null ? dmId : groupId).child(chatData.getId())
                    .child("readReceipts")
                    .child(FirebaseAuth.getInstance().getUid())
                    .setValue(System.currentTimeMillis());
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_send_btn:

                final ChatData chatData = new ChatData();
                chatData.setAuthorUid(FirebaseAuth.getInstance().getCurrentUser().getUid());
                chatData.setMessage(newMessageEt.getText().toString());
                chatData.setCreatedTimestamp(System.currentTimeMillis());
                chatData.setServerTimestamp(ServerValue.TIMESTAMP);

                if (isValidString(replyMsgId)) {
                    chatData.setType(REPLY);
                    chatData.setReplyMsgId(replyMsgId);
                } else if (isValidString(linkTitle.getText().toString())) {
                    chatData.setType(LINK);
                    chatData.setLinkTitle(linkTitle.getText().toString());
                    chatData.setLinkDesc(linkDesc.getText().toString());
                }

                if (TextUtils.isEmpty(groupId) && TextUtils.isEmpty(dmId)) {
                    final DatabaseReference pushRef = RealtimeDbHelper.getCreateDmRef().push();

                    final HashMap<String, Object> userMap = new HashMap<>();
                    final HashMap<Object, Object> map2 = new HashMap<>();
                    map2.put("isSeller", true);
                    userMap.put(receiverId, map2);

                    HashMap<String, Object> sellerMap = new HashMap<>();
                    sellerMap.put("isSeller", false);
                    sellerMap.put("otherUser", receiverId);
                    userMap.put(FirebaseAuth.getInstance().getUid(), sellerMap);

                    final HashMap<String, Object> map = new HashMap<>();
                    map.put("members", userMap);
                    map.put("message", chatData);
                    pushRef.setValue(map);
                    dmId = pushRef.getKey();

                    //todo groupReference = getLubbleGroupsRef().child(groupId);
                    messagesReference = getDmMessagesRef().child(dmId);
                    msgChildListener = msgListener(messagesReference);

                } else if (!TextUtils.isEmpty(groupId)) {
                    messagesReference.push().setValue(chatData);
                } else if (!TextUtils.isEmpty(dmId)) {
                    messagesReference.push().setValue(chatData);
                }
                newMessageEt.setText("");
                linkTitle.setText("");
                linkDesc.setText("");
                linkMetaContainer.setVisibility(View.GONE);
                replyMsgId = null;
                break;
            case R.id.iv_attach:
                if (TextUtils.isEmpty(groupId) && TextUtils.isEmpty(dmId)) {
                    Toast.makeText(getContext(), "Please send a text message first", Toast.LENGTH_SHORT).show();
                    break;
                }
                ChatFragmentPermissionsDispatcher
                        .startPhotoPickerWithPermissionCheck(ChatFragment.this, REQUEST_CODE_IMG);
                break;
            case R.id.btn_join:
                getCreateOrJoinGroupRef().child(groupId).setValue(true);
                isJoining = true;
                showJoiningDialog();
                break;
            case R.id.tv_decline:
                RealtimeDbHelper.getUserGroupsRef().child(groupId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        getActivity().finish();
                    }
                });
                break;
            case R.id.iv_link_cancel:
                linkTitle.setText("");
                linkDesc.setText("");
                prevUrl = "";
                linkMetaContainer.setVisibility(View.GONE);
                replyMsgId = null;
                break;
        }
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    public void startPhotoPicker(int REQUEST_CODE) {
        try {
            File cameraPic = createImageFile(getContext());
            currentPhotoPath = cameraPic.getAbsolutePath();
            Intent pickImageIntent = getPickImageIntent(getContext(), cameraPic);
            startActivityForResult(pickImageIntent, REQUEST_CODE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_IMG && resultCode == RESULT_OK) {
            File imageFile;
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                imageFile = getFileFromInputStreamUri(getContext(), uri);
            } else {
                // from camera
                imageFile = new File(currentPhotoPath);
            }

            final Uri fileUri = Uri.fromFile(imageFile);
            AttachImageActivity.open(getContext(), fileUri, groupId);
            /*getContext().startService(new Intent(getContext(), UploadFileService.class)
                    .putExtra(UploadFileService.EXTRA_BUCKET, UploadFileService.BUCKET_CONVO)
                    .putExtra(UploadFileService.EXTRA_FILE_NAME, fileUri.getLastPathSegment())
                    .putExtra(EXTRA_FILE_URI, fileUri)
                    .putExtra(UploadFileService.EXTRA_UPLOAD_PATH, "lubbles/0/groups/0")
                    .setAction(UploadFileService.ACTION_UPLOAD));*/

        }
    }

    private void registerMediaUploadCallback() {
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //todo hideProgressDialog();

                switch (intent.getAction()) {
                    case UploadFileService.UPLOAD_COMPLETED:
                        //todo show pic to user

                        break;
                    case UploadFileService.UPLOAD_ERROR:

                        break;
                }
            }
        }, UploadFileService.getIntentFilter());
    }

    private void setupTogglingOfSendBtn() {
        newMessageEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                sendBtn.setEnabled(editable.length() > 0);
                final String extractedUrl = extractFirstLink(editable.toString());
                if (extractedUrl != null && (!prevUrl.equalsIgnoreCase(extractedUrl))) {
                    prevUrl = extractedUrl;
                    new LinkMetaAsyncTask(prevUrl, getLinkMetaListener())
                            .execute();
                } else if (extractedUrl == null && linkMetaContainer.getVisibility() == View.VISIBLE && !isValidString(replyMsgId)) {
                    linkMetaContainer.setVisibility(View.GONE);
                    prevUrl = "";
                }
            }
        });
    }

    @NonNull
    private LinkMetaListener getLinkMetaListener() {
        return new LinkMetaListener() {
            @Override
            public void onMetaFetched(final String title, final String desc) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        linkMetaContainer.setVisibility(View.VISIBLE);
                        linkTitle.setText(title);
                        linkDesc.setText(desc);
                    }
                });
            }

            @Override
            public void onMetaFailed() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        linkMetaContainer.setVisibility(View.GONE);
                    }
                });
            }
        };
    }

    public void addReplyFor(@NonNull String selectedChatId) {
        linkMetaContainer.setVisibility(View.VISIBLE);
        replyMsgId = selectedChatId;

        messagesReference.child(selectedChatId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final ChatData quotedChatData = dataSnapshot.getValue(ChatData.class);
                RealtimeDbHelper.getUserInfoRef(quotedChatData.getAuthorUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ProfileInfo profileInfo = dataSnapshot.getValue(ProfileInfo.class);
                        linkTitle.setText(profileInfo.getName());
                        String desc = "";
                        if (isValidString(quotedChatData.getImgUrl())) {
                            desc = desc.concat("\uD83D\uDCF7 ");
                            if (!isValidString(quotedChatData.getMessage())) {
                                // add the word photo if there is no caption
                                desc = desc.concat("Photo ");
                            }
                        }
                        desc = desc.concat(quotedChatData.getMessage());
                        linkDesc.setText(desc);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void openGroupInfo() {
        if (groupData != null && (groupData.isJoined() || !groupData.getIsPrivate())) {
            ScrollingGroupInfoActivity.open(getContext(), groupId);
        }
    }

    public void openChatInfo(String chatId, boolean showReadReceipts) {
        if (chatId != null) {
            startActivity(MsgInfoActivity.getIntent(getContext(), groupId, chatId, showReadReceipts));
        } else {
            Crashlytics.logException(new NullPointerException("chatId is null when trying to open msg info"));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        recyclerViewState = chatRecyclerView.getLayoutManager().onSaveInstanceState();
        prevUrl = "";
        if (messagesReference != null) {
            messagesReference.removeEventListener(msgChildListener);
        }
        if (groupReference != null) {
            groupReference.removeEventListener(groupInfoListener);
        }
        if (bottomBarListener != null) {
            RealtimeDbHelper.getUserGroupsRef().child(groupId).removeEventListener(bottomBarListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        ChatFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnShowRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
    void showRationaleForCamera(final PermissionRequest request) {
        showStoragePermRationale(getContext(), request);
    }

    @OnPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE)
    void showDeniedForCamera() {
        Toast.makeText(getContext(), getString(R.string.storage_perm_denied_text), Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain(Manifest.permission.READ_EXTERNAL_STORAGE)
    void showNeverAskForCamera() {
        Toast.makeText(getContext(), R.string.storage_perm_never_text, Toast.LENGTH_LONG).show();
    }
}
