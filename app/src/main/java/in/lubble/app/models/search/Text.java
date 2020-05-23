package in.lubble.app.models.search;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Text {

    @SerializedName("fullyHighlighted")
    @Expose
    private boolean fullyHighlighted;
    @SerializedName("matchLevel")
    @Expose
    private String matchLevel;
    @SerializedName("matchedWords")
    @Expose
    private List<String> matchedWords = null;
    @SerializedName("value")
    @Expose
    private String value;

    public boolean isFullyHighlighted() {
        return fullyHighlighted;
    }

    public void setFullyHighlighted(boolean fullyHighlighted) {
        this.fullyHighlighted = fullyHighlighted;
    }

    public String getMatchLevel() {
        return matchLevel;
    }

    public void setMatchLevel(String matchLevel) {
        this.matchLevel = matchLevel;
    }

    public List<String> getMatchedWords() {
        return matchedWords;
    }

    public void setMatchedWords(List<String> matchedWords) {
        this.matchedWords = matchedWords;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
