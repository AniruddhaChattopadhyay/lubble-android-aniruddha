package in.lubble.app.models.search;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GroupId {

    @SerializedName("matchLevel")
    @Expose
    private String matchLevel;
    @SerializedName("value")
    @Expose
    private String value;

    public String getMatchLevel() {
        return matchLevel;
    }

    public void setMatchLevel(String matchLevel) {
        this.matchLevel = matchLevel;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
