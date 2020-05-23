package in.lubble.app.notifications;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by ishaan on 11/3/18.
 */

public class UnreadChatsSharedPrefs {

    private static UnreadChatsSharedPrefs instance;
    private final SharedPreferences preferences;
    private final String UNREAD_CHATS_SHARED_PREFERENCE_KEY = "in.lubble.UnreadChatsSharedPrefs";

    private final String REFERRER_UID = "referrer_uid";

    private UnreadChatsSharedPrefs(Context context) {
        preferences = context.getSharedPreferences(UNREAD_CHATS_SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
    }

    public static void initializeInstance(Context context) {
        if (instance == null) {
            instance = new UnreadChatsSharedPrefs(context);
        }
    }

    public static UnreadChatsSharedPrefs getInstance() {
        if (instance == null) {
            throw new IllegalStateException(UnreadChatsSharedPrefs.class.getCanonicalName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }
        return instance;
    }

    public SharedPreferences getPreferences() {
        return preferences;
    }

    /**
     * Clear all SharedPreferences
     */
    public void clearAll() {
        preferences.edit().clear().commit();
    }


    //******************************************/

    public String getReferrerUid() {
        return preferences.getString(REFERRER_UID, "");
    }

    public boolean setReferrerUid(String uid) {

        return preferences.edit().putString(REFERRER_UID, uid).commit();
    }


}
