package in.lubble.app.quiz;

import java.io.Serializable;
import java.util.ArrayList;

public class QuestionData implements Serializable {

    private static final long serialVersionUID = -7855131020149483128L;

    private int id;
    private String question;
    private ArrayList<OptionData> options;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public ArrayList<OptionData> getOptions() {
        return options;
    }

    public void setOptions(ArrayList<OptionData> options) {
        this.options = options;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
