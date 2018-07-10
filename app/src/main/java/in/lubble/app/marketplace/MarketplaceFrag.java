package in.lubble.app.marketplace;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.lubble.app.GlideApp;
import in.lubble.app.R;

public class MarketplaceFrag extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public MarketplaceFrag() {
        // Required empty public constructor
    }

    public static MarketplaceFrag newInstance() {
        MarketplaceFrag fragment = new MarketplaceFrag();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_marketplace, container, false);

        CardView cat1cv = view.findViewById(R.id.layout_cat1);
        CardView cat2cv = view.findViewById(R.id.layout_cat2);

        RecyclerView allItemsRv = view.findViewById(R.id.rv_all_items);
        allItemsRv.setNestedScrollingEnabled(false);
        RecyclerView category1Rv = cat1cv.findViewById(R.id.rv_cat_items);
        category1Rv.setNestedScrollingEnabled(false);
        RecyclerView category2Rv = cat2cv.findViewById(R.id.rv_cat_items);
        category2Rv.setNestedScrollingEnabled(false);

        category1Rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        final SmallItemAdapter cat1Adapter = new SmallItemAdapter(GlideApp.with(getContext()));
        cat1Adapter.addData("1");
        cat1Adapter.addData("2");
        cat1Adapter.addData("3");
        cat1Adapter.addData("4");
        category1Rv.setAdapter(cat1Adapter);

        category2Rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        final SmallItemAdapter cat2Adapter = new SmallItemAdapter(GlideApp.with(getContext()));
        cat2Adapter.addData("1");
        cat2Adapter.addData("2");
        cat2Adapter.addData("3");
        cat2Adapter.addData("4");
        category2Rv.setAdapter(cat2Adapter);

        allItemsRv.setLayoutManager(new GridLayoutManager(getContext(), 2));
        final BigItemAdapter allItemsAdapter = new BigItemAdapter(GlideApp.with(getContext()));
        allItemsAdapter.addData("1");
        allItemsAdapter.addData("2");
        allItemsAdapter.addData("3");
        allItemsAdapter.addData("4");
        allItemsRv.setAdapter(allItemsAdapter);

        return view;
    }

}
