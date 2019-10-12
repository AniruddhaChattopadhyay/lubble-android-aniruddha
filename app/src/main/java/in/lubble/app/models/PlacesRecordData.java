package in.lubble.app.models;

import com.google.gson.annotations.SerializedName;

public class PlacesRecordData {

    @SerializedName("id")
    private String id;
    @SerializedName("fields")
    private PlacesData placesData;
    @SerializedName("createdTime")
    private String createdTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PlacesData getPlacesData() {
        return placesData;
    }

    public void setPlacesData(PlacesData placesData) {
        this.placesData = placesData;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

}
