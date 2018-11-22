package in.lubble.app.utils;

import android.content.Context;
import android.provider.Settings;

public class LocationUtils {

    public static boolean isMockLocationsON(Context context) {
        // returns true if mock location enabled, false if not enabled.
        if (Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ALLOW_MOCK_LOCATION).equals("0"))
            return false;
        else
            return true;
    }

}
