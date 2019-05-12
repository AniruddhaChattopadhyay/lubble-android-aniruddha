package in.lubble.app.rewards.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RewardCodesData {

    @SerializedName("RewardCode")
    private String rewardCode;
    @SerializedName("RewardId")
    private List<String> rewardId = null;
    @SerializedName("Photo")
    private List<String> photo = null;
    @SerializedName("CreatedTime")
    private String createdTime;
    @SerializedName("RewardRecordId")
    private String rewardRecordId;

    public String getRewardCode() {
        return rewardCode;
    }

    public void setRewardCode(String rewardCode) {
        this.rewardCode = rewardCode;
    }

    public List<String> getRewardId() {
        return rewardId;
    }

    public void setRewardId(List<String> rewardId) {
        this.rewardId = rewardId;
    }

    public List<String> getPhoto() {
        return photo;
    }

    public void setPhoto(List<String> photo) {
        this.photo = photo;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getRewardRecordId() {
        return rewardRecordId;
    }

    public void setRewardRecordId(String rewardRecordId) {
        this.rewardRecordId = rewardRecordId;
    }
}
