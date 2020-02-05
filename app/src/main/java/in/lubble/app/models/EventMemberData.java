package in.lubble.app.models;

import java.io.Serializable;

public class EventMemberData implements Serializable {
    String uid;
    String event_id;
    String venue_idl;
    Boolean isAdmin;
    int guests;
    int response;
    long timestamp;
    int tickets;

    public int getTickets() {
        return tickets;
    }

    public void setTickets(int tickets) {
        this.tickets = tickets;
    }

    public int getGuests() {
        return guests;
    }

    public void setGuests(int guests) {
        this.guests = guests;
    }

    public Boolean getisAdmin() {
        return isAdmin;
    }

    public void setisAdmin(Boolean admin) {
        isAdmin = admin;
    }

    public int getResponse() {
        return response;
    }

    public void setResponse(int response) {
        this.response = response;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEvent_id() {
        return event_id;
    }

    public void setEvent_id(String event_id) {
        this.event_id = event_id;
    }

    public String getVenue_idl() {
        return venue_idl;
    }

    public void setVenue_idl(String venue_idl) {
        this.venue_idl = venue_idl;
    }
}
