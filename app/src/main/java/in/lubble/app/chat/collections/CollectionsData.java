package in.lubble.app.chat.collections;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class CollectionsData implements Serializable {

    private static final long serialVersionUID = -7669977074449805999L;
    @SerializedName("Title")
    private String title;
    @SerializedName("Caption")
    private String caption;
    @SerializedName("Image")
    private String imageUrl;
    @SerializedName("Places")
    private List<String> placeIdList;
    @SerializedName("Description")
    private String description;

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

    public List<String> getPlaceIdList() {
        return placeIdList;
    }

    public void setPlaceIdList(List<String> placeIdList) {
        this.placeIdList = placeIdList;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
