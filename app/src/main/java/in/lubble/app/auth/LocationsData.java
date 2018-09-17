package in.lubble.app.auth;

import java.io.Serializable;
import java.util.ArrayList;

public class LocationsData implements Serializable {

    private static final long serialVersionUID = 2064636807194358757L;

    private LubbleData defaultLubble;
    private ArrayList<LubbleData> lubbleDataList;

    public LubbleData getDefaultLubble() {
        return defaultLubble;
    }

    public void setDefaultLubble(LubbleData defaultLubble) {
        this.defaultLubble = defaultLubble;
    }

    public ArrayList<LubbleData> getLubbleDataList() {
        return lubbleDataList;
    }

    public void setLubbleDataList(ArrayList<LubbleData> lubbleDataList) {
        this.lubbleDataList = lubbleDataList;
    }

    public class LubbleData implements Serializable {

        private static final long serialVersionUID = 5765706198407211235L;

        private String lubbleName;

        public String getLubbleName() {
            return lubbleName;
        }

        public void setLubbleName(String lubbleName) {
            this.lubbleName = lubbleName;
        }

    }

}
