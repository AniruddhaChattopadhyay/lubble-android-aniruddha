package in.lubble.app.models;

import androidx.annotation.Nullable;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ishaan on 21/1/18.
 */

public class ChatData implements Serializable {
    private static final long serialVersionUID = -7409872225384209027L;

    /*POST TYPES*/
    public static final String LINK = "LINK";
    public static final String HIDDEN = "HIDDEN";
    public static final String SYSTEM = "SYSTEM";
    public static final String UNREAD = "UNREAD";
    public static final String REPLY = "REPLY";
    public static final String POLL = "POLL";
    public static final String GROUP = "GROUP";
    public static final String EVENT = "EVENT";
    public static final String GROUP_PROMPT = "GROUP_PROMPT";

    private String id;
    private String authorUid;
    private boolean authorIsSeller;
    private String message;
    private String imgUrl;
    private String vidUrl;
    private int lubbCount = 0;
    private HashMap<String, Long> lubbReceipts = new HashMap<>();
    private long createdTimestamp;
    private Object serverTimestamp;
    private HashMap<String, Long> deliveryReceipts = new HashMap<>();
    private HashMap<String, Long> readReceipts = new HashMap<>();
    private String type = "";
    private String promptQues;
    private String linkTitle;
    private String linkDesc;
    private String linkPicUrl;
    @Nullable
    private String replyMsgId = null;
    private boolean isDm;
    private boolean sendNotif = true;
    // for polls
    private ArrayList<ChoiceData> choiceList;
    private String pollQues = "";
    private HashMap<String, Integer> pollReceipts = new HashMap<>();
    private String attachedGroupId; // or attached event ID
    private HashMap<String, String> tagged; // <UID, UserName>
    private HashMap<String, Object> reporters; // <UID, Timestamp>

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
        if (this.type.equalsIgnoreCase(POLL)) {
            return "POLL";
        }
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


    public String getVidUrl() {
        return vidUrl;
    }

    public void setVidUrl(String vidUrl) {
        this.vidUrl = vidUrl;
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

    @Deprecated
    // use getServerTimestampInLong() instead
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

    public boolean isAuthorIsSeller() {
        return authorIsSeller;
    }

    public void setAuthorIsSeller(boolean authorIsSeller) {
        this.authorIsSeller = authorIsSeller;
    }

    public boolean getIsDm() {
        return isDm;
    }

    public void setIsDm(boolean dm) {
        isDm = dm;
    }

    public ArrayList<ChoiceData> getChoiceList() {
        return choiceList;
    }

    public void setChoiceList(ArrayList<ChoiceData> choiceList) {
        this.choiceList = choiceList;
    }

    public String getPollQues() {
        return pollQues;
    }

    public void setPollQues(String pollQues) {
        this.pollQues = pollQues;
    }

    public HashMap<String, Integer> getPollReceipts() {
        return pollReceipts;
    }

    public void setPollReceipts(HashMap<String, Integer> pollReceipts) {
        this.pollReceipts = pollReceipts;
    }

    // or attached event ID
    public String getAttachedGroupId() {
        return attachedGroupId;
    }

    public void setAttachedGroupId(String attachedGroupId) {
        this.attachedGroupId = attachedGroupId;
    }

    public String getLinkPicUrl() {
        return linkPicUrl;
    }

    public void setLinkPicUrl(String linkPicUrl) {
        this.linkPicUrl = linkPicUrl;
    }

    public HashMap<String, String> getTagged() {
        return tagged;
    }

    public void setTagged(HashMap<String, String> tagged) {
        this.tagged = tagged;
    }

    public String getPromptQues() {
        return promptQues;
    }

    public void setPromptQues(String promptQues) {
        this.promptQues = promptQues;
    }

    public boolean isSendNotif() {
        return sendNotif;
    }

    public void setSendNotif(boolean sendNotif) {
        this.sendNotif = sendNotif;
    }

    public HashMap<String, Object> getReporters() {
        return reporters;
    }

    public void setReporters(HashMap<String, Object> reporters) {
        this.reporters = reporters;
    }
}
