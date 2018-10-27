package in.lubble.app.network;

import com.google.firebase.auth.FirebaseAuth;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class AuthenticationInterceptor implements Interceptor {

    private String authToken;

    public AuthenticationInterceptor(String token) {
        this.authToken = token;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        Request.Builder builder = original.newBuilder()
                .header("uid", FirebaseAuth.getInstance().getUid())
                .header("Token", authToken);

        Request request = builder.build();
        return chain.proceed(request);
    }
}