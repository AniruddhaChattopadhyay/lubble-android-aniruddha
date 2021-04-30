package in.lubble.app.feed_user;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.fxn.utility.PermUtil;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.util.ArrayList;

import in.lubble.app.BaseActivity;
import in.lubble.app.BuildConfig;
import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.models.FeedPostData;
import in.lubble.app.utils.RoundedCornersTransformation;

import static in.lubble.app.utils.FileUtils.getFileFromInputStreamUri;
import static in.lubble.app.utils.UiUtils.dpToPx;

public class AddPostForFeed extends BaseActivity {

    private static final int REQ_CODE_GROUPS_SELECT = 853;
    private static final int REQUEST_CODE_MEDIA_ATTACH = 820;

    private Button postSubmitBtn;
    private EditText postText;
    private ImageView dpIv, attachedPicIv;
    private View parentLayout;
    private TextView addPicTv;
    private FeedPostData feedPostData;

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
        attachedPicIv = findViewById(R.id.iv_attached_pic);
        addPicTv = findViewById(R.id.tv_add_pic);

        feedPostData = new FeedPostData();
        addTextChangeListener();

        postSubmitBtn.setOnClickListener(v -> {
            if (postText.getText().toString().trim().length() > 0) {
                //todo add check for img path
                feedPostData.setText(postText.getText().toString());

                openGroupSelectionActivity(feedPostData);
            } else {
                Snackbar.make(parentLayout, "Can't publish an empty post", Snackbar.LENGTH_SHORT).show();
            }
        });

        addPicTv.setOnClickListener(v -> Pix.start(AddPostForFeed.this, Options.init().setRequestCode(REQUEST_CODE_MEDIA_ATTACH)));

        GlideApp.with(this)
                .load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl())
                .placeholder(R.drawable.ic_account_circle_black_no_padding)
                .error(R.drawable.ic_account_circle_black_no_padding)
                .circleCrop()
                .into(dpIv);

        postText.requestFocus();
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

                feedPostData.setAttachedImgUri(uri.toString());

                GlideApp.with(this)
                        .load(uri)
                        .transform(new RoundedCornersTransformation(dpToPx(8), 0))
                        .into(attachedPicIv);
            } else {
                Toast.makeText(this, R.string.all_something_wrong_try_again, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Pix.start(this, Options.init().setRequestCode(100));
                } else {
                    Toast.makeText(this, "Approve permissions to open Pix ImagePicker", Toast.LENGTH_LONG).show();
                }
                return;
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