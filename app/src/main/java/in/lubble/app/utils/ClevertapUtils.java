package in.lubble.app.utils;

import android.content.Context;
import com.clevertap.android.sdk.CleverTapAPI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import in.lubble.app.BuildConfig;
import in.lubble.app.LubbleSharedPrefs;

import java.util.HashMap;

public class ClevertapUtils {

    public static void setUser(Context context) {
        HashMap<String, Object> profileUpdate = new HashMap<>();

        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // user is logged in
            profileUpdate.put("Name", currentUser.getDisplayName());
            profileUpdate.put("Identity", FirebaseAuth.getInstance().getUid());
            profileUpdate.put("Firebase ID", FirebaseAuth.getInstance().getUid());
            profileUpdate.put("Lubble Id", LubbleSharedPrefs.getInstance().getLubbleId());
            profileUpdate.put("Phone", currentUser.getPhoneNumber());
            profileUpdate.put("Photo", currentUser.getPhotoUrl());
            profileUpdate.put("version name", BuildConfig.VERSION_NAME);
            profileUpdate.put("version code", BuildConfig.VERSION_CODE);

            CleverTapAPI.getDefaultInstance(context).pushProfile(profileUpdate);
        }
    }

}
