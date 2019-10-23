package in.lubble.app;

public class GoingStatsModel {
    String uid, name, url, stats;

    GoingStatsModel(String uid, String name, String url, String stats) {
        this.uid = uid;
        this.name = name;
        this.url = url;
        this.stats = stats;
    }

    public String getName() {
        return name;
    }

    public String getUid() {
        return uid;
    }

    public String getStats() {
        return stats;
    }

    public String getUrl() {
        return url;
    }
}
