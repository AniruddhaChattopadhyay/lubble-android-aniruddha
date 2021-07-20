package in.lubble.app.explore;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import in.lubble.app.models.GroupData;

public class ExploreGroupData extends GroupData {

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof ExploreGroupData) {
            GroupData objectToCompare = (GroupData) obj;
            return this.getId().equalsIgnoreCase(objectToCompare.getId());
        }
        return super.equals(obj);
    }
    @SerializedName("priority")
    private int priority;
    private int memberCount;

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

}
