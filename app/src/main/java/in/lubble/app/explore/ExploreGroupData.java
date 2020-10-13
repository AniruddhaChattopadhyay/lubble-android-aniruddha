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
    @SerializedName("priority")
    private int priority;
    private int memberCount;
    private long lastMessageTimestamp;

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

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public long getLastMessageTimestamp() {
        return this.lastMessageTimestamp;
    }

    public void setLastMessageTimestamp(long lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }
}
