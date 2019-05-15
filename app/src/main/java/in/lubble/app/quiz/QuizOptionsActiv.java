package in.lubble.app.quiz;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import in.lubble.app.BaseActivity;
import in.lubble.app.Constants;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import in.lubble.app.widget.NonSwipeableViewPager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;

import static in.lubble.app.utils.UiUtils.disableTabLayoutClicks;

public class QuizOptionsActiv extends BaseActivity implements OptionFrag.OnListFragmentInteractionListener {

    private static final String TAG = "QuizOptionsActiv";

    private NonSwipeableViewPager optionsCardViewPager;
    private TabLayout tabLayout;
    private ArrayList<QuestionData> quesDataList;
    private ProgressBar progressBar;

    public static void open(Context context) {
        context.startActivity(new Intent(context, QuizOptionsActiv.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_options);
        Analytics.triggerScreenEvent(this, this.getClass());

        optionsCardViewPager = findViewById(R.id.viewpager_quiz_options);
        progressBar = findViewById(R.id.progressbar_quiz);
        tabLayout = findViewById(R.id.tab_layout_questions);

        progressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
        fetchQuesList();
    }

    private void fetchQuesList() {
        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        endpoints.getQuizQuestions().enqueue(new Callback<ArrayList<QuestionData>>() {
            @Override
            public void onResponse(Call<ArrayList<QuestionData>> call, Response<ArrayList<QuestionData>> response) {
                if (response.isSuccessful() && !isFinishing()) {
                    progressBar.setVisibility(View.GONE);
                    quesDataList = response.body();
                    optionsCardViewPager.setAdapter(new QuizOptionsPagerAdapter(getSupportFragmentManager(), quesDataList));
                    tabLayout.setupWithViewPager(optionsCardViewPager, false);
                    disableTabLayoutClicks(tabLayout);
                } else if (!isFinishing()) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(QuizOptionsActiv.this, R.string.all_try_again, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ArrayList<QuestionData>> call, Throwable t) {
                Log.e(TAG, "onFailure: ");
                if (!isFinishing()) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(QuizOptionsActiv.this, R.string.check_internet, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @SuppressLint("ApplySharedPref")
    @Override
    public void onListFragmentInteraction(int answeredQuesId, OptionData optionData) {
        int answerId = optionData.getId();
        String answerName = optionData.getValue();
        if (answerId != -1) {
            final SharedPreferences preferences = AnswerSharedPrefs.getInstance().getPreferences();
            preferences.edit().putInt(String.valueOf(answeredQuesId), answerId).commit();
            preferences.edit().putString(String.valueOf(answeredQuesId) + "_name", answerName).commit();
        } else {
            Toast.makeText(QuizOptionsActiv.this, "Choose one", Toast.LENGTH_SHORT).show();
            return;
        }

        final int nextPos = optionsCardViewPager.getCurrentItem() + 1;
        if (nextPos < optionsCardViewPager.getAdapter().getCount()) {
            optionsCardViewPager.setCurrentItem(nextPos);
        } else {
            Analytics.triggerEvent(AnalyticsEvents.QUIZ_RESULT, this);
            final String remoteConfigResult = FirebaseRemoteConfig.getInstance().getString(Constants.QUIZ_RESULT_UI);
            if (remoteConfigResult.equalsIgnoreCase("NORMAL")) {
                QuizResultActiv.open(QuizOptionsActiv.this);
            } else if (remoteConfigResult.equalsIgnoreCase("CAM")) {
                QuizResultCamActiv.open(QuizOptionsActiv.this);
            }
            finish();
        }
    }

}
