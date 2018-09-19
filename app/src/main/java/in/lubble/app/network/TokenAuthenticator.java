package in.lubble.app.network;

import android.text.TextUtils;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class TokenAuthenticator implements Authenticator {

    private static final String TAG = "TokenAuthenticator";
    private static TokenAuthenticator instance;

    private TokenAuthenticator() {
    }

    public static TokenAuthenticator getInstance() {
        if (instance == null) {
            instance = new TokenAuthenticator();
        }
        return instance;
    }

    @Override
    public synchronized Request authenticate(Route route, Response response) {
        if (responseCount(response) >= 3) {
            Crashlytics.logException(new IllegalAccessException("3rd time failed to validate token for user: " + FirebaseAuth.getInstance().getUid()));
            return null; // If we've failed 3 times, give up.
        }
        String requestToken = response.request().header("Token");
        String savedToken = "";
        final Task<GetTokenResult> accessTokenTask = FirebaseAuth.getInstance().getAccessToken(false);
        if (accessTokenTask.isComplete() && accessTokenTask.getResult().getToken() != null) {
            savedToken = accessTokenTask.getResult().getToken();
        }
        String newToken = "";
        if (savedToken.equalsIgnoreCase(requestToken)) {
            // Only refresh token if API request was made with the same token that we have in SharedPrefs
            // If the sharedPrefs has a different token, then it must be the new one, so just re-make the request with saved token (logic after this if block)
            Log.d(TAG, "Authenticating for response: " + response);
            Log.d(TAG, "Challenges: " + response.challenges());

            String uid = FirebaseAuth.getInstance().getUid();
            if (uid == null) {
                Crashlytics.log("Invalid UID during validate token call! Dropping original request and this one too" +
                        "\n Ph: " + FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
                return null;
            }
            try {
                // Block on the task for a maximum of 1000 milliseconds, otherwise time out.
                final GetTokenResult getTokenResult = Tasks.await(FirebaseAuth.getInstance().getAccessToken(true), 3000, TimeUnit.MILLISECONDS);
                newToken = getTokenResult.getToken();

                if (TextUtils.isEmpty(newToken)) {
                    // Unable to renew token
                    // Drop the API request. Can do nothing.
                    Crashlytics.logException(new IllegalArgumentException("New TOKEN is NULL"));
                    return null;
                }
            } catch (ExecutionException e) {
                // Drop the API request. Can do nothing.
                Crashlytics.logException(e);
                return null;
            } catch (InterruptedException e) {
                // Drop the API request. Can do nothing.
                Crashlytics.logException(e);
                return null;
            } catch (TimeoutException e) {
                // Task timed out before it could complete.
                // Drop the API request. Can do nothing.
                Crashlytics.logException(e);
            }
        }
        // retry the 401 failed call with new token
        return response.request()
                .newBuilder()
                .header("Token", savedToken)
                .build();
    }

    private int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }

}