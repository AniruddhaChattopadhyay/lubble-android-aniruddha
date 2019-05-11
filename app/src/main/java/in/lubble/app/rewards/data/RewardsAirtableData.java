package in.lubble.app.rewards.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RewardsAirtableData {

    @SerializedName("records")
    private List<RewardsRecordData> records = null;

    public List<RewardsRecordData> getRecords() {
        return records;
    }

    public void setRecords(List<RewardsRecordData> records) {
        this.records = records;
    }

}
