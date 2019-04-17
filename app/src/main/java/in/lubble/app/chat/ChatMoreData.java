package in.lubble.app.chat;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ChatMoreData {

    @SerializedName("Group Name")
    private String groupName;
    @SerializedName("Collection Title")
    private String collectionTitle;
    @SerializedName("Collections")
    private List<String> collectionList = null;
    @SerializedName("Lubble")
    private String lubble;
    @SerializedName("Name")
    private String name;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
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


}
