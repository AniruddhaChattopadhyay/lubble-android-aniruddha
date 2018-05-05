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
    // if we need to show joining progress dialog
    public static final String EXTRA_IS_JOINING = "chat_activ_is_joining";
    private ImageView toolbarIcon;
    private TextView toolbarTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.icon_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbarIcon = toolbar.findViewById(R.id.iv_toolbar);
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
        final boolean isJoining = getIntent().getBooleanExtra(EXTRA_IS_JOINING, false);

        if (groupId == null) {
            throw new RuntimeException("No Group ID passed");
        }

        final ChatFragment targetFrag = ChatFragment.newInstance(groupId, isJoining);
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

    public void setGroupMeta(String title, String thumbnailUrl) {
        toolbarTv.setText(title);
        if (StringUtils.isValidString(thumbnailUrl)) {
            GlideApp.with(this).load(thumbnailUrl).circleCrop().into(toolbarIcon);
        }
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
