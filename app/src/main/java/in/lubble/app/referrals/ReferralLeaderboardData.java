package in.lubble.app.referrals;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ReferralLeaderboardData {

    @SerializedName("leaderboard")
    @Expose
    private List<LeaderboardData> leaderboard = new ArrayList<>();
    @SerializedName("rank")
    @Expose
    private Integer rank;
    @SerializedName("points")
    @Expose
    private Integer points;

    public List<LeaderboardData> getLeaderboardData() {
        return leaderboard;
    }

    public void setLeaderboardData(List<LeaderboardData> leaderboard) {
        this.leaderboard = leaderboard;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public class LeaderboardData {

        @SerializedName("uid")
        @Expose
        private String uid;
        @SerializedName("points")
        @Expose
        private Integer points;
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("thumbnail")
        @Expose
        private String thumbnail;

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public Integer getPoints() {
            return points;
        }

        public void setPoints(Integer points) {
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

}
