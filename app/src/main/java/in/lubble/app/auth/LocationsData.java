package in.lubble.app.auth;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class LocationsData implements Serializable {

    private static final long serialVersionUID = 2064636807194358757L;

    @SerializedName("name")
    private String lubbleName;
    @SerializedName("id")
    private String id;
    @SerializedName("center_lati")
    private double centerLati;
    @SerializedName("center_longi")
    private double centerLongi;

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

    public double getCenterLati() {
        return centerLati;
    }

    public void setCenterLati(double centerLati) {
        this.centerLati = centerLati;
    }

    public double getCenterLongi() {
        return centerLongi;
    }

    public void setCenterLongi(double centerLongi) {
        this.centerLongi = centerLongi;
    }

}
