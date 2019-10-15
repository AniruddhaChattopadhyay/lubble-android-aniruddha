package in.lubble.app.models;

import com.google.firebase.database.Exclude;

/**
 * Created by ishaan on 10/2/18.
 */

public class ProfileInfo {

    @Exclude
    private String id;
    private String name;
    private String thumbnail;
    private String badge;

    //public String insta_handle;
    //public boolean insta_linked;

//    public String getInsta_handle() {
//        return insta_handle;
//    }
//
//    public void setInsta_handle(String insta_handle) {
//        this.insta_handle = insta_handle;
//    }
//
//    public boolean isInsta_linked() {
//        return insta_linked;
//    }
//
//    public void setInsta_linked(boolean insta_linked) {
//        this.insta_linked = insta_linked;
//    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ProfileInfo) {
            ProfileInfo objectToCompare = (ProfileInfo) obj;
            if (this.id.equalsIgnoreCase(objectToCompare.getId())) {
                return true;
            }
            return false;
        }
        return super.equals(obj);
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }
}
