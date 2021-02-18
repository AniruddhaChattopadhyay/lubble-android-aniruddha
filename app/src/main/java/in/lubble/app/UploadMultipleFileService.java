package in.lubble.app;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

import in.lubble.app.models.ChatData;
import in.lubble.app.utils.FileUtils;

import static in.lubble.app.UploadFileService.EXTRA_FILE_URI;
import static in.lubble.app.firebase.FirebaseStorageHelper.getConvoBucketRef;
import static in.lubble.app.firebase.FirebaseStorageHelper.getDefaultBucketRef;
import static in.lubble.app.firebase.FirebaseStorageHelper.getMarketplaceBucketRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getDmMessagesRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getMessagesRef;
import static in.lubble.app.utils.FileUtils.getUriFromTempBitmap;

/**
 * Service to handle uploading files to Firebase Storage
 * Created by ishaan on 26/1/18.
 */

public class UploadMultipleFileService extends BaseTaskService {

    private static final String TAG = "UploadMultiFileService";
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
    public static final String EXTRA_MULTI_FILE_NAME = "extra_multi_file_name";
    public static final String EXTRA_MULTI_FILE_URI = "extra_file_uri";
    public static final String EXTRA_BUCKET = "extra_bucket";
    public static final String EXTRA_UPLOAD_PATH = "extra_upload_path";
    public static final String EXTRA_DOWNLOAD_URL = "extra_download_url";
    public static final String EXTRA_CAPTION = "extra_caption";
    public static final String EXTRA_CHAT_ID = "extra_chat_id";
    public static final String EXTRA_IS_DM = "EXTRA_IS_DM";
    public static final String EXTRA_AUTHOR_ID = "EXTRA_AUTHOR_ID";
    public static final String EXTRA_IS_AUTHOR_SELLER = "EXTRA_IS_AUTHOR_SELLER";
    public Uri compressedUri;
    private StorageReference mStorageRef;
    private int i;

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

        if (ACTION_UPLOAD.equals(intent.getAction())) {
            if (bucketId == BUCKET_MARKETPLACE) {
                Bundle bundle = intent.getBundleExtra("BUNDLE");
                final ArrayList<Uri> imageUriList = (ArrayList<Uri>) bundle.getSerializable(EXTRA_MULTI_FILE_URI);
                final ArrayList<String> fileNameList = (ArrayList<String>) bundle.getSerializable(EXTRA_MULTI_FILE_NAME);
                uploadFromUriWithMetadata(
                        imageUriList,
                        fileNameList,
                        intent.getStringExtra(EXTRA_UPLOAD_PATH),
                        intent.getStringExtra(EXTRA_CAPTION),
                        intent.getStringExtra(EXTRA_CHAT_ID)
                );
            } else {
                final DmInfoData dmInfoData = new DmInfoData(
                        intent.getStringExtra(EXTRA_AUTHOR_ID),
                        intent.getBooleanExtra(EXTRA_IS_DM, false),
                        intent.getBooleanExtra(EXTRA_IS_AUTHOR_SELLER, false)
                );
                Bundle bundle = intent.getBundleExtra("BUNDLE");
                final ArrayList<Uri> imageUriList = (ArrayList<Uri>) bundle.getSerializable(EXTRA_MULTI_FILE_URI);
                final ArrayList<String> fileNameList = (ArrayList<String>) bundle.getSerializable(EXTRA_MULTI_FILE_NAME);
                uploadFromUri(
                        imageUriList,
                        fileNameList,
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


    private void uploadFromUriWithMetadata(final ArrayList<Uri> fileUri, final ArrayList<String> fileName, final String uploadPath, final String caption, final String groupId) {
        Log.d(TAG, "upload:" + fileUri.toString());

        showProgressNotification(getString(R.string.progress_uploading), 0, 0);

        FirebaseAuth.getInstance().getAccessToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<GetTokenResult> task) {
                if (task.isSuccessful()) {
                    // Create file metadata including the content type
                    StorageMetadata metadata = new StorageMetadata.Builder()
                            .setContentType(FileUtils.getMimeType(fileUri.get(0)))
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

    private void uploadFromUri(final ArrayList<Uri> fileUri, final ArrayList<String> fileName, final String uploadPath, final String caption, final String groupId,
                               final boolean toTransmit, @Nullable final StorageMetadata metadata, @Nullable final DmInfoData dmInfoData) {
        Log.d(TAG, "uploadFromUri:src:" + fileUri.toString());

        showProgressNotification(getString(R.string.progress_uploading), 0, 0);
        uploadFile(fileUri,fileName, uploadPath,metadata, toTransmit, caption, groupId, dmInfoData);
    }
    private Uri getCompressedImage(Uri fileUri, String fileName){
        GlideApp.with(this).asBitmap()
                .override(1000, 1000)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .load(fileUri)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        final Uri compressedFileUri = getUriFromTempBitmap(UploadMultipleFileService.this, resource, fileName,
                                MimeTypeMap.getFileExtensionFromUrl(fileUri.toString()));
                        compressedUri = compressedFileUri;
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
        return compressedUri;
    }
    private void uploadFile(final ArrayList<Uri> fileUriList,ArrayList<String> fileNameList, final String uploadPath, @Nullable StorageMetadata metadata, final boolean toTransmit, final String caption, final String groupId, @Nullable final DmInfoData dmInfoData) {
        ArrayList<Uri> downLoadUriList = new ArrayList<>();
        final Context context =this;
        for(i=0;i<fileUriList.size();i++){
            final StorageReference photoRef = mStorageRef.child(uploadPath)
                    .child(fileNameList.get(i));
            Log.d(TAG, "uploadFromUri:dst:" + photoRef.getPath());
            final UploadTask uploadTask;
            if (metadata != null) {
                uploadTask = photoRef.putFile(fileUriList.get(i), metadata);
            } else {
                uploadTask = photoRef.putFile(fileUriList.get(i));
            }
            GlideApp.with(this).asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .load(fileUriList.get(i))
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            if (resource.getWidth() > 1000 || resource.getHeight() > 1000) {
                                fileUriList.set(i,getCompressedImage(fileUriList.get(i),fileNameList.get(i)));
                            }
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    });
            // Upload file to Firebase Storage
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
                                        downLoadUriList.add(downloadUri);
                                        if(downLoadUriList.size()==fileUriList.size()){
                                            broadcastUploadFinished(downLoadUriList, fileUriList, toTransmit, caption, groupId, dmInfoData);
                                            showUploadFinishedNotification(downLoadUriList, fileUriList, toTransmit);
                                            taskCompleted();
                                        }
                                    } else {
                                        Log.d(TAG, "onComplete: failed");
                                        // [START_EXCLUDE]
                                        broadcastUploadFinished(null, fileUriList, toTransmit, caption, groupId, dmInfoData);
                                        showUploadFinishedNotification(null, fileUriList, toTransmit);
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
                            broadcastUploadFinished(null, fileUriList, toTransmit, caption, groupId, dmInfoData);
                            showUploadFinishedNotification(null, fileUriList, toTransmit);
                            taskCompleted();
                            // [END_EXCLUDE]
                        }
                    });
        }

    }
    // [END upload_from_uri]

    /**
     * Broadcast finished upload (success or failure).
     *
     * @return true if a running receiver received the broadcast.
     */
    private boolean broadcastUploadFinished(@Nullable ArrayList<Uri> downloadUrlList, @Nullable ArrayList<Uri> fileUriList, boolean toTransmit, String caption,
                                            String chatId, DmInfoData dmInfoData) {
        boolean success = downloadUrlList != null;

        String action = success ? UPLOAD_COMPLETED : UPLOAD_ERROR;

        Intent broadcast = new Intent(action)
                .putExtra(EXTRA_DOWNLOAD_URL, downloadUrlList.get(0))
                .putExtra(EXTRA_FILE_URI, fileUriList.get(0));

        if (toTransmit && success) {
            transmitMedia(downloadUrlList, caption, chatId, dmInfoData.isDm, dmInfoData.authorId, dmInfoData.isAuthorSeller);
        }

        return LocalBroadcastManager.getInstance(getApplicationContext())
                .sendBroadcast(broadcast);
    }

    private void transmitMedia(ArrayList<Uri> downloadUriList, String caption, String chatId, boolean isDm, String authorId, boolean isAuthorSeller) {
        final DatabaseReference msgReference;
        if (isDm) {
            msgReference = getDmMessagesRef().child(chatId);
        } else {
            msgReference = getMessagesRef().child(chatId);
        }
        ArrayList<String> downloadUrlList = new ArrayList<>();
        for(Uri uri: downloadUriList){
            downloadUrlList.add(uri.toString());
        }
        final ChatData chatData = new ChatData();
        chatData.setAuthorUid(authorId);
        chatData.setAuthorIsSeller(isAuthorSeller);
        chatData.setIsDm(isDm);
        chatData.setMessage(caption);
        chatData.setMultipleImagesUrl(downloadUrlList);
        chatData.setCreatedTimestamp(System.currentTimeMillis());
        chatData.setServerTimestamp(ServerValue.TIMESTAMP);

        msgReference.push().setValue(chatData);
    }

    /**
     * Show a notification for a finished upload.
     */
    private void showUploadFinishedNotification(@Nullable ArrayList<Uri> downloadUrlList, @Nullable ArrayList<Uri> fileUriList, boolean isConvo) {
        // Hide the progress notification
        dismissProgressNotification();
        if (isConvo) {
            // img uploaded for chat convo, just dismiss the progress notif
            // do NOT show complete noif
            return;
        }

        // Make Intent to MainActivity
        Intent intent = new Intent(this, MainActivity.class)
                .putExtra(EXTRA_DOWNLOAD_URL, downloadUrlList.get(0))
                .putExtra(EXTRA_FILE_URI, fileUriList.get(0))
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        boolean success = downloadUrlList != null;
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
