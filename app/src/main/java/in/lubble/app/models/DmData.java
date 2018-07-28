package in.lubble.app.models;

import com.google.firebase.database.Exclude;

import java.util.HashMap;

public class DmData {

    @Exclude
    private String id;
    private HashMap<String, Object> members = new HashMap<>();
    private String lastMessage;
    private long lastMessageTimestamp = 0;

    public DmData() {
    }  // Needed for Firebase

    public HashMap<String, Object> getMembers() {
        return members;
    }

    public void setMembers(HashMap<String, Object> members) {
        this.members = members;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public void setLastMessageTimestamp(long lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
