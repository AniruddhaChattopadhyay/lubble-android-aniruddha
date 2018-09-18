package in.lubble.app.auth;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class LocationsData implements Serializable {

    private static final long serialVersionUID = 2064636807194358757L;

    @SerializedName("name")
    private String lubbleName;
    @SerializedName("id")
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLubbleName() {
        return lubbleName;
    }

    public void setLubbleName(String lubbleName) {
        this.lubbleName = lubbleName;
    }

}
