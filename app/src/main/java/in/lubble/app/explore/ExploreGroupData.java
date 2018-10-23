package in.lubble.app.explore;

import com.google.gson.annotations.SerializedName;

public class ExploreGroupData {

    @SerializedName("title")
    private String title;
    @SerializedName("icon")
    private String photoUrl;
    @SerializedName("firebase_id")
    private String firebaseGroupId;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getFirebaseGroupId() {
        return firebaseGroupId;
    }

    public void setFirebaseGroupId(String firebaseGroupId) {
        this.firebaseGroupId = firebaseGroupId;
    }

}
