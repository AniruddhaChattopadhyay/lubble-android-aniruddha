package in.lubble.app.models;

import java.io.Serializable;

public class FeedPostData implements Serializable {

    private static final long serialVersionUID = -787902331306384561L;

    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
