package in.lubble.app.feed_user;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;

import in.lubble.app.BaseActivity;
import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.models.FeedPostData;

public class AddPostForFeed extends BaseActivity {

    private static final int REQ_CODE_GROUPS_SELECT = 853;

    private Button postSubmitBtn;
    private EditText postText;
    private ImageView dpIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_post_for_feed);

        Toolbar toolbar = findViewById(R.id.text_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Post to " + LubbleSharedPrefs.getInstance().getLubbleName());

        postSubmitBtn = findViewById(R.id.post_btn);
        postText = findViewById(R.id.post_edt_txt);
        dpIv = findViewById(R.id.iv_profile_pic);

        postSubmitBtn.setOnClickListener(v -> {
            FeedPostData feedPostData = new FeedPostData();
            feedPostData.setText(postText.getText().toString());

            openGroupSelectionActivity(feedPostData);
        });

        GlideApp.with(this)
                .load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl())
                .placeholder(R.drawable.ic_account_circle_black_no_padding)
                .error(R.drawable.ic_account_circle_black_no_padding)
                .circleCrop()
                .into(dpIv);
    }

    private void openGroupSelectionActivity(FeedPostData feedPostData) {
        Intent groupSelectionActivIntent = GroupSelectionActiv.getIntent(this, feedPostData);
        //groupSelectionActivIntent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        startActivityForResult(groupSelectionActivIntent, REQ_CODE_GROUPS_SELECT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_GROUPS_SELECT && resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        }
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