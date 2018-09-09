
package in.lubble.app.models.marketplace;

import android.support.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import in.lubble.app.marketplace.RatingData;

public class Item {

    public static final int ITEM_PRODUCT = 0;
    public static final int ITEM_SERVICE = 1;

    public static final int ITEM_PENDING_APPROVAL = 0;
    public static final int ITEM_APPROVED = 1;
    public static final int ITEM_REJECTED = 2;

    public static final int ITEM_PRICING_PAID = 0;
    public static final int ITEM_PRICING_ON_REQUEST = 1;

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
    @SerializedName("deal_price")
    @Expose
    private int dealPrice = 0;
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
    private Category category;
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
    @SerializedName("my_rating")
    @Expose
    @Nullable
    private RatingData ratingData;
    @SerializedName("ratings")
    @Expose
    @Nullable
    private ArrayList<RatingData> userRatingsList;
    @SerializedName("total_rating")
    @Expose
    private long totalRating;
    @SerializedName("total_ratings_count")
    @Expose
    private long totalRatingsCount;
    @SerializedName("approval_status")
    @Expose
    private int approvalStatus = ITEM_PENDING_APPROVAL;
    @SerializedName("rejection_reason")
    @Expose
    private String rejectionReason;
    @SerializedName("pricing_option")
    @Expose
    private int pricingOption = ITEM_PRICING_PAID;

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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
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

    @Nullable
    public RatingData getRatingData() {
        return ratingData;
    }

    public void setRatingData(@Nullable RatingData ratingData) {
        this.ratingData = ratingData;
    }

    @Nullable
    public ArrayList<RatingData> getUserRatingsList() {
        return userRatingsList;
    }

    public void setUserRatingsList(@Nullable ArrayList<RatingData> userRatingsList) {
        this.userRatingsList = userRatingsList;
    }

    public long getTotalRating() {
        return totalRating;
    }

    public void setTotalRating(long totalRating) {
        this.totalRating = totalRating;
    }

    public long getTotalRatingsCount() {
        return totalRatingsCount;
    }

    public void setTotalRatingsCount(long totalRatingsCount) {
        this.totalRatingsCount = totalRatingsCount;
    }

    public int getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(int approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public int getPricingOption() {
        return pricingOption;
    }

    public void setPricingOption(int pricingOption) {
        this.pricingOption = pricingOption;
    }

    public Integer getDealPrice() {
        return dealPrice;
    }

    public void setDealPrice(Integer dealPrice) {
        this.dealPrice = dealPrice;
    }

    public String getSavingsText() {
        if (this.dealPrice > 0) {
            final int saving = this.mrp - this.dealPrice;
            return "₹ " + saving + " (" + Math.round(((float) saving / this.mrp) * 100) + "%)";
        } else if (!this.mrp.equals(this.sellingPrice)) {
            final int saving = this.mrp - this.sellingPrice;
            return "You Save: ₹ " + saving + " (" + Math.round(((float) saving / this.mrp) * 100) + "%)";
        } else {
            return "";
        }
    }

    public String getSavingPercentText() {
        if (this.dealPrice > 0) {
            final int saving = this.mrp - this.dealPrice;
            return Math.round(((float) saving / this.mrp) * 100) + "%\noff";
        } else if (!this.mrp.equals(this.sellingPrice)) {
            final int saving = this.mrp - this.sellingPrice;
            return Math.round(((float) saving / this.mrp) * 100) + "%\noff";
        } else {
            return "";
        }
    }

}
