package in.lubble.app.chat;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AirtableCollectionData {

    @SerializedName("records")
    @Expose
    private List<CollectionRecordData> records = null;

    public List<CollectionRecordData> getRecords() {
        return records;
    }

    public void setRecords(List<CollectionRecordData> records) {
        this.records = records;
    }

}
