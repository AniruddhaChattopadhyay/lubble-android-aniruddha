package in.lubble.app.chat.books.airtable_pojo;

import com.google.gson.annotations.SerializedName;

public class AirtableBooksRecord {

    @SerializedName("id")
    private String id;
    @SerializedName("fields")
    private AirtableBooksFields fields;
    @SerializedName("createdTime")
    private String createdTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public AirtableBooksFields getFields() {
        return fields;
    }

    public void setFields(AirtableBooksFields fields) {
        this.fields = fields;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

}
