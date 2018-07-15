package in.lubble.app.utils;

import android.util.Log;

import java.util.List;

import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.models.FeatureData;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeatureUtils {

    private static final String TAG = "FeatureUtils";

    public static void fetchAndPersistAppFeatures() {
        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        endpoints.fetchAppFeatures().enqueue(new Callback<FeatureData>() {
            @Override
            public void onResponse(Call<FeatureData> call, Response<FeatureData> response) {
                final FeatureData featureData = response.body();
                if (featureData != null) {
                    final List<Integer> sellerList = featureData.getSellers();
                    if (sellerList != null && sellerList.size() > 0) {
                        LubbleSharedPrefs.getInstance().setSellerId(sellerList.get(0));
                    }
                }
            }

            @Override
            public void onFailure(Call<FeatureData> call, Throwable t) {
                Log.e(TAG, "onFailure: ");
            }
        });
    }

}
