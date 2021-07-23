package in.lubble.app.models;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.google.firebase.database.Exclude;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by ishaan on 28/1/18.
 */

public class GroupData {

    @SerializedName("firebase_id")
    private String id;
    @SerializedName("icon")
    private String profilePic;
    private String thumbnail;
    @SerializedName("title")
    private String title;
    private String description;
    private boolean isPrivate;
    private HashMap<String, Object> members = new HashMap<>();
    private String lastMessage;
    private long lastMessageTimestamp = 0;
    private String createdBy;
    private String question;
    private String questionChatId = "101";
    @Exclude
    private Set<String> invitedBy;
    private boolean isPinned;
    @Exclude
    private boolean isDm;
    @Exclude
    private boolean isJoined;

    public GroupData() {
    }  // Needed for Firebase

    public boolean equals(Object obj) {
        if (obj instanceof GroupData) {
            GroupData objectToCompare = (GroupData) obj;
            if (this.id.equalsIgnoreCase(objectToCompare.getId())) {
                return true;
            }
            return false;
        }
        return super.equals(obj);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Exclude
    @Deprecated
    public boolean isJoined() {
        return isJoined;
    }

    @Exclude
    public void setJoined(boolean isJoined) {
        this.isJoined = isJoined;
    }

    public boolean getIsPrivate() {
        return this.isPrivate;
    }

    public void setIsPrivate(boolean aPrivate) {
        this.isPrivate = aPrivate;
    }

    public String getThumbnail() {
        return thumbnail == null ? profilePic : thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public HashMap<String, Object> getMembers() {
        return members;
    }

    public void setMembers(HashMap<String, Object> members) {
        this.members = members;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getLastMessageTimestamp() {
        return this.lastMessageTimestamp;
    }

    public void setLastMessageTimestamp(long lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    @Exclude
    @Nullable
    public Set<String> getInvitedBy() {
        return invitedBy;
    }

    @Exclude
    public void setInvitedBy(Set<String> invitedBy) {
        this.invitedBy = invitedBy;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public boolean getIsDm() {
        return isDm;
    }

    public void setIsDm(boolean isDm) {
        this.isDm = isDm;
    }

    public boolean getIsPinned() {
        return isPinned;
    }

    public void setIsPinned(boolean pinned) {
        isPinned = pinned;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getQuestionChatId() {
        return !TextUtils.isEmpty(this.questionChatId) ? questionChatId : "101";
    }

    public void setQuestionChatId(String questionChatId) {
        this.questionChatId = questionChatId;
    }

}
