package in.lubble.app.network;

import com.google.gson.annotations.SerializedName;

import in.lubble.app.models.marketplace.MarketplaceData;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface Endpoints {

    @GET("marketplace/home")
    Call<MarketplaceData> fetchMarketplaceData();

    @POST("marketplace/item")
    Call<ResponseBean> uploadNewItem(@Body RequestBody params);


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
