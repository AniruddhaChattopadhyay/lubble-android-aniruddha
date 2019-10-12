package in.lubble.app.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AirtablePlacesData {

    @SerializedName("records")
    @Expose
    private List<PlacesRecordData> records = null;
    @SerializedName("offset")
    @Expose
    private String offset;

    public List<PlacesRecordData> getRecords() {
        return records;
    }

    public void setRecords(List<PlacesRecordData> records) {
        this.records = records;
    }

    public String getOffset() {
        return offset;
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }

}
