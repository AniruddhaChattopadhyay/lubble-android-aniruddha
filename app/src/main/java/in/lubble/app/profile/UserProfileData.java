package in.lubble.app.profile;

import com.google.gson.annotations.SerializedName;

public class UserProfileData {

    @SerializedName("points")
    private int points;
    @SerializedName("rank")
    private int rank;
    @SerializedName("referrals")
    private int referrals;

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getReferrals() {
        return referrals;
    }

    public void setReferrals(int referrals) {
        this.referrals = referrals;
    }
}
