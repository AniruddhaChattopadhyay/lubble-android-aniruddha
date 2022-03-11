package in.lubble.app;

import static in.lubble.app.firebase.FirebaseStorageHelper.getConvoBucketRef;
import static in.lubble.app.firebase.FirebaseStorageHelper.getDefaultBucketRef;
import static in.lubble.app.firebase.FirebaseStorageHelper.getMarketplaceBucketRef;
import static in.lubble.app.utils.FileUtils.getFileFromInputStreamUri;
import static in.lubble.app.utils.FileUtils.getFileNameFromUri;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.linkedin.android.litr.MediaTransformer;
import com.linkedin.android.litr.TransformationListener;
import com.linkedin.android.litr.analytics.TrackTransformationInfo;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.models.FeedPostData;
import in.lubble.app.services.FeedServices;

public class UploadVideoFeedService extends BaseTaskService {
    private static final String TAG = "UploadFileService";
    public static final int BUCKET_DEFAULT = 362;
    public static final int BUCKET_CONVO = 491;
    public static final int BUCKET_MARKETPLACE = 839;
    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    /**
     * Intent Actions
     **/
    public static final String ACTION_UPLOAD = "action_upload";
    public static final String UPLOAD_COMPLETED = "upload_completed";
    public static final String UPLOAD_ERROR = "upload_error";

    /**
     * Intent Extras
     **/
    public static final String EXTRA_FILE_NAME = "extra_file_name";
    public static final String EXTRA_FILE_URI = "extra_file_uri";
    public static final String EXTRA_BUCKET = "extra_bucket";
    public static final String EXTRA_UPLOAD_PATH = "extra_upload_path";
    public static final String EXTRA_DOWNLOAD_URL = "extra_download_url";
    public static final String EXTRA_FEED_GROUP_NAME = "extra_feed_group_name";
    public static final String EXTRA_FEED_FEED_NAME = "EXTRA_FEED_FEED_NAME";
    public static final String EXTRA_FEED_POST_DATA = "EXTRA_FEED_POST_DATA";
    public static final String EXTRA_FEED_IS_GROUP_JOINED = "EXTRA_FEED_IS_GROUP_JOINED";
    public static final String TRACK_UPLOAD_TIME = "TRACK_UPLOAD_TIME";
    public static final String TRACK_COMPRESS_TIME = "TRACK_COMPRESS_TIME";

    public boolean isGroupJoined;

    private StorageReference mStorageRef;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand:" + intent + ":" + startId);

        final int bucketId = intent.getIntExtra(EXTRA_BUCKET, BUCKET_DEFAULT);
        if (bucketId == BUCKET_CONVO) {
            mStorageRef = getConvoBucketRef();
        } else if (bucketId == BUCKET_MARKETPLACE) {
            mStorageRef = getMarketplaceBucketRef();
        } else {
            mStorageRef = getDefaultBucketRef();
        }

        taskStarted();

        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText("uploading post")
                .setSmallIcon(R.drawable.ic_upload)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);


        Uri fileUri = intent.getParcelableExtra(EXTRA_FILE_URI);
        isGroupJoined = intent.getBooleanExtra(EXTRA_FEED_IS_GROUP_JOINED, false);
        uploadFromUri(
                fileUri,
                intent.getStringExtra(EXTRA_FILE_NAME),
                intent.getStringExtra(EXTRA_UPLOAD_PATH),
                intent.getStringExtra(EXTRA_FEED_GROUP_NAME),
                intent.getStringExtra(EXTRA_FEED_FEED_NAME),
                (FeedPostData) intent.getSerializableExtra(EXTRA_FEED_POST_DATA)
        );
        return START_REDELIVER_INTENT;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void uploadFromUri(final Uri fileUri, final String fileName, final String uploadPath, final String groupName, final String feedName, FeedPostData feedPostData) {
        Log.d(TAG, "uploadFromUri:src:" + fileUri.toString());

        showProgressNotification(getString(R.string.progress_uploading), 0, 0);
        final StorageReference photoRef = mStorageRef.child(uploadPath)
                .child(fileName);
        //uploadFile(fileUri, photoRef, groupName, feedName, feedPostData);
        compressAndUpload(fileUri, fileName, photoRef, uploadPath, groupName, feedName, feedPostData);

    }


    private void compressAndUpload(final Uri fileUri, final String fileName, final StorageReference photoRef, final String uploadPath, final String groupName, final String feedName, FeedPostData feedPostData) {
        String requestId = UUID.randomUUID().toString();
        File outputFile = new File(getFilesDir(), getFileNameFromUri(fileUri));
        TransformationListener listener;
        Trace compressTime = FirebasePerformance.getInstance().newTrace(TRACK_COMPRESS_TIME);
        listener = new TransformationListener() {
            @Override
            public void onStarted(@NonNull String id) {
                Log.d(TAG, "onStarted: ");
                compressTime.start();
            }

            @Override
            public void onProgress(@NonNull String id, float progress) {
                Log.d(TAG, "onStarted: ");
            }

            @Override
            public void onCompleted(@NonNull String id, @Nullable List<TrackTransformationInfo> trackTransformationInfos) {
                Log.d(TAG, "onStarted: ");
                compressTime.stop();
                File fOriginal = new File(fileUri.getPath());
                long sizeOutput = outputFile.length();
                long sizeOriginal = fOriginal.length();
                if (sizeOutput < sizeOriginal) {
                    Analytics.triggerEvent(AnalyticsEvents.FEED_VIDEO_COMPRESS_SUCCESS, getApplicationContext());
                    fOriginal.delete();
                    uploadFile(Uri.fromFile(outputFile), photoRef, groupName, feedName, feedPostData);
                } else {
                    outputFile.delete();
                    Analytics.triggerEvent(AnalyticsEvents.FEED_VIDEO_COMPRESS_BLOATED, getApplicationContext());
                    uploadFile(fileUri, photoRef, groupName, feedName, feedPostData);
                }
            }

            @Override
            public void onCancelled(@NonNull String id, @Nullable List<TrackTransformationInfo> trackTransformationInfos) {
                Analytics.triggerEvent(AnalyticsEvents.FEED_VIDEO_COMPRESS_FAILED, getApplicationContext());
                uploadFile(fileUri, photoRef, groupName, feedName, feedPostData);
            }

            @Override
            public void onError(@NonNull String id, @Nullable Throwable cause, @Nullable List<TrackTransformationInfo> trackTransformationInfos) {
                Analytics.triggerEvent(AnalyticsEvents.FEED_VIDEO_COMPRESS_FAILED, getApplicationContext());
                FirebaseCrashlytics.getInstance().log("Feed video upload error");
                FirebaseCrashlytics.getInstance().recordException(cause);
                uploadFile(fileUri, photoRef, groupName, feedName, feedPostData);

            }
        };
        try {
            MediaExtractor mediaExtractor = new MediaExtractor();
            mediaExtractor.setDataSource(Uri.parse(fileUri.toString()).getPath());
            MediaFormat inputVideoFormat = selectTrack(mediaExtractor, "video");
            MediaFormat inputAudioFormat = selectTrack(mediaExtractor, "audio");

            MediaFormat videoFormat = MediaFormat.createVideoFormat(
                    MediaFormat.MIMETYPE_VIDEO_AVC,
                    inputVideoFormat.getInteger(MediaFormat.KEY_WIDTH),
                    inputVideoFormat.getInteger(MediaFormat.KEY_HEIGHT));

            MediaFormat audioFormat = MediaFormat.createAudioFormat(
                    MediaFormat.MIMETYPE_AUDIO_AAC,
                    inputAudioFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE),
                    inputAudioFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            );

            int videoHeight = inputVideoFormat.getInteger(MediaFormat.KEY_HEIGHT);
            int videoWidth = inputVideoFormat.getInteger(MediaFormat.KEY_WIDTH);
            float aspectRatio = (float) videoWidth / videoHeight;
            int outputHeight, outputWidth;
            if (videoWidth <= 720 && videoHeight <= 720) {
                outputHeight = videoHeight;
                outputWidth = videoWidth;
            } else if (videoHeight > videoWidth) {
                outputHeight = 720;
                outputWidth = (int) (outputHeight * aspectRatio);
            } else {
                outputWidth = 720;
                outputHeight = (int) (outputWidth / aspectRatio);
            }
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(fileUri.getPath());
            int videoBitrate = Integer.parseInt(retriever.extractMetadata((MediaMetadataRetriever.METADATA_KEY_BITRATE)));
            int audioBitrate;
            if (inputAudioFormat.containsKey(MediaFormat.KEY_BIT_RATE)) {
                audioBitrate = inputAudioFormat.getInteger(MediaFormat.KEY_BIT_RATE);
            } else {
                audioBitrate = 0;
            }

            Log.d(TAG, "OG bitrate is : " + videoBitrate);

            int destBitrate = videoBitrate;
            if (videoBitrate > 5 * 1024 * 1024 || new File(fileUri.toString()).length() > 10 * 1024 * 1024) {
                destBitrate = Math.min(videoBitrate / 2, 5 * 1024 * 1024); // max Bitrate allowed = 5Mbps
            }

            videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, destBitrate);
            videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, inputVideoFormat.getInteger(MediaFormat.KEY_FRAME_RATE));
            videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
            videoFormat.setInteger(MediaFormat.KEY_WIDTH, outputWidth);
            videoFormat.setInteger(MediaFormat.KEY_HEIGHT, outputHeight);
            audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, audioBitrate / 2);

            MediaTransformer mediaTransformer = new MediaTransformer(getApplicationContext());

            mediaTransformer.transform(requestId,
                    Uri.parse(fileUri.toString()),
                    outputFile.getPath(),
                    videoFormat,
                    null,
                    listener,
                    null);


        } catch (IOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            e.printStackTrace();
        }
    }


    private void uploadFile(final Uri compressedFileUri, final StorageReference photoRef, final String groupName, final String feedName, FeedPostData feedPostData) {
        // Upload file to Firebase Storage
        Log.d(TAG, "uploadFromUri:dst:" + photoRef.getPath());
        final UploadTask uploadTask;
        uploadTask = photoRef.putFile(compressedFileUri);
        uploadTask.
                addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        showProgressNotification(getString(R.string.progress_uploading),
                                taskSnapshot.getBytesTransferred(),
                                taskSnapshot.getTotalByteCount());
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Upload succeeded
                        Log.d(TAG, "uploadFromUri:onSuccess");

                        // Get the public download URL
                        photoRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    final Uri downloadUri = task.getResult();
                                    // [START_EXCLUDE]
                                    broadcastUploadFinished(downloadUri, compressedFileUri, true, groupName, feedName, feedPostData);

                                    showUploadFinishedNotification(downloadUri, compressedFileUri, true);
                                    taskCompleted();
                                    // [END_EXCLUDE]
                                } else {
                                    Log.d(TAG, "onComplete: failed");

                                    // [START_EXCLUDE]
                                    broadcastUploadFinished(null, compressedFileUri, false, groupName, feedName, feedPostData);

                                    showUploadFinishedNotification(null, compressedFileUri, false);
                                    taskCompleted();
                                    // [END_EXCLUDE]
                                }
                            }
                        });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Upload failed
                        Log.w(TAG, "uploadFromUri:onFailure", exception);

                        // [START_EXCLUDE]
                        broadcastUploadFinished(null, compressedFileUri, false, groupName, feedName, feedPostData);

                        showUploadFinishedNotification(null, compressedFileUri, false);
                        taskCompleted();
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END upload_from_uri]

    /**
     * Broadcast finished upload (success or failure).
     *
     * @return true if a running receiver received the broadcast.
     */
    private boolean broadcastUploadFinished(@Nullable Uri downloadUrl, Uri fileUri, boolean toTransmit, final String groupName, final String feedName, FeedPostData feedPostData) {
        boolean success = downloadUrl != null;

        String action = success ? UPLOAD_COMPLETED : UPLOAD_ERROR;

        Intent broadcast = new Intent(action)
                .putExtra(EXTRA_DOWNLOAD_URL, downloadUrl)
                .putExtra(EXTRA_FILE_URI, fileUri);

        if (toTransmit && success) {
            File videoFile;
            videoFile = getFileFromInputStreamUri(this, fileUri);
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(videoFile.getAbsolutePath());
            int width = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
            int height = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
            retriever.release();
            videoFile.delete();
            float aspectRatio = (float) width / height;
            FeedServices.post(feedPostData, groupName, feedName, null, downloadUrl.toString(), aspectRatio, isGroupJoined, null);
        }

        return LocalBroadcastManager.getInstance(getApplicationContext())
                .sendBroadcast(broadcast);
    }

    /**
     * Show a notification for a finished upload.
     */
    private void showUploadFinishedNotification(@Nullable Uri downloadUrl, @Nullable Uri fileUri, boolean isConvo) {
        // Hide the progress notification
        dismissProgressNotification();
        if (isConvo) {
            // img uploaded for chat convo, just dismiss the progress notif
            // do NOT show complete noif
            return;
        }

        // Make Intent to MainActivity
        Intent intent = new Intent(this, MainActivity.class)
                .putExtra(EXTRA_DOWNLOAD_URL, downloadUrl)
                .putExtra(EXTRA_FILE_URI, fileUri)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        boolean success = downloadUrl != null;
        String caption = success ? getString(R.string.upload_success) : getString(R.string.upload_failure);
        showFinishedNotification(caption, intent, success);
    }

    private MediaFormat selectTrack(MediaExtractor mediaExtractor, String mime) {
        for (int track = 0; track < mediaExtractor.getTrackCount(); track++) {
            MediaFormat mediaFormat = mediaExtractor.getTrackFormat(track);
            String mimeType = mediaFormat.getString(MediaFormat.KEY_MIME);
            if (mimeType == null) {
                continue;
            }
            if (mimeType.startsWith(mime)) {
                return mediaExtractor.getTrackFormat(track);
            }
        }
        return null;
    }
}
