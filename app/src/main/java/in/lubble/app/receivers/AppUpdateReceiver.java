package in.lubble.app.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.io.File;

import in.lubble.app.BuildConfig;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.utils.FileUtils;
import in.lubble.app.utils.NotifUtils;

import static in.lubble.app.analytics.Analytics.setAnalyticsUser;

public class AppUpdateReceiver extends BroadcastReceiver {

    private static final String TAG = "AppUpdateReceiver";

    @Override
    public void onReceive(final Context context, Intent intent) {
        NotifUtils.showAllPendingChatNotifs(context, false);
        setAnalyticsUser(context);
        final Bundle bundle = new Bundle();
        bundle.putString("new_version_name", BuildConfig.VERSION_NAME);
        bundle.putInt("new_version_code", BuildConfig.VERSION_CODE);
        Analytics.triggerEvent(AnalyticsEvents.APP_UPDATED, bundle, context);
        FileUtils.deleteCache(context);

        if (BuildConfig.VERSION_CODE < 297) {
            // added in v295
            File databaseFile = context.getDatabasePath("lubble-dev.firebaseio.com_default");
            if (databaseFile != null && databaseFile.canWrite()) {
                boolean isDeleted = databaseFile.delete();
                final Bundle bundle1 = new Bundle();
                bundle1.putBoolean("is_deleted", isDeleted);
                Log.d(TAG, "is_deleted: " + isDeleted);
                Analytics.triggerEvent(AnalyticsEvents.RTDB_PRUNED, bundle1, context);
            }
        }
    }
}
