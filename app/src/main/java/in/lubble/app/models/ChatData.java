package in.lubble.app.models;

import android.support.annotation.Nullable;

import com.google.firebase.database.Exclude;

import java.util.HashMap;

/**
 * Created by ishaan on 21/1/18.
 */

public class ChatData {

    /*POST TYPES*/
    public static final String LINK = "LINK";
    public static final String HIDDEN = "HIDDEN";
    public static final String SYSTEM = "SYSTEM";
    public static final String UNREAD = "UNREAD";
    public static final String REPLY = "REPLY";

    private String id;
    private String authorUid;
    private String message;
    private String imgUrl;
    private int lubbCount = 0;
    private HashMap<String, Long> lubbReceipts = new HashMap<>();
    private long createdTimestamp;
    private Object serverTimestamp;
    private HashMap<String, Long> deliveryReceipts = new HashMap<>();
    private HashMap<String, Long> readReceipts = new HashMap<>();
    private String type = "";
    private String linkTitle;
    private String linkDesc;
    @Nullable
    private String replyMsgId = null;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ChatData) {
            ChatData objectToCompare = (ChatData) obj;
            if (this.id != null && objectToCompare.getId() != null && this.id.equalsIgnoreCase(objectToCompare.getId())) {
                return true;
            }
            return false;
        }
        return super.equals(obj);
    }

    public String getAuthorUid() {
        return authorUid;
    }

    public void setAuthorUid(String authorUid) {
        this.authorUid = authorUid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public int getLubbCount() {
        return lubbCount;
    }

    public void setLubbCount(int lubbCount) {
        this.lubbCount = lubbCount;
    }

    public HashMap<String, Long> getLubbReceipts() {
        return lubbReceipts;
    }

    public void setLubbReceipts(HashMap<String, Long> lubbReceipts) {
        this.lubbReceipts = lubbReceipts;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public Object getServerTimestamp() {
        return serverTimestamp;
    }

    public void setServerTimestamp(Object serverTimestamp) {
        this.serverTimestamp = serverTimestamp;
    }

    @Exclude
    public Long getServerTimestampInLong() {
        return (Long) this.serverTimestamp;
    }

    public HashMap<String, Long> getDeliveryReceipts() {
        return deliveryReceipts;
    }

    public void setDeliveryReceipts(HashMap<String, Long> deliveryReceipts) {
        this.deliveryReceipts = deliveryReceipts;
    }

    public HashMap<String, Long> getReadReceipts() {
        return readReceipts;
    }

    public void setReadReceipts(HashMap<String, Long> readReceipts) {
        this.readReceipts = readReceipts;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLinkTitle() {
        return linkTitle;
    }

    public void setLinkTitle(String linkTitle) {
        this.linkTitle = linkTitle;
    }

    public String getLinkDesc() {
        return linkDesc;
    }

    public void setLinkDesc(String linkDesc) {
        this.linkDesc = linkDesc;
    }

    @Nullable
    public String getReplyMsgId() {
        return replyMsgId;
    }

    public void setReplyMsgId(@Nullable String replyMsgId) {
        this.replyMsgId = replyMsgId;
    }
}
