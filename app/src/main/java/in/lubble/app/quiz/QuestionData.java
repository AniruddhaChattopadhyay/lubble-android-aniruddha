package in.lubble.app.quiz;

import java.io.Serializable;
import java.util.ArrayList;

public class QuestionData implements Serializable {

    private static final long serialVersionUID = -7855131020149483128L;

    private int quesId;
    private String quesName;
    private ArrayList<OptionData> options;

    public String getQuesName() {
        return quesName;
    }

    public void setQuesName(String quesName) {
        this.quesName = quesName;
    }

    public ArrayList<OptionData> getOptions() {
        return options;
    }

    public void setOptions(ArrayList<OptionData> options) {
        this.options = options;
    }

    public int getQuesId() {
        return quesId;
    }

    public void setQuesId(int quesId) {
        this.quesId = quesId;
    }
}
