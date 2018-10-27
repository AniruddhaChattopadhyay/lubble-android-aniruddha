package in.lubble.app.services;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.models.marketplace.Category;
import in.lubble.app.models.marketplace.Item;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ServiceCategoryDetailFrag extends Fragment {

    private static final String TAG = "ServiceCategoryDetailFr";

    private static final String ARG_CATEGORY_ID = "ARG_CATEGORY_ID";
    private int categoryId;
    private ProgressBar progressBar;
    private RecyclerView servicesRv;
    private ServicesAdapter servicesAdapter;

    public ServiceCategoryDetailFrag() {
        // Required empty public constructor
    }

    public static ServiceCategoryDetailFrag newInstance(int categoryId) {
        ServiceCategoryDetailFrag fragment = new ServiceCategoryDetailFrag();
        Bundle args = new Bundle();
        args.putInt(ARG_CATEGORY_ID, categoryId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getInt(ARG_CATEGORY_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_service_category_detail, container, false);

        progressBar = view.findViewById(R.id.progress_bar_service_detail);
        servicesRv = view.findViewById(R.id.rv_services);
        servicesRv.setLayoutManager(new LinearLayoutManager(getContext()));
        servicesAdapter = new ServicesAdapter(GlideApp.with(getContext()));
        servicesRv.setAdapter(servicesAdapter);
        DividerItemDecoration itemDecor = new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL);
        servicesRv.addItemDecoration(itemDecor);

        fetchServices();

        return view;
    }

    private void fetchServices() {
        progressBar.setVisibility(View.VISIBLE);
        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        endpoints.fetchCategoryItems(categoryId).enqueue(new Callback<Category>() {
            @Override
            public void onResponse(Call<Category> call, Response<Category> response) {
                progressBar.setVisibility(View.GONE);
                final Category categoryData = response.body();
                if (response.isSuccessful() && categoryData != null && isAdded() && isVisible()) {

                    getActivity().setTitle(categoryData.getName());

                    if (categoryData.getItems() != null && !categoryData.getItems().isEmpty()) {
                        servicesAdapter.clear();
                        for (Item item : categoryData.getItems()) {
                            if (item.getType() == Item.ITEM_SERVICE) {
                                servicesAdapter.addData(item);
                            }
                        }
                    } else {
                        Crashlytics.logException(new IllegalArgumentException("Category has not services hawww"));
                        Toast.makeText(getContext(), "No Services for this category", Toast.LENGTH_SHORT).show();
                        getActivity().finish();
                    }

                } else if (isAdded() && isVisible()) {
                    Crashlytics.log("categories services list bad response");
                    Toast.makeText(getContext(), R.string.all_try_again, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Category> call, Throwable t) {
                if (isAdded() && isVisible()) {
                    Log.e(TAG, "onFailure: ");
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), R.string.check_internet, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
