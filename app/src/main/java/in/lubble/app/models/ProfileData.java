package in.lubble.app.models;

import java.util.List;

import static in.lubble.app.utils.StringUtils.getTitleCase;

/**
 * Created by ishaangarg on 01/11/17.
 */

public class ProfileData {

    private String id;
    private String name;
    private String dp;
    private String coverPic;
    private String locality;
    private String bio;
    private String token;
    private String profile_pic;
    private String cover_pic;
    private List<Object> lubbles;
    private List<Object> groups;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return getTitleCase(name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDp() {
        return dp;
    }

    public void setDp(String dp) {
        this.dp = dp;
    }

    public String getCoverPic() {
        return coverPic;
    }

    public void setCoverPic(String coverPic) {
        this.coverPic = coverPic;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public List<Object> getLubbles() {
        return lubbles;
    }

    public void setLubbles(List<Object> lubbles) {
        this.lubbles = lubbles;
    }

    public List<Object> getGroups() {
        return groups;
    }

    public void setGroups(List<Object> groups) {
        this.groups = groups;
    }

    public String getProfile_pic() {
        return profile_pic;
    }

    public void setProfile_pic(String profile_pic) {
        this.profile_pic = profile_pic;
    }

    public String getCover_pic() {
        return cover_pic;
    }

    public void setCover_pic(String cover_pic) {
        this.cover_pic = cover_pic;
    }
}
