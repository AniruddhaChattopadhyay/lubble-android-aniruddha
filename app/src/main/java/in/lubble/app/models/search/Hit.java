package in.lubble.app.models.search;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Hit {

    @SerializedName("_highlightResult")
    @Expose
    private HighlightResult highlightResult;
    @SerializedName("chatId")
    @Expose
    private String chatId;
    @SerializedName("objectID")
    @Expose
    private String objectID;
    @SerializedName("text")
    @Expose
    private String text;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("entity")
    @Expose
    private String entity;

    public HighlightResult get_highlightResult() {
        return highlightResult;
    }

    public void set_highlightResult(HighlightResult highlightResult) {
        this.highlightResult = highlightResult;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }
}
