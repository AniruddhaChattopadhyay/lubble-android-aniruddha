package in.lubble.app.analytics;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import in.lubble.app.rewards.data.RewardsAirtableData;
import okhttp3.RequestBody;
import retrofit2.Response;

import static in.lubble.app.Constants.MEDIA_TYPE;

public class AppsListWorker extends Worker {

    private Context context;
    private static final String TAG = "AppsListWorker";

    public AppsListWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
    }

    @Override
    public Result doWork() {
        // Do the work here
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> pkgAppsList = packageManager.queryIntentActivities(mainIntent, 0);

        final ArrayList<String> list = new ArrayList<>();
        for (ResolveInfo resolveInfo : pkgAppsList) {
            list.add(resolveInfo.loadLabel(packageManager).toString());
        }

        HashMap<String, Object> params = new HashMap<>();
        params.put("user_id", FirebaseAuth.getInstance().getUid());
        params.put("app_list", list.toString().replace("[", "").replace("]", ""));
        RequestBody body = RequestBody.create(MEDIA_TYPE, new JSONObject(params).toString());

        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        try {
            final Response<RewardsAirtableData> response = endpoints.uploadInstalledApps(body).execute();
            if (response.isSuccessful()) {
                // Indicate the task finished successfully with the Result
                return Result.success();
            } else {
                return Result.retry();
            }
        } catch (IOException e) {
            Crashlytics.logException(e);
            return Result.retry();
        }
    }
}