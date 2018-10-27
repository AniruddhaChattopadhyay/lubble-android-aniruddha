package in.lubble.app.services;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.crashlytics.android.Crashlytics;
import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.MainActivity;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.marketplace.SearchActivity;
import in.lubble.app.marketplace.SellerDashActiv;
import in.lubble.app.marketplace.SellerEditActiv;
import in.lubble.app.models.marketplace.Category;
import in.lubble.app.models.marketplace.Item;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;

public class ServicesFrag extends Fragment {

    private static final String TAG = "ServicesFrag";

    private LinearLayout searchContainer;
    private ProgressBar progressBar;
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

        searchContainer = view.findViewById(R.id.container_search);
        progressBar = view.findViewById(R.id.progress_bar_categories);
        RecyclerView serviceCategoryRv = view.findViewById(R.id.rv_services);
        LinearLayout newServiceContainer = view.findViewById(R.id.new_service_container);
        serviceCategoryRv.setNestedScrollingEnabled(false);

        serviceCategoryRv.setLayoutManager(new GridLayoutManager(getContext(), 2));
        servicesAdapter = new ServiceCategoryAdapter(GlideApp.with(getContext()));
        serviceCategoryRv.setAdapter(servicesAdapter);

        fetchServiceCategories();

        newServiceContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int sellerId = LubbleSharedPrefs.getInstance().getSellerId();
                if (sellerId == -1) {
                    // no seller ID found, start activ to create a new seller
                    SellerEditActiv.open(getContext());
                } else {
                    // seller ID found, open dashboard
                    getContext().startActivity(SellerDashActiv.getIntent(getContext(), sellerId, false, Item.ITEM_SERVICE));
                }
            }
        });

        if (!LubbleSharedPrefs.getInstance().getIsServicesOpened()) {
            LubbleSharedPrefs.getInstance().setIsServicesOpened(true);
            if (getActivity() != null) {
                ((MainActivity) getActivity()).removeServicesBadge();
            }
        }

        searchContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchActivity.open(getContext());
            }
        });

        Analytics.triggerEvent(AnalyticsEvents.SERVICES_FRAG, getContext());

        return view;
    }

    private void fetchServiceCategories() {
        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        endpoints.fetchServiceCategories().enqueue(new Callback<ArrayList<Category>>() {
            @Override
            public void onResponse(Call<ArrayList<Category>> call, Response<ArrayList<Category>> response) {
                progressBar.setVisibility(View.GONE);
                final ArrayList<Category> categoriesList = response.body();
                if (response.isSuccessful() && categoriesList != null && isAdded() && isVisible()) {

                    for (Category category : categoriesList) {
                        if (category.getType() == Item.ITEM_SERVICE) {
                            servicesAdapter.addData(category);
                        }
                    }

                } else if (isAdded() && isVisible()) {
                    Crashlytics.log("categories bad response");
                    Toast.makeText(getContext(), R.string.all_try_again, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Category>> call, Throwable t) {
                if (isAdded() && isVisible()) {
                    Log.e(TAG, "onFailure: ");
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), R.string.check_internet, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
