package in.lubble.app.models.airtable_pojo;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class AirtableBooksData implements Serializable {

    private static final long serialVersionUID = -7430638098690465518L;
    @SerializedName("records")
    private List<AirtableBooksRecord> records = null;
    @SerializedName("offset")
    private String offset;

    public List<AirtableBooksRecord> getRecords() {
        return records;
    }

    public void setRecords(List<AirtableBooksRecord> records) {
        this.records = records;
    }

    public String getOffset() {
        return offset;
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }


}
