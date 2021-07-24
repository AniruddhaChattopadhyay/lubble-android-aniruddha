package in.lubble.app.explore;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import in.lubble.app.models.GroupInfoData;

public class ExploreGroupData extends GroupInfoData {

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof ExploreGroupData) {
            GroupInfoData objectToCompare = (GroupInfoData) obj;
            return this.getId().equalsIgnoreCase(objectToCompare.getId());
        }
        return super.equals(obj);
    }

    @SerializedName("priority")
    private int priority;

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

}
