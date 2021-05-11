package in.lubble.app.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class FeedGroupData implements Serializable {

    private static final long serialVersionUID = 474117089511355909L;

    @SerializedName("id")
    private Integer id;

    public FeedGroupData(Integer id, String name, String feedName, String lubble) {
        this.id = id;
        this.name = name;
        this.feedName = feedName;
        this.lubble = lubble;
    }

    @SerializedName("name")
    private String name;
    @SerializedName("feedName")
    private String feedName;
    @SerializedName("lubble")
    private String lubble;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFeedName() {
        return feedName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    public String getLubble() {
        return lubble;
    }

    public void setLubble(String lubble) {
        this.lubble = lubble;
    }

}
