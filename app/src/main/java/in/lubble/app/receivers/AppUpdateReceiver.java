package in.lubble.app.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import in.lubble.app.BuildConfig;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.utils.FileUtils;
import in.lubble.app.utils.NotifUtils;

import static in.lubble.app.analytics.Analytics.setAnalyticsUser;

public class AppUpdateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        NotifUtils.showAllPendingChatNotifs(context);
        setAnalyticsUser(context);
        final Bundle bundle = new Bundle();
        bundle.putString("new_version_name", BuildConfig.VERSION_NAME);
        bundle.putInt("new_version_code", BuildConfig.VERSION_CODE);
        Analytics.triggerEvent(AnalyticsEvents.APP_UPDATED, bundle, context);
        FileUtils.deleteCache(context);
    }
}
