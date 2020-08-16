package in.lubble.app.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;

import static android.content.Intent.EXTRA_CHOSEN_COMPONENT;

public class ShareSheetReceiver extends BroadcastReceiver {

    private static final String TAG = "ShareSheetReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getExtras() != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            String selectedAppPackage = String.valueOf(intent.getExtras().get(EXTRA_CHOSEN_COMPONENT));
            Bundle bundle = new Bundle();
            bundle.putString("app_package", selectedAppPackage);
            Analytics.triggerEvent(AnalyticsEvents.SHARED_VIA, bundle, context);
            Log.d(TAG, "onReceive: " + selectedAppPackage);
        }
    }
}
