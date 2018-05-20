package in.lubble.app.models;

/**
 * Created by ishaan on 20/5/18.
 */

public class EventData {

    private String picUrl;
    private String title;
    private String desc;
    private long startTimeTimestamp;
    private long endTimeTimestamp = 0L;
    private double lati;
    private double longi;
    private String address;
    private String gid;

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
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

    public long getStartTimeTimestamp() {
        return startTimeTimestamp;
    }

    public void setStartTimeTimestamp(long startTimeTimestamp) {
        this.startTimeTimestamp = startTimeTimestamp;
    }

    public long getEndTimeTimestamp() {
        return endTimeTimestamp;
    }

    public void setEndTimeTimestamp(long endTimeTimestamp) {
        this.endTimeTimestamp = endTimeTimestamp;
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
}
