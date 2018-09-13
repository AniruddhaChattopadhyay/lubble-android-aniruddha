package in.lubble.app.referrals;

import com.google.gson.annotations.SerializedName;

public class ReferralLeaderboardData {

    @SerializedName("points")
    private int points;
    @SerializedName("name")
    private String name;
    @SerializedName("thumbnail")
    private String thumbnail;

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
}
