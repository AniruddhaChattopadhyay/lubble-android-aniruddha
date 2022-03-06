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
        SliderData sliderData1 = new SliderData("📰 Local News", "https://i.imgur.com/ydEeVAD.jpg");
        SliderData sliderData2 = new SliderData("🥳 Local Events", "https://i.imgur.com/bPDz535.jpg");
        SliderData sliderData3 = new SliderData("🙏 Local Help", "https://i.imgur.com/06xcbAJ.jpg");
        SliderData sliderData4 = new SliderData("🔨 Find Local Services", "https://i.imgur.com/loMHM7F.jpg");
        SliderData sliderData5 = new SliderData("🌟 Neighbourhood Recommendations", "https://i.imgur.com/DuIfSfY.jpg");
        SliderData sliderData6 = new SliderData("🤑 Buy/Sell Nearby", "https://i.imgur.com/7VP5Rkv.jpeg");
        sliderDataList.add(sliderData1);
        sliderDataList.add(sliderData2);
        sliderDataList.add(sliderData3);
        sliderDataList.add(sliderData4);
        sliderDataList.add(sliderData5);
        sliderDataList.add(sliderData6);
        loginSignupBtn.setOnClickListener(v -> {
            Analytics.triggerEvent(AnalyticsEvents.WELCOME_SCREEN_LOGIN_BTN_CLICKED, getContext());
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.putExtra("Link", link);
            startActivity(intent);
            requireActivity().finish();
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