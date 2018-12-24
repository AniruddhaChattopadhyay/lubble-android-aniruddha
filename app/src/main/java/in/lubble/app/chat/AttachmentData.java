package in.lubble.app.chat;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;

public class AttachmentData {

    @DrawableRes
    private int icon;
    private String text;
    @ColorRes
    private int color;

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

    @ColorRes
    public int getColor() {
        return color;
    }

    public void setColor(@ColorRes int color) {
        this.color = color;
    }
}
