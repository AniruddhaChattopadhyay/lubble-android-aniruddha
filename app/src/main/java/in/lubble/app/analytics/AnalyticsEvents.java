package in.lubble.app.analytics;

/**
 * Created by ishaan on 17/4/18.
 */

public class AnalyticsEvents {

    /**
     * String value must be separated with UNDERSCORES (_)
     */
    public static final String LOGOUT_SUCCESS_EVENT = "LOGOUT_SUCCESS_EVENT";
    public static final String LOC_CHECK_FAILED = "LOC_CHECK_FAILED";
    public static final String EVENT_JOINED_OPEN_GROUP = "EVENT_JOINED_OPEN_GROUP";
    public static final String POP_LIKE_CLICK = "POP_LIKE_CLICK";
    public static final String EXPAND_LIKES = "EXPAND_LIKES";

    public static final String ITEM_NOT_FOUND = "ITEM_NOT_FOUND";
    public static final String MPLACE_CHAT_BTN_CLICKED = "MPLACE_CHAT_BTN_CLICKED";
    public static final String CALL_BTN_CLICKED = "CALL_BTN_CLICKED";
    public static final String RECOMMEND_BTN_CLICK = "RECOMMEND_BTN_CLICK";
    public static final String VISIT_SHOP_CLICK = "VISIT_SHOP_CLICK";

    public static final String HELP_BTN_CLICKED = "HELP_BTN_CLICKED";
    public static final String HELP_PHONE_CLICKED = "HELP_PHONE_CLICKED";

    public static final String NOTIF_RECVD = "NOTIF_RECVD";
    public static final String NOTIF_ABORTED = "NOTIF_ABORTED";
    public static final String NOTIF_SNOOZED = "NOTIF_SNOOZED";
    public static final String NOTIF_DIGEST_CREATED = "NOTIF_DIGEST_CREATED";
    public static final String NOTIF_DISPLAYED = "NOTIF_DISPLAYED";
    public static final String NOTIF_SUMMARY_DISPLAYED = "NOTIF_DISPLAYED";
    public static final String NOTIF_OPENED = "NOTIF_OPENED";
    public static final String NOTIF_DISMISSED = "NOTIF_DISMISSED";
    public static final String NOTIF_MARKED_READ = "NOTIF_MARKED_READ";
    public static final String NOTIF_REPLIED = "NOTIF_REPLIED";
    public static final String NOTIF_DM_BLOCKED = "NOTIF_DM_BLOCKED";
    public static final String GROUP_SNOOZED = "GROUP_SNOOZED";

    public static final String REFERRAL_FB_SHARE = "REFERRAL_FB_SHARE";
    public static final String REFERRAL_WA_SHARE = "REFERRAL_WA_SHARE";
    public static final String REFERRAL_COPY_LINK = "REFERRAL_COPY_LINK";
    public static final String REFERRAL_MORE_SHARE = "REFERRAL_MORE_SHARE";
    public static final String REFERRAL_PROFILE_SHARE = "REFERRAL_PROFILE_SHARE";
    public static final String REFERRAL_LOCATION_SCREEN = "REFERRAL_LOCATION_SCREEN";
    public static final String REFERRAL_INVITE_MEMBERS = "REFERRAL_INVITE_MEMBERS";
    public static final String REFERRAL_LEADERBOARD_BOTTOM_SHARE = "REFERRAL_LEADERBOARD_BOTTOM_SHARE";

    public static final String SERVICES_FRAG = "SERVICES_FRAG";
    public static final String SHARE_ITEM = "SHARE_ITEM";
    public static final String SHARE_CATALOGUE = "SHARE_CATALOGUE";

    public static final String EXPLORE_DIALOG_SHOWN = "EXPLORE_DIALOG_SHOWN";
    public static final String FEED_EXPLORE_DIALOG_SHOWN = "FEED_EXPLORE_DIALOG_SHOWN";
    public static final String EXPLORE_CONTINUE_CLICKED = "EXPLORE_CONTINUE_CLICKED";
    public static final String FEED_EXPLORE_CONTINUE_CLICKED = "FEED_EXPLORE_CONTINUE_CLICKED";

    public static final String APP_UPDATE_BLOCK = "APP_UPDATE_BLOCK";
    public static final String APP_UPDATE_BLOCK_RETRY = "APP_UPDATE_BLOCK_RETRY";

    public static final String APP_UPDATE_REMINDER = "APP_UPDATE_REMINDER";
    public static final String APP_UPDATE_REMINDER_POSITIVE = "APP_UPDATE_REMINDER_POSITIVE";
    public static final String APP_UPDATE_REMINDER_LATER = "APP_UPDATE_REMINDER_LATER";

    public static final String APP_UPDATED = "APP_UPDATED";
    public static final String PHONE_BOOT_COMPLETE = "PHONE_BOOT_COMPLETE";

    public static final String WRITE_PERM_GRANTED = "WRITE_PERM_GRANTED";
    public static final String FAILED_SEARCH = "FAILED_SEARCH";

    public static final String QUIZ_PLAY = "QUIZ_PLAY";
    public static final String QUIZ_EARN_COINS = "QUIZ_EARN_COINS";
    public static final String QUIZ_RESULT = "QUIZ_RESULT";
    public static final String QUIZ_RETRY_CLICKED = "QUIZ_RETRY_CLICKED";
    public static final String QUIZ_RETRY_DONE = "QUIZ_RETRY_DONE";
    public static final String QUIZ_CAMERA_CLICKED = "QUIZ_SHARE_CLICKED";
    public static final String QUIZ_SHARE_CLICKED = "QUIZ_SHARE_CLICKED";
    public static final String QUIZ_PIC_SAVE_CLICKED = "QUIZ_PIC_SAVE_CLICKED";

    public static final String SEND_GROUP_CHAT = "SEND_GROUP_CHAT";
    public static final String LOC_FAIL_PHONE_SUBMIT = "LOC_FAIL_PHONE_SUBMIT";

    public static final String GROUP_CHAT_FRAG = "GROUP_CHAT_FRAG";
    public static final String GROUP_MORE_FRAG = "GROUP_MORE_FRAG";

    public static final String BOOK_CHECKOUT_PAGE = "BOOK_CHECKOUT_PAGE";
    public static final String ADD_BOOK_CLICKED = "ADD_BOOK_CLICKED";
    public static final String BOOK_SEARCHED = "BOOK_SEARCHED";
    public static final String BOOK_UPLOADED = "BOOK_UPLOADED";
    public static final String BOOK_PROCEEDED = "BOOK_PROCEEDED";
    public static final String BOOK_CHANGE = "BOOK_CHANGE";
    public static final String MY_BOOK_SELECTED = "MY_BOOK_SELECTED";
    public static final String BOOK_ADDRESS_CLICKED = "BOOK_ADDRESS_CLICKED";
    public static final String BOOK_PHONE_CLICKED = "BOOK_PHONE_CLICKED";
    public static final String BOOK_PLACE_ORDER = "BOOK_PLACE_ORDER";

    public static final String FLAIR_UPDATED = "FLAIR_UPDATED";
    public static final String COLLECTION_PLACE_MAP_CLICK = "COLLECTION_PLACE_MAP_CLICK";
    public static final String COLLECTION_PLACE_CTA_CLICK = "COLLECTION_PLACE_CTA_CLICK";

    public static final String EVENT_OPEN_FROM_GROUP_MORE = "EVENT_OPEN_FROM_GROUP_MORE";
    public static final String CHAT_PAGINATION = "CHAT_PAGINATION";
    public static final String BOOK_EARN_MORE = "BOOK_EARN_MORE";
    public static final String BOOK_LESS_COINS = "BOOK_LESS_COINS";

    public static final String MARKED_SPAM = "MARKED_SPAM";

    public static final String RATING_DIALOG_SHOWN = "RATING_DIALOG_SHOWN";
    public static final String RATING_DIALOG_STARS = "RATING_DIALOG_STARS";
    public static final String RATING_DIALOG_FORM = "RATING_DIALOG_FORM";
    public static final String RATING_DIALOG_FORM_YES = "RATING_DIALOG_FORM_YES";
    public static final String RATING_STORE_DIALOG = "RATING_STORE_DIALOG";
    public static final String RATING_STORE_DIALOG_YES = "RATING_STORE_DIALOG_YES";
    public static final String RATING_STORE_DIALOG_NEVER = "RATING_STORE_DIALOG_NEVER";

    public static final String VIDEO_OPENED = "VIDEO_OPENED";

    public static final String GROUP_PROMPT_SHOWN = "GROUP_PROMPT_SHOWN";
    public static final String GROUP_PROMPT_REPLIED = "GROUP_PROMPT_REPLIED";

    public static final String NEW_DM_CLICKED = "NEW_DM_CLICKED";
    public static final String NEW_DM_SENT = "NEW_DM_SENT";
    public static final String DM_BLOCKED = "DM_BLOCKED";
    public static final String DM_REPORTED = "DM_REPORTED";

    public static final String REPORT_MSG = "REPORT_MSG";
    public static final String MAP_SHARE = "MAP_SHARE";

    public static final String EXISTING_SELLER_DIALOG_SHOWN = "EXISTING_SELLER_DIALOG_SHOWN";
    public static final String EXISTING_SELLER_DIALOG_CLICK = "EXISTING_SELLER_DIALOG_CLICK";

    public static final String DOWNLOAD_VIDEO = "DOWNLOAD_VIDEO";
    public static final String DOWNLOAD_PDF = "DOWNLOAD_PDF";
    public static final String MSG_COPIED = "MSG_COPIED";

    public static final String EDIT_STATUS_CLICKED_FROM_CHAT = "EDIT_STATUS_CLICKED_FROM_CHAT";
    public static final String ADD_STATUS_CLICKED_FROM_CHAT = "ADD_STATUS_CLICKED_FROM_CHAT";
    public static final String CLICK_ON_OTHERS_STATUS = "CLICK_ON_OTHERS_STATUS";
    public static final String SET_STATUS_CLICKED = "SET_STATUS_CLICKED";
    public static final String SET_STATUS_FOR_CUSTOM_STATUS_CLICKED = "SET_STATUS_FOR_CUSTOM_STATUS_CLICKED";
    public static final String CLICK_ON_SET_STATUS_FROM_OTHERS_STATUS = "CLICK_ON_SET_STATUS_FROM_OTHERS_STATUS";

    public static final String MSG_SHARED = "MSG_SHARED";
    public static final String SHARED_VIA = "SHARED_VIA";
    public static final String REFERRAL_WIKI_SHARE = "REFERRAL_WIKI_SHARE";
    public static final String JOIN_GROUP = "JOIN_GROUP";

    public static final String EXPAND_PIN_MSG = "EXPAND_PIN_MSG";
    public static final String DISMISS_PIN_MSG = "DISMISS_PIN_MSG";

    public static final String STORY_VIEWED = "STORY_VIEWED";
    public static final String STORY_CHANGED = "STORY_CHANGED";

    public static final String DUPLICATE_MSG_WARN_SHOWN = "DUPLICATE_MSG_WARN_SHOWN";
    public static final String DUPLICATE_MSG_WARN_DISMISSED = "DUPLICATE_MSG_WARN_DISMISSED";
    public static final String DUPLICATE_MSG_WARN_HELP = "DUPLICATE_MSG_WARN_HELP";

    public static final String MSG_LINK_CLICKED = "MSG_LINK_CLICKED";
    public static final String MSG_DP_CLICKED = "MSG_DP_CLICKED";

    public static final String POST_LINK_CLICKED = "POST_LINK_CLICKED";
    public static final String COMMENT_LINK_CLICKED = "COMMENT_LINK_CLICKED";
    public static final String POST_LINK_LONG_CLICKED = "POST_LINK_LONG_CLICKED";
    public static final String POST_SHARED = "POST_SHARED";
    public static final String FEED_SEND_POST = "FEED_SEND_POST";
    public static final String FEED_POST_COMPOSED = "FEED_POST_COMPOSED";
    public static final String FEED_POST_IMPRESSION = "FEED_POST_IMPRESSION";
    public static final String FEED_POST_ENGAGEMENT = "FEED_POST_ENGAGEMENT";

    public static final String RTDB_PRUNED = "RTDB_PRUNED";

    public static final String FEED_GROUP_INVITE_CLICKED = "FEED_GROUP_INVITE_CLICKED";

    public static final String GAMES_LOCKED_INVITE_CLICK = "GAMES_LOCKED_INVITE_CLICK";

    public static final String FEED_INTRO_SHOWN = "FEED_INTRO_SHOWN";
    public static final String FEED_INTRO_START = "FEED_INTRO_START";
    public static final String FEED_INTRO_CANCELLED = "FEED_INTRO_CANCELLED";
    public static final String FEED_INTRO_GROUP_VIA_SNACKBAR = "FEED_INTRO_GROUP_VIA_SNACKBAR";

    public static final String WELCOME_SCREEN_LOGIN_BTN_CLICKED = "WELCOME_SCREEN_LOGIN_BTN_CLICKED";
    public static final String FEED_VIDEO_COMPRESS_SUCCESS = "FEED_VIDEO_COMPRESS_SUCCESS";
    public static final String FEED_VIDEO_COMPRESS_FAILED = "FEED_VIDEO_COMPRESS_FAILED";
    public static final String FEED_VIDEO_COMPRESS_BLOATED = "FEED_VIDEO_COMPRESS_BLOATED";

    public static final String FEED_REFRESHED = "FEED_REFRESHED";
}
