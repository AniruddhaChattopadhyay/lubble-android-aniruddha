package in.lubble.app.models;

import com.google.firebase.database.Exclude;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
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
    private String id=null;
    private String profilePic = "";
    @SerializedName("name")
    private String title;
    private String desc;
    private String organizer = "";
    @SerializedName("attendee")
    private List<EventMemberData> members = new ArrayList<>();
    private long startTimestamp;
    private long endTimestamp = 0L;
    @SerializedName("latitude")
    private double lati;
    @SerializedName("longitude")
    private double longi;
    private String address;
    private String gid;
    private String lubble_id;
    private String relatedGroups = "";
    private String ticketUrl;
    private String event_id;

    public String getEvent_id() {
        return event_id;
    }

    public void setEvent_id(String event_id) {
        this.event_id = event_id;
    }

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

    public String getLubble_id() {
        return lubble_id;
    }

    public void setLubble_id(String lubble_id) {
        this.lubble_id = lubble_id;
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

    public String getTicketUrl() {
        return ticketUrl;
    }

    public void setTicketUrl(String ticketUrl) {
        this.ticketUrl = ticketUrl;
    }
    public List<EventMemberData> getMembers() {
        return members;
    }

    public void setMembers(List<EventMemberData> members) {
        this.members = members;
    }

    @Exclude
    public List<String> getRelatedGroupsList() {
        return Arrays.asList(this.relatedGroups.split("\\s*,\\s*"));
    }

    public void setRelatedGroups(String relatedGroups) {
        this.relatedGroups = relatedGroups;
    }
}
