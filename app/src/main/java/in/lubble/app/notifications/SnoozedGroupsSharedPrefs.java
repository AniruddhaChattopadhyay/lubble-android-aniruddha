package in.lubble.app.notifications;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by ishaan on 11/3/18.
 */

public class SnoozedGroupsSharedPrefs {

    private static SnoozedGroupsSharedPrefs instance;
    private final SharedPreferences preferences;
    private final String SNOOZED_GROUPS_SHARED_PREFERENCE_KEY = "in.lubble.SnoozedGroupsSharedPrefs";

    // if a group is snoozed till this TS then notifs are permanently disabled
    public static final long DISABLED_NOTIFS_TS = 1337;

    private SnoozedGroupsSharedPrefs(Context context) {
        preferences = context.getSharedPreferences(SNOOZED_GROUPS_SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
    }

    public static void initializeInstance(Context context) {
        if (instance == null) {
            instance = new SnoozedGroupsSharedPrefs(context);
        }
    }

    public static SnoozedGroupsSharedPrefs getInstance() {
        if (instance == null) {
            throw new IllegalStateException(SnoozedGroupsSharedPrefs.class.getCanonicalName() +
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


}
