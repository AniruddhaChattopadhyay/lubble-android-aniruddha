package in.lubble.app.network;

import android.text.TextUtils;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthenticationInterceptor implements Interceptor {

    public AuthenticationInterceptor() {
    }

    private static final String TAG = "AuthenticationIntercept";
    @Override
    public @NotNull Response intercept(Chain chain) throws IOException {
        String authToken = "";
        Request original = chain.request();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Task<GetTokenResult> task = user.getIdToken(false);
            FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
            try {
                GetTokenResult tokenResult = Tasks.await(task);
                authToken = tokenResult.getToken();
            } catch (ExecutionException e) {
                crashlytics.recordException(e);
                e.printStackTrace();
            } catch (InterruptedException e) {
                crashlytics.recordException(e);
                e.printStackTrace();
            }
        }
        if (!TextUtils.isEmpty(authToken)) {
            Request.Builder builder = original.newBuilder()
                    .header("uid", user.getUid())
                    .header("Token", authToken);
            Request request = builder.build();
            return chain.proceed(request);
        }
        return chain.proceed(original);
    }
}