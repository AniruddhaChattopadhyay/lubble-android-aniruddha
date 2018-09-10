package in.lubble.app.marketplace;

import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SliderData implements Serializable {

    private static final long serialVersionUID = -3715386894938735967L;

    public static final int ITEM = 0;
    public static final int CATEGORY = 1;
    public static final int SELLER = 2;
    public static final int DASH = 3;
    public static final int REFER = 4;

    @SerializedName("photo")
    private String url;

    @SerializedName("title")
    @Nullable
    private String title;

    @SerializedName("description")
    @Nullable
    private String desc;

    @SerializedName("click_type")
    private int clickType = -1;

    @SerializedName("click_id")
    private int clickId = -1;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Nullable
    public String getTitle() {
        return title;
    }

    public void setTitle(@Nullable String title) {
        this.title = title;
    }

    @Nullable
    public String getDesc() {
        return desc;
    }

    public void setDesc(@Nullable String desc) {
        this.desc = desc;
    }

    public int getClickType() {
        return clickType;
    }

    public void setClickType(int clickType) {
        this.clickType = clickType;
    }

    public int getClickId() {
        return clickId;
    }

    public void setClickId(int clickId) {
        this.clickId = clickId;
    }
}
