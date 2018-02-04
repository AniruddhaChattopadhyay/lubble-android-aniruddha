package in.lubble.app.models;

/**
 * Created by ishaan on 28/1/18.
 */

public class GroupData {

    private String id;
    private String iconUrl;
    private String title;
    private String description;
    private boolean isJoined = true;
    private boolean isPrivate;

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

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
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

    public boolean isJoined() {
        return isJoined;
    }

    public void setJoined(boolean joined) {
        isJoined = joined;
    }

    public boolean getIsPrivate() {
        return this.isPrivate;
    }

    public void setIsPrivate(boolean aPrivate) {
        this.isPrivate = aPrivate;
    }

}
