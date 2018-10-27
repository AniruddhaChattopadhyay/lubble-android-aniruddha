package in.lubble.app.firebase;

import android.util.Log;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import static in.lubble.app.firebase.RealtimeDbHelper.getThisUserRef;

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

        try {
            getThisUserRef().child("token").setValue(refreshedToken);
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }
}
