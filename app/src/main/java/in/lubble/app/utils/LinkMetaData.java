package in.lubble.app.utils;

import com.google.gson.annotations.SerializedName;

public class LinkMetaData {
    @SerializedName("title")
    String title;

    @SerializedName("desc")
    String desc;

    @SerializedName("image")
    String imageUrl;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

}
