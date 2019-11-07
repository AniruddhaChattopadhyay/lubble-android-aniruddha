package in.lubble.app;

public class GoingStatsModel {
    private String uid, name, url;
    private int status;

    GoingStatsModel(String uid, String name, String url, int stats) {
        this.uid = uid;
        this.name = name;
        this.url = url;
        this.status = stats;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
