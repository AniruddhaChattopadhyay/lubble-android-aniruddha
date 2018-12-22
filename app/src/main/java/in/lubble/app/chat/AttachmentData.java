package in.lubble.app.chat;

import androidx.annotation.DrawableRes;

public class AttachmentData {

    @DrawableRes
    private int icon;
    private String text;

    @DrawableRes
    public int getIcon() {
        return icon;
    }

    public void setIcon(@DrawableRes int icon) {
        this.icon = icon;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
