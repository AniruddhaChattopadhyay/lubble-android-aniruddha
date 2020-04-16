package in.lubble.app.marketplace;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.viewpager.widget.ViewPager;

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
import in.lubble.app.models.marketplace.SellerData;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import in.lubble.app.utils.StringUtils;
import me.crosswall.lib.coverflow.CoverFlow;
import me.crosswall.lib.coverflow.core.PagerContainer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static androidx.recyclerview.widget.StaggeredGridLayoutManager.HORIZONTAL;

public class MarketplaceFrag extends Fragment {

    private static final String TAG = "MarketplaceFrag";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private LinearLayout searchContainer;
    private TextView viewAllTv;
    private ProgressBar viewAllProgressBar;
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
    private BigItemAdapter allItemsAdapter;
    private RelativeLayout cat1cv, cat2cv;
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

        searchContainer = view.findViewById(R.id.container_search);
        viewAllTv = view.findViewById(R.id.tv_view_all_items);
        viewAllProgressBar = view.findViewById(R.id.progressbar_view_all);
        categoriesRv = view.findViewById(R.id.rv_categories);
        categoriesRv.setNestedScrollingEnabled(false);

        progressBar = view.findViewById(R.id.progress_bar);
        pagerContainer = view.findViewById(R.id.pager_container);
        viewPager = view.findViewById(R.id.viewpager);
        viewPager.setClipChildren(false);
        viewPager.setOffscreenPageLimit(4);

        cat1cv = view.findViewById(R.id.layout_cat1);
        cat2cv = view.findViewById(R.id.layout_cat2);

        cat1Name = cat1cv.findViewById(R.id.tv_category);
        cat2Name = cat2cv.findViewById(R.id.tv_category);
        allItemsRv = view.findViewById(R.id.rv_all_items);
        newItemContainer = view.findViewById(R.id.new_item_container);
        allItemsRv.setNestedScrollingEnabled(false);

        category1Rv = cat1cv.findViewById(R.id.rv_cat_items);
        category1Rv.setNestedScrollingEnabled(false);
        category2Rv = cat2cv.findViewById(R.id.rv_cat_items);
        category2Rv.setNestedScrollingEnabled(false);

        categoriesRv.setLayoutManager(new StaggeredGridLayoutManager(2, HORIZONTAL));
        category1Rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        category2Rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        allItemsRv.setLayoutManager(new GridLayoutManager(getContext(), 2));

        final ColoredChipsAdapter catAdapter = new ColoredChipsAdapter(GlideApp.with(getContext()));
        categoriesRv.setAdapter(catAdapter);

        final SmallSellerAdapter cat1Adapter = new SmallSellerAdapter(GlideApp.with(getContext()));
        category1Rv.setAdapter(cat1Adapter);

        final SmallSellerAdapter cat2Adapter = new SmallSellerAdapter(GlideApp.with(getContext()));
        category2Rv.setAdapter(cat2Adapter);

        allItemsAdapter = new BigItemAdapter(GlideApp.with(getContext()), false);
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
                    getContext().startActivity(SellerDashActiv.getIntent(getContext(), sellerId, false, Item.ITEM_PRODUCT));
                }
            }
        });

        searchContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchActivity.open(getContext());
            }
        });

        if (!LubbleSharedPrefs.getInstance().getIsMplaceOpened()) {
            LubbleSharedPrefs.getInstance().setIsMplaceOpened(true);
            if (getActivity() != null) {
                ((MainActivity) getActivity()).removeMplaceBadge();
            }
        }

        viewAllTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchAllItems();
            }
        });

        Analytics.triggerScreenEvent(getContext(), this.getClass());
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null && getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).toggleSearchInToolbar(false);
        }
    }

    private void fetchAllItems() {
        viewAllProgressBar.setVisibility(View.VISIBLE);
        viewAllTv.setText("");
        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        endpoints.fetchAllItems().enqueue(new Callback<ArrayList<Item>>() {
            @Override
            public void onResponse(Call<ArrayList<Item>> call, Response<ArrayList<Item>> response) {
                viewAllProgressBar.setVisibility(View.GONE);
                final ArrayList<Item> itemArrayList = response.body();
                if (itemArrayList != null && isAdded() && isVisible() && !itemArrayList.isEmpty()) {
                    viewAllTv.setVisibility(View.GONE);
                    allItemsAdapter.clear();
                    for (Item item : itemArrayList) {
                        allItemsAdapter.addData(item);
                    }
                } else if (isAdded() && isVisible()) {
                    viewAllTv.setText("View All");
                    Crashlytics.logException(new IllegalArgumentException("itemArrayList is NULL"));
                    Toast.makeText(getContext(), R.string.all_try_again, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Item>> call, Throwable t) {
                if (isAdded() && isVisible()) {
                    Log.e(TAG, "onFailure: ");
                    viewAllProgressBar.setVisibility(View.GONE);
                    viewAllTv.setText("View All");
                    Toast.makeText(getContext(), R.string.check_internet, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchMarketplaceData(final SmallSellerAdapter cat1Adapter, final SmallSellerAdapter cat2Adapter, final BigItemAdapter allItemsAdapter, final ColoredChipsAdapter catAdapter) {
        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        endpoints.fetchMarketplaceData().enqueue(new Callback<MarketplaceData>() {
            @Override
            public void onResponse(Call<MarketplaceData> call, Response<MarketplaceData> response) {
                progressBar.setVisibility(View.GONE);
                final MarketplaceData marketplaceData = response.body();
                if (response.isSuccessful() && marketplaceData != null && isAdded() && isVisible()) {
                    if (marketplaceData.getShowcaseCategories().size() > 0) {
                        final Category category1 = marketplaceData.getShowcaseCategories().get(0);
                        cat1Name.setText(StringUtils.getTitleCase(category1.getHumanReadableName()));
                        if (category1.getSellers().size() > 0) {
                            category1Rv.setVisibility(View.VISIBLE);
                            for (SellerData sellerData : category1.getSellers()) {
                                cat1Adapter.addSeller(sellerData);
                            }
                        } else {
                            cat1cv.setVisibility(View.GONE);
                        }

                        if (marketplaceData.getShowcaseCategories().size() >= 2) {
                            final Category category2 = marketplaceData.getShowcaseCategories().get(1);
                            cat2Name.setText(StringUtils.getTitleCase(category2.getHumanReadableName()));
                            if (category2.getSellers().size() > 0) {
                                category2Rv.setVisibility(View.VISIBLE);
                                for (SellerData sellerData : category2.getSellers()) {
                                    cat2Adapter.addSeller(sellerData);
                                }
                            } else {
                                cat2cv.setVisibility(View.GONE);
                            }
                        } else {
                            cat2cv.setVisibility(View.GONE);
                        }
                    } else {
                        cat1cv.setVisibility(View.GONE);
                        cat2cv.setVisibility(View.GONE);
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
                    Crashlytics.log("Marketplace bad response");
                    Toast.makeText(getContext(), R.string.all_try_again, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MarketplaceData> call, Throwable t) {
                if (isAdded() && isVisible()) {
                    Log.e(TAG, "onFailure: ");
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), R.string.check_internet, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupSlider() {
        viewPager.setAdapter(new SliderViewPagerAdapter(getChildFragmentManager(), sliderDataList, false));

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
        }, 100, 5000);

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
