package in.lubble.app.chat;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ChatMoreData {

    @SerializedName("GroupID")
    private List<String> groupId;
    @SerializedName("CollectionTitle")
    private String collectionTitle;
    @SerializedName("Collections")
    private List<String> collectionList = null;
    @SerializedName("Lubble")
    private String lubble;
    @SerializedName("Name")
    private String name;
    @SerializedName("IsBooksGroup")
    private boolean isBooksGroup;

    public List<String> getGroupId() {
        return groupId;
    }

    public void setGroupId(List<String> groupId) {
        this.groupId = groupId;
    }

    public List<String> getCollectionList() {
        return collectionList;
    }

    public void setCollectionList(List<String> entries1) {
        this.collectionList = entries1;
    }

    public String getCollectionTitle() {
        return collectionTitle;
    }

    public void setCollectionTitle(String title1) {
        this.collectionTitle = title1;
    }

    public String getLubble() {
        return lubble;
    }

    public void setLubble(String lubble) {
        this.lubble = lubble;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getIsBooksGroup() {
        return isBooksGroup;
    }

    public void setIsBooksGroup(boolean booksGroup) {
        isBooksGroup = booksGroup;
    }
}
