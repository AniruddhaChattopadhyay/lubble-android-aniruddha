package in.lubble.app.quiz;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.format.DateUtils;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.models.ProfileData;
import in.lubble.app.profile.ProfileActivity;
import in.lubble.app.utils.RoundedCornersTransformation;

import java.util.concurrent.TimeUnit;

import static in.lubble.app.firebase.RealtimeDbHelper.getQuizRefForThisUser;
import static in.lubble.app.firebase.RealtimeDbHelper.getThisUserRef;
import static in.lubble.app.utils.UiUtils.dpToPx;

public class GamesFrag extends Fragment {

    private static final int RETRY_COST = 40;
    private TextView currentCoinsTv;
    private TextView countdownTv;
    private ImageView quizPlayCoinIv;
    private TextView quizPlayCostTv;
    private TextView earnCoinsTv;
    private LinearLayout playContainer;
    private boolean isFreePlayEnabled;
    private RelativeLayout whereTonightContainer;
    private ValueEventListener timeListener;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
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
        quizPlayCoinIv = view.findViewById(R.id.iv_quiz_play_coin);
        quizPlayCostTv = view.findViewById(R.id.tv_quiz_play_cost);
        ImageView whereTonightPicIv = view.findViewById(R.id.iv_wheretonight_pic);
        earnCoinsTv = view.findViewById(R.id.tv_earn_more);
        final LinearLayout currentCoinsContainer = view.findViewById(R.id.container_current_coins);

        quizPlayCostTv.setText(String.valueOf(RETRY_COST));

        currentCoinsContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileActivity.open(requireContext(), FirebaseAuth.getInstance().getUid());
            }
        });

        earnCoinsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileActivity.open(requireContext(), FirebaseAuth.getInstance().getUid());
            }
        });

        GlideApp.with(requireContext())
                .load(R.drawable.ic_having_fun_iais)
                .transform(new RoundedCornersTransformation(dpToPx(8), 0))
                .into(whereTonightPicIv);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        whereTonightContainer.setAlpha(1f);
        syncTime();
    }

    private void syncTime() {
        if (timeListener != null) {
            getQuizRefForThisUser("whereTonight").child("lastPlayedTime").removeEventListener(timeListener);
        }
        timeListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    Long lastPlayedTime = dataSnapshot.getValue(Long.class);
                    final long millisInFuture = lastPlayedTime + 3 * DateUtils.HOUR_IN_MILLIS;
                    new CountDownTimer(millisInFuture - System.currentTimeMillis(), 1000) {

                        public void onTick(long millisUntilFinished) {
                            if (isFreePlayEnabled) {
                                isFreePlayEnabled = false;
                            }
                            String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % TimeUnit.HOURS.toMinutes(1),
                                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % TimeUnit.MINUTES.toSeconds(1));

                            countdownTv.setText("Free Play in " + hms);
                            countdownTv.setVisibility(View.VISIBLE);
                            quizPlayCoinIv.setVisibility(View.VISIBLE);
                            quizPlayCostTv.setVisibility(View.VISIBLE);
                            whereTonightContainer.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    startQuiz();
                                }
                            });
                        }

                        public void onFinish() {
                            enableFreePlay();
                        }
                    }.start();
                } else {
                    enableFreePlay();
                }
                syncCurrentCoins();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        getQuizRefForThisUser("whereTonight").child("lastPlayedTime").addValueEventListener(timeListener);
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
                    if (profileData.getCoins() >= RETRY_COST) {
                        playContainer.setAlpha(1f);
                        earnCoinsTv.setVisibility(View.GONE);
                        whereTonightContainer.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startQuiz();
                            }
                        });
                    } else {
                        playContainer.setAlpha(0.5f);
                        earnCoinsTv.setVisibility(View.VISIBLE);
                        whereTonightContainer.setOnClickListener(null);
                    }
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
        quizPlayCoinIv.setVisibility(View.GONE);
        quizPlayCostTv.setVisibility(View.GONE);
        whereTonightContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startQuiz();
            }
        });
    }

    private void startQuiz() {
        if (isFreePlayEnabled) {
            QuizOptionsActiv.open(requireContext());
        } else if (currentCoins >= RETRY_COST) {

            whereTonightContainer.setAlpha(0.5f);
            whereTonightContainer.setOnClickListener(null);
            Analytics.triggerEvent(AnalyticsEvents.QUIZ_USE_COINS, requireContext());
            getThisUserRef().runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    ProfileData profileData = mutableData.getValue(ProfileData.class);
                    if (profileData == null) {
                        return Transaction.success(mutableData);
                    }

                    if (profileData.getCoins() >= RETRY_COST) {
                        // go ahead & play quiz
                        profileData.setCoins(profileData.getCoins() - RETRY_COST);
                        // Set value and report transaction success
                        mutableData.setValue(profileData);
                        return Transaction.success(mutableData);
                    } else {
                        return Transaction.abort();
                    }
                }

                @Override
                public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                    // Transaction completed
                    if (committed && isAdded()) {
                        QuizOptionsActiv.open(requireContext());
                    } else if (isAdded()) {
                        isFreePlayEnabled = false;
                        syncTime();
                    }
                }
            });
        } else {
            Animation shake = AnimationUtils.loadAnimation(requireContext(), R.anim.shake);
            earnCoinsTv.startAnimation(shake);
            return;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getQuizRefForThisUser("whereTonight").child("lastPlayedTime").removeEventListener(timeListener);
        getThisUserRef().removeEventListener(coinsListener);
    }
}
