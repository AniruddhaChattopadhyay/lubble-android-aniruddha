package in.lubble.app;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by ishaangarg on 04/11/17.
 */

public class UserSharedPrefs {

    private static UserSharedPrefs instance;
    private final SharedPreferences preferences;
    private final String APP_SHARED_PREFERENCE_KEY = "app_shared_prefs_key";

    public static final String DEFAULT_USER_ID = "-1";

    private final String USER_ID = "UserId";
    private final String AUTH_TOKEN = "authToken";

    private UserSharedPrefs(Context context) {
        preferences = context.getSharedPreferences(APP_SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
    }

    public static void initializeInstance(Context context) {
        if (instance == null) {
            instance = new UserSharedPrefs(context);
        }
    }

    public static UserSharedPrefs getInstance() {
        if (instance == null) {
            throw new IllegalStateException(UserSharedPrefs.class.getCanonicalName() +
                    " is not initialized, call initializeInstance() method first.");
        }
        return instance;
    }

    public SharedPreferences getPreferences() {
        return preferences;
    }

    /**
     * Clear all SharedPreferences for RiderInfo
     */
    public void clearAll() {
        preferences.edit().clear().commit();
    }

    //******************************************/

    public String getUserId() {
        return preferences.getString(USER_ID, DEFAULT_USER_ID);
    }

    public boolean setUserId(String userId) {

        return preferences.edit().putString(USER_ID, userId).commit();
    }

    public String getAuthToken() {
        return preferences.getString(AUTH_TOKEN, null);
    }

    public boolean setAuthToken(String token) {
        return preferences.edit().putString(AUTH_TOKEN, token).commit();
    }

}
