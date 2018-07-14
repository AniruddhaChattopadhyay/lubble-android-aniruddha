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

    public static final String CHAT_NOTIF_CHANNEL = "Chat Notifications";
    public static final String SENDING_MEDIA_NOTIF_CHANNEL = "Sending Media";
    public static final String NOTICE_NOTIF_CHANNEL = "Notice Notifications";
    public static final String APP_NOTIF_CHANNEL = "App Notifications";

    public static final String DEFAULT_GROUP = LubbleSharedPrefs.getInstance().getDefaultGroupId();

    public static final String LAST_GROUP_MAPPING_ID = "LAST_GROUP_MAPPING_ID";
    public static final String LAST_KEY_MAPPING_ID = "LAST_KEY_MAPPING_ID";

    public static final double SVR_LATI = 28.696660;
    public static final double SVR_LONGI = 77.124772;
    public static final String FAMILY_FUN_NIGHT = "FAMILY_FUN_NIGHT";

    public static final MediaType MEDIA_TYPE = MediaType.parse("application/json");

}
