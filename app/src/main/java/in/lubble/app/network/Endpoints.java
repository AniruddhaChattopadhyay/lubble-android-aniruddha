package in.lubble.app.network;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import in.lubble.app.auth.LocationsData;
import in.lubble.app.explore.ExploreGroupData;
import in.lubble.app.marketplace.ItemSearchData;
import in.lubble.app.marketplace.RatingData;
import in.lubble.app.marketplace.SliderData;
import in.lubble.app.models.AirtableCollectionData;
import in.lubble.app.models.AirtablePlacesData;
import in.lubble.app.models.EventData;
import in.lubble.app.models.EventIdData;
import in.lubble.app.models.FeatureData;
import in.lubble.app.models.FeedGroupData;
import in.lubble.app.models.GroupData;
import in.lubble.app.models.GroupInfoData;
import in.lubble.app.models.airtable_pojo.AirtableBooksData;
import in.lubble.app.models.airtable_pojo.AirtableBooksRecord;
import in.lubble.app.models.marketplace.Category;
import in.lubble.app.models.marketplace.Item;
import in.lubble.app.models.marketplace.MarketplaceData;
import in.lubble.app.models.marketplace.SellerData;
import in.lubble.app.models.pojos.BooksData;
import in.lubble.app.profile.UserProfileData;
import in.lubble.app.quiz.PlaceData;
import in.lubble.app.quiz.QuestionData;
import in.lubble.app.referrals.ReferralHistoryData;
import in.lubble.app.referrals.ReferralLeaderboardData;
import in.lubble.app.rewards.data.RewardCodesAirtableData;
import in.lubble.app.rewards.data.RewardsAirtableData;
import in.lubble.app.utils.LinkMetaData;
import in.lubble.app.utils.YoutubeData;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

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

    @GET("marketplace/items/seller/{seller_name}/")
    Call<SellerData> fetchSellerItems(@Path("seller_name") String sellerName);

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

    @GET("marketplace/servicecategories/")
    Call<ArrayList<Category>> fetchServiceCategories();

    @GET("referral/history/")
    Call<ReferralHistoryData> fetchReferralHistory();

    @GET("referral/leaderboard/")
    Call<ReferralLeaderboardData> fetchReferralLeaderboard();

    @POST("signup/")
    Call<ArrayList<LocationsData>> uploadSignUp(@Body RequestBody params);

    @POST("signup_complete/")
    Call<Void> uploadSignUpComplete(@Body RequestBody params);

    @GET("home/")
    Call<ArrayList<SliderData>> fetchHomeData();

    @GET("explore/{lubble_id}/")
    Call<ArrayList<ExploreGroupData>> fetchExploreGroups(@Path("lubble_id") String lubbleId);

    @GET("profile/{user_id}/")
    Call<UserProfileData> fetchUserProfile(@Path("user_id") String userId);

    @GET
    public Call<YoutubeData> getYoutubeData(@Url String url);

    @GET("quiz/")
    public Call<ArrayList<QuestionData>> getQuizQuestions();

    @POST("quiz/")
    public Call<PlaceData> getQuizResult(@Body RequestBody params);

    @GET
    public Call<AirtableData> fetchMore(@Url String url);

    @GET
    public Call<AirtableCollectionData> fetchEntries(@Url String url);

    @GET
    public Call<AirtablePlacesData> fetchPlaces(@Url String url);

    @GET
    public Call<BooksData> searchBooks(@Url String url);

    @GET
    public Call<AirtableBooksData> fetchBooks(@Url String url);

    @POST
    public Call<AirtableBooksRecord> uploadNewBook(@Url String url, @Body RequestBody params);

    @POST
    public Call<AirtableBooksRecord> uploadNewOrder(@Url String url, @Body RequestBody params);

    @GET
    public Call<RewardsAirtableData> fetchRewards(@Url String url);

    @GET
    public Call<RewardCodesAirtableData> fetchRewardCodes(@Url String url);

    @PATCH
    public Call<RewardsAirtableData> uploadRewardClaim(@Url String url, @Body RequestBody params);

    @POST
    public Call<RewardsAirtableData> uploadInstalledApps(@Url String url, @Body RequestBody params);

    @PUT("marketplace/applist/")
    public Call<RewardsAirtableData> uploadInstalledApps(@Body RequestBody params);

    @GET("marketplace/events/list/")
    Call<List<EventData>> getEvents(@Query("lubble_id") String lubble_id);

    @GET("marketplace/events/list/")
    Call<List<EventData>> getEvents(@Query("lat") double latitude, @Query("long") double longitude);

    @GET("marketplace/events/")
    Call<List<EventData>> getEvent(@Query("event_id") String event_id);

    @POST("marketplace/events/attendee/")
    Call<Void> uploadattendee(@Body RequestBody params);

    @POST("marketplace/events/")
    Call<EventIdData> upload_new_event(@Body RequestBody params);

    @GET("marketplace/seller/phone_find/")
    Call<ExistingSellerData> fetchExistingSellerFromPh(@Query("phone") String phone);

    @PUT("marketplace/autolike/")
    Call<Void> superLikeMsg(@Body RequestBody params);

    @GET("marketplace/getFeedUserToken/")
    Call<StreamCredentials> getStreamCredentials(@Query("feed_id") String feed_id);

    @GET("marketplace/getFeedGroupList/")
    Call<List<FeedGroupData>> getFeedGroupList();

    @GET("marketplace/getAllFeedGroups/")
    Call<List<FeedGroupData>> getAllFeedGroupList();

    @GET("marketplace/getExploreFeedGroupList/")
    Call<List<FeedGroupData>> getExploreFeedGroupList();

    @POST("marketplace/addToUserLocalityAndGroupFeed/")
    Call<Void> addFeedPost(@Body RequestBody params);

    @POST("marketplace/addDeleteGroupForUser/")
    Call<Void> addGroupForUser(@Body RequestBody params);

    //@DELETE("marketplace/addDeleteGroupForUser/")
    @HTTP(method = "DELETE", path = "marketplace/addDeleteGroupForUser/", hasBody = true)
    Call<Void> deleteGroupForUser(@Body RequestBody params);

    @POST("marketplace/promotePost/")
    Call<Void> promotePost(@Body RequestBody params);

    @POST("marketplace/deletePostByUserOrAdmin/")
    Call<Void> deletePost(@Body RequestBody params);

    @POST("marketplace/batchFollowGroups/")
    Call<Void> batchFollowGroups(@Body RequestBody params);

    @GET("marketplace/checkIfUserHasJoinedGroups/")
    Call<String> checkIfGroupJoined();

    @GET("users.json")
    Call<JsonObject> fetchLubbleMembers(@Query("orderBy") String orderBy, @Query("startAt") String startAt, @Query("auth") String token);

    @GET("users.json")
    Call<JsonObject> fetchLubbleMembersLimit(@Query("orderBy") String orderBy, @Query("startAt") String startAt, @Query("limitToLast") int limit, @Query("auth") String token);

    @GET("lubbles/{lubble_id}/groups/{group_id}.json")
    Call<GroupData> fetchGroupData(@Path("lubble_id") String lubbleId, @Path("group_id") String groupId, @Query("auth") String token);

    @GET("lubbles/{lubble_id}/groups/{group_id}/groupInfo.json")
    Call<GroupInfoData> fetchGroupInfo(@Path("lubble_id") String lubbleId, @Path("group_id") String groupId, @Query("auth") String token);

    @GET("marketplace/getFeedGroupInfo/")
    Call<FeedGroupData> getFeedGroupInfo(@Query("feedName") String feedName);

    @POST("marketplace/getLinkMetaData/")
    Call<LinkMetaData> getLinkMetaData(@Body RequestBody params);

    public class StreamCredentials {
        private String api_key;
        private String user_token;

        public String getApi_key() {
            return api_key;
        }

        public String getUser_token() {
            return user_token;
        }

        public void setUser_token(String user_token) {
            this.user_token = user_token;
        }

        public void setApi_key(String api_key) {
            this.api_key = api_key;
        }

    }

    public class ExistingSellerData {

        @SerializedName("seller_id")
        private ArrayList<String> sellerIdList;

        public ArrayList<String> getSellerIdList() {
            return sellerIdList;
        }

        public void setSellerIdList(ArrayList<String> sellerIdList) {
            this.sellerIdList = sellerIdList;
        }
    }

}
