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

    public static final double SVR_LATI = 28.696660;
    public static final double SVR_LONGI = 77.124772;

    public static final String LAST_GROUP_MAPPING_ID = "LAST_GROUP_MAPPING_ID";
    public static final String LAST_KEY_MAPPING_ID = "LAST_KEY_MAPPING_ID";

    public static final MediaType MEDIA_TYPE = MediaType.parse("application/json");
    public static final String YOUTUBE_DEVELOPER_KEY = "AIzaSyBu0jLDE8AOSzC1MqZrbJVsXud1D0e_UPM";

    public static final String NEW_CHAT_ACTION = "in.lubble.app.NEW_MSG_INTENT";

    public static final String AIRTABLE_CHAABI = "keynD8giLhbC5OtN5";

}
