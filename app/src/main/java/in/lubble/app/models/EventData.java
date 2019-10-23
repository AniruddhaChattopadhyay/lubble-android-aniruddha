package in.lubble.app.models;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ishaan on 20/5/18.
 */

public class EventData implements Serializable {

    private static final long serialVersionUID = -5348821431333945167L;
    public static final int NO = 0;
    public static final int GOING = 1;
    public static final int MAYBE = 2;

    @Exclude
    private String id;
    private String profilePic = "";
    private String title;
    private String desc;
    private String organizer = "";
    private HashMap<String, Object> members = new HashMap<>();
    private long startTimestamp;
    private long endTimestamp = 0L;
    private double lati;
    private double longi;
    private String address;
    private String gid;
    private String relatedGroups = "";

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EventData) {
            EventData objectToCompare = (EventData) obj;
            if (this.id.equalsIgnoreCase(objectToCompare.getId())) {
                return true;
            }
            return false;
        }
        return super.equals(obj);
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(long startTimeTimestamp) {
        this.startTimestamp = startTimeTimestamp;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(long endTimeTimestamp) {
        this.endTimestamp = endTimeTimestamp;
    }

    public double getLati() {
        return lati;
    }

    public void setLati(double lati) {
        this.lati = lati;
    }

    public double getLongi() {
        return longi;
    }

    public void setLongi(double longi) {
        this.longi = longi;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getGid() {
        return gid;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }

    public HashMap<String, Object> getMembers() {
        return members;
    }

    public void setMembers(HashMap<String, Object> members) {
        this.members = members;
    }

    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRelatedGroups() {
        return relatedGroups;
    }

    @Exclude
    public List<String> getRelatedGroupsList() {
        return Arrays.asList(this.relatedGroups.split("\\s*,\\s*"));
    }

    public void setRelatedGroups(String relatedGroups) {
        this.relatedGroups = relatedGroups;
    }
}
