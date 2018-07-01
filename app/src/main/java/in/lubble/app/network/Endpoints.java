package in.lubble.app.network;

import java.util.List;

import in.lubble.app.network.pojos.TestData;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface Endpoints {

    @GET("/users/{user}/repos")
    Call<List<TestData>> reposForUser(
            @Path("user") String user
    );

}
