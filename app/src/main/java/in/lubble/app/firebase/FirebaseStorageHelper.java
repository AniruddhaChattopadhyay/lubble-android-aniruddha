package in.lubble.app.firebase;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Created by ishaan on 30/1/18.
 */

public class FirebaseStorageHelper {

    public static StorageReference getDefaultBucketRef() {
        return FirebaseStorage.getInstance("gs://lubble-in-default").getReference();
    }

    public static StorageReference getConvoBucketRef() {
        return FirebaseStorage.getInstance("gs://lubble-in-convo").getReference();
    }

}
