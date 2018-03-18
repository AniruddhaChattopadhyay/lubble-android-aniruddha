package in.lubble.app.models;

import java.util.HashMap;

/**
 * Created by ishaan on 18/3/18.
 */

public class UserGroupData {

    private int unreadCount;
    private boolean joined;
    private HashMap<String, Boolean> invitedBy;
    private HashMap<String, Boolean> invitees;

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public boolean isJoined() {
        return joined;
    }

    public void setJoined(boolean joined) {
        this.joined = joined;
    }

    public HashMap<String, Boolean> getInvitedBy() {
        return invitedBy;
    }

    public void setInvitedBy(HashMap<String, Boolean> invitedBy) {
        this.invitedBy = invitedBy;
    }

    public HashMap<String, Boolean> getInvited() {
        return invitees;
    }

    public void setInvited(HashMap<String, Boolean> invitees) {
        this.invitees = invitees;
    }

}
