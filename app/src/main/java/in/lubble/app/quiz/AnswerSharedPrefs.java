package in.lubble.app.quiz;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by ishaan on 11/3/18.
 */

public class AnswerSharedPrefs {

    private static AnswerSharedPrefs instance;
    private final SharedPreferences preferences;
    private final String QUIZ_ANSWER_SHARED_PREFERENCE_KEY = "in.lubble.QuizAnswerSharedPrefs";

    private AnswerSharedPrefs(Context context) {
        preferences = context.getSharedPreferences(QUIZ_ANSWER_SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
    }

    public static void initializeInstance(Context context) {
        if (instance == null) {
            instance = new AnswerSharedPrefs(context);
        }
    }

    public static AnswerSharedPrefs getInstance() {
        if (instance == null) {
            throw new IllegalStateException(AnswerSharedPrefs.class.getCanonicalName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }
        return instance;
    }

    public SharedPreferences getPreferences() {
        return preferences;
    }

    /**
     * Clear all SharedPreferences
     */
    public void clearAll() {
        preferences.edit().clear().commit();
    }


    //******************************************/


}
