package in.lubble.app.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import in.lubble.app.analytics.AnalyticsEvents;

public class NotifDeleteBroadcastRecvr extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.hasExtra("groupId")) {
            final String groupId = intent.getStringExtra("groupId");
            if (groupId != null) {
                NotifUtils.sendNotifAnalyticEvent(AnalyticsEvents.NOTIF_DISMISSED, groupId, context);
            }
        }
    }
}