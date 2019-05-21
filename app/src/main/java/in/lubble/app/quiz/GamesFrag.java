package in.lubble.app.quiz;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.MainActivity;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.models.ProfileData;
import in.lubble.app.referrals.ReferralActivity;
import in.lubble.app.utils.RoundedCornersTransformation;

import static in.lubble.app.firebase.RealtimeDbHelper.getThisUserRef;
import static in.lubble.app.utils.UiUtils.dpToPx;

public class GamesFrag extends Fragment {

    private TextView currentCoinsTv;
    private TextView countdownTv;
    private TextView earnCoinsTv;
    private LinearLayout playContainer;
    private boolean isFreePlayEnabled = true;
    private boolean isClicked;
    private RelativeLayout whereTonightContainer;
    private ValueEventListener coinsListener;
    private long currentCoins = 0;

    public GamesFrag() {
        // Required empty public constructor
    }

    public static GamesFrag newInstance() {
        GamesFrag fragment = new GamesFrag();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_games, container, false);

        whereTonightContainer = view.findViewById(R.id.container_wheretonight);
        currentCoinsTv = view.findViewById(R.id.tv_total_coins);
        countdownTv = view.findViewById(R.id.tv_quiz_play_countdown);
        playContainer = view.findViewById(R.id.container_quiz_play);
        ImageView whereTonightPicIv = view.findViewById(R.id.iv_wheretonight_pic);
        earnCoinsTv = view.findViewById(R.id.tv_earn_more);
        final LinearLayout currentCoinsContainer = view.findViewById(R.id.container_current_coins);

        Analytics.triggerScreenEvent(getContext(), this.getClass());

        currentCoinsContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReferralActivity.open(requireContext(), true);
            }
        });

        earnCoinsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Analytics.triggerEvent(AnalyticsEvents.QUIZ_EARN_COINS, requireContext());
                ReferralActivity.open(requireContext(), true);
            }
        });

        GlideApp.with(requireContext())
                .load(R.drawable.ic_having_fun_iais)
                .transform(new RoundedCornersTransformation(dpToPx(8), 0))
                .into(whereTonightPicIv);


        if (!LubbleSharedPrefs.getInstance().getIsQuizOpened()) {
            Animation shake = AnimationUtils.loadAnimation(requireContext(), R.anim.shake);
            shake.setStartOffset(2000);
            playContainer.startAnimation(shake);
            LubbleSharedPrefs.getInstance().setIsQuizOpened(true);
            if (getActivity() != null) {
                ((MainActivity) getActivity()).removeQuizBadge();
            }
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        isClicked = false;
        whereTonightContainer.setAlpha(1f);
        enableFreePlay();
        syncCurrentCoins();
    }

    private void syncCurrentCoins() {
        if (coinsListener != null) {
            getThisUserRef().removeEventListener(coinsListener);
        }
        coinsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final ProfileData profileData = dataSnapshot.getValue(ProfileData.class);
                currentCoins = profileData.getCoins();
                currentCoinsTv.setText(String.valueOf(currentCoins));
                if (!isFreePlayEnabled) {
                    playContainer.setAlpha(1f);
                    earnCoinsTv.setVisibility(View.GONE);
                    whereTonightContainer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startQuiz();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        getThisUserRef().addValueEventListener(coinsListener);
    }

    private void enableFreePlay() {
        countdownTv.setVisibility(View.GONE);
        isFreePlayEnabled = true;
        whereTonightContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startQuiz();
            }
        });
    }

    private void startQuiz() {
        if (isClicked) {
            return;
        }
        final Bundle bundle = new Bundle();
        bundle.putString("type", "free");
        Analytics.triggerEvent(AnalyticsEvents.QUIZ_PLAY, bundle, requireContext());
        QuizOptionsActiv.open(requireContext());
    }

    @Override
    public void onPause() {
        super.onPause();
        if (coinsListener != null) {
            getThisUserRef().removeEventListener(coinsListener);
        }
    }
}
