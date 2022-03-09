package in.lubble.app.marketplace;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SliderData implements Serializable {

    private static final long serialVersionUID = -3715386894938735967L;

    @SerializedName("photo")
    private String url;
    @SerializedName("deep_link")
    private String deepLink;
    @SerializedName("title")
    private String title;

    public SliderData(){

    }

    public SliderData(String title, String url){
        this.title = title;
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDeepLink() {
        return deepLink;
    }

    public void setDeepLink(String deepLink) {
        this.deepLink = deepLink;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
