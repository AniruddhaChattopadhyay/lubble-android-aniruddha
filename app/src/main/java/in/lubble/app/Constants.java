package in.lubble.app;

import okhttp3.MediaType;

/**
 * Created by ishaan on 26/1/18.
 */

public class Constants {

    /**
     * Notification IDs
     */
    public static final int PROGRESS_NOTIFICATION_ID = 100;
    public static final int FINISHED_NOTIFICATION_ID = 101;
    public static final int CHAT_NOTIFICATION_ID = 102;

    @Deprecated
    public static final String CHAT_NOTIF_CHANNEL = "Chat Notifications";
    public static final String NEW_CHAT_NOTIF_CHANNEL = "Chat Messages";
    public static final String DM_CHAT_NOTIF_CHANNEL = "DM Messages";
    public static final String SENDING_MEDIA_NOTIF_CHANNEL = "Sending Media";
    public static final String NOTICE_NOTIF_CHANNEL = "Notice Notifications";
    public static final String LUBB_NOTIF_CHANNEL = "Message Like Notifications";
    public static final String APP_NOTIF_CHANNEL = "App Notifications";

    public static final String DEFAULT_GROUP = LubbleSharedPrefs.getInstance().getDefaultGroupId();

    /**
     * FIREBASE REMOTE CONFIG KEYS
     */
    public static final String REFER_MSG = "REFER_MSG";
    public static final String IS_QUIZ_SHOWN = "IS_QUIZ_SHOWN";
    public static final String QUIZ_RESULT_UI = "quiz_result_ui";
    public static final String GROUP_QUES_ENABLED = "GROUP_QUES_ENABLED";
    public static final String DELIVERY_FEE = "DELIVERY_FEE";
    public static final String IS_REWARDS_SHOWN = "IS_REWARDS_SHOWN";
    public static final String IS_RATING_DIALOG_ACTIVE = "IS_RATING_DIALOG_ACTIVE";
    public static final String REWARDS_EXPLAINER = "REWARDS_EXPLAINER";
    public static final String MAP_HTML = "MAP_HTML";
    public static final String MAP_BTN_URL = "MAP_BTN_URL";
    public static final String MAP_SHARE_TEXT = "MAP_SHARE_TEXT";
    public static final String EVENTS_MAINTENANCE_TEXT = "EVENTS_MAINTENANCE_TEXT";
    public static final String EVENTS_MAINTENANCE_IMG = "EVENTS_MAINTENANCE_IMG";
    public static final String MARKET_MAINTENANCE_TEXT = "MARKET_MAINTENANCE_TEXT";
    public static final String DEFAULT_SHOP_PIC = "DEFAULT_SHOP_PIC";
    public static final String IS_NOTIF_SNOOZE_ON = "IS_NOTIF_SNOOZE_ON";
    public static final String IS_TIME_SHOWN = "IS_TIME_SHOWN";
    public static final String MSG_WATERMARK_TEXT = "MSG_WATERMARK_TEXT";
    public static final String WIKI_URL = "WIKI_URL";
    public static final String IS_GAMES_ENABLED = "IS_GAMES_ENABLED";
    public static final String IS_CHATS_ENABLED_FOR_KML = "IS_CHATS_ENABLED_FOR_KML";

    public static final double SVR_LATI = 12.9344758;
    public static final double SVR_LONGI = 77.6192442;

    public static final String LAST_GROUP_MAPPING_ID = "LAST_GROUP_MAPPING_ID";
    public static final String LAST_KEY_MAPPING_ID = "LAST_KEY_MAPPING_ID";

    public static final MediaType MEDIA_TYPE = MediaType.parse("application/json");
    public static final String YOUTUBE_DEVELOPER_KEY = "AIzaSyBu0jLDE8AOSzC1MqZrbJVsXud1D0e_UPM";

    public static final String NEW_CHAT_ACTION = "in.lubble.app.NEW_MSG_INTENT";

    public static final String AIRTABLE_CHAABI = "keynD8giLhbC5OtN5";

    public static final String IS_UXCAM_ENABLED = "IS_UXCAM_ENABLED";
    public static final String UXCAM_DEV_CHAABI = "1yo34nnyl85mnzv";
    public static final String UXCAM_PROD_CHAABI = "6dl8m9y0uc2q0dy";
    public static final String IS_MAP_SHOWN = "IS_MAP_SHOWN";
    public static final String IS_IMPRESSIONS_COUNT_ENABLED = "IS_IMPRESSIONS_COUNT_ENABLED";
    public static final String SHOW_IMPRESSIONS_COUNT = "SHOW_IMPRESSIONS_COUNT";

}
