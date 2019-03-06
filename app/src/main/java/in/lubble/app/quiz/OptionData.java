package in.lubble.app.quiz;

import java.io.Serializable;

public class OptionData implements Serializable {

    private static final long serialVersionUID = -4462586081643912942L;
    private int id;
    private String value;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
