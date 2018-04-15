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
import in.lubble.app.groups.group_info.GroupInfoActivity;
import in.lubble.app.utils.StringUtils;

import static in.lubble.app.utils.FragUtils.replaceFrag;

public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_GROUP_ID = "chat_activ_group_id";
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
        toolbarTv = toolbar.findViewById(R.id.tv_toolbar_title);
        setTitle("");
        toolbarIcon.setImageResource(R.drawable.ic_account_circle_black_no_padding);

        final String groupId = getIntent().getStringExtra(EXTRA_GROUP_ID);
        final boolean isJoining = getIntent().getBooleanExtra(EXTRA_IS_JOINING, false);

        if (groupId == null) {
            throw new RuntimeException("No Group ID passed");
        }

        replaceFrag(getSupportFragmentManager(), ChatFragment.newInstance(groupId, isJoining), R.id.frame_fragContainer);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroupInfoActivity.newInstance(ChatActivity.this, groupId);
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
