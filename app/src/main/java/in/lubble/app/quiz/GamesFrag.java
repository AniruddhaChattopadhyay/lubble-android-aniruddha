package in.lubble.app.quiz;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import in.lubble.app.R;
import in.lubble.app.models.ProfileData;
import in.lubble.app.profile.ProfileFrag;

import java.util.concurrent.TimeUnit;

import static in.lubble.app.firebase.RealtimeDbHelper.getQuizRefForThisUser;
import static in.lubble.app.firebase.RealtimeDbHelper.getThisUserRef;


public class GamesFrag extends Fragment {

    private static final int RETRY_COST = 40;
    private TextView currentCoinsTv;
    private TextView countdownTv;
    private ImageView quizPlayCoinIv;
    private TextView quizPlayCostTv;
    private TextView earnCoinsTv;
    private LinearLayout playContainer;
    private boolean isFreePlayEnabled;

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

        final RelativeLayout whereTonightContainer = view.findViewById(R.id.container_wheretonight);
        currentCoinsTv = view.findViewById(R.id.tv_total_coins);
        countdownTv = view.findViewById(R.id.tv_quiz_play_countdown);
        playContainer = view.findViewById(R.id.container_quiz_play);
        quizPlayCoinIv = view.findViewById(R.id.iv_quiz_play_coin);
        quizPlayCostTv = view.findViewById(R.id.tv_quiz_play_cost);
        earnCoinsTv = view.findViewById(R.id.tv_earn_more);
        final LinearLayout currentCoinsContainer = view.findViewById(R.id.container_current_coins);

        quizPlayCostTv.setText(String.valueOf(RETRY_COST));

        syncCurrentCoins();
        syncTime();

        whereTonightContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QuizOptionsActiv.open(requireContext());
            }
        });

        currentCoinsContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileFrag.newInstance(FirebaseAuth.getInstance().getUid());
            }
        });

        return view;
    }


    private void syncTime() {
        getQuizRefForThisUser("whereTonight").child("lastPlayedTime").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    Long lastPlayedTime = dataSnapshot.getValue(Long.class);
                    final long millisInFuture = lastPlayedTime + 3 * DateUtils.HOUR_IN_MILLIS;
                    new CountDownTimer(millisInFuture - System.currentTimeMillis(), 1000) {

                        public void onTick(long millisUntilFinished) {
                            String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % TimeUnit.HOURS.toMinutes(1),
                                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % TimeUnit.MINUTES.toSeconds(1));

                            countdownTv.setText("Free Play in " + hms);
                            countdownTv.setVisibility(View.VISIBLE);
                            quizPlayCoinIv.setVisibility(View.VISIBLE);
                            quizPlayCostTv.setVisibility(View.VISIBLE);
                        }

                        public void onFinish() {
                            enableFreePlay();
                        }
                    }.start();
                } else {
                    enableFreePlay();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void enableFreePlay() {
        countdownTv.setVisibility(View.GONE);
        isFreePlayEnabled = true;
        quizPlayCoinIv.setVisibility(View.GONE);
        quizPlayCostTv.setVisibility(View.GONE);
    }

    private void syncCurrentCoins() {
        getThisUserRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final ProfileData profileData = dataSnapshot.getValue(ProfileData.class);
                currentCoinsTv.setText(String.valueOf(profileData.getCoins()));
                if (profileData.getCoins() >= RETRY_COST) {
                    playContainer.setAlpha(1f);
                    earnCoinsTv.setVisibility(View.GONE);
                } else {
                    playContainer.setAlpha(0.5f);
                    earnCoinsTv.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
