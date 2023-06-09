package in.lubble.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import in.lubble.app.models.FeedPostData;
import in.lubble.app.services.FeedServices;

import static in.lubble.app.firebase.FirebaseStorageHelper.getConvoBucketRef;
import static in.lubble.app.firebase.FirebaseStorageHelper.getDefaultBucketRef;
import static in.lubble.app.firebase.FirebaseStorageHelper.getMarketplaceBucketRef;
import static in.lubble.app.utils.FileUtils.getUriFromTempBitmap;

/**
 * Service to handle uploading files to Firebase Storage
 * Created by ishaan on 26/1/18.
 */

public class UploadImageFeedService extends BaseTaskService {

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
        isGroupJoined = intent.getBooleanExtra(EXTRA_FEED_IS_GROUP_JOINED,false);
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

        GlideApp.with(this).asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .load(fileUri)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        int width = resource.getWidth();
                        int height = resource.getHeight();
                        if (width > 1000 || height > 1000) {
                            float downsizeFactor;
                            if (width > height) {
                                downsizeFactor = width / 1000f;
                            } else {
                                downsizeFactor = height / 1000f;
                            }
                            int targetWidth = Math.round(width / downsizeFactor);
                            int targetHeight = Math.round(height / downsizeFactor);
                            compressAndUpload(fileUri, fileName, targetWidth, targetHeight, photoRef, groupName, feedName, feedPostData);
                        } else {
                            uploadFile(fileUri, photoRef, groupName, feedName, (float) width / height, feedPostData);
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }

    private void compressAndUpload(final Uri fileUri, final String fileName, int targetWidth, int targetHeight, final StorageReference photoRef, final String groupName, final String feedName, FeedPostData feedPostData) {
        GlideApp.with(this).asBitmap()
                .override(targetWidth, targetHeight)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .load(fileUri)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        final Uri compressedFileUri = getUriFromTempBitmap(UploadImageFeedService.this, resource, fileName, MimeTypeMap.getFileExtensionFromUrl(fileUri.toString()));
                        uploadFile(compressedFileUri, photoRef, groupName, feedName, (float) targetWidth / targetHeight, feedPostData);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }


    private void uploadFile(final Uri compressedFileUri, final StorageReference photoRef, final String groupName, final String feedName, final float aspectRatio, FeedPostData feedPostData) {
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
                                    broadcastUploadFinished(downloadUri, compressedFileUri, true, groupName, feedName, aspectRatio, feedPostData);

                                    showUploadFinishedNotification(downloadUri, compressedFileUri, true);
                                    taskCompleted();
                                    // [END_EXCLUDE]
                                } else {
                                    Log.d(TAG, "onComplete: failed");

                                    // [START_EXCLUDE]
                                    broadcastUploadFinished(null, compressedFileUri, false, groupName, feedName, 0, feedPostData);

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
                        broadcastUploadFinished(null, compressedFileUri, false, groupName, feedName, 0, feedPostData);

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
    private boolean broadcastUploadFinished(@Nullable Uri downloadUrl, @Nullable Uri fileUri, boolean toTransmit, final String groupName, final String feedName, float aspectRatio, FeedPostData feedPostData) {
        boolean success = downloadUrl != null;

        String action = success ? UPLOAD_COMPLETED : UPLOAD_ERROR;

        Intent broadcast = new Intent(action)
                .putExtra(EXTRA_DOWNLOAD_URL, downloadUrl)
                .putExtra(EXTRA_FILE_URI, fileUri);

        if (toTransmit && success) {
            FeedServices.post(feedPostData, groupName, feedName, downloadUrl.toString(),null, aspectRatio,isGroupJoined, null);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
