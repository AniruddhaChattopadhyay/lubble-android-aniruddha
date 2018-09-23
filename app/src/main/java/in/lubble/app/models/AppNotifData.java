package in.lubble.app.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ishaan on 24/4/18.
 */

public class AppNotifData {

    @SerializedName("notifKey")
    private String notifKey;
    @SerializedName("title")
    private String title;
    @SerializedName("body")
    private String msg;
    @SerializedName("type")
    private String type;
    @SerializedName("groupId")
    private String groupId;
    @SerializedName("messageId")
    private String messageId;
    @SerializedName("iconUrl")
    private String iconUrl;
    @SerializedName("imageUrl")
    private String imageUrl;
    @SerializedName("deepLink")
    private String deepLink;

    public String getNotifKey() {
        return notifKey;
    }

    public void setNotifKey(String notifKey) {
        this.notifKey = notifKey;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDeepLink() {
        return deepLink;
    }

    public void setDeepLink(String deepLink) {
        this.deepLink = deepLink;
    }
}
