package in.lubble.app.models;

import java.util.HashMap;

/**
 * Created by ishaan on 20/5/18.
 */

public class EventData {

    public static final int NO = 0;
    public static final int GOING = 1;
    public static final int MAYBE = 2;

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
}
