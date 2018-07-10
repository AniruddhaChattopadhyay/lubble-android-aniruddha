
package in.lubble.app.models.marketplace;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class MarketplaceData {

    @SerializedName("categories")
    @Expose
    private ArrayList<Category> categories = null;
    @SerializedName("items")
    @Expose
    private ArrayList<Item> items = null;

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<Category> categories) {
        this.categories = categories;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(ArrayList<Item> items) {
        this.items = items;
    }

}
