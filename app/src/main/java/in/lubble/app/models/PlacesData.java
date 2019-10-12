package in.lubble.app.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class PlacesData implements Serializable {

    private static final long serialVersionUID = -3921587497121130689L;

    @SerializedName("RatingProvider")
    private String ratingProvider;
    @SerializedName("Desc")
    private String desc;
    @SerializedName("Collections")
    private List<String> collections = null;
    @SerializedName("Name")
    private String name;
    @SerializedName("PriceHint")
    private String priceHint = "Price for two: ";
    @SerializedName("Price")
    private int price = 0;
    @SerializedName("Locality")
    private String locality;
    @SerializedName("Rating")
    private String rating;
    @SerializedName("Image")
    private String image;
    @SerializedName("Timing")
    private String timing;
    @SerializedName("CtaText")
    private String cTAText;
    @SerializedName("CtaLink")
    private String cTALink;
    @SerializedName("Latitude")
    private Double latitude;
    @SerializedName("Longitude")
    private Double longitude;
    @SerializedName("Special")
    private String special;

    public String getRatingProvider() {
        return ratingProvider;
    }

    public void setRatingProvider(String ratingProvider) {
        this.ratingProvider = ratingProvider;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public List<String> getCollections() {
        return collections;
    }

    public void setCollections(List<String> collections) {
        this.collections = collections;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTiming() {
        return timing;
    }

    public void setTiming(String timing) {
        this.timing = timing;
    }

    public String getCTAText() {
        return cTAText;
    }

    public void setCTAText(String cTAText) {
        this.cTAText = cTAText;
    }

    public String getCTALink() {
        return cTALink;
    }

    public void setCTALink(String cTALink) {
        this.cTALink = cTALink;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getSpecial() {
        return special;
    }

    public void setSpecial(String special) {
        this.special = special;
    }

    public String getPriceHint() {
        return priceHint;
    }

    public void setPriceHint(String priceHint) {
        this.priceHint = priceHint;
    }
}
