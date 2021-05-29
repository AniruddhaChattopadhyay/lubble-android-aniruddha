package in.lubble.app.feed_user;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.fxn.utility.PermUtil;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;

import in.lubble.app.BaseActivity;
import in.lubble.app.BuildConfig;
import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.models.FeedPostData;
import in.lubble.app.network.LinkMetaAsyncTask;
import in.lubble.app.network.LinkMetaListener;
import in.lubble.app.utils.RoundedCornersTransformation;

import static in.lubble.app.utils.FileUtils.getFileFromInputStreamUri;
import static in.lubble.app.utils.StringUtils.extractFirstLink;
import static in.lubble.app.utils.UiUtils.dpToPx;


public class AddPostForFeed extends BaseActivity {

    private static final int REQ_CODE_GROUPS_SELECT = 853;
    private static final int REQUEST_CODE_MEDIA_ATTACH = 820;

    private Button postSubmitBtn;
    private EditText postText;
    private ImageView dpIv, attachedPicIv, linkImageIv, linkCloseIv;
    private View parentLayout;
    private FeedPostData feedPostData;
    private RelativeLayout linkPreviewContainer;
    private TextView addPhotoToFeedTv, linkTitleTv, linkDescTv;
    private Uri imageUri = null;
    private String uploadPath = "feed_photos/";
    private String prevUrl = "";
    private LinkMetaAsyncTask linkMetaAsyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_post_for_feed);

        Analytics.triggerScreenEvent(this, this.getClass());

        Toolbar toolbar = findViewById(R.id.text_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Post to " + LubbleSharedPrefs.getInstance().getLubbleName());

        parentLayout = findViewById(android.R.id.content);
        postSubmitBtn = findViewById(R.id.post_btn);
        postText = findViewById(R.id.post_edt_txt);
        dpIv = findViewById(R.id.iv_profile_pic);
        attachedPicIv = findViewById(R.id.iv_attached_pic);
        addPhotoToFeedTv = findViewById(R.id.add_photo_feed_tv);
        linkPreviewContainer = findViewById(R.id.cont_link_preview);
        linkImageIv = findViewById(R.id.iv_link_image);
        linkTitleTv = findViewById(R.id.tv_link_title);
        linkDescTv = findViewById(R.id.tv_link_desc);
        linkCloseIv = findViewById(R.id.iv_link_close);
        linkCloseIv.setVisibility(View.VISIBLE);

        feedPostData = new FeedPostData();
        addTextChangeListener();

        postSubmitBtn.setOnClickListener(v -> {
            if (postText.getText().toString().trim().length() > 0) {
                feedPostData.setText(postText.getText().toString());
                if (imageUri != null) {
                    feedPostData.setImgUri(imageUri.toString());
                }
                openGroupSelectionActivity(feedPostData);

                Analytics.triggerEvent(AnalyticsEvents.FEED_POST_COMPOSED, this);
            } else {
                Snackbar.make(parentLayout, "Can't publish an empty post", Snackbar.LENGTH_SHORT).show();
            }
        });

        GlideApp.with(this)
                .load(LubbleSharedPrefs.getInstance().getProfilePicUrl())
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

        linkCloseIv.setOnClickListener(v -> resetLinkPreview());
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
                if (s.length() > 0 && s.toString().trim().length() > 0) {
                    if (!postSubmitBtn.isEnabled()) {
                        postSubmitBtn.setEnabled(true);
                    }
                    final String inputString = s.toString();
                    final String extractedUrl = extractFirstLink(inputString);
                    if (extractedUrl != null && !prevUrl.equalsIgnoreCase(extractedUrl)) {
                        prevUrl = extractedUrl;
                        linkMetaAsyncTask = new LinkMetaAsyncTask(prevUrl, getLinkMetaListener());
                        linkMetaAsyncTask.execute();
                    } else if (extractedUrl == null && linkPreviewContainer.getVisibility() == View.VISIBLE) {
                        resetLinkPreview();
                    }
                }
            }
        });
    }

    public void resetLinkPreview() {
        linkPreviewContainer.setVisibility(View.GONE);
        prevUrl = "";
        linkTitleTv.setText("");
        linkDescTv.setText("");
        feedPostData.setLinkTitle("");
        feedPostData.setLinkDesc("");
        feedPostData.setLinkImageUrl("");
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

                addPhotoToFeedTv.setText("Change Photo");
                imageUri = uri;

                feedPostData.setImgUri(uri.toString());

                attachedPicIv.setVisibility(View.VISIBLE);
                GlideApp.with(this)
                        .load(uri)
                        .transform(new RoundedCornersTransformation(dpToPx(8), 0))
                        .into(attachedPicIv);
            } else {
                Toast.makeText(this, R.string.all_something_wrong_try_again, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private LinkMetaListener getLinkMetaListener() {
        return new LinkMetaListener() {
            @Override
            public void onMetaFetched(final String title, final String desc, final String imgUrl) {
                if (!isFinishing()) {
                    runOnUiThread(() -> {
                        linkPreviewContainer.setVisibility(View.VISIBLE);
                        linkTitleTv.setText(title);
                        linkDescTv.setText(desc);
                        feedPostData.setLinkTitle(title);
                        feedPostData.setLinkDesc(desc);
                        feedPostData.setLinkUrl(prevUrl);
                        if (!TextUtils.isEmpty(imgUrl)) {
                            feedPostData.setLinkImageUrl(imgUrl);
                            GlideApp.with(AddPostForFeed.this)
                                    .load(imgUrl)
                                    .placeholder(R.drawable.ic_public_black_24dp)
                                    .error(R.drawable.ic_public_black_24dp)
                                    .into(linkImageIv);
                        } else {
                            linkImageIv.setImageResource(R.drawable.ic_public_black_24dp);
                        }
                    });
                }
            }

            @Override
            public void onMetaFailed() {
                if (!isFinishing()) {
                    runOnUiThread(() -> linkPreviewContainer.setVisibility(View.GONE));
                }
            }
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Pix.start(this, Options.init().setRequestCode(100));
                } else {
                    Toast.makeText(this, "Approve permissions to open ImagePicker", Toast.LENGTH_LONG).show();
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