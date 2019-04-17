package in.lubble.app.chat;

import com.google.gson.annotations.SerializedName;

public class CollectionRecordData {

    @SerializedName("fields")
    private CollectionsData collectionsData;

    public CollectionsData getCollectionsData() {
        return collectionsData;
    }

    public void setCollectionsData(CollectionsData collectionsData) {
        this.collectionsData = collectionsData;
    }

}
