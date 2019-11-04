package in.lubble.app.models;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Set;

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
    private String question = "default ques";
    @Exclude
    private Set<String> invitedBy;
    private boolean isPinned;

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
        // is a DM if member size is 0
        return getMembers().size() == 0 || getMembers().get(FirebaseAuth.getInstance().getUid()) != null;
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
        return lastMessageTimestamp;
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
        if (isJoined() && getMembers().size() > 0) {
            // is a DM if member size is 0
            return (Long) ((HashMap) getMembers().get(FirebaseAuth.getInstance().getUid())).get("joinedTimestamp");
        } else {
            return 0;
        }
    }

    public boolean isDm() {
        // Yeah. Weird.
        return getMembers().size() == 0;
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
}
