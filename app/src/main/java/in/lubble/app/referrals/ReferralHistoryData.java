package in.lubble.app.referrals;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ReferralHistoryData {

    @SerializedName("total_points")
    private int totalPoints;
    @SerializedName("history")
    private ArrayList<ReferralPersonData> referralPersonData;

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public ArrayList<ReferralPersonData> getReferralPersonData() {
        return referralPersonData;
    }

    public void setReferralPersonData(ArrayList<ReferralPersonData> referralPersonData) {
        this.referralPersonData = referralPersonData;
    }
}
