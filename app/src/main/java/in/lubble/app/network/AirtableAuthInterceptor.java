package in.lubble.app.network;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import static in.lubble.app.Constants.AIRTABLE_CHAABI;

public class AirtableAuthInterceptor implements Interceptor {

    public AirtableAuthInterceptor() {
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        Request.Builder builder = original.newBuilder()
                .header("Authorization", "Bearer " + AIRTABLE_CHAABI);

        Request request = builder.build();
        return chain.proceed(request);
    }
}