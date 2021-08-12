package in.lubble.app.profile;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import in.lubble.app.models.FeedGroupData;

public class UserProfileData {

    @SerializedName("points")
    private int points;
    @SerializedName("rank")
    private int rank;
    @SerializedName("referrals")
    private int referrals;
    @SerializedName("Joined Groups")
    private List<FeedGroupData> joinedGroups;

    public List<FeedGroupData> getJoinedGroups() {
        return joinedGroups;
    }

    public void setJoinedGroups(List<FeedGroupData> joinedGroups) {
        this.joinedGroups = joinedGroups;
    }

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
