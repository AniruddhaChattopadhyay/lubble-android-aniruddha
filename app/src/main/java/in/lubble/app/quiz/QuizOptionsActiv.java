package in.lubble.app.quiz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.tabs.TabLayout;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import in.lubble.app.widget.NonSwipeableViewPager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;

import static in.lubble.app.utils.UiUtils.disableTabLayoutClicks;

public class QuizOptionsActiv extends AppCompatActivity implements OptionFrag.OnListFragmentInteractionListener {

    private static final String TAG = "QuizOptionsActiv";

    private NonSwipeableViewPager optionsCardViewPager;
    private Button nextBtn;
    private TabLayout tabLayout;
    private ArrayList<QuestionData> quesDataList;
    private int answeredQuesId;
    private ProgressBar progressBar;
    private int answerId = -1;
    private String answerName;

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
        nextBtn = findViewById(R.id.btn_quiz_next);
        tabLayout = findViewById(R.id.tab_layout_questions);

        progressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
        disableTabLayoutClicks(tabLayout);

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (answerId != -1) {
                    persistAnswer();
                    answerId = -1;
                } else {
                    Toast.makeText(QuizOptionsActiv.this, "Choose one", Toast.LENGTH_SHORT).show();
                    return;
                }

                final int nextPos = optionsCardViewPager.getCurrentItem() + 1;
                if (nextPos < optionsCardViewPager.getAdapter().getCount()) {
                    optionsCardViewPager.setCurrentItem(nextPos);
                } else {
                    QuizResultActiv.open(QuizOptionsActiv.this);
                    finish();
                }
            }
        });

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

    @Override
    public void onListFragmentInteraction(int answeredQuesId, OptionData optionData) {
        for (QuestionData questionData : quesDataList) {
            if (questionData.getQuesId() == answeredQuesId) {
                this.answeredQuesId = answeredQuesId;
                this.answerId = optionData.getId();
                this.answerName = optionData.getValue();
            }
        }
    }

    private void persistAnswer() {
        final SharedPreferences preferences = AnswerSharedPrefs.getInstance().getPreferences();
        preferences.edit().putInt(String.valueOf(answeredQuesId), answerId).commit();
        preferences.edit().putString(String.valueOf(answeredQuesId) + "_name", answerName).commit();
    }

}
