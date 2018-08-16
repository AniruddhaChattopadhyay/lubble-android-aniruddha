package in.lubble.app.models.marketplace;

import com.google.gson.annotations.SerializedName;

public class ServiceData {

    @SerializedName("title")
    private String title;
    @SerializedName("price")
    private Integer price = -1;

    public ServiceData(String title, Integer price) {
        this.title = title;
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }
}
