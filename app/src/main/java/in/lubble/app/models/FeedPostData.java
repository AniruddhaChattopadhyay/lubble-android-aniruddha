package in.lubble.app.models;

import android.net.Uri;

import java.io.Serializable;

public class FeedPostData implements Serializable {

    private static final long serialVersionUID = -787902331306384561L;

    private String text;
    private String imgUri = null;

    public String getImgUri() {
        return imgUri;
    }

    public void setImgUri(String imgUri) {
        this.imgUri = imgUri;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
