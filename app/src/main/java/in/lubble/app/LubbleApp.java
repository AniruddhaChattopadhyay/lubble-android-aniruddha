package in.lubble.app;

import android.app.Application;

/**
 * Created by ishaan on 20/1/18.
 */

public class LubbleApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        UserSharedPrefs.initializeInstance(getApplicationContext());

    }
}
