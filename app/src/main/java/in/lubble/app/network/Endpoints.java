package in.lubble.app.network;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import in.lubble.app.marketplace.ItemSearchData;
import in.lubble.app.marketplace.RatingData;
import in.lubble.app.models.FeatureData;
import in.lubble.app.models.marketplace.Category;
import in.lubble.app.models.marketplace.Item;
import in.lubble.app.models.marketplace.MarketplaceData;
import in.lubble.app.models.marketplace.SellerData;
import in.lubble.app.referrals.ReferralHistoryData;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface Endpoints {

    @GET("marketplace/home/")
    Call<MarketplaceData> fetchMarketplaceData();

    @GET("marketplace/categories/")
    Call<ArrayList<Category>> fetchCategories();

    @POST("marketplace/item/")
    Call<Item> uploadNewItem(@Body RequestBody params);

    @PUT("marketplace/item/{item_id}/")
    Call<Item> updateItem(@Path("item_id") int itemId, @Body RequestBody params);

    @GET("marketplace/item/{item_id}/")
    Call<Item> fetchItemDetails(@Path("item_id") int itemId);

    @POST("marketplace/seller/")
    Call<SellerData> uploadSellerProfile(@Body RequestBody params);

    @GET("marketplace/seller/{seller_id}/")
    Call<SellerData> fetchSellerProfile(@Path("seller_id") int sellerId);

    @PUT("marketplace/seller/{seller_id}/")
    Call<SellerData> updateSellerProfile(@Path("seller_id") int sellerId, @Body RequestBody params);

    @GET("app/features/")
    Call<FeatureData> fetchAppFeatures();

    @GET("marketplace/items/category/{cat_id}/")
    Call<Category> fetchCategoryItems(@Path("cat_id") int categoryId);

    @GET("marketplace/items/seller/{seller_id}/")
    Call<SellerData> fetchSellerItems(@Path("seller_id") int sellerId);

    @GET("marketplace/itemlist/")
    Call<ArrayList<ItemSearchData>> fetchItemList();

    @POST("marketplace/item/{item_id}/rating/")
    Call<RatingData> uploadNewRating(@Path("item_id") int itemId, @Body RequestBody params);

    @PUT("marketplace/rating/{rating_id}/")
    Call<RatingData> updateRating(@Path("rating_id") int ratingId, @Body RequestBody params);

    @POST("marketplace/seller/{seller_id}/recommend/")
    Call<RatingData> uploadRecommendation(@Path("seller_id") int sellerId);

    @DELETE("marketplace/seller/{seller_id}/recommend/")
    Call<RatingData> deleteRecommendation(@Path("seller_id") int sellerId);

    @GET("marketplace/allitems/")
    Call<ArrayList<Item>> fetchAllItems();

    @GET("/marketplace/servicecategories/")
    Call<ArrayList<Category>> fetchServiceCategories();

    @GET("/referral/history/")
    Call<ReferralHistoryData> fetchReferralHistory();

    class ResponseBean {

        @SerializedName("message")
        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

}
