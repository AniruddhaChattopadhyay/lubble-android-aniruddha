package in.lubble.app;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;

/**
 * Created by ishaan on 20/1/18.
 */

public class LubbleApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        UserSharedPrefs.initializeInstance(getApplicationContext());

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        if (BuildConfig.DEBUG) {
            FirebaseDatabase.getInstance().setLogLevel(Logger.Level.DEBUG);
        }

        createNotifChannel();
    }

    private void createNotifChannel() {
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotifyMgr.createNotificationChannel(
                    new NotificationChannel(
                            Constants.DEFAULT_NOTIF_CHANNEL,
                            "Default",
                            NotificationManager.IMPORTANCE_HIGH));
        }
    }
}
