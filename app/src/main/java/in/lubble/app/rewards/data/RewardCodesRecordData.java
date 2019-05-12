package in.lubble.app.rewards.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RewardCodesRecordData {

    @SerializedName("fields")
    @Expose
    private RewardCodesData fields;

    public RewardCodesData getFields() {
        return fields;
    }

    public void setFields(RewardCodesData fields) {
        this.fields = fields;
    }

}
