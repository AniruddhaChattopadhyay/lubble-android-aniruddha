package in.lubble.app.network;

import in.lubble.app.models.marketplace.MarketplaceData;
import retrofit2.Call;
import retrofit2.http.GET;

public interface Endpoints {

    @GET("marketplace/home")
    Call<MarketplaceData> fetchMarketplaceData();

}
