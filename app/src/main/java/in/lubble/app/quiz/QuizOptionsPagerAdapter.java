package in.lubble.app.quiz;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

/**
 * Created by shadow-admin on 22/1/18.
 */

public class QuizOptionsPagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<QuestionData> questionData;

    public QuizOptionsPagerAdapter(FragmentManager fm, ArrayList<QuestionData> questionData) {
        super(fm);
        this.questionData = questionData;
    }

    @Override
    public Fragment getItem(int position) {
        return OptionFrag.newInstance(questionData.get(position));
    }

    @Override
    public int getCount() {
        return questionData.size();
    }
}
