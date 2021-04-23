package in.lubble.app.feed_user;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.MissingFormatArgumentException;

import in.lubble.app.BaseActivity;
import in.lubble.app.R;
import in.lubble.app.models.FeedPostData;

public class GroupSelectionActiv extends BaseActivity {

    private static final String EXTRA_FEED_POST_DATA = "LBL_EXTRA_FEED_POST_DATA";

    public static Intent getIntent(Context context, FeedPostData feedPostData) {
        Intent intent = new Intent(context, GroupSelectionActiv.class);
        intent.putExtra(EXTRA_FEED_POST_DATA, feedPostData);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_selection);
        if (savedInstanceState == null) {
            if (getIntent().hasExtra(EXTRA_FEED_POST_DATA)) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, GroupSelectionFrag.newInstance((FeedPostData) getIntent().getSerializableExtra(EXTRA_FEED_POST_DATA)))
                        .commitNow();
            } else {
                throw new MissingFormatArgumentException("no FeedPostData passed while opening GroupSelectionActiv");
            }
        }
    }
}