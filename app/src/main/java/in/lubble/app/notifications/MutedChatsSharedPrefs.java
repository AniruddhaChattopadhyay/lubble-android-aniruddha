package in.lubble.app.notifications;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by ishaan on 11/3/18.
 */

public class MutedChatsSharedPrefs {

    private static MutedChatsSharedPrefs instance;
    private final SharedPreferences preferences;
    private final String MUTED_CHATS_SHARED_PREFERENCE_KEY = "in.lubble.MutedChatsSharedPrefs";

    private MutedChatsSharedPrefs(Context context) {
        preferences = context.getSharedPreferences(MUTED_CHATS_SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
    }

    public static void initializeInstance(Context context) {
        if (instance == null) {
            instance = new MutedChatsSharedPrefs(context);
        }
    }

    public static MutedChatsSharedPrefs getInstance() {
        if (instance == null) {
            throw new IllegalStateException(MutedChatsSharedPrefs.class.getCanonicalName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }
        return instance;
    }

    public SharedPreferences getPreferences() {
        return preferences;
    }

    public static boolean isGroupMuted(String groupId) {
        return MutedChatsSharedPrefs.getInstance().getPreferences().getBoolean(groupId, false);
    }

    /**
     * Clear all SharedPreferences
     */
    public void clearAll() {
        preferences.edit().clear().commit();
    }


    //******************************************/


}
