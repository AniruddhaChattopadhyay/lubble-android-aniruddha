package in.lubble.app.feed_post;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

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
        if (savedInstanceState == null) {

            String postId = getIntent().getStringExtra(ARG_POST_ID);
            if (postId != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, FeedPostFrag.newInstance(postId))
                        .commitNow();
            } else {
                throw new IllegalArgumentException("Missing ARG_POST_ID for FeedPostActivity");
            }
        }
    }
}