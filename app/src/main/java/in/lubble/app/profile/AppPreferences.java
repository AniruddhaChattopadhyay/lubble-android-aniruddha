package in.lubble.app.profile;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPreferences {
    public static final String INSTA_APP_PREFERENCES_FILE_NAME = "userdata";
    public static final String INSTA_USER_ID = "userID";
    public static final String INSTA_TOKEN = "token";
    public static final String INSTA_PROFILE_PIC = "profile_pic";
    public static final String INSTA_USER_NAME = "username";

    private SharedPreferences preferences;

    public AppPreferences(Context context) {
        this.preferences = context.getSharedPreferences(INSTA_APP_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
    }

    public String getString(String key) {
        return preferences.getString(key, null);
    }

    public void putString(String key, String value)
    {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void clear()
    {
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }
}
