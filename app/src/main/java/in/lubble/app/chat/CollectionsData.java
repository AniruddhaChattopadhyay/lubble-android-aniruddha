package in.lubble.app.chat;

import com.google.gson.annotations.SerializedName;

public class CollectionsData {

    @SerializedName("Title")
    private String title;
    @SerializedName("Caption")
    private String caption;
    @SerializedName("Image")
    private String imageUrl;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
