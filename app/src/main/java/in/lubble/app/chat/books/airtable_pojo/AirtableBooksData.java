package in.lubble.app.chat.books.airtable_pojo;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AirtableBooksData {

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
