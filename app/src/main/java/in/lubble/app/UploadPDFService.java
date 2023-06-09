package in.lubble.app;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
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

import java.io.ByteArrayOutputStream;
import java.io.File;

import in.lubble.app.models.ChatData;
import in.lubble.app.utils.ChatUtils;
import in.lubble.app.utils.FileUtils;

import static in.lubble.app.firebase.FirebaseStorageHelper.getConvoBucketRef;
import static in.lubble.app.firebase.FirebaseStorageHelper.getDefaultBucketRef;
import static in.lubble.app.firebase.FirebaseStorageHelper.getMarketplaceBucketRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getDmMessagesRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getMessagesRef;

public class UploadPDFService extends BaseTaskService {
    private static final String TAG = "UploadPDFService";
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
                        intent.getStringExtra(EXTRA_CHAT_ID)
                );
            } else {
                final UploadPDFService.DmInfoData dmInfoData = new UploadPDFService.DmInfoData(
                        intent.getStringExtra(EXTRA_AUTHOR_ID),
                        intent.getBooleanExtra(EXTRA_IS_DM, false),
                        intent.getBooleanExtra(EXTRA_IS_AUTHOR_SELLER, false)
                );
                Uri fileUri = intent.getParcelableExtra(EXTRA_FILE_URI);
                uploadFromUri(
                        fileUri,
                        intent.getStringExtra(EXTRA_FILE_NAME),
                        intent.getStringExtra(EXTRA_UPLOAD_PATH),
                        intent.getStringExtra(EXTRA_CHAT_ID),
                        bucketId == BUCKET_CONVO,
                        null,
                        dmInfoData
                );
            }
        }

        return START_REDELIVER_INTENT;
    }


    private void uploadFromUriWithMetadata(final Uri fileUri, final String fileName, final String uploadPath, final String groupId) {
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
                    uploadFromUri(fileUri, fileName, uploadPath, groupId, false, metadata, null);
                } else {
                    taskCompleted();
                }
            }
        });
    }


    private void uploadFromUri(final Uri fileUri, final String fileName, final String uploadPath, final String groupId,
                               final boolean toTransmit, @Nullable final StorageMetadata metadata, @Nullable final UploadPDFService.DmInfoData dmInfoData) {
        Log.d(TAG, "uploadFromUri:src:" + fileUri.toString());

        showProgressNotification(getString(R.string.progress_uploading), 0, 0);
        final StorageReference pdfreference = mStorageRef.child(uploadPath)
                .child(fileName + ".pdf");
        uploadFile(fileUri, pdfreference, fileName, uploadPath, metadata, toTransmit, groupId, dmInfoData);
    }

    @Nullable
    private Bitmap pdfToBitmap(Uri uri) {
        File pdfFile = new File(uri.getPath());
        Bitmap bitmap = null;
        try {
            PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY));
            PdfRenderer.Page page = renderer.openPage(0);

            int width = getResources().getDisplayMetrics().densityDpi / 72 * page.getWidth();
            int height = getResources().getDisplayMetrics().densityDpi / 72 * page.getHeight();
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.WHITE);
            canvas.drawBitmap(bitmap, 0, 0, null);
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            page.close();
            renderer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(ex);
        }
        return bitmap;

    }

    private void uploadFile(final Uri FileUri, final StorageReference pdfRef, final String fileName, final String uploadPath, @Nullable StorageMetadata metadata, final boolean toTransmit, final String groupId, @Nullable final UploadPDFService.DmInfoData dmInfoData) {
        // Upload file to Firebase Storage
        Log.d(TAG, "uploadFromUri:dst:" + pdfRef.getPath());
        final UploadTask uploadTask;
        final UploadTask uploadTaskThumbnail;
        final Trace uploadTime = FirebasePerformance.getInstance().newTrace(TRACK_UPLOAD_TIME);
        uploadTime.start();
        if (metadata != null) {
            uploadTask = pdfRef.putFile(FileUri, metadata);
        } else {
            uploadTask = pdfRef.putFile(FileUri);
        }
        Bitmap bitmap = pdfToBitmap(FileUri);
        if (bitmap != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            final StorageReference pdfRefThumbnail = mStorageRef.child(uploadPath)
                    .child(fileName + " thumbnail");

            uploadTaskThumbnail = pdfRefThumbnail.putBytes(data);
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
                            pdfRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        final Uri downloadUri = task.getResult();
                                        uploadTaskThumbnail.
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
                                                        uploadTime.stop();

                                                        pdfRefThumbnail.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Uri> task) {
                                                                if (task.isSuccessful()) {
                                                                    final Uri downloadUriThumbnail = task.getResult();

                                                                    // [START_EXCLUDE]
                                                                    broadcastUploadFinished(downloadUri, downloadUriThumbnail, fileName, FileUri, toTransmit, groupId, dmInfoData);
                                                                    showUploadFinishedNotification(downloadUri, downloadUriThumbnail, FileUri, toTransmit);
                                                                    taskCompleted();
                                                                    // [END_EXCLUDE]
                                                                } else {
                                                                    Log.d(TAG, "onComplete: failed");

                                                                    // [START_EXCLUDE]
                                                                    broadcastUploadFinished(null, null, null, FileUri, toTransmit, groupId, dmInfoData);
                                                                    showUploadFinishedNotification(null, null, FileUri, toTransmit);
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
                                                        broadcastUploadFinished(null, null, null, FileUri, toTransmit, groupId, dmInfoData);
                                                        showUploadFinishedNotification(null, null, FileUri, toTransmit);
                                                        taskCompleted();
                                                        //[END_EXCLUDE]
                                                    }
                                                });
                                    } else {
                                        Log.d(TAG, "onComplete: failed");

                                        // [START_EXCLUDE]
                                        broadcastUploadFinished(null, null, null, FileUri, toTransmit, groupId, dmInfoData);
                                        showUploadFinishedNotification(null, null, FileUri, toTransmit);
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
                            broadcastUploadFinished(null, null, null, FileUri, toTransmit, groupId, dmInfoData);
                            showUploadFinishedNotification(null, null, FileUri, toTransmit);
                            taskCompleted();
                        }
                    });
        } else {
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
                            pdfRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        final Uri downloadUri = task.getResult();
                                        // [START_EXCLUDE]
                                        broadcastUploadFinished(downloadUri, null, fileName, FileUri, toTransmit, groupId, dmInfoData);
                                        showUploadFinishedNotification(downloadUri, null, FileUri, toTransmit);
                                        taskCompleted();
                                        // [END_EXCLUDE]
                                    } else {
                                        Log.d(TAG, "onComplete: failed");

                                        // [START_EXCLUDE]
                                        broadcastUploadFinished(null, null, fileName, FileUri, toTransmit, groupId, dmInfoData);
                                        showUploadFinishedNotification(null, null, FileUri, toTransmit);
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
                            broadcastUploadFinished(null, null, fileName, FileUri, toTransmit, groupId, dmInfoData);
                            showUploadFinishedNotification(null, null, FileUri, toTransmit);
                            taskCompleted();
                            // [END_EXCLUDE]
                        }
                    });
        }
    }

    /**
     * Broadcast finished upload (success or failure).
     *
     * @return true if a running receiver received the broadcast.
     */
    private boolean broadcastUploadFinished(@Nullable Uri downloadUrl, @Nullable Uri downloadUriThumbnail, @Nullable String filename, @Nullable Uri fileUri, boolean toTransmit,
                                            String chatId, UploadPDFService.DmInfoData dmInfoData) {
        boolean success = downloadUrl != null;

        String action = success ? UPLOAD_COMPLETED : UPLOAD_ERROR;

        Intent broadcast = new Intent(action)
                .putExtra(EXTRA_DOWNLOAD_URL, downloadUrl)
                .putExtra(EXTRA_FILE_URI, fileUri);

        if (toTransmit && success) {
            transmitMedia(downloadUrl, downloadUriThumbnail, filename, chatId, dmInfoData.isDm, dmInfoData.authorId, dmInfoData.isAuthorSeller);
        }

        return LocalBroadcastManager.getInstance(getApplicationContext())
                .sendBroadcast(broadcast);
    }

    private void transmitMedia(Uri downloadUrl, Uri downloadThumbnailUrl, String filename, String chatId, boolean isDm, String authorId, boolean isAuthorSeller) {
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
        chatData.setMessage("\uD83D\uDCC4 PDF Attached");
        ChatUtils.addAuthorNameandDp(chatData, LubbleSharedPrefs.getInstance().getUserFlair());
        chatData.setPdfFileName(filename);
        if (downloadThumbnailUrl == null)
            chatData.setPdfThumbnailUrl("https://i.imgur.com/ma03D59.png");
        else
            chatData.setPdfThumbnailUrl(downloadThumbnailUrl.toString());

        chatData.setPdfUrl(downloadUrl.toString());
        chatData.setCreatedTimestamp(System.currentTimeMillis());
        chatData.setServerTimestamp(ServerValue.TIMESTAMP);

        msgReference.push().setValue(chatData);
    }

    /**
     * Show a notification for a finished upload.
     */
    private void showUploadFinishedNotification(@Nullable Uri downloadUrl, @Nullable Uri downloadUrlThumbnail, @Nullable Uri fileUri, boolean isConvo) {
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

        boolean success = downloadUrl != null && downloadUrlThumbnail != null;
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
