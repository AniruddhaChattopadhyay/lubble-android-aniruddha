package in.lubble.app.feed_post;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;

import in.lubble.app.BaseActivity;
import in.lubble.app.R;

public class FeedPostActivity extends BaseActivity {

    private static final String ARG_POST_ID = "LBL_ARG_POST_ID";

    public static void open(Context context, String postId) {
        Intent intent = new Intent(context, FeedPostActivity.class);
        intent.putExtra(ARG_POST_ID, postId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed_post_activity);

        Toolbar toolbar = findViewById(R.id.text_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Post");

        String postId = getIntent().getStringExtra(ARG_POST_ID);
        if (postId != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, FeedPostFrag.newInstance(postId))
                    .commitNow();
        } else {
            throw new IllegalArgumentException("Missing ARG_POST_ID for FeedPostActivity");
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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