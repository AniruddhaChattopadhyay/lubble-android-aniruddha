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

}
