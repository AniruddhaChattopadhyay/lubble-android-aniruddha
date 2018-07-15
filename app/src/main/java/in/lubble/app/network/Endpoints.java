package in.lubble.app.network;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import in.lubble.app.models.marketplace.Category;
import in.lubble.app.models.marketplace.Item;
import in.lubble.app.models.marketplace.MarketplaceData;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface Endpoints {

    @GET("marketplace/home")
    Call<MarketplaceData> fetchMarketplaceData();

    @GET("marketplace/categories")
    Call<ArrayList<Category>> fetchCategories();

    @POST("marketplace/item")
    Call<Item> uploadNewItem(@Body RequestBody params);

    @GET("marketplace/item/{item_id}")
    Call<Item> fetchItemDetails(@Path("item_id") int itemId);

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
