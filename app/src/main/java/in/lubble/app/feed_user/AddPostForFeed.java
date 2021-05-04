package in.lubble.app.feed_user;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileUtils;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.util.ArrayList;

import in.lubble.app.BaseActivity;
import in.lubble.app.BuildConfig;
import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.UploadFileService;
import in.lubble.app.UploadImageFeedService;
import in.lubble.app.chat.AttachImageActivity;
import in.lubble.app.chat.ChatFragment;
import in.lubble.app.models.FeedPostData;

import static in.lubble.app.UploadFileService.EXTRA_FILE_URI;
import static in.lubble.app.utils.FileUtils.getFileFromInputStreamUri;


public class AddPostForFeed extends BaseActivity {

    private static final int REQ_CODE_GROUPS_SELECT = 853;

    private Button postSubmitBtn;
    private EditText postText;
    private ImageView dpIv;
    private View parentLayout;
    private TextView addPhotoToFeedTv;
    private static final int REQUEST_CODE_MEDIA_ATTACH = 100;
    private Uri imageUri = null;
    private String uploadPath = "feed_photos/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_post_for_feed);

        Toolbar toolbar = findViewById(R.id.text_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Post to " + LubbleSharedPrefs.getInstance().getLubbleName());

        parentLayout = findViewById(android.R.id.content);
        postSubmitBtn = findViewById(R.id.post_btn);
        postText = findViewById(R.id.post_edt_txt);
        dpIv = findViewById(R.id.iv_profile_pic);
        addPhotoToFeedTv = findViewById(R.id.add_photo_feed_tv);

        addTextChangeListener();

        postSubmitBtn.setOnClickListener(v -> {
            if (postText.getText().toString().trim().length() > 0) {
                //todo add check for img path
                FeedPostData feedPostData = new FeedPostData();
                feedPostData.setText(postText.getText().toString());
                if (imageUri!=null){
                    feedPostData.setImgUri(imageUri.toString());
                }
                openGroupSelectionActivity(feedPostData);
            } else {
                Snackbar.make(parentLayout, "Can't publish an empty post", Snackbar.LENGTH_SHORT).show();
            }
        });

        GlideApp.with(this)
                .load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl())
                .placeholder(R.drawable.ic_account_circle_black_no_padding)
                .error(R.drawable.ic_account_circle_black_no_padding)
                .circleCrop()
                .into(dpIv);

        postText.requestFocus();

        addPhotoToFeedTv.setOnClickListener(v -> {
            Pix.start(this, Options.init().setRequestCode(REQUEST_CODE_MEDIA_ATTACH));
        });
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(postText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void addTextChangeListener() {
        postText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0 && s.toString().trim().length() > 0 && !postSubmitBtn.isEnabled()) {
                    postSubmitBtn.setEnabled(true);
                }
            }
        });
    }

    private void openGroupSelectionActivity(FeedPostData feedPostData) {
        Intent groupSelectionActivIntent = GroupSelectionActiv.getIntent(this, feedPostData);
        startActivityForResult(groupSelectionActivIntent, REQ_CODE_GROUPS_SELECT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_GROUPS_SELECT && resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_MEDIA_ATTACH) {
            ArrayList<String> returnValue = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
            if (returnValue != null && !returnValue.isEmpty()) {
                Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", new File(returnValue.get(0)));
                File imageFile = getFileFromInputStreamUri(this, uri);
                uri = Uri.fromFile(imageFile);
                addPhotoToFeedTv.setText("Photo added");
                imageUri = uri;
            }
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.none, R.anim.slide_to_bottom_fast);
    }

}