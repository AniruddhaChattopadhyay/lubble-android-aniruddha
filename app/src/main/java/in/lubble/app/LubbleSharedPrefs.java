package in.lubble.app;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.Nullable;
import com.crashlytics.android.Crashlytics;
import in.lubble.app.utils.UserUtils;

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
    private final String IS_LOGOUT_PENDING = "IS_LOGOUT_PENDING";
    private final String IS_GROUP_INFO_OPENED = "IS_GROUP_INFO_OPENED";
    private final String IS_MPLACE_OPENED = "IS_MPLACE_OPENED";
    private final String IS_SERVICES_OPENED = "IS_SERVICES_OPENED";
    private final String SELLER_ID = "SELLER_ID";
    private final String IS_VIEW_COUNT_ENABLED = "IS_VIEW_COUNT_ENABLED";
    private final String REFERRAL_CODE = "REFERRAL_CODE";
    private final String IS_EXPLORE_SHOWN = "IS_EXPLORE_SHOWN";
    private final String CENTER_LATI = "CENTER_LATI";
    private final String CENTER_LONGI = "CENTER_LONGI";
    private final String SUPPORT_UID = "SUPPORT_UID";
    private final String IS_QUIZ_OPENED = "IS_QUIZ_OPENED";
    private final String IS_DEFAULT_GRP_OPENED = "IS_DEFAULT_GRP_OPENED";
    private final String IS_BOOK_EXCHANGE_OPENED = "IS_BOOK_EXCHANGE_OPENED";

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

    @Nullable
    public String getLubbleId() {
        return preferences.getString(LUBBLE_ID, "");
    }

    public String requireLubbleId() {
        if (preferences.getString(LUBBLE_ID, null) == null) {
            UserUtils.logout(LubbleApp.getAppContext());

            final IllegalArgumentException illegalArgumentException = new IllegalArgumentException("Tried to access Lubble ID but was not found in Shared Prefs");
            Crashlytics.logException(illegalArgumentException);
            throw illegalArgumentException;
        }
        return preferences.getString(LUBBLE_ID, "");
    }

    public boolean setLubbleId(String lubbleId) {

        return preferences.edit().putString(LUBBLE_ID, lubbleId).commit();
    }

    public String getDefaultGroupId() {
        return preferences.getString(DEFAULT_GROUP_ID, "MYLUBBLE");
    }

    public boolean setDefaultGroupId(String defaultGroupId) {
        return preferences.edit().putString(DEFAULT_GROUP_ID, defaultGroupId).commit();
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

    public boolean getIsQuizOpened() {
        return preferences.getBoolean(IS_QUIZ_OPENED, false);
    }

    public boolean setIsQuizOpened(boolean isShown) {
        return preferences.edit().putBoolean(IS_QUIZ_OPENED, isShown).commit();
    }

    public boolean getIsDefaultGroupOpened() {
        return preferences.getBoolean(IS_DEFAULT_GRP_OPENED, true);
    }

    public boolean setIsDefaultGroupOpened(boolean isOpened) {
        return preferences.edit().putBoolean(IS_DEFAULT_GRP_OPENED, isOpened).commit();
    }

    public boolean getIsBookExchangeOpened() {
        return preferences.getBoolean(IS_BOOK_EXCHANGE_OPENED, false);
    }

    public boolean setIsBookExchangeOpened(boolean isOpened) {
        return preferences.edit().putBoolean(IS_BOOK_EXCHANGE_OPENED, isOpened).commit();
    }


    public double getCenterLati() {
        return getDouble(preferences, CENTER_LATI, Constants.SVR_LATI);
    }

    public boolean setCenterLati(double lati) {
        return putDouble(preferences.edit(), CENTER_LATI, lati).commit();
    }


    public double getCenterLongi() {
        return getDouble(preferences, CENTER_LONGI, Constants.SVR_LONGI);
    }

    public boolean setCenterLongi(double longi) {
        return putDouble(preferences.edit(), CENTER_LONGI, longi).commit();
    }

    SharedPreferences.Editor putDouble(final SharedPreferences.Editor edit, final String key, final double value) {
        return edit.putLong(key, Double.doubleToRawLongBits(value));
    }

    double getDouble(final SharedPreferences prefs, final String key, final double defaultValue) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(defaultValue)));
    }

    @Nullable
    public String getSupportUid() {
        return preferences.getString(SUPPORT_UID, null);
    }

    public boolean setSupportUid(String supportUid) {
        return preferences.edit().putString(SUPPORT_UID, supportUid).commit();
    }

}
