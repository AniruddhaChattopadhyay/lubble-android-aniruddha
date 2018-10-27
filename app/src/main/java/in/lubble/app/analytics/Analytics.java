package in.lubble.app.analytics;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.utils.StringUtils;

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
        }
    }

    public static void triggerEvent(String title, Bundle attributes, Context context) {
        if (context != null) {
            setupDefaultAttributes(context, attributes);
            FirebaseAnalytics.getInstance(context).logEvent(title, attributes);
        }
    }

    public static void triggerLoginEvent(Context context) {
        Bundle attributes = new Bundle();
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, attributes);
        setUser(context);
    }

    public static void triggerSignUpEvent(Context context) {
        Bundle attributes = new Bundle();
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, attributes);
        setUser(context);
    }

    private static void setUser(Context context) {
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        String riderId = FirebaseAuth.getInstance().getUid();
        firebaseAnalytics.setUserId(riderId);
        firebaseAnalytics.setUserProperty("uid", riderId);
        firebaseAnalytics.setUserProperty("lubble_id", LubbleSharedPrefs.getInstance().getLubbleId());
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

}