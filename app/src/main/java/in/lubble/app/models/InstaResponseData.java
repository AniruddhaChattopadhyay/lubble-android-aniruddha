package in.lubble.app.models;

import com.google.gson.annotations.SerializedName;

public class InstaResponseData {
    //getting data from insta json
    @SerializedName("data")
    private InstaData data;

    public InstaData getData() {
        return data;
    }

    public void setData(InstaData data) {
        this.data = data;
    }
}