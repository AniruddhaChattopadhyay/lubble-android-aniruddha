package in.lubble.app.chat;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import java.util.ArrayList;
import java.util.HashMap;

import in.lubble.app.Constants;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.UploadFileService;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.groups.group_info.GroupInfoActivity;
import in.lubble.app.models.ChatData;
import in.lubble.app.models.GroupData;
import in.lubble.app.models.ProfileInfo;
import in.lubble.app.models.UserGroupData;
import in.lubble.app.network.LinkMetaAsyncTask;
import in.lubble.app.network.LinkMetaListener;
import in.lubble.app.utils.AppNotifUtils;
import in.lubble.app.utils.DateTimeUtils;

import static android.app.Activity.RESULT_OK;
import static in.lubble.app.firebase.RealtimeDbHelper.getCreateOrJoinGroupRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getLubbleGroupsRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getMessagesRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getUserInfoRef;
import static in.lubble.app.models.ChatData.LINK;
import static in.lubble.app.models.ChatData.SYSTEM;
import static in.lubble.app.models.ChatData.UNREAD;
import static in.lubble.app.utils.FileUtils.createImageFile;
import static in.lubble.app.utils.FileUtils.getFileFromInputStreamUri;
import static in.lubble.app.utils.FileUtils.getPickImageIntent;
import static in.lubble.app.utils.NotifUtils.deleteUnreadMsgsForGroupId;
import static in.lubble.app.utils.StringUtils.extractFirstLink;
import static in.lubble.app.utils.StringUtils.isValidString;

public class ChatFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "ChatFragment";
    private static final int REQUEST_CODE_IMG = 789;
    private static final String KEY_GROUP_ID = "CHAT_GROUP_ID";
    private static final String KEY_IS_JOINING = "KEY_IS_JOINING";

    @Nullable
    private GroupData groupData;
    private RelativeLayout joinContainer;
    private TextView joinDescTv;
    private Button joinBtn;
    private TextView declineTv;
    private CardView composeCardView;
    private Group linkMetaContainer;
    private RecyclerView chatRecyclerView;
    private EditText newMessageEt;
    private ImageView sendBtn;
    private ImageView attachMediaBtn;
    private TextView linkTitle;
    private TextView linkDesc;
    private DatabaseReference groupReference;
    private DatabaseReference messagesReference;
    private String currentPhotoPath;
    private String groupId;
    private boolean isJoining;
    private ChildEventListener msgChildListener;
    private ValueEventListener groupInfoListener;
    private HashMap<String, ProfileInfo> groupMembersMap;
    private String prevUrl = "";
    private boolean foundFirstUnreadMsg;
    private RelativeLayout bottomContainer;
    private View pvtSystemMsg;
    private ProgressDialog joiningProgressDialog;
    private ValueEventListener bottomBarListener;

    public ChatFragment() {
        // Required empty public constructor
    }

    public static ChatFragment newInstance(String groupId, boolean isJoining) {

        Bundle args = new Bundle();
        args.putString(KEY_GROUP_ID, groupId);
        args.putBoolean(KEY_IS_JOINING, isJoining);
        ChatFragment fragment = new ChatFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        groupId = getArguments().getString(KEY_GROUP_ID);
        isJoining = getArguments().getBoolean(KEY_IS_JOINING);

        groupReference = getLubbleGroupsRef().child(groupId);
        messagesReference = getMessagesRef().child(groupId);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        registerMediaUploadCallback();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_chat, container, false);

        composeCardView = view.findViewById(R.id.compose_container);
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

        showPublicGroupWarning();

        return view;
    }

    private void showJoiningDialog() {
        joiningProgressDialog = new ProgressDialog(getContext());
        joiningProgressDialog.setTitle("Joining group");
        joiningProgressDialog.setMessage("Please Wait...");
        joiningProgressDialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();

        syncGroupInfo();
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        chatRecyclerView.setLayoutManager(layoutManager);
        final ChatAdapter chatAdapter = new ChatAdapter(
                getActivity(),
                getContext(),
                new ArrayList<ChatData>(),
                chatRecyclerView,
                this);
        chatRecyclerView.setAdapter(chatAdapter);
        msgChildListener = msgListener(chatAdapter);

        deleteUnreadMsgsForGroupId(groupId, getContext());
        AppNotifUtils.deleteAppNotif(getContext(), groupId);
        foundFirstUnreadMsg = false;
        chatAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int msgCount = chatAdapter.getItemCount();
                int lastVisiblePosition =
                        layoutManager.findLastCompletelyVisibleItemPosition();
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
    }

    private void syncGroupInfo() {
        groupInfoListener = groupReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                groupData = dataSnapshot.getValue(GroupData.class);
                // fetchMembersProfile(groupData.getMembers()); to be used for tagging
                if (groupData != null) {
                    if (!groupData.isJoined() && groupData.getIsPrivate()) {
                        chatRecyclerView.setVisibility(View.GONE);
                        pvtSystemMsg.setVisibility(View.VISIBLE);
                        ((TextView) pvtSystemMsg.findViewById(R.id.tv_system_msg)).setText("Messages are hidden in private groups until you join");
                    } else {
                        chatRecyclerView.setVisibility(View.VISIBLE);
                        pvtSystemMsg.setVisibility(View.GONE);
                    }
                    ((ChatActivity) getActivity()).setGroupMeta(groupData.getTitle(), groupData.getThumbnail());
                    resetUnreadCount();
                    showBottomBar(groupData);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
                    composeCardView.setVisibility(View.VISIBLE);
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
                                final ProfileInfo profileInfo = dataSnapshot.getValue(ProfileInfo.class);
                                joinDescTv.setText("Invited by " + profileInfo.getName());
                                declineTv.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        composeCardView.setVisibility(View.GONE);
                        joinContainer.setVisibility(View.VISIBLE);
                    } else {
                        joinDescTv.setText("Join group to send messages");
                        declineTv.setVisibility(View.GONE);
                        composeCardView.setVisibility(View.GONE);
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
        if (!LubbleSharedPrefs.getInstance().getIsPublicGroupInfoShown() && groupId.equalsIgnoreCase(Constants.DEFAULT_GROUP)) {
            final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity());
            View sheetView = getActivity().getLayoutInflater().inflate(R.layout.bottom_sheet_info, null);
            bottomSheetDialog.setContentView(sheetView);
            bottomSheetDialog.setCancelable(false);
            bottomSheetDialog.setCanceledOnTouchOutside(false);
            bottomSheetDialog.show();

            final TextView gotItTv = sheetView.findViewById(R.id.tv_got_it);
            gotItTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LubbleSharedPrefs.getInstance().setIsPublicGroupInfoShown(true);
                    bottomSheetDialog.dismiss();
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

    private ChildEventListener msgListener(final ChatAdapter chatAdapter) {
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
            getMessagesRef().child(groupId).child(chatData.getId())
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

                if (isValidString(linkTitle.getText().toString())) {
                    chatData.setType(LINK);
                    chatData.setLinkTitle(linkTitle.getText().toString());
                    chatData.setLinkDesc(linkDesc.getText().toString());
                }

                messagesReference.push().setValue(chatData);

                newMessageEt.setText("");
                break;
            case R.id.iv_attach:
                startPhotoPicker(REQUEST_CODE_IMG);
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
        }
    }

    private void startPhotoPicker(int REQUEST_CODE) {
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
                } else if (extractedUrl == null && linkMetaContainer.getVisibility() == View.VISIBLE) {
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

        messagesReference.child(selectedChatId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final ChatData quotedChatData = dataSnapshot.getValue(ChatData.class);
                linkTitle.setText("Reply");
                linkDesc.setText(quotedChatData.getMessage());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void openGroupInfo() {
        if (groupData.isJoined() || !groupData.getIsPrivate()) {
            GroupInfoActivity.newInstance(getContext(), groupId);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        prevUrl = "";
        messagesReference.removeEventListener(msgChildListener);
        groupReference.removeEventListener(groupInfoListener);
        RealtimeDbHelper.getUserGroupsRef().child(groupId).removeEventListener(bottomBarListener);
    }

}
