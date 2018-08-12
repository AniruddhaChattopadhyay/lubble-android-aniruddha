package in.lubble.app.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.utils.StringUtils;

import static in.lubble.app.utils.FragUtils.replaceFrag;

public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_GROUP_ID = "chat_activ_group_id";
    public static final String EXTRA_MSG_ID = "chat_activ_msg_id";
    // if we need to show joining progress dialog
    public static final String EXTRA_IS_JOINING = "chat_activ_is_joining";
    // for when DM exists
    public static final String EXTRA_DM_ID = "EXTRA_DM_ID";
    // for new DMs
    public static final String EXTRA_RECEIVER_ID = "EXTRA_RECEIVER_ID";
    public static final String EXTRA_RECEIVER_NAME = "EXTRA_RECEIVER_NAME";
    public static final String EXTRA_RECEIVER_DP_URL = "EXTRA_RECEIVER_DP_URL";
    public static final String EXTRA_ITEM_TITLE = "EXTRA_ITEM_TITLE";
    private ImageView toolbarIcon;
    private ImageView toolbarLockIcon;
    private TextView toolbarTv;
    private ChatFragment targetFrag = null;

    public static void openForGroup(@NonNull Context context, @NonNull String groupId, boolean isJoining, @Nullable String msgId) {
        final Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(EXTRA_GROUP_ID, groupId);
        intent.putExtra(EXTRA_IS_JOINING, isJoining);
        intent.putExtra(EXTRA_MSG_ID, msgId);
        context.startActivity(intent);
    }

    public static void openForDm(@NonNull Context context, @NonNull String dmId, @Nullable String msgId) {
        final Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(EXTRA_DM_ID, dmId);
        intent.putExtra(EXTRA_MSG_ID, msgId);
        context.startActivity(intent);
    }

    public static void openForEmptyDm(@NonNull Context context, @Nullable String receiverId, @Nullable String receiverName, @Nullable String receiverDpUrl,
                                      @Nullable String itemTitle) {
        final Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(EXTRA_RECEIVER_ID, receiverId);
        intent.putExtra(EXTRA_RECEIVER_NAME, receiverName);
        intent.putExtra(EXTRA_RECEIVER_DP_URL, receiverDpUrl);
        intent.putExtra(EXTRA_ITEM_TITLE, itemTitle);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.icon_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbarIcon = toolbar.findViewById(R.id.iv_toolbar);
        toolbarLockIcon = toolbar.findViewById(R.id.iv_lock_icon);
        TextView toolbarInviteHint = toolbar.findViewById(R.id.tv_invite_hint);
        toolbarTv = toolbar.findViewById(R.id.tv_toolbar_title);
        setTitle("");

        toolbarIcon.setImageResource(R.drawable.ic_circle_group_24dp);

        final String groupId = getIntent().getStringExtra(EXTRA_GROUP_ID);
        final String msgId = getIntent().getStringExtra(EXTRA_MSG_ID);
        final boolean isJoining = getIntent().getBooleanExtra(EXTRA_IS_JOINING, false);
        final String dmId = getIntent().getStringExtra(EXTRA_DM_ID);

        if (!LubbleSharedPrefs.getInstance().getIsGroupInfoOpened() && !TextUtils.isEmpty(groupId)) {
            toolbarInviteHint.setVisibility(View.VISIBLE);
            toolbarInviteHint.setHorizontallyScrolling(true);
            toolbarInviteHint.setSelected(true);
        } else {
            toolbarInviteHint.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(groupId)) {
            targetFrag = ChatFragment.newInstanceForGroup(
                    groupId, isJoining, msgId
            );
        } else if (!TextUtils.isEmpty(dmId)) {
            targetFrag = ChatFragment.newInstanceForDm(
                    dmId, msgId
            );
        } else if (getIntent().hasExtra(EXTRA_RECEIVER_ID) && getIntent().hasExtra(EXTRA_RECEIVER_NAME)) {
            targetFrag = ChatFragment.newInstanceForEmptyDm(
                    getIntent().getStringExtra(EXTRA_RECEIVER_ID),
                    getIntent().getStringExtra(EXTRA_RECEIVER_NAME),
                    getIntent().getStringExtra(EXTRA_RECEIVER_DP_URL),
                    getIntent().getStringExtra(EXTRA_ITEM_TITLE)
            );
        } else {
            throw new RuntimeException("Invalid Args, see the valid factory methods by searching for this error string");
        }

        replaceFrag(getSupportFragmentManager(), targetFrag, R.id.frame_fragContainer);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (targetFrag != null) {
                    targetFrag.openGroupInfo();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent().hasExtra(EXTRA_GROUP_ID)) {
            LubbleSharedPrefs.getInstance().setCurrentActiveGroupId(getIntent().getStringExtra(EXTRA_GROUP_ID));
        } else if (getIntent().hasExtra(EXTRA_DM_ID)) {
            LubbleSharedPrefs.getInstance().setCurrentActiveGroupId(getIntent().getStringExtra(EXTRA_DM_ID));
        }
    }

    public void setGroupMeta(String title, String thumbnailUrl, boolean isPrivate) {
        toolbarTv.setText(title);
        if (StringUtils.isValidString(thumbnailUrl)) {
            GlideApp.with(this).load(thumbnailUrl).circleCrop().into(toolbarIcon);
        }
        toolbarLockIcon.setVisibility(isPrivate ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LubbleSharedPrefs.getInstance().setCurrentActiveGroupId("");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
