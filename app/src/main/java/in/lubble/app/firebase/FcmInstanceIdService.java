package in.lubble.app.firebase;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by ishaan on 26/1/18.
 */

public class FcmInstanceIdService extends FirebaseInstanceIdService {

    private static final String TAG = "FcmInstanceIdService";

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        FirebaseDatabase.getInstance().getReference("users")
                .child(FirebaseAuth.getInstance().getUid() + "/token")
                .setValue(refreshedToken);
    }
}
