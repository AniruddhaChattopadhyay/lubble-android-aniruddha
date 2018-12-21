package in.lubble.app;

import android.content.Context;
import android.content.SharedPreferences;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import in.lubble.app.firebase.RealtimeDbHelper;

import java.util.HashMap;

/**
 * Created by ishaan on 17/2/18.
 */

public class LubbleSharedPrefs {

    private static LubbleSharedPrefs instance;
    private final SharedPreferences preferences;
    private final String LUBBLE_SHARED_PREFERENCE_KEY = "in.lubble.mainSharedPrefs";

    private final String IS_APP_INTRO_SHOWN = "is_app_intro_shown";
    private final String REFERRER_UID = "referrer_uid";
    private final String LUBBLE_ID = "LUBBLE_ID";
    private final String DEFAULT_GROUP_ID = "DEFAULT_GROUP_ID";
    private final String IS_DEFAULT_GROUP_INFO_SHOWN = "IS_DEFAULT_GROUP_INFO_SHOWN";
    private final String IS_PUBLIC_GROUP_INFO_SHOWN = "IS_PUBLIC_GROUP_INFO_SHOWN";
    private final String IS_LOGOUT_PENDING = "IS_LOGOUT_PENDING";
    private final String IS_GROUP_INFO_OPENED = "IS_GROUP_INFO_OPENED";
    private final String IS_MPLACE_OPENED = "IS_MPLACE_OPENED";
    private final String IS_SERVICES_OPENED = "IS_SERVICES_OPENED";
    private final String SELLER_ID = "SELLER_ID";
    private final String IS_VIEW_COUNT_ENABLED = "IS_VIEW_COUNT_ENABLED";
    private final String FULL_NAME = "FULL_NAME";
    private final String REFERRAL_CODE = "REFERRAL_CODE";
    private final String IS_EXPLORE_SHOWN = "IS_EXPLORE_SHOWN";

    private LubbleSharedPrefs(Context context) {
        preferences = context.getSharedPreferences(LUBBLE_SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
    }

    public static void initializeInstance(Context context) {
        if (instance == null) {
            instance = new LubbleSharedPrefs(context);
        }
    }

    public static LubbleSharedPrefs getInstance() {
        if (instance == null) {
            throw new IllegalStateException(LubbleSharedPrefs.class.getCanonicalName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }
        return instance;
    }

    public SharedPreferences getPreferences() {
        return preferences;
    }

    /**
     * Clear all SharedPreferences for RiderInfo
     */
    public void clearAll() {
        preferences.edit().clear().apply();
    }

    //******************************************/

    public boolean getIsAppIntroShown() {
        return preferences.getBoolean(IS_APP_INTRO_SHOWN, false);
    }

    public boolean setIsAppIntroShown(boolean isAppIntroShown) {
        return preferences.edit().putBoolean(IS_APP_INTRO_SHOWN, isAppIntroShown).commit();
    }

    public String getReferrerUid() {
        return preferences.getString(REFERRER_UID, "");
    }

    public boolean setReferrerUid(String uid) {

        return preferences.edit().putString(REFERRER_UID, uid).commit();
    }

    public String getLubbleId() {
        /*if (!TextUtils.isEmpty(preferences.getString(LUBBLE_ID, ""))) {
            return preferences.getString(LUBBLE_ID, "");
        } else if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            // user is signed in but has no lubble ID
            // start Main Activity to fetch & update lubble ID
            try {
                final Intent intent = new Intent(LubbleApp.getAppContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                LubbleApp.getAppContext().startActivity(intent);
            } catch (Exception e) {
                return "";
            }
            return "";
        } else {
            // user is logged out, Lubble ID does not exist at this time
            return "";
        }*/
        final String lubbleId = preferences.getString(LUBBLE_ID, "");
        if (lubbleId == null || lubbleId.isEmpty()) {
            fetchAndUpdateLubbleId();
            return "dev".equalsIgnoreCase(BuildConfig.FLAVOR) ? "DEV" : "saraswati_vihar";
        } else {
            return lubbleId;
        }
    }

    public boolean setLubbleId(String lubbleId) {

        return preferences.edit().putString(LUBBLE_ID, lubbleId).commit();
    }

    private void fetchAndUpdateLubbleId() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            RealtimeDbHelper.getThisUserRef().child("lubbles").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    HashMap<String, String> map = (HashMap<String, String>) dataSnapshot.getValue();
                    if (map != null && !map.isEmpty()) {
                        setLubbleId((String) map.keySet().toArray()[0]);
                    } else {
                        Crashlytics.logException(new IllegalAccessException("User has NO lubble ID"));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Crashlytics.logException(new IllegalAccessException(databaseError.getCode() + " " + databaseError.getMessage()));
                }
            });
        }
    }

    public String getDefaultGroupId() {
        return preferences.getString(DEFAULT_GROUP_ID, "MYLUBBLE");
    }

    public boolean setDefaultGroupId(String defaultGroupId) {
        return preferences.edit().putString(DEFAULT_GROUP_ID, defaultGroupId).commit();
    }

    public String getFullName() {
        return preferences.getString(FULL_NAME, "");
    }

    public boolean setFullName(String fullName) {
        return preferences.edit().putString(FULL_NAME, fullName).commit();
    }

    public String getReferralCode() {
        return preferences.getString(REFERRAL_CODE, "");
    }

    public boolean setReferralCode(String referralCode) {
        return preferences.edit().putString(REFERRAL_CODE, referralCode).commit();
    }

    public boolean getIsDefaultGroupInfoShown() {
        return preferences.getBoolean(IS_DEFAULT_GROUP_INFO_SHOWN, false);
    }

    public boolean setIsDefaultGroupInfoShown(boolean isShown) {

        return preferences.edit().putBoolean(IS_DEFAULT_GROUP_INFO_SHOWN, isShown).commit();
    }

    public boolean getIsPublicGroupInfoShown() {
        return preferences.getBoolean(IS_PUBLIC_GROUP_INFO_SHOWN, false);
    }

    public boolean setIsPublicGroupInfoShown(boolean isShown) {

        return preferences.edit().putBoolean(IS_PUBLIC_GROUP_INFO_SHOWN, isShown).commit();
    }

    public boolean getIsLogoutPending() {
        return preferences.getBoolean(IS_LOGOUT_PENDING, false);
    }

    public boolean setIsLogoutPending(boolean isLogoutPending) {

        return preferences.edit().putBoolean(IS_LOGOUT_PENDING, isLogoutPending).commit();
    }

    public boolean getIsGroupInfoOpened() {
        return preferences.getBoolean(IS_GROUP_INFO_OPENED, false);
    }

    public boolean setIsGroupInfoOpened(boolean isOpened) {
        return preferences.edit().putBoolean(IS_GROUP_INFO_OPENED, isOpened).commit();
    }

    public boolean getIsMplaceOpened() {
        return preferences.getBoolean(IS_MPLACE_OPENED, false);
    }

    public boolean setIsMplaceOpened(boolean isOpened) {
        return preferences.edit().putBoolean(IS_MPLACE_OPENED, isOpened).commit();
    }

    public boolean getIsServicesOpened() {
        return preferences.getBoolean(IS_SERVICES_OPENED, false);
    }

    public boolean setIsServicesOpened(boolean isOpened) {
        return preferences.edit().putBoolean(IS_SERVICES_OPENED, isOpened).commit();
    }

    public int getSellerId() {
        return preferences.getInt(SELLER_ID, -1);
    }

    public boolean setSellerId(int sellerId) {
        return preferences.edit().putInt(SELLER_ID, sellerId).commit();
    }

    public boolean getIsViewCountEnabled() {
        return preferences.getBoolean(IS_VIEW_COUNT_ENABLED, false);
    }

    public boolean setIsViewCountEnabled(boolean isViewCountEnabled) {
        return preferences.edit().putBoolean(IS_VIEW_COUNT_ENABLED, isViewCountEnabled).commit();
    }

    public boolean getIsExploreShown() {
        return preferences.getBoolean(IS_EXPLORE_SHOWN, false);
    }

    public boolean setIsExploreShown(boolean isShown) {
        return preferences.edit().putBoolean(IS_EXPLORE_SHOWN, isShown).commit();
    }

}
