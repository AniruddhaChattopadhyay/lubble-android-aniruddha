package in.lubble.app.models.marketplace;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class SellerData {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("phone")
    @Expose
    private String phone;
    @SerializedName("bio")
    @Expose
    private String bio;
    @SerializedName("photo")
    @Expose
    private String photoUrl;
    @SerializedName("items")
    @Expose
    private ArrayList<Item> itemList;
    @SerializedName("view_count")
    @Expose
    private long viewCount;
    @SerializedName("recommendations_count")
    @Expose
    private long recommendationCount = 0;
    @SerializedName("is_recommended")
    @Expose
    private boolean isRecommended;
    @SerializedName("is_call_enabled")
    @Expose
    private boolean isCallEnabled;
    @SerializedName("web_link")
    @Expose
    private String webLink; // this is just the unique name, like "Pocketables"
    @SerializedName("share_link")
    @Expose
    private String shareLink; // this is the complete share URL, like "https://shop...."
    @SerializedName("subtitle")
    @Expose
    private String subtitle;
    @SerializedName("deal_percent")
    @Expose
    private int dealPercent = 0;

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

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public ArrayList<Item> getItemList() {
        return itemList;
    }

    public void setItemList(ArrayList<Item> itemList) {
        this.itemList = itemList;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public long getViewCount() {
        return viewCount;
    }

    public void setViewCount(long viewCount) {
        this.viewCount = viewCount;
    }

    public long getRecommendationCount() {
        return recommendationCount;
    }

    public void setRecommendationCount(long recommendationCount) {
        this.recommendationCount = recommendationCount;
    }

    public boolean getIsRecommended() {
        return isRecommended;
    }

    public void setIsRecommended(boolean recommended) {
        isRecommended = recommended;
    }

    public boolean isCallEnabled() {
        return isCallEnabled;
    }

    public void setCallEnabled(boolean callEnabled) {
        isCallEnabled = callEnabled;
    }

    public String getWebLink() {
        return webLink;
    }

    public void setWebLink(String webLink) {
        this.webLink = webLink;
    }

    public String getShareLink() {
        return shareLink;
    }

    public void setShareLink(String shareLink) {
        this.shareLink = shareLink;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public int getDealPercent() {
        return dealPercent;
    }

    public void setDealPercent(int dealPercent) {
        this.dealPercent = dealPercent;
    }
}
