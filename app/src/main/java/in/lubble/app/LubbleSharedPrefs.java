package in.lubble.app;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.HashSet;
import java.util.Set;

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
    private final String INVITED_GROUP_ID = "INVITED_GROUP_ID";
    private final String LUBBLE_ID = "LUBBLE_ID";
    private final String LUBBLE_NAME = "LUBBLE_NAME";
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
    private final String IS_REWARDS_OPENED = "IS_REWARDS_OPENED";
    private final String IS_REWARDS_EXPLAINER_SEEN = "IS_REWARDS_EXPLAINER_SEEN";
    private final String SHOW_RATING_DIALOG = "SHOW_RATING_DIALOG";
    private final String RATING_DIALOG_LAST_SHOWN = "RATING_DIALOG_LAST_SHOWN";
    private final String READ_EVENTS = "READ_EVENTS";
    private final String FLEXI_UPDATE_TS = "FLEXI_UPDATE_TS";
    private final String IS_MAP_DISCLAIMER_CLOSED = "IS_MAP_DISCLAIMER_CLOSED";
    private final String SHARE_MSG_COPY_URL = "SHARE_MSG_COPY_URL";
    private final String SHARE_MSG_URL = "SHARE_MSG_URL";

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
     * Clear all SharedPreferences
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

    public String getInvitedGroupId() {
        return preferences.getString(INVITED_GROUP_ID, "");
    }

    public boolean setInvitedGroupId(String uid) {

        return preferences.edit().putString(INVITED_GROUP_ID, uid).commit();
    }

    @Nullable
    public String getLubbleId() {
        return preferences.getString(LUBBLE_ID, "");
    }

    public String requireLubbleId() {
        if (preferences.getString(LUBBLE_ID, null) == null) {
            UserUtils.logout(LubbleApp.getAppContext());

            final IllegalArgumentException illegalArgumentException = new IllegalArgumentException("Tried to access Lubble ID but was not found in Shared Prefs");
            FirebaseCrashlytics.getInstance().recordException(illegalArgumentException);
            throw illegalArgumentException;
        }
        return preferences.getString(LUBBLE_ID, "");
    }

    public boolean setLubbleId(String lubbleId) {

        return preferences.edit().putString(LUBBLE_ID, lubbleId).commit();
    }

    public String getDefaultGroupId() {
        return preferences.getString(DEFAULT_GROUP_ID, "");
    }

    public boolean setDefaultGroupId(String defaultGroupId) {
        return preferences.edit().putString(DEFAULT_GROUP_ID, defaultGroupId).commit();
    }

    public String getLubbleName() {
        return preferences.getString(LUBBLE_NAME, "The Hood");
    }

    public boolean setLubbleName(String lubbleName) {
        return preferences.edit().putString(LUBBLE_NAME, lubbleName).commit();
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


    public boolean getIsRewardsOpened() {
        return preferences.getBoolean(IS_REWARDS_OPENED, false);
    }

    public boolean setIsRewardsOpened(boolean isOpened) {
        return preferences.edit().putBoolean(IS_REWARDS_OPENED, isOpened).commit();
    }


    public boolean getIsRewardsExplainerSeen() {
        return preferences.getBoolean(IS_REWARDS_EXPLAINER_SEEN, false);
    }

    public boolean setIsRewardsExplainerSeen(boolean isOpened) {
        return preferences.edit().putBoolean(IS_REWARDS_EXPLAINER_SEEN, isOpened).commit();
    }

    public boolean getIsMapDisclaimerClosed() {
        return preferences.getBoolean(IS_MAP_DISCLAIMER_CLOSED, false);
    }

    public boolean setIsMapDisclaimerClosed(boolean isClosed) {
        return preferences.edit().putBoolean(IS_MAP_DISCLAIMER_CLOSED, isClosed).commit();
    }

    public boolean getShowRatingDialog() {
        return preferences.getBoolean(SHOW_RATING_DIALOG, false);
    }

    public boolean setShowRatingDialog(boolean showDialog) {
        return preferences.edit().putBoolean(SHOW_RATING_DIALOG, showDialog).commit();
    }

    public long getRatingDialogLastShown() {
        return preferences.getLong(RATING_DIALOG_LAST_SHOWN, 1L);
        //if ZERO then never show dialog
    }

    public boolean setRatingDialogLastShown(long unixTime) {
        return preferences.edit().putLong(RATING_DIALOG_LAST_SHOWN, unixTime).commit();
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

    @Nullable
    public String getMsgCopyShareUrl() {
        return preferences.getString(SHARE_MSG_COPY_URL, null);
    }

    public boolean setMsgCopyShareUrl(String shareUrl) {
        return preferences.edit().putString(SHARE_MSG_COPY_URL, shareUrl).commit();
    }

    @Nullable
    public String getMsgShareUrl() {
        return preferences.getString(SHARE_MSG_URL, null);
    }

    public boolean setMsgShareUrl(String shareUrl) {
        return preferences.edit().putString(SHARE_MSG_URL, shareUrl).commit();
    }

    @Nullable
    public long getFlexiUpdateTs() {
        return preferences.getLong(FLEXI_UPDATE_TS, 0L);
    }

    public boolean setFlexiUpdateTs(long ts) {
        return preferences.edit().putLong(FLEXI_UPDATE_TS, ts).commit();
    }

    public Set<String> getEventSet() {
        return new HashSet<>(preferences.getStringSet(READ_EVENTS, new HashSet<String>()));
    }

    public boolean setEventSet(Set<String> set) {
        return preferences.edit().putStringSet(READ_EVENTS, set).commit();
    }

}
