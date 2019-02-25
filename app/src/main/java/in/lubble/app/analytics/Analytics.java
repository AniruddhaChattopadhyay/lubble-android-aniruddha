package in.lubble.app.analytics;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import com.clevertap.android.sdk.CleverTapAPI;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.heapanalytics.android.Heap;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class Analytics {

    private static final String TAG = "Analytics";

    public static void triggerScreenEvent(Context context, Object className) {
        final String simpleName = ((Class<?>) className).getSimpleName();
        triggerEvent(simpleName, context);
        FirebaseAnalytics.getInstance(context).setCurrentScreen((Activity) context, simpleName, null);
    }

    public static void triggerScreenEvent(Context context, Object className, Bundle attributes) {
        final String simpleName = ((Class<?>) className).getSimpleName();
        triggerEvent(simpleName, attributes, context);
        FirebaseAnalytics.getInstance(context).setCurrentScreen((Activity) context, simpleName, null);
    }

    public static void triggerEvent(String title, Context context) {
        if (context != null) {
            Bundle attributes = new Bundle();
            setupDefaultAttributes(context, attributes);
            FirebaseAnalytics.getInstance(context).logEvent(title, attributes);
            CleverTapAPI.getDefaultInstance(context).pushEvent(title, bundleToMap(attributes));
        }
    }

    public static void triggerEvent(String title, Bundle attributes, Context context) {
        if (context != null) {
            setupDefaultAttributes(context, attributes);
            FirebaseAnalytics.getInstance(context).logEvent(title, attributes);
            CleverTapAPI.getDefaultInstance(context).pushEvent(title, bundleToMap(attributes));
        }
    }

    public static void triggerLoginEvent(Context context) {
        Bundle attributes = new Bundle();
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, attributes);
        CleverTapAPI.getDefaultInstance(context).pushEvent("LOGIN", bundleToMap(attributes));
        setUser(context);
    }

    public static void triggerSignUpEvent(Context context) {
        Bundle attributes = new Bundle();
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, attributes);
        CleverTapAPI.getDefaultInstance(context).pushEvent("SIGNUP", bundleToMap(attributes));
        setUser(context);
    }

    private static void setUser(Context context) {
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        String userId = FirebaseAuth.getInstance().getUid();
        firebaseAnalytics.setUserId(userId);
        firebaseAnalytics.setUserProperty("uid", userId);
        firebaseAnalytics.setUserProperty("lubble_id", LubbleSharedPrefs.getInstance().getLubbleId());
        firebaseAnalytics.setUserProperty("name", LubbleSharedPrefs.getInstance().getFullName());
        Heap.identify(userId.toLowerCase());
        Map<String, String> props = new HashMap<>();
        props.put("uid", userId);
        props.put("lubble_id", LubbleSharedPrefs.getInstance().getLubbleId());
        props.put("name", LubbleSharedPrefs.getInstance().getFullName());
        Heap.addUserProperties(props);
    }

    private static void unSetUser(Context context) {
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        firebaseAnalytics.setUserId(null);
        firebaseAnalytics.setUserProperty("uid", null);
        firebaseAnalytics.setUserProperty("lubble_id", null);
        Heap.resetIdentity();
    }

    public static void triggerLogoutEvent(Context context) {
        if (context != null) {
            Bundle attributes = new Bundle();
            setupDefaultAttributes(context, attributes);
            FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context);
            firebaseAnalytics.logEvent(AnalyticsEvents.LOGOUT_SUCCESS_EVENT, attributes);
            CleverTapAPI.getDefaultInstance(context).pushEvent("LOGOUT", bundleToMap(attributes));
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


}