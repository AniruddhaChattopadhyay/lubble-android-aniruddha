package in.lubble.app.chat;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
    private ImageView toolbarIcon;
    private ImageView toolbarLockIcon;
    private TextView toolbarTv;

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
        if (!LubbleSharedPrefs.getInstance().getIsGroupInfoOpened()) {
            toolbarInviteHint.setVisibility(View.VISIBLE);
            toolbarInviteHint.setHorizontallyScrolling(true);
            toolbarInviteHint.setSelected(true);
        } else {
            toolbarInviteHint.setVisibility(View.GONE);
        }
        toolbarIcon.setImageResource(R.drawable.ic_circle_group_24dp);

        final String groupId = getIntent().getStringExtra(EXTRA_GROUP_ID);
        final String msgId = getIntent().getStringExtra(EXTRA_MSG_ID);
        final boolean isJoining = getIntent().getBooleanExtra(EXTRA_IS_JOINING, false);
        final String dmId = getIntent().getStringExtra(EXTRA_DM_ID);

        final ChatFragment targetFrag = ChatFragment.newInstance(
                groupId, isJoining, msgId, dmId,
                getIntent().getStringExtra(EXTRA_RECEIVER_ID),
                getIntent().getStringExtra(EXTRA_RECEIVER_NAME),
                getIntent().getStringExtra(EXTRA_RECEIVER_DP_URL)
        );
        replaceFrag(getSupportFragmentManager(), targetFrag, R.id.frame_fragContainer);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                targetFrag.openGroupInfo();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        LubbleSharedPrefs.getInstance().setCurrentActiveGroupId(getIntent().getStringExtra(EXTRA_GROUP_ID));
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
