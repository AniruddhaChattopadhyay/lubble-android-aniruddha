package in.lubble.app.models;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by ishaan on 28/1/18.
 */

public class GroupInfoData {

    private String profilePic;
    private String id;
    private String thumbnail;
    private String title;
    private String description;
    private boolean isPrivate;
    private String lastMessage;
    private long lastMessageTimestamp = 0;
    private String createdBy;
    private String question;
    private String questionChatId = "101";
    private boolean isPinned;
    @Exclude
    private boolean isDm;
    @Exclude
    private Set<String> invitedBy;

    public GroupInfoData() {
    }  // Needed for Firebase

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

    public boolean getIsPrivate() {
        return this.isPrivate;
    }

    public void setIsPrivate(boolean aPrivate) {
        this.isPrivate = aPrivate;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Exclude
    public boolean getIsDm() {
        return isDm;
    }

    @Exclude
    public void setIsDm(boolean dm) {
        isDm = dm;
    }

    @Exclude
    public Set<String> getInvitedBy() {
        return invitedBy;
    }

    @Exclude
    public void setInvitedBy(Set<String> invitedBy) {
        this.invitedBy = invitedBy;
    }
}
