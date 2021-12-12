package in.lubble.app.quiz;

import static in.lubble.app.Constants.IS_GAMES_ENABLED;
import static in.lubble.app.firebase.RealtimeDbHelper.getThisUserRef;
import static in.lubble.app.utils.UiUtils.dpToPx;

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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.MainActivity;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.feed_bottom_sheet.FeedUserShareBottomSheetFrag;
import in.lubble.app.map.LubbleMapActivity;
import in.lubble.app.referrals.ReferralActivity;
import in.lubble.app.utils.RoundedCornersTransformation;

public class GamesFrag extends Fragment {

    private TextView currentCoinsTv;
    private TextView countdownTv;
    private TextView earnCoinsTv;
    private LinearLayout playContainer;
    private boolean isFreePlayEnabled = true;
    private boolean isClicked;
    private RelativeLayout whereTonightContainer;
    private RelativeLayout mapContainer;
    private ValueEventListener coinsListener;
    private View lockedBg;
    private MaterialCardView lockedCardView;
    private TextView lockedTitleTv, lockedSubtitleTv;
    private MaterialButton inviteBtn;
    private Long currentCoins = 0L;

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
        mapContainer = view.findViewById(R.id.container_map);
        lockedBg = view.findViewById(R.id.view_lock_bg);
        lockedCardView = view.findViewById(R.id.mcv_locked_card);
        lockedTitleTv = view.findViewById(R.id.tv_locked_title);
        lockedSubtitleTv = view.findViewById(R.id.tv_locked_subtitle);
        inviteBtn = view.findViewById(R.id.btn_invite);
        final LinearLayout currentCoinsContainer = view.findViewById(R.id.container_current_coins);

        Analytics.triggerScreenEvent(getContext(), this.getClass());

        if (FirebaseRemoteConfig.getInstance().getBoolean(IS_GAMES_ENABLED)) {
            // games tab is enabled
            lockedBg.setVisibility(View.GONE);
            lockedCardView.setVisibility(View.GONE);
        } else {
            // lock games tab
            lockedBg.setVisibility(View.VISIBLE);
            lockedCardView.setVisibility(View.VISIBLE);
            String lubbleName = LubbleSharedPrefs.getInstance().getLubbleName();
            lockedTitleTv.setText(String.format(getString(R.string.new_nhood_locked_title), lubbleName));
            lockedSubtitleTv.setText(String.format(getString(R.string.new_nhood_locked_subtitle), lubbleName));
            lockedBg.setOnClickListener(v -> {
                // just consume all clicks
            });
            inviteBtn.setOnClickListener(btnView -> {
                ReferralActivity.open(requireContext(), true);
                Analytics.triggerEvent(AnalyticsEvents.GAMES_LOCKED_INVITE_CLICK, requireContext());
            });
        }

        currentCoinsContainer.setOnClickListener(v -> ReferralActivity.open(requireContext(), true));

        earnCoinsTv.setOnClickListener(v -> {
            Analytics.triggerEvent(AnalyticsEvents.QUIZ_EARN_COINS, getContext());
            ReferralActivity.open(requireContext(), true);
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

        mapContainer.setOnClickListener(v -> LubbleMapActivity.open(requireContext()));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        isClicked = false;
        whereTonightContainer.setAlpha(1f);
        enableFreePlay();
        syncCurrentCoins();
        if (getActivity() != null && getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).toggleSearchInToolbar(false);
        }
    }

    private void syncCurrentCoins() {
        if (coinsListener != null) {
            getThisUserRef().removeEventListener(coinsListener);
        }
        coinsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentCoins = dataSnapshot.getValue(Long.class);
                if (currentCoins != null) {
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        getThisUserRef().child("coins").addValueEventListener(coinsListener);
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
