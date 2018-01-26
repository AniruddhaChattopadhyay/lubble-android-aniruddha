package in.lubble.app;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by ishaan on 20/1/18.
 */

public class LubbleApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        UserSharedPrefs.initializeInstance(getApplicationContext());

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
