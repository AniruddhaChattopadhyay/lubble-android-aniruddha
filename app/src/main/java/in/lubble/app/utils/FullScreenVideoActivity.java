package in.lubble.app.utils;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import java.io.File;
import java.net.URLDecoder;

import in.lubble.app.BaseActivity;
import in.lubble.app.BuildConfig;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

import static in.lubble.app.utils.FileUtils.showStoragePermRationale;

@RuntimePermissions
public class FullScreenVideoActivity extends BaseActivity {
    private static final String TAG = "FullScreenVideoActivity";
    private static final String EXTRA_IMG_PATH = BuildConfig.APPLICATION_ID + "_EXTRA_IMG_PATH";
    private SimpleExoPlayerView exoPlayerView;
    private SimpleExoPlayer exoPlayer;
    private ProgressBar progressBar;
    private String videoname;
    private Uri videourl = null;
    private Uri videoUrlHttp = null;
    private Long position = C.TIME_UNSET;
    private final String lubble_vid_dir = "Lubble Videos";
    private File lubble_vid_file;
    File matchingFile = null;


    public static void open(Activity activity, Context context, String vidPath) {
        Intent intent = new Intent(context, FullScreenVideoActivity.class);
        intent.putExtra(EXTRA_IMG_PATH, vidPath);
        Bundle bundle = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(activity).toBundle();
        }
        context.startActivity(intent, bundle);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_video);
        progressBar = findViewById(R.id.progress_bar_full_vid);
        Toolbar toolbar = findViewById(R.id.transparent_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("");

        Analytics.triggerScreenEvent(this, this.getClass());
        Analytics.triggerEvent(AnalyticsEvents.VIDEO_OPENED, this);
        exoPlayerView = findViewById(R.id.exo_player_full_screen);

        videourl = Uri.parse(getIntent().getStringExtra(EXTRA_IMG_PATH));
        if (savedInstanceState != null) {
            if (videourl == null) {
                videourl = Uri.parse(savedInstanceState.getString(EXTRA_IMG_PATH));
            }
            position = savedInstanceState.getLong("SELECTED_POSITION", C.TIME_UNSET);
        }

        videoUrlHttp = videourl;
        videoname = getFileName();
        FullScreenVideoActivityPermissionsDispatcher.makeGetFileForDownloadWithPermissionCheck(FullScreenVideoActivity.this);
        Log.d(TAG, "before" + videourl.toString());
        if (matchingFile != null) {
            Log.d(TAG, "*****************" + matchingFile.getName());
            videourl = Uri.fromFile(matchingFile);
        } else {
            progressBar.setVisibility(View.VISIBLE);
        }
        Log.d(TAG, "after" + videourl.toString());
        Log.d(TAG, videoname);
        Log.d(TAG, videourl.toString());
        initializePlayer(videourl);
    }


    private String getFileName() {
        String decode = null;
        decode = URLDecoder.decode(getIntent().getStringExtra(EXTRA_IMG_PATH));
        Uri uri = Uri.parse(decode);
        return uri.getLastPathSegment();
    }

    private static File findFilesForId(File dir, final String file_name_to_be_searched) {
        File[] files = dir.listFiles();
        for (File f : files) {
            if (f.getName().equals(file_name_to_be_searched))
                return f;
        }
        return null;
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public void makeGetFileForDownload() {
        File f = new File(Environment.getExternalStorageDirectory(), lubble_vid_dir);
        if (!f.exists()) {
            f.mkdirs();
        }
        lubble_vid_file = f;
        matchingFile = findFilesForId(lubble_vid_file, videoname);
    }


    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public void download_Video(Uri videouri, String videoname) {
        DownloadManager.Request request = new DownloadManager.Request(videouri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setTitle(videoname);
        request.setDescription("Downloading Video ...");
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(lubble_vid_dir, videoname);

        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
        Toast.makeText(this, "Download started, you will be notified", Toast.LENGTH_SHORT).show();
        Bundle bundle = new Bundle();
        bundle.putString("video_name", videoname);
        Analytics.triggerEvent(AnalyticsEvents.DOWNLOAD_VIDEO, bundle, this);
    }

    private void initializePlayer(Uri mediaUri) {
        if (exoPlayer == null) {
            BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
            TrackSelector trackSelector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(bandwidthMeter));
            exoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
            exoPlayer.addListener(new ExoPlayer.EventListener() {
                @Override
                public void onTimelineChanged(Timeline timeline, Object manifest) {
                }

                @Override
                public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                }

                @Override
                public void onLoadingChanged(boolean isLoading) {
                }

                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                    if (playbackState == ExoPlayer.STATE_BUFFERING) {
                        Log.d("FullScreenVideoActivit1", "inside buffer");
                        progressBar.setVisibility(View.VISIBLE);
                    } else {
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                }

                @Override
                public void onPlayerError(ExoPlaybackException error) {
                }

                @Override
                public void onPositionDiscontinuity() {
                }

                @Override
                public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
                }
            });
            DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, "ua");
            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
            MediaSource mediaSource = new ExtractorMediaSource(videourl, dataSourceFactory, extractorsFactory, null, null);
            exoPlayerView.setPlayer(exoPlayer);
            exoPlayer.prepare(mediaSource);
            exoPlayer.setPlayWhenReady(true);
            if (position != C.TIME_UNSET) {
                exoPlayer.seekTo(position);
            }
        }
    }

    public void onSaveInstanceState(Bundle currentState) {
        super.onSaveInstanceState(currentState);
        currentState.putLong("SELECTED_POSITION", position);
        currentState.putString(EXTRA_IMG_PATH, getIntent().getStringExtra(EXTRA_IMG_PATH));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.full_screen_video_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_download_vid:
                FullScreenVideoActivityPermissionsDispatcher.download_VideoWithPermissionCheck(FullScreenVideoActivity.this, videoUrlHttp, videoname);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        FullScreenVideoActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showRationaleForCamera(final PermissionRequest request) {
        showStoragePermRationale(this, request);
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showDeniedForCamera() {
        Toast.makeText(this, R.string.storage_perm_denied_text, Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showNeverAskForCamera() {
        Toast.makeText(this, R.string.storage_perm_never_text, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (exoPlayer != null) {
            exoPlayer.release();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exoPlayer != null) {
            exoPlayer.release();
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "on resume");
        super.onResume();
        if (videourl != null) {
            initializePlayer(videourl);
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "on pause");
        super.onPause();
        if (exoPlayer != null) {
            position = exoPlayer.getCurrentPosition();
            exoPlayer.stop();
            exoPlayer.release();
            exoPlayer = null;
        }
    }
}