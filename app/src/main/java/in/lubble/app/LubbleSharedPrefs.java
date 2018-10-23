package in.lubble.app;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by ishaan on 17/2/18.
 */

public class LubbleSharedPrefs {

    private static LubbleSharedPrefs instance;
    private final SharedPreferences preferences;
    private final String LUBBLE_SHARED_PREFERENCE_KEY = "in.lubble.mainSharedPrefs";

    private final String IS_APP_INTRO_SHOWN = "is_app_intro_shown";
    private final String REFERRER_UID = "referrer_uid";
    private final String CURRENT_ACTIVE_GROUP = "CURRENT_ACTIVE_GROUP";
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

    public String getCurrentActiveGroupId() {
        return preferences.getString(CURRENT_ACTIVE_GROUP, "");
    }

    public boolean setCurrentActiveGroupId(String gid) {

        return preferences.edit().putString(CURRENT_ACTIVE_GROUP, gid).commit();
    }

    public String getLubbleId() {
        setLubbleId("dev".equalsIgnoreCase(BuildConfig.FLAVOR) ? "DEV" : "saraswati_vihar");
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

}
