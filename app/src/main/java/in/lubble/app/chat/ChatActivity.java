package in.lubble.app.chat;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import in.lubble.app.R;

import static in.lubble.app.utils.FragUtils.replaceFrag;

public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_GROUP_ID = "chat_activ_group_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.lubble_toolbar);
        setSupportActionBar(toolbar);

        final String groupId = getIntent().getStringExtra(EXTRA_GROUP_ID);

        if (groupId == null) {
            throw new RuntimeException("No Group ID passed");
        }

        replaceFrag(getSupportFragmentManager(), ChatFragment.newInstance(groupId), R.id.frame_fragContainer);
    }
}
