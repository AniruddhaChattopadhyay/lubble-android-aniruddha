package in.lubble.app.models;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FeedGroupData {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("feedName")
    @Expose
    private String feedName;
    @SerializedName("lubble")
    @Expose
    private Integer lubble;

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

    public Integer getLubble() {
        return lubble;
    }

    public void setLubble(Integer lubble) {
        this.lubble = lubble;
    }

}
