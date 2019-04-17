package in.lubble.app.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AirtableData {

    @SerializedName("records")
    @Expose
    private List<AirtableRecordData> records = null;

    public List<AirtableRecordData> getRecords() {
        return records;
    }

    public void setRecords(List<AirtableRecordData> records) {
        this.records = records;
    }

}
