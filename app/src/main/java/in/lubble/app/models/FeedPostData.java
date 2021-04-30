package in.lubble.app.models;

import java.io.Serializable;

public class FeedPostData implements Serializable {

    private static final long serialVersionUID = -787902331306384561L;

    private String text;
    private String attachedImgUri;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAttachedImgUri() {
        return attachedImgUri;
    }

    public void setAttachedImgUri(String attachedImgUri) {
        this.attachedImgUri = attachedImgUri;
    }
}
