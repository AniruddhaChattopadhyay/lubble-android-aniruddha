package in.lubble.app.utils;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import in.lubble.app.BuildConfig;
import in.lubble.app.R;
import permissions.dispatcher.PermissionRequest;

import static in.lubble.app.LubbleApp.getAppContext;

/**
 * Created by ishaangarg on 17/11/17.
 */

public class FileUtils {

    public static double Video_Size = 0;

    public static File createImageFile(Context context) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return image;
    }

    public static Intent getPickImageIntent(Context context, File cameraPic) {
        Intent chooserIntent = null;

        List<Intent> intentList = new ArrayList<>();

        Intent pickIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intentList = addIntentsToList(context, intentList, pickIntent);
        intentList = addIntentsToList(context, intentList, getTakePhotoIntent(context, cameraPic));

        if (intentList.size() > 0) {
            chooserIntent = Intent.createChooser(intentList.remove(intentList.size() - 1), "Choose Photo");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new Parcelable[]{}));
        }

        return chooserIntent;
    }

    private static List<Intent> addIntentsToList(Context context, List<Intent> list, Intent intent) {
        List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resInfo) {
            String packageName = resolveInfo.activityInfo.packageName;
            Intent targetedIntent = new Intent(intent);
            targetedIntent.setPackage(packageName);
            list.add(targetedIntent);
        }
        return list;
    }

    public static Intent getGalleryIntent(Context context) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        photoPickerIntent.setType("*/*");
        photoPickerIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
        photoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        Log.d("GroupID", Intent.EXTRA_MIME_TYPES);
        return photoPickerIntent;
    }

    public static String getMimeType(Uri uri) {
        String mimeType = null;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = getAppContext().getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }

    public static String getFileExtension(Context context, Uri uri) {
        String extension;

        //Check uri format to avoid null
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //If scheme is a content
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());
        }

        return extension;
    }

    public static Intent getTakePhotoIntent(Context context, File cameraPic) {
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Continue only if the File was successfully created
        if (cameraPic != null) {
            Uri photoURI = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", cameraPic);
            takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        }
        return takePhotoIntent;
    }

    @Nullable
    public static File getFileFromInputStreamUri(Context context, Uri uri) {
        InputStream inputStream = null;
        File photoFile = null;

        if (uri.getAuthority() != null) {
            try {
                inputStream = context.getContentResolver().openInputStream(uri); // context needed
                String mimeType = getMimeType(uri);
                if (mimeType.contains("video"))
                    photoFile = createTemporalVideoFileFrom(context, inputStream);
                else if (mimeType.contains("image")) {
                    photoFile = createTemporalFileFrom(context, inputStream, mimeType);
                } else {
                    String name = getFileNameFromUri(uri);
                    photoFile = createTemporalGenericFileFrom(context, inputStream, mimeType, name);
                }

            } catch (FileNotFoundException e) {
                // log
            } catch (IOException e) {
                // log
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return photoFile;
    }

    private static File createTemporalVideoFileFrom(Context context, InputStream inputStream) throws IOException {
        File targetFile = null;

        if (inputStream != null) {
            int read;
            byte[] buffer = new byte[8 * 1024];

            targetFile = createTemporalVideoFile(context);
            OutputStream outputStream = new FileOutputStream(targetFile);

            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();

            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return targetFile;
    }

    private static File createTemporalGenericFileFrom(Context context, InputStream inputStream, String mimeType, String fileName) throws IOException {
        File targetFile = null;

        if (inputStream != null) {
            int read;
            byte[] buffer = new byte[8 * 1024];

            targetFile = createTemporalGenericFile(context, mimeType, fileName);
            OutputStream outputStream = new FileOutputStream(targetFile);

            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();

            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return targetFile;
    }

    private static File createTemporalGenericFile(Context context, String mimeType, String fileName) {
        String extension = "pdf";
        if (!TextUtils.isEmpty(mimeType)) {
            try {
                String calculatedExtension = mimeType.split("/")[1];
                if (!TextUtils.isEmpty(calculatedExtension)) {
                    extension = calculatedExtension;
                }
            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        }
        return new File(context.getExternalCacheDir(), fileName + "." + extension); // context needed
    }

    private static File createTemporalFileFrom(Context context, InputStream inputStream, String mimeType) throws IOException {
        File targetFile = null;

        if (inputStream != null) {
            int read;
            byte[] buffer = new byte[8 * 1024];

            targetFile = createTemporalFile(context, mimeType);
            OutputStream outputStream = new FileOutputStream(targetFile);

            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();

            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return targetFile;
    }

    private static File createTemporalFile(Context context, String mimeType) {
        String extension = "jpg";
        if (!TextUtils.isEmpty(mimeType)) {
            try {
                String calculatedExtension = mimeType.split("/")[1];
                if (!TextUtils.isEmpty(calculatedExtension)) {
                    extension = calculatedExtension;
                }
            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        }
        return new File(context.getExternalCacheDir(), String.valueOf(System.currentTimeMillis()) + "." + extension); // context needed
    }

    private static File createTemporalVideoFile(Context context) {
        return new File(context.getExternalCacheDir(), String.valueOf(System.currentTimeMillis()) + ".mp4"); // context needed
    }

    public static void showStoragePermRationale(Context context, final PermissionRequest request) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(context.getString(R.string.storage_perm_rationale_title));
        alertDialog.setMessage(context.getString(R.string.storage_perm_rationale_subtitle));
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.all_allow), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                request.proceed();
            }
        });
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.all_deny), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                request.cancel();
            }
        });
        alertDialog.show();
    }

    public static String saveImageInGallery(Bitmap image, String msgId, Context context, @Nullable Uri uri) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && isExternalStorageWritable()) {
            String savedImagePath = null;
            String extension = getExtensionFromMime(uri);

            String imageFileName = "JPEG_" + msgId + "." + extension;
            File storageDir = new File(
                    context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                            + File.separator + "Lubble_pics");
            boolean success = true;
            if (!storageDir.exists()) {
                success = storageDir.mkdirs();
            }
            if (success) {
                File imageFile = new File(storageDir, imageFileName);
                savedImagePath = imageFile.getAbsolutePath();
                try {
                    OutputStream fOut = new FileOutputStream(imageFile);
                    image.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                    fOut.close();
                    // Add the image to the system gallery
                    galleryAddPic(context, savedImagePath);
                    return savedImagePath;
                } catch (Exception e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private static String getExtensionFromMime(@Nullable Uri uri) {
        String extension = "jpg";
        if (uri != null) {
            try {
                final String mimeType = getMimeType(uri);
                if (mimeType != null) {
                    String calculatedExtension = mimeType.split("/")[1];
                    if (!TextUtils.isEmpty(calculatedExtension)) {
                        extension = calculatedExtension;
                    }
                }
            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        }
        return extension;
    }

    private static void galleryAddPic(Context context, String imagePath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(imagePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    @Nullable
    public static String getSavedImageForMsgId(Context context, String msgId, Uri uri) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && isExternalStorageReadable()) {
            String extension = getExtensionFromMime(uri);
            File imgFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    + File.separator + "Lubble_pics" + File.separator + "JPEG_" + msgId + "." + extension);

            if (imgFile.exists()) {
                //return BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                return imgFile.getAbsolutePath();
            }
        }
        return null;
    }

    /**
     * only if cache size exceeds 3MB
     */
    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            e.printStackTrace();
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }


    /**
     * This temporarily saves bitmap in cache to get the URI then deletes it in next runover
     */
    @Nullable
    public static Uri getUriFromTempBitmap(Context inContext, Bitmap bitmap, String title, String mime) {
        try {
            File cachePath = new File(inContext.getCacheDir(), "images");
            cachePath.mkdirs();
            deleteFilesIn(cachePath);
            FileOutputStream stream = new FileOutputStream(cachePath + File.separator + title + "." + mime); // overwrites this image every time
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            stream.close();
            File imagePath = new File(inContext.getCacheDir(), "images");
            File newFile = new File(imagePath, title + "." + mime);
            return FileProvider.getUriForFile(inContext, inContext.getPackageName() + ".fileprovider", newFile);
        } catch (IOException e) {
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(e);
            return null;
        }
    }

    private static void deleteFilesIn(@NonNull File directory) {
        final File[] files = directory.listFiles();
        if (files != null && files.length > 0) {
            for (File child : files) {
                child.delete();
            }
        }
    }

    public static String getFileNameFromUri(Uri uri) {
        Cursor returnCursor =
                getAppContext().getContentResolver().query(uri, null, null, null, null);
        if (returnCursor == null)
            return uri.getLastPathSegment();
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        //int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
    }

}
