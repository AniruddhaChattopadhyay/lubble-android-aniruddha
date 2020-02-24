package in.lubble.app.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class EventIdData implements Serializable {
    @SerializedName("event_id")
    private  String event_id;

    public String getEvent_id() {
        return event_id;
    }

    public void setEvent_id(String event_id) {
        this.event_id = event_id;
    }
}
