package in.lubble.app.quiz;

import android.app.Dialog;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.*;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.models.ProfileData;
import in.lubble.app.referrals.ReferralActivity;

import java.util.concurrent.TimeUnit;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static in.lubble.app.firebase.RealtimeDbHelper.getQuizRefForThisUser;
import static in.lubble.app.firebase.RealtimeDbHelper.getThisUserRef;

public class RetryQuizBottomSheet extends BottomSheetDialogFragment {

    private static final String TAG = "RetryQuizBottomSheet";

    private TextView currentCoinsTv;
    private TextView earnCoinsTv;
    private TextView countdownTv;
    private LinearLayout waitContainer;
    private LinearLayout useCoinsContainer;
    private static final int RETRY_COST = 40;
    private OnQuizRetryListener listener;
    private boolean isFreePlayEnabled;

    public static RetryQuizBottomSheet newInstance() {

        Bundle args = new Bundle();

        RetryQuizBottomSheet fragment = new RetryQuizBottomSheet();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        View view = View.inflate(getContext(), R.layout.frag_retry_quiz, null);
        dialog.setContentView(view);

        Analytics.triggerScreenEvent(requireContext(), this.getClass());

        currentCoinsTv = view.findViewById(R.id.tv_current_coins);
        waitContainer = view.findViewById(R.id.container_wait);
        useCoinsContainer = view.findViewById(R.id.container_use_coins);
        earnCoinsTv = view.findViewById(R.id.tv_earn_coins);
        countdownTv = view.findViewById(R.id.tv_quiz_countdown);
        listener = (OnQuizRetryListener) getActivity();

        syncCurrentCoins();
        syncTime();

        useCoinsContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                useCoinsContainer.setAlpha(0.5f);
                useCoinsContainer.setOnClickListener(null);
                final Bundle bundle = new Bundle();
                bundle.putString("type", "paid");
                Analytics.triggerEvent(AnalyticsEvents.QUIZ_RETRY_DONE, bundle, requireContext());
                getThisUserRef().runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        ProfileData profileData = mutableData.getValue(ProfileData.class);
                        if (profileData == null) {
                            return Transaction.success(mutableData);
                        }

                        if (profileData.getCoins() >= RETRY_COST) {
                            // go ahead & play quiz again
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
                            listener.onSheetInteraction(RESULT_OK);
                            dismiss();
                        } else if (isAdded()) {
                            useCoinsContainer.setAlpha(0.5f);
                            earnCoinsTv.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        });

        waitContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFreePlayEnabled) {
                    final Bundle bundle = new Bundle();
                    bundle.putString("type", "free");
                    Analytics.triggerEvent(AnalyticsEvents.QUIZ_RETRY_DONE, bundle, requireContext());
                    listener.onSheetInteraction(RESULT_OK);
                    dismiss();
                } else {
                    final Bundle bundle = new Bundle();
                    bundle.putString("type", "wait");
                    Analytics.triggerEvent(AnalyticsEvents.QUIZ_RETRY_DONE, bundle, requireContext());
                    listener.onSheetInteraction(RESULT_CANCELED);
                    dismiss();
                }
            }
        });

        earnCoinsTv.setPaintFlags(earnCoinsTv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        earnCoinsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Analytics.triggerEvent(AnalyticsEvents.QUIZ_EARN_COINS, requireContext());
                ReferralActivity.open(requireContext());
                dismiss();
            }
        });

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
        countdownTv.setText("Play For Free!");
        isFreePlayEnabled = true;
        useCoinsContainer.setAlpha(0.5f);
        useCoinsContainer.setOnClickListener(null);
    }

    private void syncCurrentCoins() {
        getThisUserRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final ProfileData profileData = dataSnapshot.getValue(ProfileData.class);
                currentCoinsTv.setText(String.valueOf(profileData.getCoins()));
                if (profileData.getCoins() >= RETRY_COST) {
                    useCoinsContainer.setAlpha(1f);
                    earnCoinsTv.setVisibility(View.GONE);
                } else {
                    useCoinsContainer.setAlpha(0.5f);
                    earnCoinsTv.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public interface OnQuizRetryListener {
        void onSheetInteraction(int id);
    }

}
