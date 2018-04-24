package in.lubble.app.notifications;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * For mapping notif server key to an int ID to later dismiss such notifs
 * Created by ishaan on 11/3/18.
 */

public class KeyMappingSharedPrefs {

    private static KeyMappingSharedPrefs instance;
    private final SharedPreferences preferences;
    private final String KEY_MAPPING_SHARED_PREFERENCE_KEY = "in.lubble.KeyMappingSharedPrefs";

    private KeyMappingSharedPrefs(Context context) {
        preferences = context.getSharedPreferences(KEY_MAPPING_SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
    }

    public static void initializeInstance(Context context) {
        if (instance == null) {
            instance = new KeyMappingSharedPrefs(context);
        }
    }

    public static KeyMappingSharedPrefs getInstance() {
        if (instance == null) {
            throw new IllegalStateException(KeyMappingSharedPrefs.class.getCanonicalName() +
                    " is not initialized, call initializeInstance(..) method first.");
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

}
