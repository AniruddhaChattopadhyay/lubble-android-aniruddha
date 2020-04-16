package in.lubble.app.models.search;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class HighlightResult {

    @SerializedName("groupId")
    @Expose
    private GroupId groupId;
    @SerializedName("lubbleId")
    @Expose
    private LubbleId lubbleId;
    @SerializedName("text")
    @Expose
    private Text text;

    public GroupId getGroupId() {
        return groupId;
    }

    public void setGroupId(GroupId groupId) {
        this.groupId = groupId;
    }

    public LubbleId getLubbleId() {
        return lubbleId;
    }

    public void setLubbleId(LubbleId lubbleId) {
        this.lubbleId = lubbleId;
    }

    public Text getText() {
        return text;
    }

    public void setText(Text text) {
        this.text = text;
    }

}
