package in.lubble.app.referrals;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LeaderboardPersonData {

    @SerializedName("uid")
    @Expose
    private String uid;
    @SerializedName("points")
    @Expose
    private int points;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("thumbnail")
    @Expose
    private String thumbnail;
    @SerializedName("rank")
    @Expose
    private int currentUserRank = 0;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
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

    public int getCurrentUserRank() {
        return currentUserRank;
    }

    public void setCurrentUserRank(int currentUserRank) {
        this.currentUserRank = currentUserRank;
    }
}
