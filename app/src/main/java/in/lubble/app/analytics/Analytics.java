package in.lubble.app.analytics;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.freshchat.consumer.sdk.Freshchat;
import com.freshchat.consumer.sdk.FreshchatUser;
import com.freshchat.consumer.sdk.exception.MethodNotAllowedException;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.segment.analytics.Properties;
import com.segment.analytics.Traits;
import com.uxcam.UXCam;

import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import in.lubble.app.BuildConfig;
import in.lubble.app.LubbleApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.utils.StringUtils;
import io.getstream.cloud.CloudClient;
import io.getstream.core.Region;
import io.getstream.core.models.Content;
import io.getstream.core.models.Engagement;
import io.getstream.core.models.Impression;
import io.getstream.core.models.UserData;

public class Analytics {

    private static final String TAG = "Analytics";

    public static void triggerScreenEvent(Context context, Object className) {
        final String simpleName = ((Class<?>) className).getSimpleName();
        triggerEvent(simpleName, context);
        FirebaseAnalytics.getInstance(context).setCurrentScreen((Activity) context, simpleName, null);
        Bundle attributes = new Bundle();
        setupDefaultAttributes(context, attributes);
        com.segment.analytics.Analytics.with(context).screen(simpleName, bundleToSegmentProperties(attributes));
        UXCam.tagScreenName(simpleName);
    }

    public static void triggerScreenEvent(Context context, Object className, Bundle attributes) {
        final String simpleName = ((Class<?>) className).getSimpleName();
        setupDefaultAttributes(context, attributes);
        triggerEvent(simpleName, attributes, context);
        FirebaseAnalytics.getInstance(context).setCurrentScreen((Activity) context, simpleName, null);
        com.segment.analytics.Analytics.with(context).screen(simpleName, bundleToSegmentProperties(attributes));
        UXCam.tagScreenName(simpleName);
    }

    public static void triggerEvent(String title, Context context) {
        if (context != null) {
            Bundle attributes = new Bundle();
            setupDefaultAttributes(context, attributes);
            FirebaseAnalytics.getInstance(context).logEvent(title, attributes);
            //CleverTapAPI.getDefaultInstance(context).pushEvent(title, bundleToMap(attributes));
            com.segment.analytics.Analytics.with(context).track(title, bundleToSegmentProperties(attributes));
            UXCam.logEvent(title);
        }
    }

    public static void triggerEvent(String title, Bundle attributes, Context context) {
        if (context != null) {
            setupDefaultAttributes(context, attributes);
            FirebaseAnalytics.getInstance(context).logEvent(title, attributes);
            //CleverTapAPI.getDefaultInstance(context).pushEvent(title, bundleToMap(attributes));
            com.segment.analytics.Analytics.with(context).track(title, bundleToSegmentProperties(attributes));
            UXCam.logEvent(title, bundleToMap(attributes));
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
        UXCam.logEvent("LOGIN", bundleToMap(attributes));
    }

    public static void triggerSignUpEvent(Context context) {
        Bundle attributes = new Bundle();
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, attributes);
        //CleverTapAPI.getDefaultInstance(context).pushEvent("SIGNUP", bundleToMap(attributes));
        com.segment.analytics.Analytics.with(context).track("SIGNUP", bundleToSegmentProperties(attributes));
        setUser(context);
        setAnalyticsUser(context);
        UXCam.logEvent("SIGNUP", bundleToMap(attributes));
    }

    private static void setUser(Context context) {
        /**
         *  DONT Change this method. Use method setAnalyticsUser() in this class
         */
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        String userId = FirebaseAuth.getInstance().getUid();
        firebaseAnalytics.setUserId(userId);
        firebaseAnalytics.setUserProperty("uid", userId);
        firebaseAnalytics.setUserProperty("lubble_id", LubbleSharedPrefs.getInstance().getLubbleId());
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseCrashlytics.getInstance().setUserId(userId);
        if (currentUser != null) {
            firebaseAnalytics.setUserProperty("name", currentUser.getDisplayName());
        }
    }

    private static void unSetUser(Context context) {
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        firebaseAnalytics.setUserId(null);
        firebaseAnalytics.setUserProperty("uid", null);
        firebaseAnalytics.setUserProperty("lubble_id", null);
        com.segment.analytics.Analytics.with(context).reset();
        Freshchat.resetUser(context);
    }

    public static void triggerLogoutEvent(Context context) {
        if (context != null) {
            Bundle attributes = new Bundle();
            setupDefaultAttributes(context, attributes);
            FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context);
            firebaseAnalytics.logEvent(AnalyticsEvents.LOGOUT_SUCCESS_EVENT, attributes);
            //CleverTapAPI.getDefaultInstance(context).pushEvent("LOGOUT", bundleToMap(attributes));
            com.segment.analytics.Analytics.with(context).track("LOGOUT", bundleToSegmentProperties(attributes));
            unSetUser(context);
            UXCam.logEvent("LOGOUT", bundleToMap(attributes));
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
        final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            // user is logged in
            FirebaseCrashlytics.getInstance().setUserId(currentUser.getUid());
            final Traits traits = new Traits();
            traits.putName(currentUser.getDisplayName());
            traits.putPhone(currentUser.getPhoneNumber());
            traits.putEmail(currentUser.getEmail());
            traits.put("Firebase ID", firebaseAuth.getUid());
            traits.put("Lubble Id", LubbleSharedPrefs.getInstance().getLubbleId());
            traits.put("lubble_id", LubbleSharedPrefs.getInstance().getLubbleId());
            traits.put("version name", BuildConfig.VERSION_NAME);
            traits.put("version code", BuildConfig.VERSION_CODE);
            com.segment.analytics.Analytics.with(context).identify(firebaseAuth.getUid(), traits, null);

            final Freshchat freshchat = Freshchat.getInstance(context);
            FreshchatUser freshUser = freshchat.getUser();
            freshUser.setFirstName(currentUser.getDisplayName());
            freshUser.setEmail(currentUser.getEmail());
            freshUser.setPhone("+91", currentUser.getPhoneNumber());

            Map<String, String> userMeta = new HashMap<>();
            userMeta.put("uid", firebaseAuth.getUid());
            userMeta.put("lubble_id", LubbleSharedPrefs.getInstance().getLubbleId());

            try {
                freshchat.setUserProperties(userMeta);
                freshchat.setUser(freshUser);
                freshchat.identifyUser(firebaseAuth.getUid(), null);
            } catch (MethodNotAllowedException e) {
                e.printStackTrace();
            }
            com.segment.analytics.Analytics.with(context).identify(FirebaseAuth.getInstance().getUid(), traits, null);
            UXCam.setUserIdentity(firebaseAuth.getUid());
            UXCam.setUserProperty("uid", firebaseAuth.getUid());
            UXCam.setUserProperty("lubble_id", LubbleSharedPrefs.getInstance().getLubbleId());
            UXCam.setUserProperty("name", currentUser.getDisplayName());
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

            /*StreamAnalyticsImpl.getInstance().setUserId(firebaseAuth.getUid());*/
        }
    }

    private static CloudClient getAnalyticsClient() throws MalformedURLException {
        CloudClient analyticsClient;
        if ("dev".equalsIgnoreCase(BuildConfig.FLAVOR)) {
            analyticsClient = CloudClient
                    .builder("nvhsd4sv68k4",
                            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJyZXNvdXJjZSI6ImFuYWx5dGljcyIsImFjdGlvbiI6IioiLCJ1c2VyX2lkIjoiKiJ9.JNBodILjaJEuW2fwIjZTZcvKn8lXI0roercYGAZ1xAg",
                            FirebaseAuth.getInstance().getUid())
                    .build();
        } else {
            analyticsClient = CloudClient
                    .builder("qeyr2a54nh9w",
                            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJyZXNvdXJjZSI6ImFuYWx5dGljcyIsImFjdGlvbiI6IioiLCJ1c2VyX2lkIjoiKiJ9.TUwowYvGfa0rJTC2gOcDLsBmAAL5-9EAFkeQBtw9wgA",
                            FirebaseAuth.getInstance().getUid())
                    .region(Region.SINGAPORE)
                    .region("Singapore")
                    .build();
        }
        return analyticsClient;
    }

    public static void triggerFeedImpression(ArrayList<Content> contentList, @NotNull String feedName, String location) {
        try {
            Impression.Builder eventBuilder = new Impression.Builder()
                    .contentList(contentList)
                    .feedID(feedName)
                    .userData(new UserData(FirebaseAuth.getInstance().getUid(), FirebaseAuth.getInstance().getUid()))
                    .location(location);
            getAnalyticsClient().analytics().trackImpression(eventBuilder.build());
        } catch (Exception e) {
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(e);
        }

    }

    public static void triggerFeedEngagement(String foreignId, String action, int boost, @NotNull String feedName, String location) {
        try {
            Engagement engagement = new Engagement.Builder()
                    .feedID(feedName)
                    .content(new Content(foreignId))
                    .label(action)
                    .userData(new UserData(FirebaseAuth.getInstance().getUid(), FirebaseAuth.getInstance().getUid()))
                    .boost(boost)
                    .location(location)
                    .position(1)
                    .build();
            getAnalyticsClient().analytics().trackEngagement(engagement);

            Bundle bundle = new Bundle();
            bundle.putString("foreignId", foreignId);
            bundle.putString("action", action);
            bundle.putInt("boost", boost);
            bundle.putString("feedName", feedName);
            bundle.putString("location", location);
            triggerEvent(AnalyticsEvents.FEED_POST_ENGAGEMENT, bundle, LubbleApp.getAppContext());
        } catch (Exception e) {
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

}