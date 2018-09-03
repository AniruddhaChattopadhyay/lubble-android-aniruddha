package in.lubble.app.services;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.lubble.app.GlideApp;
import in.lubble.app.R;


public class ServiceCategoryDetailFrag extends Fragment {

    private static final String TAG = "ServiceCategoryDetailFr";

    private static final String ARG_CATEGORY_ID = "ARG_CATEGORY_ID";
    private String categoryId;


    public ServiceCategoryDetailFrag() {
        // Required empty public constructor
    }

    public static ServiceCategoryDetailFrag newInstance(String categoryId) {
        ServiceCategoryDetailFrag fragment = new ServiceCategoryDetailFrag();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY_ID, categoryId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString(ARG_CATEGORY_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_service_category_detail, container, false);

        RecyclerView servicesRv = view.findViewById(R.id.rv_services);
        servicesRv.setLayoutManager(new LinearLayoutManager(getContext()));
        final ServicesAdapter servicesAdapter = new ServicesAdapter(GlideApp.with(getContext()));
        servicesRv.setAdapter(servicesAdapter);

        return view;
    }

}
