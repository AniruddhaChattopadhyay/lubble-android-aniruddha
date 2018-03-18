package in.lubble.app.models;

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
    private HashMap<String, Boolean> members = new HashMap<>();
    private String lastMessage;
    private long lastMessageTimestamp = 0;
    private String createdBy;
    private HashMap<String, Boolean> admins;
    @Exclude
    private Set<String> invitedBy;

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
        return getMembers().get(FirebaseAuth.getInstance().getUid()) != null;
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

    public HashMap<String, Boolean> getMembers() {
        return members;
    }

    public void setMembers(HashMap<String, Boolean> members) {
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

    public HashMap<String, Boolean> getAdmins() {
        return admins;
    }

    public void setAdmins(HashMap<String, Boolean> admins) {
        this.admins = admins;
    }
}
