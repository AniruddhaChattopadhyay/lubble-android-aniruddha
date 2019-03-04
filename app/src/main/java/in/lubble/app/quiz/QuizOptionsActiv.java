package in.lubble.app.quiz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.widget.NonSwipeableViewPager;

import java.util.ArrayList;

import static in.lubble.app.utils.UiUtils.disableTabLayoutClicks;

public class QuizOptionsActiv extends AppCompatActivity implements OptionFrag.OnListFragmentInteractionListener {

    private static final String TAG = "QuizOptionsActiv";

    private NonSwipeableViewPager optionsCardViewPager;
    private Button nextBtn;
    private TabLayout tabLayout;
    private ArrayList<QuestionData> quesDataList;
    private int answeredQuesId;
    private int answerId = -1;

    public static void open(Context context) {
        context.startActivity(new Intent(context, QuizOptionsActiv.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_options);
        Analytics.triggerScreenEvent(this, this.getClass());

        optionsCardViewPager = findViewById(R.id.viewpager_quiz_options);
        nextBtn = findViewById(R.id.btn_quiz_next);
        tabLayout = findViewById(R.id.tab_layout_questions);

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

        quesDataList = new ArrayList<>();

        final DatabaseReference quizRef = RealtimeDbHelper.getLubbleRef().child("quiz/wheretonight/questions");

        quizRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    final QuestionData questionData = child.getValue(QuestionData.class);
                    quesDataList.add(questionData);
                    optionsCardViewPager.setAdapter(new QuizOptionsPagerAdapter(getSupportFragmentManager(), quesDataList));
                    tabLayout.setupWithViewPager(optionsCardViewPager, false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onListFragmentInteraction(int answeredQuesId, OptionData optionData) {
        for (QuestionData questionData : quesDataList) {
            if (questionData.getId() == answeredQuesId) {
                //final String answerKey = (String) getKeyFromValue(questionData.getOptions(), answerValue);
                this.answeredQuesId = answeredQuesId;
                this.answerId = optionData.getId();
            }
        }
    }

    private void persistAnswer() {
        final SharedPreferences preferences = AnswerSharedPrefs.getInstance().getPreferences();
        preferences.edit().putInt(String.valueOf(answeredQuesId), answerId).commit();
    }

}
