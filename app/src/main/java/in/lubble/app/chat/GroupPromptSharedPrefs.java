package in.lubble.app.chat;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by ishaan on 11/3/18.
 */

public class GroupPromptSharedPrefs {

    private static GroupPromptSharedPrefs instance;
    private final SharedPreferences preferences;
    private final String GROUP_PROMPT_SHARED_PREFERENCE_KEY = "in.lubble.GroupPromptSharedPrefs";

    private GroupPromptSharedPrefs(Context context) {
        preferences = context.getSharedPreferences(GROUP_PROMPT_SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
    }

    public static void initializeInstance(Context context) {
        if (instance == null) {
            instance = new GroupPromptSharedPrefs(context);
        }
    }

    public static GroupPromptSharedPrefs getInstance() {
        if (instance == null) {
            throw new IllegalStateException(GroupPromptSharedPrefs.class.getCanonicalName() +
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

    public void putGroupId(String groupId) {
        preferences.edit().putBoolean(groupId, true).apply();
    }

    public void removeGroupId(String groupId) {
        preferences.edit().remove(groupId).apply();
    }

    public boolean getGroupId(String groupId) {
        return preferences.getBoolean(groupId, false);
    }

}
