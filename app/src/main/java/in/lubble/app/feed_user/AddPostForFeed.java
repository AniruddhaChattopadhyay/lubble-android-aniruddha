package in.lubble.app.feed_user;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;

import in.lubble.app.BaseActivity;
import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.models.FeedPostData;

public class AddPostForFeed extends BaseActivity {

    private Button postSubmitBtn;
    private EditText postText;
    private ImageView dpIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_post_for_feed);

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
        groupSelectionActivIntent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        startActivity(groupSelectionActivIntent);
        finish();
    }

}