
package in.lubble.app.models.marketplace;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import in.lubble.app.marketplace.SliderData;

public class MarketplaceData {

    @SerializedName("categories")
    @Expose
    private ArrayList<Category> categories = null;
    @SerializedName("sliders")
    @Expose
    private ArrayList<SliderData> sliderDataList = new ArrayList<>();
    @SerializedName("showcase_categories")
    @Expose
    private ArrayList<Category> showcaseCategories = null;
    @SerializedName("items")
    @Expose
    private ArrayList<Item> items = null;

    public List<Category> getShowcaseCategories() {
        return showcaseCategories;
    }

    public void setShowcaseCategories(ArrayList<Category> showcaseCategories) {
        this.showcaseCategories = showcaseCategories;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(ArrayList<Item> items) {
        this.items = items;
    }

    public ArrayList<Category> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<Category> categories) {
        this.categories = categories;
    }

    public ArrayList<SliderData> getSliderDataList() {
        return sliderDataList;
    }

    public void setSliderDataList(ArrayList<SliderData> sliderDataList) {
        this.sliderDataList = sliderDataList;
    }
}
