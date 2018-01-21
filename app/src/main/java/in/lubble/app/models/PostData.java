package in.lubble.app.models;

import android.support.annotation.Nullable;

import in.lubble.app.utils.DateTimeUtils;

/**
 * Created by ishaangarg on 27/10/17.
 */

public class PostData {

    private int postId;
    @Nullable
    private ProfileData user;
    private String authDp;
    private String group;
    private String text;
    private boolean priority;
    private String timestamp;
    private int lubbCount;
    private boolean isLubbed;

    public PostData() {
    }

    public PostData(String group, boolean priority) {
        this.group = group;
        this.priority = priority;
    }

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public String getAuthDp() {
        return authDp;
    }

    public void setAuthDp(String authDp) {
        this.authDp = authDp;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean getPriority() {
        return priority;
    }

    public void setPriority(boolean priority) {
        this.priority = priority;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getHumanTimestamp() {
        return DateTimeUtils.getHumanTimestamp(timestamp);
    }

    @Nullable
    public ProfileData getUser() {
        return user;
    }

    public void setUser(@Nullable ProfileData user) {
        this.user = user;
    }

    public boolean getIsLubbed() {
        return isLubbed;
    }

    public void setIsLubbed(boolean isLubbed) {
        this.isLubbed = isLubbed;
    }

    public int getLubbCount() {
        return lubbCount;
    }

    public void setLubbCount(int lubbCount) {
        this.lubbCount = lubbCount;
    }
}
