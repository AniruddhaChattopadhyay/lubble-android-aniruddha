package in.lubble.app.services;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.models.marketplace.Item;
import in.lubble.app.models.marketplace.MarketplaceData;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServicesFrag extends Fragment {

    private static final String TAG = "ServicesFrag";

    private ServiceCategoryAdapter servicesAdapter;

    public ServicesFrag() {
        // Required empty public constructor
    }

    public static ServicesFrag newInstance() {
        ServicesFrag fragment = new ServicesFrag();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_services, container, false);

        RecyclerView serviceCategoryRv = view.findViewById(R.id.rv_services);
        serviceCategoryRv.setLayoutManager(new GridLayoutManager(getContext(), 2));
        servicesAdapter = new ServiceCategoryAdapter(GlideApp.with(getContext()));
        serviceCategoryRv.setAdapter(servicesAdapter);

        fetchMarketplaceData();

        return view;
    }

    private void fetchMarketplaceData() {
        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        endpoints.fetchMarketplaceData().enqueue(new Callback<MarketplaceData>() {
            @Override
            public void onResponse(Call<MarketplaceData> call, Response<MarketplaceData> response) {
                /// TODO: 4/9/18  progressBar.setVisibility(View.GONE);
                final MarketplaceData marketplaceData = response.body();
                if (response.isSuccessful() && marketplaceData != null && isAdded() && isVisible()) {

                    for (Item item : marketplaceData.getItems()) {
                        if (item.getType() == Item.ITEM_SERVICE) {
                            servicesAdapter.addData(item);
                        }
                    }

                } else if (isAdded() && isVisible()) {
                    Crashlytics.log("Marketplace bad response");
                    Toast.makeText(getContext(), R.string.all_try_again, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MarketplaceData> call, Throwable t) {
                Log.e(TAG, "onFailure: ");
                /// TODO: 4/9/18 progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), R.string.check_internet, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
