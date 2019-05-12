package in.lubble.app.rewards.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RewardCodesAirtableData {

    @SerializedName("records")
    private List<RewardCodesRecordData> records = null;

    public List<RewardCodesRecordData> getRecords() {
        return records;
    }

    public void setRecords(List<RewardCodesRecordData> records) {
        this.records = records;
    }

}
