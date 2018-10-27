package in.lubble.app.utils;

import android.util.Log;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.database.DbSingleton;
import in.lubble.app.marketplace.ItemSearchData;
import in.lubble.app.models.FeatureData;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class MainUtils {

    private static final String TAG = "MainUtils";

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
                    LubbleSharedPrefs.getInstance().setIsViewCountEnabled(featureData.isViewCountEnabled());
                }
            }

            @Override
            public void onFailure(Call<FeatureData> call, Throwable t) {
                Log.e(TAG, "onFailure: ");
            }
        });
    }

    public static void fetchAndPersistMplaceItems() {
        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        endpoints.fetchItemList().enqueue(new Callback<ArrayList<ItemSearchData>>() {
            @Override
            public void onResponse(Call<ArrayList<ItemSearchData>> call, Response<ArrayList<ItemSearchData>> response) {
                final ArrayList<ItemSearchData> itemsList = response.body();
                if (itemsList != null && itemsList.size() > 0) {
                    insertItemsToDb(itemsList);
                }
            }

            @Override
            public void onFailure(Call<ArrayList<ItemSearchData>> call, Throwable t) {
                Log.e(TAG, "onFailure: ");
            }
        });
    }

    private static void insertItemsToDb(final ArrayList<ItemSearchData> itemsList) {
        DbSingleton.getInstance().deleteAllSearchData();
        for (ItemSearchData itemSearchData : itemsList) {
            DbSingleton.getInstance().createItemSearchData(itemSearchData);
        }
    }

}
