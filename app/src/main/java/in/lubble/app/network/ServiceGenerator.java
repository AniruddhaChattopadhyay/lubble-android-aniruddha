package in.lubble.app.network;

import in.lubble.app.BuildConfig;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceGenerator {

    private static final String BASE_URL = "dev".equalsIgnoreCase(BuildConfig.FLAVOR) ? "https://devapi.lubble.in/" : "https://api.lubble.in/";
//    private static final String BASE_URL = "http://172.30.80.1:8000/";
    private static final String AIRTABLE_API_URL = "dev".equalsIgnoreCase(BuildConfig.FLAVOR) ? "https://api.airtable.com/v0/" : "https://api.lubble.in/";

    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create());

    private static Retrofit.Builder airtableBuilder =
            new Retrofit.Builder()
                    .baseUrl(AIRTABLE_API_URL)
                    .addConverterFactory(GsonConverterFactory.create());

    private static Retrofit retrofit = builder.build();
    private static Retrofit airtableRetrofit = airtableBuilder.build();

    private static HttpLoggingInterceptor logging =
            new HttpLoggingInterceptor()
                    .setLevel(HttpLoggingInterceptor.Level.BODY);

    private static OkHttpClient.Builder httpClient =
            new OkHttpClient.Builder();
    private static OkHttpClient.Builder airtableHttpClient =
            new OkHttpClient.Builder();
    private static AuthenticationInterceptor authenticationInterceptor =
            new AuthenticationInterceptor();

    public static <S> S createService(Class<S> serviceClass) {

        for (Interceptor mInterceptor : httpClient.interceptors()) {
            if (mInterceptor == null) {
                httpClient.interceptors().clear();
                break;
            }
        }

        httpClient.authenticator(TokenAuthenticator.getInstance());

        if (!httpClient.interceptors().contains(authenticationInterceptor)) {
            httpClient.addInterceptor(authenticationInterceptor);

            builder.client(httpClient.build());
            retrofit = builder.build();
        }

        if (!httpClient.interceptors().contains(logging)) {
            httpClient.addInterceptor(logging);
            builder.client(httpClient.build());
            retrofit = builder.build();
        }
        return retrofit.create(serviceClass);
    }

    public static <S> S createAirtableService(Class<S> serviceClass) {

        AirtableAuthInterceptor interceptor =
                new AirtableAuthInterceptor();

        if (!airtableHttpClient.interceptors().contains(interceptor)) {
            airtableHttpClient.addInterceptor(interceptor);

            airtableBuilder.client(airtableHttpClient.build());
            airtableRetrofit = airtableBuilder.build();
        }

        if (!airtableHttpClient.interceptors().contains(logging)) {
            airtableHttpClient.addInterceptor(logging);
            airtableBuilder.client(airtableHttpClient.build());
            airtableRetrofit = airtableBuilder.build();
        }
        return airtableRetrofit.create(serviceClass);
    }

}
