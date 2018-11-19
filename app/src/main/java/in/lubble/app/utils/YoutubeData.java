package in.lubble.app.utils;

import com.google.gson.annotations.SerializedName;

public class YoutubeData {

    @SerializedName("thumbnail_url")
    private String thumbnailUrl;
    @SerializedName("title")
    private String title;

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
