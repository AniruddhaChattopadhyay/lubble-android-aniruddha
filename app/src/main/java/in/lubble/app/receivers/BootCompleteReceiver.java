package in.lubble.app.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.utils.NotifUtils;

public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotifUtils.showAllPendingChatNotifs(context);
        Analytics.triggerEvent(AnalyticsEvents.PHONE_BOOT_COMPLETE, context);
    }
}
