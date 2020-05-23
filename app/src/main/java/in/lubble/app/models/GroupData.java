package in.lubble.app.models;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Set;

import in.lubble.app.LubbleSharedPrefs;

/**
 * Created by ishaan on 28/1/18.
 */

public class GroupData {

    private String id;
    private String profilePic;
    private String thumbnail;
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
    @Exclude
    private long unreadCount = 0;
    private boolean isPinned;
    @Exclude
    private boolean isDm;

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
    public boolean isJoined() {
        return getMembers().get(FirebaseAuth.getInstance().getUid()) != null
                || getMembers().get(String.valueOf(LubbleSharedPrefs.getInstance().getSellerId())) != null;
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

    @Exclude
    public long getJoinedTimestamp() {
        if (isJoined() && !this.isDm) {
            return (Long) ((HashMap) getMembers().get(FirebaseAuth.getInstance().getUid())).get("joinedTimestamp");
        } else {
            return 0;
        }
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

    @Exclude
    public long getUnreadCount() {
        return unreadCount;
    }

    @Exclude
    public void setUnreadCount(long unreadCount) {
        this.unreadCount = unreadCount;
    }
}
