package in.lubble.app.models;

import com.google.gson.annotations.SerializedName;

public class InstaData {
    //getting username from within data of instagram json
    @SerializedName("username")
    private String username;
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
