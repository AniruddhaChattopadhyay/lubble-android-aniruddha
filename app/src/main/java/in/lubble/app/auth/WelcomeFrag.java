package in.lubble.app.auth;

import static android.app.Activity.RESULT_OK;
import static in.lubble.app.firebase.RealtimeDbHelper.getThisUserRef;
import static in.lubble.app.utils.FragUtils.addFrag;
import static in.lubble.app.utils.FragUtils.replaceStack;
import static in.lubble.app.utils.UserUtils.isNewUser;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.MainActivity;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.marketplace.SliderData;
import in.lubble.app.marketplace.SliderViewPagerAdapter;
import in.lubble.app.models.ProfileData;
import in.lubble.app.models.ProfileInfo;
import in.lubble.app.utils.StringUtils;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import me.crosswall.lib.coverflow.CoverFlow;
import me.crosswall.lib.coverflow.core.PagerContainer;

public class WelcomeFrag extends Fragment {

    private PagerContainer pagerContainer;
    private ViewPager viewPager;
    private int currentPage = 0;
    private Handler handler = new Handler();
    private ArrayList<SliderData> sliderDataList = new ArrayList<>();
    static final int REQUEST_LOCATION = 636;
    private String link = null;
    private Button login_signup;
    private View v;

    public WelcomeFrag() {
        // Required empty public constructor
    }
    public static WelcomeFrag newInstance(String link) {
    Bundle args = new Bundle();
    args.putString("link", link);
    WelcomeFrag fragment = new WelcomeFrag();
    fragment.setArguments(args);
    fragment.setArguments(args);
    return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            link = getArguments().getString("link");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_welcome, container, false);
        pagerContainer = view.findViewById(R.id.pager_container);
        login_signup = view.findViewById(R.id.login_signup_btn);
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
        setupSlider();
        login_signup.setOnClickListener(v->{
            Intent intent = new Intent(getActivity(),LoginActivity.class);
            intent.putExtra("Link",link);
            startActivity(intent);
        });
        return view;
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