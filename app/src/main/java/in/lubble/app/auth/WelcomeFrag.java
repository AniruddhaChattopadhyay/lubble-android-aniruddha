package in.lubble.app.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.marketplace.SliderData;
import in.lubble.app.marketplace.SliderViewPagerAdapter;
import me.crosswall.lib.coverflow.CoverFlow;
import me.crosswall.lib.coverflow.core.PagerContainer;

public class WelcomeFrag extends Fragment {

    private PagerContainer pagerContainer;
    private ViewPager viewPager;
    private int currentPage = 0;
    private Handler handler = new Handler();
    private ArrayList<SliderData> sliderDataList = new ArrayList<>();
    private String link = null;
    private MaterialButton loginSignupBtn;
    private Timer timer;
    private TabLayout tabLayout;

    public WelcomeFrag() {
        // Required empty public constructor
    }

    public static WelcomeFrag newInstance(String link) {
        Bundle args = new Bundle();
        args.putString("link", link);
        WelcomeFrag fragment = new WelcomeFrag();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            link = getArguments().getString("link");
        }
        Analytics.triggerScreenEvent(getContext(), this.getClass());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_welcome, container, false);
        pagerContainer = view.findViewById(R.id.pager_container);
        loginSignupBtn = view.findViewById(R.id.login_signup_btn);
        tabLayout = view.findViewById(R.id.tab_dots);
        viewPager = view.findViewById(R.id.viewpager);
        viewPager.setClipChildren(false);
        viewPager.setSaveEnabled(false);
        SliderData sliderData1 = new SliderData("https://cdn.pixabay.com/photo/2015/04/23/22/00/tree-736885__480.jpg");
        SliderData sliderData2 = new SliderData("https://images.unsplash.com/photo-1453728013993-6d66e9c9123a?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxzZWFyY2h8Mnx8dmlld3xlbnwwfHwwfHw%3D&w=1000&q=80");
        SliderData sliderData3 = new SliderData("https://thumbs.dreamstime.com/b/imagination-girl-kiss-lion-love-nature-abstract-concept-young-steals-male-wildlife-children-like-to-imagine-play-129595579.jpg");
        SliderData sliderData4 = new SliderData("https://images.ctfassets.net/hrltx12pl8hq/61DiwECVps74bWazF88Cy9/2cc9411d050b8ca50530cf97b3e51c96/Image_Cover.jpg?fit=fill&w=480&h=270");
        sliderDataList.add(sliderData1);
        sliderDataList.add(sliderData2);
        sliderDataList.add(sliderData3);
        sliderDataList.add(sliderData4);
        loginSignupBtn.setOnClickListener(v -> {
            Analytics.triggerEvent(AnalyticsEvents.WELCOME_SCREEN_LOGIN_BTN_CLICKED, getContext());
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.putExtra("Link", link);
            startActivity(intent);

        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        setupSlider();
    }

    private void setupSlider() {
        tabLayout.setupWithViewPager(viewPager);
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
                tabLayout.getTabAt(position).select();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(update);
            }
        }, 100, 2000);

    }

    @Override
    public void onStop() {
        super.onStop();
        timer.cancel();
    }

    private final Runnable update = new Runnable() {
        public void run() {
            if (currentPage == sliderDataList.size()) {
                currentPage = 0;
            }
            viewPager.setCurrentItem(currentPage++, true);
        }
    };
}