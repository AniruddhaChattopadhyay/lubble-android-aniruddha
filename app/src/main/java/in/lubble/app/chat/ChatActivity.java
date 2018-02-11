package in.lubble.app.chat;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.utils.StringUtils;

import static in.lubble.app.utils.FragUtils.replaceFrag;

public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_GROUP_ID = "chat_activ_group_id";
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

        if (groupId == null) {
            throw new RuntimeException("No Group ID passed");
        }

        replaceFrag(getSupportFragmentManager(), ChatFragment.newInstance(groupId), R.id.frame_fragContainer);
    }

    public void setGroupMeta(String title, String thumbnailUrl) {
        toolbarTv.setText(title);
        if (StringUtils.isValidString(thumbnailUrl)) {
            GlideApp.with(this).load(thumbnailUrl).circleCrop().into(toolbarIcon);
        }
    }
}
