package in.lubble.app.explore;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class ExploreGroupData {

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof ExploreGroupData) {
            ExploreGroupData objectToCompare = (ExploreGroupData) obj;
            if (this.firebaseGroupId.equalsIgnoreCase(objectToCompare.getFirebaseGroupId())) {
                return true;
            }
            return false;
        }
        return super.equals(obj);
    }

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
