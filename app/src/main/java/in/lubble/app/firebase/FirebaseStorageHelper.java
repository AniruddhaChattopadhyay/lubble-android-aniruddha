package in.lubble.app.firebase;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import in.lubble.app.BuildConfig;

/**
 * Created by ishaan on 30/1/18.
 */

public class FirebaseStorageHelper {

    public static StorageReference getDefaultBucketRef() {
        if ("dev".equalsIgnoreCase(BuildConfig.FLAVOR)) {
            return FirebaseStorage.getInstance("gs://lubble-dev-default").getReference();
        }
        return FirebaseStorage.getInstance("gs://lubble-in-default").getReference();
    }

    public static StorageReference getConvoBucketRef() {
        if ("dev".equalsIgnoreCase(BuildConfig.FLAVOR)) {
            return FirebaseStorage.getInstance("gs://lubble-dev-convo").getReference();
        }
        return FirebaseStorage.getInstance("gs://lubble-in-convo").getReference();
    }

}
