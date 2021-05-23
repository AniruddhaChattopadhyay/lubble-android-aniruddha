package in.lubble.app.models;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class FeedGroupData implements Serializable {

    private static final long serialVersionUID = 474117089511355909L;

    @Nullable
    @SerializedName("id")
    private Integer id;

    public FeedGroupData(String name, String feedName, String lubble) {
        this.name = name;
        this.feedName = feedName;
        this.lubble = lubble;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FeedGroupData that = (FeedGroupData) o;

        return feedName.equals(that.feedName);
    }

    @Override
    public int hashCode() {
        return feedName.hashCode();
    }

    @SerializedName("name")
    private String name;
    @SerializedName("feedName")
    private String feedName;
    @SerializedName("lubble")
    private String lubble;

    public @Nullable Integer getId() {
        return id;
    }

    public void setId(@Nullable Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFeedName() {
        return feedName == null ? this.name + "_" + this.lubble : feedName;
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
