package in.lubble.app.network;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthenticationInterceptor implements Interceptor {


    public AuthenticationInterceptor() {
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        String authToken = "";
        Request original = chain.request();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Task<GetTokenResult> task = user.getIdToken(false);
            try {
                GetTokenResult tokenResult = Tasks.await(task);
                authToken = tokenResult.getToken();
            } catch (ExecutionException e) {
                Crashlytics.logException(e);
                e.printStackTrace();
            } catch (InterruptedException e) {
                Crashlytics.logException(e);
                e.printStackTrace();
            }
        }
        Request.Builder builder = original.newBuilder()
                .header("uid", FirebaseAuth.getInstance().getUid())
                .header("Token", authToken);

        Request request = builder.build();
        return chain.proceed(request);
    }
}