
package in.lubble.app.models.marketplace;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Category {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("display_name")
    @Expose
    private String displayName;
    @SerializedName("icon")
    @Expose
    private String icon;
    @SerializedName("items")
    @Expose
    @Nullable
    private List<Item> items = null;
    @SerializedName("sellers")
    @Nullable
    private List<SellerData> sellers = null;
    @SerializedName("type")
    @Expose
    private int type = Item.ITEM_PRODUCT;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getHumanReadableName() {
        return TextUtils.isEmpty(this.displayName) ? name.replace("_", " ") : this.displayName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Nullable
    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Nullable
    public List<SellerData> getSellers() {
        return sellers;
    }

    public void setSellers(@Nullable List<SellerData> sellers) {
        this.sellers = sellers;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
