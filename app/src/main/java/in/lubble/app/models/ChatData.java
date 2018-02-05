package in.lubble.app.models;

import java.util.HashMap;

/**
 * Created by ishaan on 21/1/18.
 */

public class ChatData {

    private String id;
    private String authorUid;
    private String authorName;
    private String message;
    private String imgUrl;
    private int lubbCount = 0;
    private HashMap<String, Boolean> lubbers = new HashMap<>();

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ChatData) {
            ChatData objectToCompare = (ChatData) obj;
            if (this.id.equalsIgnoreCase(objectToCompare.getId())) {
                return true;
            }
            return false;
        }
        return super.equals(obj);
    }

    public String getAuthorUid() {
        return authorUid;
    }

    public void setAuthorUid(String authorUid) {
        this.authorUid = authorUid;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public int getLubbCount() {
        return lubbCount;
    }

    public void setLubbCount(int lubbCount) {
        this.lubbCount = lubbCount;
    }

    public HashMap<String, Boolean> getLubbers() {
        return lubbers;
    }

    public void setLubbers(HashMap<String, Boolean> lubbers) {
        this.lubbers = lubbers;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
