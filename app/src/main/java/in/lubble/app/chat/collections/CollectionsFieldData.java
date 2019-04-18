package in.lubble.app.chat.collections;

import com.google.gson.annotations.SerializedName;

public class CollectionsFieldData {

    @SerializedName("fields")
    private CollectionsData collectionsData;

    public CollectionsData getCollectionsData() {
        return collectionsData;
    }

    public void setCollectionsData(CollectionsData collectionsData) {
        this.collectionsData = collectionsData;
    }

}
