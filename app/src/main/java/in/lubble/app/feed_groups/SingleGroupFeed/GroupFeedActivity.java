package in.lubble.app.feed_groups.SingleGroupFeed;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;

import java.util.MissingFormatArgumentException;

import in.lubble.app.BaseActivity;
import in.lubble.app.R;
import in.lubble.app.models.FeedGroupData;

public class GroupFeedActivity extends BaseActivity {

    private static final String EXTRA_FEED_GROUP_DATA = "LBL_EXTRA_FEED_GROUP_DATA";

    public static void open(Context context, FeedGroupData feedGroupData) {
        Intent intent = new Intent(context, GroupFeedActivity.class);
        intent.putExtra(EXTRA_FEED_GROUP_DATA, feedGroupData);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_feed);

        Toolbar toolbar = findViewById(R.id.text_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent().hasExtra(EXTRA_FEED_GROUP_DATA)) {
            FeedGroupData feedGroupData = (FeedGroupData) getIntent().getSerializableExtra(EXTRA_FEED_GROUP_DATA);
            setTitle(feedGroupData.getName());
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, SingleGroupFeed.newInstance(feedGroupData.getFeedName()))
                    .commitNow();
        } else {
            throw new MissingFormatArgumentException("no EXTRA_FEED_NAME passed while opening GroupFeedActivity");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Respond to the action bar's Up/Home button
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}