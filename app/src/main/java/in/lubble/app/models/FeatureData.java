package in.lubble.app.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class FeatureData {

    @SerializedName("sellers")
    @Expose
    private List<Integer> sellers = new ArrayList<>();
    @SerializedName("is_view_count_enabled")
    @Expose
    private boolean isViewCountEnabled;

    public List<Integer> getSellers() {
        return sellers;
    }

    public void setSellers(List<Integer> sellers) {
        this.sellers = sellers;
    }

    public boolean isViewCountEnabled() {
        return isViewCountEnabled;
    }

    public void setViewCountEnabled(boolean viewCountEnabled) {
        isViewCountEnabled = viewCountEnabled;
    }
}
