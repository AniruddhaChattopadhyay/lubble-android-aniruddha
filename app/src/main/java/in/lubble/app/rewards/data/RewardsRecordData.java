package in.lubble.app.rewards.data;

import com.google.gson.annotations.SerializedName;

public class RewardsRecordData {
    @SerializedName("fields")
    private RewardsData fields;

    public RewardsData getFields() {
        return fields;
    }

    public void setFields(RewardsData fields) {
        this.fields = fields;
    }

}
