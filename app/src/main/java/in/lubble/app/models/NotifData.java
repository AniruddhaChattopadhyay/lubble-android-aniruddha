package in.lubble.app.models;

import com.google.gson.annotations.SerializedName;

import in.lubble.app.LubbleApp;
import in.lubble.app.R;
import in.lubble.app.utils.StringUtils;

/**
 * Created by ishaan on 10/3/18.
 */

public class NotifData {

    @SerializedName("messageId")
    private String messageId;
    @SerializedName("authorId")
    private String authorId;
    @SerializedName("authorName")
    private String authorName;
    @SerializedName("authorThumbnail")
    private String authorDpUrl;
    @SerializedName("groupThumbnail")
    private String groupDpUrl;
    @SerializedName("groupId")
    private String groupId;
    @SerializedName("groupTitle")
    private String groupName;
    @SerializedName("message")
    private String messageBody;
    @SerializedName("notification_channel")
    private String notifChannel;
    @SerializedName("timestamp")
    private String timestamp;
    @SerializedName("isImage")
    private String hasImage;
    @SerializedName("isPdf")
    private String hasPdf;
    @SerializedName("pdfFileName")
    private String pdfFileName;
    @SerializedName("type")
    private String notifType;
    @SerializedName("isSeller")
    private Boolean isSeller;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorDpUrl() {
        return authorDpUrl;
    }

    public void setAuthorDpUrl(String authorDpUrl) {
        this.authorDpUrl = authorDpUrl;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getMessageBody() {
        if (StringUtils.isValidString(hasImage) && hasImage.equalsIgnoreCase("True")) {
            return "\uD83D\uDCF7 " + messageBody;
        } else if (this.messageBody.equalsIgnoreCase(LubbleApp.getAppContext().getString(R.string.poll_msg_body))) {
            return "\uD83D\uDCCA POLL";
        }
        else if(StringUtils.isValidString(hasPdf) && hasPdf.equalsIgnoreCase("True")){
            return "\uD83D\uDCC4 " + pdfFileName + ".pdf ";
        }
        else {
            return messageBody;
        }
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public String getNotifChannel() {
        return notifChannel;
    }

    public void setNotifChannel(String notifChannel) {
        this.notifChannel = notifChannel;
    }

    public String getGroupDpUrl() {
        return groupDpUrl;
    }

    public void setGroupDpUrl(String groupDpUrl) {
        this.groupDpUrl = groupDpUrl;
    }

    public long getTimestamp() {
        return Long.parseLong(timestamp);
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getHasImage() {
        return hasImage;
    }

    public void setHasImage(String hasImage) {
        this.hasImage = hasImage;
    }

    public String getNotifType() {
        return notifType;
    }

    public void setNotifType(String notifType) {
        this.notifType = notifType;
    }

    public Boolean getIsSeller() {
        return isSeller;
    }

    public void setIsSeller(Boolean seller) {
        isSeller = seller;
    }
}
