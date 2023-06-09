package in.lubble.app;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.SiliCompressor;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import in.lubble.app.models.ChatData;
import in.lubble.app.utils.ChatUtils;
import in.lubble.app.utils.FileUtils;

import static in.lubble.app.firebase.FirebaseStorageHelper.getConvoBucketRef;
import static in.lubble.app.firebase.FirebaseStorageHelper.getDefaultBucketRef;
import static in.lubble.app.firebase.FirebaseStorageHelper.getMarketplaceBucketRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getDmMessagesRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getMessagesRef;

public class UploadVideoService extends BaseTaskService {
    private static final String TAG = "UploadVideoService";
    public static final int BUCKET_DEFAULT = 362;
    public static final int BUCKET_CONVO = 491;
    public static final int BUCKET_MARKETPLACE = 839;

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
    public static final String EXTRA_CAPTION = "extra_caption";
    public static final String EXTRA_CHAT_ID = "extra_chat_id";
    public static final String EXTRA_IS_DM = "EXTRA_IS_DM";
    public static final String EXTRA_AUTHOR_ID = "EXTRA_AUTHOR_ID";
    public static final String EXTRA_IS_AUTHOR_SELLER = "EXTRA_IS_AUTHOR_SELLER";
    public static final String TRACK_UPLOAD_TIME = "TRACK_UPLOAD_TIME";
    public static final String TRACK_COMPRESS_TIME = "TRACK_COMPRESS_TIME";

    private StorageReference mStorageRef;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("uploadFrom", "Started");
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

        if (ACTION_UPLOAD.equals(intent.getAction())) {
            if (bucketId == BUCKET_MARKETPLACE) {
                Uri fileUri = intent.getParcelableExtra(EXTRA_FILE_URI);
                uploadFromUriWithMetadata(
                        fileUri,
                        intent.getStringExtra(EXTRA_FILE_NAME),
                        intent.getStringExtra(EXTRA_UPLOAD_PATH),
                        intent.getStringExtra(EXTRA_CAPTION),
                        intent.getStringExtra(EXTRA_CHAT_ID)
                );
            } else {
                final UploadVideoService.DmInfoData dmInfoData = new UploadVideoService.DmInfoData(
                        intent.getStringExtra(EXTRA_AUTHOR_ID),
                        intent.getBooleanExtra(EXTRA_IS_DM, false),
                        intent.getBooleanExtra(EXTRA_IS_AUTHOR_SELLER, false)
                );
                Uri fileUri = intent.getParcelableExtra(EXTRA_FILE_URI);
                uploadFromUri(
                        fileUri,
                        intent.getStringExtra(EXTRA_FILE_NAME),
                        intent.getStringExtra(EXTRA_UPLOAD_PATH),
                        intent.getStringExtra(EXTRA_CAPTION),
                        intent.getStringExtra(EXTRA_CHAT_ID),
                        bucketId == BUCKET_CONVO,
                        null,
                        dmInfoData
                );
            }
        }

        return START_REDELIVER_INTENT;
    }


    private void uploadFromUriWithMetadata(final Uri fileUri, final String fileName, final String uploadPath, final String caption, final String groupId) {
        Log.d(TAG, "uploadFromUri:src:" + fileUri.toString());

        showProgressNotification(getString(R.string.progress_uploading), 0, 0);

        FirebaseAuth.getInstance().getAccessToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<GetTokenResult> task) {
                if (task.isSuccessful()) {
                    // Create file metadata including the content type
                    StorageMetadata metadata = new StorageMetadata.Builder()
                            .setContentType(FileUtils.getMimeType(fileUri))
                            .setCustomMetadata("uid", FirebaseAuth.getInstance().getUid())
                            .setCustomMetadata("token", task.getResult().getToken())
                            .build();
                    uploadFromUri(fileUri, fileName, uploadPath, caption, groupId, false, metadata, null);
                } else {
                    taskCompleted();
                }
            }
        });
    }


    private void uploadFromUri(final Uri fileUri, final String fileName, final String uploadPath, final String caption, final String groupId,
                               final boolean toTransmit, @Nullable final StorageMetadata metadata, @Nullable final UploadVideoService.DmInfoData dmInfoData) {
        Log.d(TAG, "uploadFromUri:src:" + fileUri.toString());

        showProgressNotification(getString(R.string.progress_uploading), 0, 0);
        final StorageReference photoRef = mStorageRef.child(uploadPath)
                .child(fileName);
        // uploadFile(fileUri, photoRef, metadata, toTransmit, caption, groupId, dmInfoData);
        compressAndUpload(fileUri, caption, groupId, toTransmit, metadata, dmInfoData, photoRef);

    }

    private void compressAndUpload(Uri fileUri, final String caption, final String groupId, final boolean toTransmit, @Nullable final StorageMetadata metadata, @Nullable final DmInfoData dmInfoData, final StorageReference photoRef) {
        File f = new File(getFilesDir().getAbsolutePath() + "/" + getPackageName() + "/media/videos");
        if (f.mkdirs() || f.isDirectory()) {
            //compress and output new video specs
            Log.d(TAG, "inside if to async task");
            new VideoCompressAsyncTask(this, this, caption, groupId, toTransmit, metadata, dmInfoData, photoRef).execute(fileUri.toString(), f.getPath());
        }
    }

    private void uploadFile(final Uri compressedFileUri, final StorageReference photoRef, @Nullable StorageMetadata metadata, final boolean toTransmit, final String caption, final String groupId, @Nullable final UploadVideoService.DmInfoData dmInfoData) {
        // Upload file to Firebase Storage
        Log.d(TAG, "uploadFromUri:dst:" + photoRef.getPath());
        final UploadTask uploadTask;
        final Trace uploadTime = FirebasePerformance.getInstance().newTrace(TRACK_UPLOAD_TIME);
        uploadTime.start();
        if (metadata != null) {
            uploadTask = photoRef.putFile(compressedFileUri, metadata);
        } else {
            uploadTask = photoRef.putFile(compressedFileUri);
        }
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
                        uploadTime.stop();
                        //delete the compressed file which is no longer needed
                        if (compressedFileUri != null) {
                            File file = new File(compressedFileUri.getPath());
                            file.delete();
                            if (file.exists()) {
                                try {
                                    file.getCanonicalFile().delete();
                                    if (file.exists()) {
                                        getApplicationContext().deleteFile(file.getName());
                                    }
                                } catch (IOException e) {
                                    FirebaseCrashlytics.getInstance().recordException(e);
                                    e.printStackTrace();
                                }
                            }
                        }


                        // Get the public download URL
                        photoRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    final Uri downloadUri = task.getResult();
                                    // [START_EXCLUDE]
                                    broadcastUploadFinished(downloadUri, compressedFileUri, toTransmit, caption, groupId, dmInfoData);
                                    showUploadFinishedNotification(downloadUri, compressedFileUri, toTransmit);
                                    taskCompleted();
                                    // [END_EXCLUDE]
                                } else {
                                    Log.d(TAG, "onComplete: failed");

                                    // [START_EXCLUDE]
                                    broadcastUploadFinished(null, compressedFileUri, toTransmit, caption, groupId, dmInfoData);
                                    showUploadFinishedNotification(null, compressedFileUri, toTransmit);
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
                        broadcastUploadFinished(null, compressedFileUri, toTransmit, caption, groupId, dmInfoData);
                        showUploadFinishedNotification(null, compressedFileUri, toTransmit);
                        taskCompleted();
                        //[END_EXCLUDE]
                    }
                });
    }

    class VideoCompressAsyncTask extends AsyncTask<String, String, String> {
        Trace compressTime = FirebasePerformance.getInstance().newTrace(TRACK_COMPRESS_TIME);
        Context mContext;
        UploadVideoService uploadVideoService;
        Uri ccompressedUri;
        String caption;
        String groupId;
        boolean toTransmit;
        StorageMetadata metadata;
        DmInfoData dmInfoData;
        StorageReference photoRef;
        Uri cachevid = null;
        double compessed_video_size = 0;

        public VideoCompressAsyncTask(Context context, UploadVideoService uploadVideoService, String caption, String groupId, boolean toTransmit, @Nullable StorageMetadata metadata, @Nullable DmInfoData dmInfoData, StorageReference photoRef) {
            mContext = context;
            this.uploadVideoService = uploadVideoService;
            this.caption = caption;
            this.groupId = groupId;
            this.toTransmit = toTransmit;
            this.metadata = metadata;
            this.dmInfoData = dmInfoData;
            this.photoRef = photoRef;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            compressTime.start();
        }

        @Override
        protected String doInBackground(String... paths) {
            String filePath = null;
            try {
                //Uri pathdesc = Uri.parse(paths[1]);
                Log.d(TAG, "path0 = " + paths[0] + " path1=" + paths[1] + "from file " + Uri.fromFile(new File(paths[1])).toString());
                Log.d(TAG, "path0 = " + Uri.parse(paths[0]).getPath() + " path1=" + paths[1] + "from file " + Uri.fromFile(new File(paths[1])).getPath());

                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(Uri.parse(paths[0]).getPath());
                int bitrate = Integer.parseInt(retriever.extractMetadata((MediaMetadataRetriever.METADATA_KEY_BITRATE)));

                Log.d(TAG, "OG bitrate is : " + bitrate);

                int destBitrate = bitrate;
                if (bitrate > 5 * 1024 * 1024 || new File(paths[0]).length() > 10 * 1024 * 1024) {
                    destBitrate = Math.min(bitrate / 2, 5 * 1024 * 1024); // max Bitrate allowed = 5Mbps
                }

                filePath = SiliCompressor.with(mContext).compressVideo(Uri.parse(paths[0]).getPath(), Uri.fromFile(new File(paths[1])).getPath(), 0, 0, destBitrate);
                cachevid = Uri.parse(paths[0]);

            } catch (URISyntaxException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                e.printStackTrace();
            }
            return filePath;

        }


        @Override
        protected void onPostExecute(String compressedFilePath) {
            super.onPostExecute(compressedFilePath);
            File imageFile = new File(compressedFilePath);
            compessed_video_size = imageFile.length() / (1024f * 1024f);
            compressTime.stop();
            ccompressedUri = Uri.fromFile(imageFile);
            Log.i(TAG, "Path: " + compressedFilePath);
            File file = new File(cachevid.getPath());

            Log.d(TAG, "OG SIZE: " + file.length() / (1024f * 1024f));
            Log.d(TAG, "compressed size: " + compessed_video_size);

            if (compessed_video_size <= FileUtils.Video_Size) {
                //delete image in cache
                if (cachevid != null) {
                    file.delete();
                    if (file.exists()) {
                        try {
                            file.getCanonicalFile().delete();
                            if (file.exists()) {
                                getApplicationContext().deleteFile(file.getName());
                            }
                        } catch (IOException e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                            e.printStackTrace();
                        }
                    }
                }
                uploadVideoService.uploadFile(ccompressedUri, photoRef, metadata, toTransmit, caption, groupId, dmInfoData);
            } else
                uploadVideoService.uploadFile(cachevid, photoRef, metadata, toTransmit, caption, groupId, dmInfoData);
        }
    }
    // [END upload_from_uri]

    /**
     * Broadcast finished upload (success or failure).
     *
     * @return true if a running receiver received the broadcast.
     */
    private boolean broadcastUploadFinished(@Nullable Uri downloadUrl, @Nullable Uri fileUri, boolean toTransmit, String caption,
                                            String chatId, UploadVideoService.DmInfoData dmInfoData) {
        boolean success = downloadUrl != null;

        String action = success ? UPLOAD_COMPLETED : UPLOAD_ERROR;

        Intent broadcast = new Intent(action)
                .putExtra(EXTRA_DOWNLOAD_URL, downloadUrl)
                .putExtra(EXTRA_FILE_URI, fileUri);

        if (toTransmit && success) {
            transmitMedia(downloadUrl, caption, chatId, dmInfoData.isDm, dmInfoData.authorId, dmInfoData.isAuthorSeller);
        }

        return LocalBroadcastManager.getInstance(getApplicationContext())
                .sendBroadcast(broadcast);
    }

    private void transmitMedia(Uri downloadUrl, String caption, String chatId, boolean isDm, String authorId, boolean isAuthorSeller) {
        final DatabaseReference msgReference;
        if (isDm) {
            msgReference = getDmMessagesRef().child(chatId);
        } else {
            msgReference = getMessagesRef().child(chatId);
        }

        final ChatData chatData = new ChatData();
        chatData.setAuthorUid(authorId);
        chatData.setAuthorIsSeller(isAuthorSeller);
        chatData.setIsDm(isDm);
        chatData.setMessage(caption);
        ChatUtils.addAuthorNameandDp(chatData, LubbleSharedPrefs.getInstance().getUserFlair());
        chatData.setVidUrl(downloadUrl.toString());
        chatData.setCreatedTimestamp(System.currentTimeMillis());
        chatData.setServerTimestamp(ServerValue.TIMESTAMP);

        msgReference.push().setValue(chatData);
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

    public static IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UPLOAD_COMPLETED);
        filter.addAction(UPLOAD_ERROR);

        return filter;
    }

    private class DmInfoData {
        private String authorId;
        private boolean isDm;
        private boolean isAuthorSeller;

        DmInfoData(String authorId, boolean isDm, boolean isAuthorSeller) {
            this.authorId = authorId;
            this.isDm = isDm;
            this.isAuthorSeller = isAuthorSeller;
        }

    }
}
