package in.lubble.app.referrals;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ReferralLeaderboardData {

    @SerializedName("leaderboard")
    @Expose
    private List<LeaderboardPersonData> leaderboard = new ArrayList<>();

    @SerializedName("current_user")
    @Expose
    private LeaderboardPersonData currentUser;


    public List<LeaderboardPersonData> getLeaderboardData() {
        return leaderboard;
    }

    public void setLeaderboardData(List<LeaderboardPersonData> leaderboard) {
        this.leaderboard = leaderboard;
    }

    public LeaderboardPersonData getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(LeaderboardPersonData currentUser) {
        this.currentUser = currentUser;
    }

}
