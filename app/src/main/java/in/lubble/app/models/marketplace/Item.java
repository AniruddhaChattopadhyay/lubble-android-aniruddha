
package in.lubble.app.models.marketplace;

import android.support.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Item {

    public static final int ITEM_PRODUCT = 0;
    public static final int ITEM_SERVICE = 1;

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("created_timestamp")
    @Expose
    private String createdTimestamp;
    @SerializedName("updated_timestamp")
    @Expose
    private String updatedTimestamp;
    @SerializedName("is_active")
    @Expose
    private Boolean isActive;
    @SerializedName("photos")
    @Expose
    private ArrayList<PhotoData> photos;
    @SerializedName("mrp")
    @Expose
    private Integer mrp;
    @SerializedName("selling_price")
    @Expose
    private Integer sellingPrice;
    @SerializedName("starting_price")
    @Expose
    private Integer startingPrice;
    @SerializedName("view_count")
    @Expose
    private Integer viewCount;
    @SerializedName("seller")
    @Expose
    private String seller;
    @SerializedName("category")
    @Expose
    private Integer category;
    @SerializedName("seller_details")
    @Expose
    @Nullable
    private SellerData sellerData;
    @SerializedName("type")
    @Expose
    private int type = 0;
    @SerializedName("service_catalog")
    @Expose
    @Nullable
    private ArrayList<ServiceData> serviceDataList;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Item) {
            Item objectToCompare = (Item) obj;
            if (this.id != null && objectToCompare.getId() != null && this.id.equals(objectToCompare.getId())) {
                return true;
            }
            return false;
        }
        return super.equals(obj);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(String createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public String getUpdatedTimestamp() {
        return updatedTimestamp;
    }

    public void setUpdatedTimestamp(String updatedTimestamp) {
        this.updatedTimestamp = updatedTimestamp;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public ArrayList<PhotoData> getPhotos() {
        return photos;
    }

    public void setPhotos(ArrayList<PhotoData> photos) {
        this.photos = photos;
    }

    public Integer getMrp() {
        return mrp;
    }

    public void setMrp(Integer mrp) {
        this.mrp = mrp;
    }

    public Integer getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(Integer sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public Integer getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(Integer startingPrice) {
        this.startingPrice = startingPrice;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    @Nullable
    public SellerData getSellerData() {
        return sellerData;
    }

    public void setSellerData(@Nullable SellerData sellerData) {
        this.sellerData = sellerData;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Nullable
    public ArrayList<ServiceData> getServiceDataList() {
        return serviceDataList;
    }

    public void setServiceDataList(@Nullable ArrayList<ServiceData> serviceDataList) {
        this.serviceDataList = serviceDataList;
    }
}
