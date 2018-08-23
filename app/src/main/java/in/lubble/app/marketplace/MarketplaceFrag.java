package in.lubble.app.marketplace;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.MainActivity;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.models.marketplace.Category;
import in.lubble.app.models.marketplace.Item;
import in.lubble.app.models.marketplace.MarketplaceData;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import me.crosswall.lib.coverflow.CoverFlow;
import me.crosswall.lib.coverflow.core.PagerContainer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MarketplaceFrag extends Fragment {

    private static final String TAG = "MarketplaceFrag";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private TextView searchTv;
    private RecyclerView categoriesRv;
    private ProgressBar progressBar;
    private TextView cat1Name;
    private TextView cat2Name;
    private RecyclerView allItemsRv;
    private RecyclerView category1Rv;
    private RecyclerView category2Rv;
    private LinearLayout newItemContainer;
    private PagerContainer pagerContainer;
    private ViewPager viewPager;
    private int currentPage = 0;
    private Handler handler = new Handler();
    private ArrayList<SliderData> sliderDataList = new ArrayList<>();

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_marketplace, container, false);

        searchTv = view.findViewById(R.id.tv_search);
        categoriesRv = view.findViewById(R.id.rv_categories);
        categoriesRv.setNestedScrollingEnabled(false);

        progressBar = view.findViewById(R.id.progress_bar);
        pagerContainer = view.findViewById(R.id.pager_container);
        viewPager = view.findViewById(R.id.viewpager);
        viewPager.setClipChildren(false);
        viewPager.setOffscreenPageLimit(4);

        RelativeLayout cat1cv = view.findViewById(R.id.layout_cat1);
        RelativeLayout cat2cv = view.findViewById(R.id.layout_cat2);

        cat1Name = cat1cv.findViewById(R.id.tv_category);
        cat2Name = cat2cv.findViewById(R.id.tv_category);
        allItemsRv = view.findViewById(R.id.rv_all_items);
        newItemContainer = view.findViewById(R.id.new_item_container);
        allItemsRv.setNestedScrollingEnabled(false);

        category1Rv = cat1cv.findViewById(R.id.rv_cat_items);
        category1Rv.setNestedScrollingEnabled(false);
        category2Rv = cat2cv.findViewById(R.id.rv_cat_items);
        category2Rv.setNestedScrollingEnabled(false);

        categoriesRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        category1Rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        category2Rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        allItemsRv.setLayoutManager(new GridLayoutManager(getContext(), 2));

        final ColoredChipsAdapter catAdapter = new ColoredChipsAdapter(GlideApp.with(getContext()));
        categoriesRv.setAdapter(catAdapter);

        final SmallItemAdapter cat1Adapter = new SmallItemAdapter(GlideApp.with(getContext()));
        category1Rv.setAdapter(cat1Adapter);

        final SmallItemAdapter cat2Adapter = new SmallItemAdapter(GlideApp.with(getContext()));
        category2Rv.setAdapter(cat2Adapter);

        final BigItemAdapter allItemsAdapter = new BigItemAdapter(GlideApp.with(getContext()), false);
        allItemsRv.setAdapter(allItemsAdapter);

        fetchMarketplaceData(cat1Adapter, cat2Adapter, allItemsAdapter, catAdapter);

        newItemContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int sellerId = LubbleSharedPrefs.getInstance().getSellerId();
                if (sellerId == -1) {
                    // no seller ID found, start activ to create a new seller
                    SellerEditActiv.open(getContext());
                } else {
                    // seller ID found, open dashboard
                    getContext().startActivity(SellerDashActiv.getIntent(getContext(), sellerId, false));
                }
            }
        });

        searchTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchActivity.open(getContext());
            }
        });

        if (!LubbleSharedPrefs.getInstance().getIsMplaceOpened()) {
            LubbleSharedPrefs.getInstance().setIsMplaceOpened(true);
            if (getActivity() != null) {
                ((MainActivity) getActivity()).removeBadge();
            }
        }

        Analytics.triggerScreenEvent(getContext(), this.getClass());
        return view;
    }

    private void fetchMarketplaceData(final SmallItemAdapter cat1Adapter, final SmallItemAdapter cat2Adapter, final BigItemAdapter allItemsAdapter, final ColoredChipsAdapter catAdapter) {
        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        endpoints.fetchMarketplaceData().enqueue(new Callback<MarketplaceData>() {
            @Override
            public void onResponse(Call<MarketplaceData> call, Response<MarketplaceData> response) {
                progressBar.setVisibility(View.GONE);
                final MarketplaceData marketplaceData = response.body();
                if (marketplaceData != null && isAdded() && isVisible()) {
                    final Category category1 = marketplaceData.getShowcaseCategories().get(0);
                    cat1Name.setText(category1.getName());
                    for (Item item : category1.getItems()) {
                        cat1Adapter.addData(item);
                    }

                    final Category category2 = marketplaceData.getShowcaseCategories().get(1);
                    cat2Name.setText(category2.getName());
                    for (Item item : category2.getItems()) {
                        cat2Adapter.addData(item);
                    }

                    for (Item item : marketplaceData.getItems()) {
                        allItemsAdapter.addData(item);
                    }

                    for (int i = 0; i < marketplaceData.getCategories().size(); i++) {
                        catAdapter.addData(marketplaceData.getCategories().get(i));
                    }

                    if (marketplaceData.getSliderDataList().size() > 0) {
                        pagerContainer.setVisibility(View.VISIBLE);
                        sliderDataList = marketplaceData.getSliderDataList();
                        setupSlider();
                    } else {
                        pagerContainer.setVisibility(View.GONE);
                    }
                } else if (isAdded() && isVisible()) {
                    Crashlytics.logException(new IllegalArgumentException("marketplaceData is NULL"));
                    Toast.makeText(getContext(), R.string.all_try_again, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MarketplaceData> call, Throwable t) {
                Log.e(TAG, "onFailure: ");
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), R.string.check_internet, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSlider() {
        viewPager.setAdapter(new SliderViewPagerAdapter(getChildFragmentManager(), sliderDataList));

        new CoverFlow.Builder()
                .with(viewPager)
                .pagerMargin(getResources().getDimensionPixelSize(R.dimen.pager_margin))
                .scale(0.3f)
                .spaceSize(0f)
                .rotationY(0f)
                .build();

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentPage = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(update);
            }
        }, 100, 2000);

    }

    private Runnable update = new Runnable() {
        public void run() {
            if (currentPage == sliderDataList.size()) {
                currentPage = 0;
            }
            viewPager.setCurrentItem(currentPage++, true);
        }
    };

}
