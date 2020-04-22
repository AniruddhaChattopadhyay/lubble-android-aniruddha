package in.lubble.app.marketplace;

import com.google.gson.annotations.SerializedName;

public class ItemSearchData {
    @SerializedName("id")
    private int id;
    @SerializedName("name")
    private String name;
    @SerializedName("entity")
    private String entity;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }
}
