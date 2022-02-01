package in.lubble.app.feed_user;

import static in.lubble.app.utils.FileUtils.Video_Size;
import static in.lubble.app.utils.FileUtils.getFileFromInputStreamUri;
import static in.lubble.app.utils.FileUtils.getMimeType;
import static in.lubble.app.utils.StringUtils.extractFirstLink;
import static in.lubble.app.utils.UiUtils.dpToPx;

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
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

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
import in.lubble.app.utils.FileUtils;
import in.lubble.app.utils.RoundedCornersTransformation;


public class AddPostForFeed extends BaseActivity {

    private static final int REQ_CODE_GROUPS_SELECT = 853;
    private static final int REQUEST_CODE_MEDIA_ATTACH = 820;
    public static final String ARG_POST_TYPE = "ARG_FEED_POST_TYPE";
    public static final String TYPE_QNA = "TYPE_QNA";
    public static final String TYPE_INTRO = "TYPE_INTRO";

    private Button postSubmitBtn;
    private EditText postText;
    private ImageView dpIv, attachedPicIv, linkImageIv, linkCloseIv;
    private View parentLayout;
    private FeedPostData feedPostData;
    private MaterialCardView introMcv;
    private RelativeLayout linkPreviewContainer;
    private TextView addPhotoToFeedTv, linkTitleTv, linkDescTv, introTitleTv, introSubtitleTv;
    private String uploadPath = "feed_photos/";
    private String prevUrl = "";
    private LinkMetaAsyncTask linkMetaAsyncTask;
    private boolean isLinkPreviewClosedByUser;
    private boolean isQandA, isIntro;
    private PlayerView exoPlayerView;
    private SimpleExoPlayer exoPlayer;
    private static final int PERMITTED_VIDEO_SIZE = 30;
    private String videoLink = null;
    private String photoLink = null;

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
        introMcv = findViewById(R.id.mcv_intro);
        introTitleTv = findViewById(R.id.tv_intro_title);
        introSubtitleTv = findViewById(R.id.tv_intro_subtitle);
        exoPlayerView = findViewById(R.id.exo_player_add_feed_post);
        linkCloseIv.setVisibility(View.VISIBLE);

        feedPostData = new FeedPostData();
        String stringExtra = getIntent().getStringExtra(ARG_POST_TYPE);
        if (TYPE_QNA.equalsIgnoreCase(stringExtra)) {
            isQandA = true;
        } else if (TYPE_INTRO.equalsIgnoreCase(stringExtra)) {
            isIntro = true;
        }
        addTextChangeListener();
        postSubmitBtn.setOnClickListener(v -> {
            if (postText.getText().toString().trim().length() > 0) {
                feedPostData.setText(postText.getText().toString());
                if (photoLink != null) {
                    feedPostData.setImgUri(photoLink);
                }
                if (videoLink != null) {
                    feedPostData.setVidUri(videoLink);
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

        linkCloseIv.setOnClickListener(v -> {
            isLinkPreviewClosedByUser = true;
            resetLinkPreview();
        });

        if (isQandA) {
            Editable edit = postText.getText();
            postText.setText("?");
            postText.setSelection(0);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        showIntroCard();
    }

    private void showIntroCard() {
        if (isIntro) {
            introMcv.setVisibility(View.VISIBLE);
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            String name = (currentUser != null && currentUser.getDisplayName() != null) ? currentUser.getDisplayName().split(" ")[0] : "Neighbour";
            introTitleTv.setText(String.format("\uD83D\uDC4B Let's introduce you, %s!", name));
            introSubtitleTv.setText(String.format("Start by mentioning things like\n\uD83C\uDFE1 Which area in %s you stay in\n⌚ For how long have you been in B'luru\n\uD83D\uDC83 What are your interests or hobbies\n\nWhen you're done, click 'POST' button at the bottom",
                    LubbleSharedPrefs.getInstance().getLubbleName()));
        } else {
            introMcv.setVisibility(View.GONE);
        }
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
                    if (extractedUrl != null && !prevUrl.equalsIgnoreCase(extractedUrl)
                            && !isLinkPreviewClosedByUser) {
                        prevUrl = extractedUrl;
                        linkMetaAsyncTask = new LinkMetaAsyncTask(prevUrl, getLinkMetaListener());
                        linkMetaAsyncTask.execute();
                    } else if (extractedUrl == null) {
                        if (linkPreviewContainer.getVisibility() == View.VISIBLE) {
                            resetLinkPreview();
                        }
                        // url no longer exists in the text; reset user override flag
                        isLinkPreviewClosedByUser = false;
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
        feedPostData.setLinkUrl("");
    }

    private void openGroupSelectionActivity(FeedPostData feedPostData) {
        Intent groupSelectionActivIntent = GroupSelectionActiv.getIntent(this, feedPostData);
        if (isQandA) {
            groupSelectionActivIntent.putExtra(ARG_POST_TYPE, TYPE_QNA);
        } else if (isIntro) {
            groupSelectionActivIntent.putExtra(ARG_POST_TYPE, TYPE_INTRO);
        }
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
            Uri uri = null;
            String type = null;
            File imageFile;
            if (returnValue != null) {
                uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", new File(returnValue.get(0)));
                type = getMimeType(uri);
                imageFile = getFileFromInputStreamUri(this, uri);
            }
            if (type != null && (type.contains("image") || type.contains("jpg") || type.contains("jpeg"))) {//returnValue != null && !returnValue.isEmpty()
                exoPlayerView.setVisibility(View.GONE);
                imageFile = getFileFromInputStreamUri(this, uri);
                uri = Uri.fromFile(imageFile);

                addPhotoToFeedTv.setText("Change Media");
                photoLink = uri.toString();

                attachedPicIv.setVisibility(View.VISIBLE);
                GlideApp.with(this)
                        .load(uri)
                        .transform(new RoundedCornersTransformation(dpToPx(8), 0))
                        .into(attachedPicIv);
            } else if (returnValue != null && (type.contains("video") || type.contains("mp4"))) {
                attachedPicIv.setVisibility(View.GONE);
                String extension = FileUtils.getFileExtension(this, uri);
                addPhotoToFeedTv.setText("Change Media");
                if (!TextUtils.isEmpty(extension) && extension.contains("mov")) {
                    Toast.makeText(this, "Unsupported File type", Toast.LENGTH_LONG).show();
                } else {
                    File videoFile;
                    videoFile = getFileFromInputStreamUri(this, uri);
                    Video_Size = videoFile.length() / (1024f * 1024f);
                    if (Video_Size > PERMITTED_VIDEO_SIZE) {
                        Toast.makeText(this, "Choose a video size less than 30 MB", Toast.LENGTH_LONG).show();
                        videoFile.delete();
                    } else {
                        final Uri vidUri = Uri.fromFile(videoFile);
                        videoLink = vidUri.toString();
                        exoPlayerView.setVisibility(View.VISIBLE);
                        prepareExoPlayerFromFileUri(vidUri);
                        exoPlayer.setPlayWhenReady(false);
                    }
                }
            } else {
                Toast.makeText(this, R.string.all_something_wrong_try_again, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void prepareExoPlayerFromFileUri(Uri uri) {
        exoPlayer = new SimpleExoPlayer.Builder(this).build();
        DataSpec dataSpec = new DataSpec(uri);
        final FileDataSource fileDataSource = new FileDataSource();
        try {
            fileDataSource.open(dataSpec);
        } catch (FileDataSource.FileDataSourceException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            e.printStackTrace();
        }

        DataSource.Factory factory = new DataSource.Factory() {
            @Override
            public DataSource createDataSource() {
                return fileDataSource;
            }
        };
        /*todo deprecated MediaSource videosource = new ExtractorMediaSource(fileDataSource.getUri(),
                factory, new DefaultExtractorsFactory(), null, null);*/
        exoPlayer.setMediaItem(MediaItem.fromUri(fileDataSource.getUri()));
        exoPlayerView.setPlayer(exoPlayer);
        exoPlayer.prepare();
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
        if (exoPlayer != null)
            exoPlayer.release();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exoPlayer != null)
            exoPlayer.release();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (exoPlayer != null)
            exoPlayer.release();
    }

}