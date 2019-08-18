package in.lubble.app.analytics;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.work.*;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.segment.analytics.Properties;
import com.segment.analytics.Traits;
import in.lubble.app.BuildConfig;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Analytics {

    private static final String TAG = "Analytics";

    public static void triggerScreenEvent(Context context, Object className) {
        final String simpleName = ((Class<?>) className).getSimpleName();
        triggerEvent(simpleName, context);
        FirebaseAnalytics.getInstance(context).setCurrentScreen((Activity) context, simpleName, null);
        Bundle attributes = new Bundle();
        setupDefaultAttributes(context, attributes);
        com.segment.analytics.Analytics.with(context).screen(simpleName, bundleToSegmentProperties(attributes));
    }

    public static void triggerScreenEvent(Context context, Object className, Bundle attributes) {
        final String simpleName = ((Class<?>) className).getSimpleName();
        setupDefaultAttributes(context, attributes);
        triggerEvent(simpleName, attributes, context);
        FirebaseAnalytics.getInstance(context).setCurrentScreen((Activity) context, simpleName, null);
        com.segment.analytics.Analytics.with(context).screen(simpleName, bundleToSegmentProperties(attributes));
    }

    public static void triggerEvent(String title, Context context) {
        if (context != null) {
            Bundle attributes = new Bundle();
            setupDefaultAttributes(context, attributes);
            FirebaseAnalytics.getInstance(context).logEvent(title, attributes);
            //CleverTapAPI.getDefaultInstance(context).pushEvent(title, bundleToMap(attributes));
            com.segment.analytics.Analytics.with(context).track(title, bundleToSegmentProperties(attributes));
        }
    }

    public static void triggerEvent(String title, Bundle attributes, Context context) {
        if (context != null) {
            setupDefaultAttributes(context, attributes);
            FirebaseAnalytics.getInstance(context).logEvent(title, attributes);
            //CleverTapAPI.getDefaultInstance(context).pushEvent(title, bundleToMap(attributes));
            com.segment.analytics.Analytics.with(context).track(title, bundleToSegmentProperties(attributes));
        }
    }

    public static void triggerLoginEvent(Context context) {
        Bundle attributes = new Bundle();
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, attributes);
        //CleverTapAPI.getDefaultInstance(context).pushEvent("LOGIN", bundleToMap(attributes));
        com.segment.analytics.Analytics.with(context).track("LOGIN", bundleToSegmentProperties(attributes));
        setUser(context);
        setAnalyticsUser(context);
    }

    public static void triggerSignUpEvent(Context context) {
        Bundle attributes = new Bundle();
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, attributes);
        //CleverTapAPI.getDefaultInstance(context).pushEvent("SIGNUP", bundleToMap(attributes));
        com.segment.analytics.Analytics.with(context).track("SIGNUP", bundleToSegmentProperties(attributes));
        setUser(context);
        setAnalyticsUser(context);
    }

    private static void setUser(Context context) {
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        String userId = FirebaseAuth.getInstance().getUid();
        firebaseAnalytics.setUserId(userId);
        firebaseAnalytics.setUserProperty("uid", userId);
        firebaseAnalytics.setUserProperty("lubble_id", LubbleSharedPrefs.getInstance().getLubbleId());
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            firebaseAnalytics.setUserProperty("name", currentUser.getDisplayName());
        }
    }

    private static void unSetUser(Context context) {
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        firebaseAnalytics.setUserId(null);
        firebaseAnalytics.setUserProperty("uid", null);
        firebaseAnalytics.setUserProperty("lubble_id", null);
    }

    public static void triggerLogoutEvent(Context context) {
        if (context != null) {
            Bundle attributes = new Bundle();
            setupDefaultAttributes(context, attributes);
            FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context);
            firebaseAnalytics.logEvent(AnalyticsEvents.LOGOUT_SUCCESS_EVENT, attributes);
            //CleverTapAPI.getDefaultInstance(context).pushEvent("LOGOUT", bundleToMap(attributes));
            com.segment.analytics.Analytics.with(context).track("LOGOUT", bundleToSegmentProperties(attributes));
            com.segment.analytics.Analytics.with(context).reset();
            unSetUser(context);
        }
    }

    private static void setupDefaultAttributes(Context context, Bundle attributes) {
        if (context != null) {
            String uid = FirebaseAuth.getInstance().getUid();
            if (StringUtils.isValidString(uid)) {
                attributes.putString("uid", uid);
            }
            String lubbleId = LubbleSharedPrefs.getInstance().getLubbleId();
            attributes.putString("lubble_id", lubbleId);
        }
    }

    private static Map<String, Object> bundleToMap(Bundle attributes) {
        final Map<String, Object> map = new HashMap<>();
        for (String key : attributes.keySet()) {
            map.put(key, attributes.get(key));
        }
        return map;
    }

    private static Properties bundleToSegmentProperties(Bundle attributes) {
        final Properties properties = new Properties();
        for (String key : attributes.keySet()) {
            properties.put(key, attributes.get(key));
        }
        return properties;
    }

    public static void setAnalyticsUser(Context context) {
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // user is logged in
            final Traits traits = new Traits();
            traits.putName(currentUser.getDisplayName());
            traits.putPhone(currentUser.getPhoneNumber());
            traits.putEmail(currentUser.getEmail());
            traits.put("Firebase ID", FirebaseAuth.getInstance().getUid());
            traits.put("Lubble Id", LubbleSharedPrefs.getInstance().getLubbleId());
            traits.put("version name", BuildConfig.VERSION_NAME);
            traits.put("version code", BuildConfig.VERSION_CODE);
            com.segment.analytics.Analytics.with(context).identify(FirebaseAuth.getInstance().getUid(), traits, null);

            /**
             * Upload installed apps list
             */
            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresCharging(true)
                    .build();

            OneTimeWorkRequest uploadWorkRequest = new OneTimeWorkRequest.Builder(AppsListWorker.class)
                    .setConstraints(constraints)
                    .setBackoffCriteria(
                            BackoffPolicy.LINEAR,
                            10,
                            TimeUnit.MINUTES)
                    .build();
            WorkManager.getInstance(context).enqueue(uploadWorkRequest);

        }
    }

}