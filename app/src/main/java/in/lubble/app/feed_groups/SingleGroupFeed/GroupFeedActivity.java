package in.lubble.app.feed_groups.SingleGroupFeed;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.MissingFormatArgumentException;

import in.lubble.app.BaseActivity;
import in.lubble.app.R;

public class GroupFeedActivity extends BaseActivity {

    private static final String EXTRA_FEED_NAME = "LBL_EXTRA_FEED_NAME";

    public static void open(Context context, String groupName) {
        Intent intent = new Intent(context, GroupFeedActivity.class);
        intent.putExtra(EXTRA_FEED_NAME, groupName);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_feed);
        if (savedInstanceState == null) {
            if (getIntent().hasExtra(EXTRA_FEED_NAME)) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, SingleGroupFeed.newInstance(getIntent().getStringExtra(EXTRA_FEED_NAME)))
                        .commitNow();
            } else {
                throw new MissingFormatArgumentException("no EXTRA_FEED_NAME passed while opening GroupFeedActivity");
            }
        }
    }

}