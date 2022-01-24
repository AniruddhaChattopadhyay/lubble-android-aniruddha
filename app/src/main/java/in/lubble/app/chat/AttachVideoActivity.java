package in.lubble.app.chat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import in.lubble.app.BaseActivity;
import in.lubble.app.BuildConfig;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.UploadVideoService;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;

import static in.lubble.app.UploadFileService.EXTRA_FILE_URI;

public class AttachVideoActivity extends BaseActivity {

    private static final String EXTRA_VID_PATH = BuildConfig.APPLICATION_ID + "_NEW_IMG_PATH";
    private static final String EXTRA_GROUP_ID = BuildConfig.APPLICATION_ID + "_NEW_IMG_GROUP_ID";
    private static final String EXTRA_CAPTION = BuildConfig.APPLICATION_ID + "_CAPTION";
    private static final String EXTRA_IS_DM = BuildConfig.APPLICATION_ID + "_IS_DM";
    private static final String EXTRA_AUTHOR_ID = BuildConfig.APPLICATION_ID + "_AUTHOR_ID";
    private static final String EXTRA_IS_AUTHOR_SELLER = BuildConfig.APPLICATION_ID + "_IS_AUTHOR_SELLER";
    private ImageButton muteBtn;
    PlayerView exoPlayerView;
    SimpleExoPlayer exoPlayer;
    private EditText captionEt;
    private ImageView sendIcon;

    public static void open(Context context, Uri vidUri, String groupId, @Nullable String caption, boolean isDm, boolean isAuthorSeller, String authorId) {
        context.startActivity(getIntent(context, vidUri, groupId, caption, isDm, isAuthorSeller, authorId));
    }

    public static Intent getIntent(Context context, Uri vidUri, String groupId, @Nullable String caption, boolean isDm, boolean isAuthorSeller, String authorId) {
        Intent intent = new Intent(context, AttachVideoActivity.class);
        intent.putExtra(EXTRA_VID_PATH, vidUri);
        intent.putExtra(EXTRA_GROUP_ID, groupId);
        intent.putExtra(EXTRA_CAPTION, caption);
        intent.putExtra(EXTRA_IS_DM, isDm);
        intent.putExtra(EXTRA_AUTHOR_ID, authorId);
        intent.putExtra(EXTRA_IS_AUTHOR_SELLER, isAuthorSeller);
        return intent;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attach_video);

        Toolbar toolbar = findViewById(R.id.text_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("");

        captionEt = findViewById(R.id.et_vid_caption);
        sendIcon = findViewById(R.id.iv_send_btn_vid);
        muteBtn = findViewById(R.id.exo_mute_btn);
        final Uri vidUri = getIntent().getParcelableExtra(EXTRA_VID_PATH);
        final String chatId = getIntent().getStringExtra(EXTRA_GROUP_ID);
        final String caption = getIntent().getStringExtra(EXTRA_CAPTION);

        captionEt.setText(caption);

        exoPlayerView = findViewById(R.id.exo_player);
        prepareExoPlayerFromFileUri(vidUri);
        exoPlayer.setPlayWhenReady(false);


        sendIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final boolean isDm = getIntent().getBooleanExtra(EXTRA_IS_DM, false);
                String uploadPath = "lubbles/" + LubbleSharedPrefs.getInstance().requireLubbleId() + "/groups/" + chatId;
                if (isDm) {
                    uploadPath = "dms/" + chatId;
                }
                Log.d("uploadFromVideo", "on click send");
                startService(new Intent(AttachVideoActivity.this, UploadVideoService.class)
                        .putExtra(UploadVideoService.EXTRA_BUCKET, UploadVideoService.BUCKET_CONVO)
                        .putExtra(UploadVideoService.EXTRA_FILE_NAME, vidUri.getLastPathSegment())
                        .putExtra(EXTRA_FILE_URI, vidUri)
                        .putExtra(UploadVideoService.EXTRA_UPLOAD_PATH, uploadPath)
                        .putExtra(UploadVideoService.EXTRA_CAPTION, captionEt.getText().toString())
                        .putExtra(UploadVideoService.EXTRA_CHAT_ID, chatId)
                        .putExtra(UploadVideoService.EXTRA_IS_DM, isDm)
                        .putExtra(UploadVideoService.EXTRA_AUTHOR_ID, getIntent().getStringExtra(EXTRA_AUTHOR_ID))
                        .putExtra(UploadVideoService.EXTRA_IS_AUTHOR_SELLER, getIntent().getBooleanExtra(EXTRA_IS_AUTHOR_SELLER, false))
                        .setAction(UploadVideoService.ACTION_UPLOAD));
                final Bundle bundle = new Bundle();
                bundle.putString("group_id", chatId);
                Analytics.triggerEvent(AnalyticsEvents.SEND_GROUP_CHAT, bundle, AttachVideoActivity.this);
                setResult(RESULT_OK);
                finish();
            }
        });

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
        muteVideo(exoPlayer);
    }


    public void muteVideo(ExoPlayer exoPlayer){
        if(exoPlayer.getVolume() == 0F) {
            exoPlayer.setVolume(0.75F);
            muteBtn.setImageResource(R.drawable.ic_mute);
        }
        else{
            exoPlayer.setVolume(0F);
            muteBtn.setImageResource(R.drawable.ic_volume_up_black_24dp);
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