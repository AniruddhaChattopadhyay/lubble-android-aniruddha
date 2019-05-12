package in.lubble.app.rewards.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class RewardsData implements Serializable {

    private static final long serialVersionUID = 1875545942123998076L;
    @SerializedName("RecordId")
    private String recordId;
    @SerializedName("LubbleId")
    private String lubbleId;
    @SerializedName("BrandLogo")
    private String brandLogo;
    @SerializedName("Brand")
    private String brand;
    @SerializedName("Title")
    private String title;
    @SerializedName("Description")
    private String description;
    @SerializedName("Photo")
    private String photo;
    @SerializedName("DetailPhoto")
    private String detailPhoto;
    @SerializedName("Cost")
    private Integer cost;
    @SerializedName("Details")
    private String details;
    @SerializedName("Tnc")
    private String tnc;
    @SerializedName("Color")
    private String color;
    @SerializedName("IsExpired")
    private boolean isExpired;
    @SerializedName("IsAvailable")
    private int isAvailable;

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String id) {
        this.recordId = id;
    }

    public String getLubbleId() {
        return lubbleId;
    }

    public void setLubbleId(String lubbleId) {
        this.lubbleId = lubbleId;
    }

    public String getBrandLogo() {
        return brandLogo;
    }

    public void setBrandLogo(String brandLogo) {
        this.brandLogo = brandLogo;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getDetailPhoto() {
        return detailPhoto;
    }

    public void setDetailPhoto(String detailPhoto) {
        this.detailPhoto = detailPhoto;
    }

    public Integer getCost() {
        return cost;
    }

    public void setCost(Integer cost) {
        this.cost = cost;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getTnc() {
        return tnc;
    }

    public void setTnc(String tnc) {
        this.tnc = tnc;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isExpired() {
        return isExpired;
    }

    public void setExpired(boolean expired) {
        isExpired = expired;
    }

    public int getIsAvailable() {
        return isAvailable;
    }

    public boolean isAvailable() {
        return isAvailable == 1;
    }

    public void setIsAvailable(int isAvailable) {
        this.isAvailable = isAvailable;
    }
}
