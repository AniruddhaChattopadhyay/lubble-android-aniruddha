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

    public FeedGroupData(String name, String feedName, String photoUrl,String emptyString) {
        this.name = name;
        this.feedName = feedName;
        this.photoUrl = photoUrl;
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
    @SerializedName("groupPhotoUrl")
    private String photoUrl;
    @Nullable
    @SerializedName("joined")
    private boolean groupJoined;

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
        return feedName == null ? this.name.replaceAll(" ","_") + "_" + this.lubble : feedName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    // don't use this lubble. Feed Groups are not meant to be constrained to any lubble (n'hood)
    private String getLubble() {
        return lubble;
    }

    public void setLubble(String lubble) {
        this.lubble = lubble;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public boolean isGroupJoined() {
        return groupJoined;
    }

    public void setGroupJoined(boolean groupJoined) {
        this.groupJoined = groupJoined;
    }
}
