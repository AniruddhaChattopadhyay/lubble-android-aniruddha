package in.lubble.app.models;

import java.util.List;

/**
 * Created by ishaangarg on 01/11/17.
 */

public class ProfileData {

    private String id;
    private ProfileInfo info;
    private String profilePic;
    private String coverPic;
    private String locality;
    private String bio;
    private String token;
    private String referredBy;
    private List<Object> lubbles;
    private List<Object> groups;
    private boolean isOwner;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
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

    public ProfileInfo getInfo() {
        return info;
    }

    public void setInfo(ProfileInfo profileInfo) {
        this.info = profileInfo;
    }

    public String getReferredBy() {
        return referredBy;
    }

    public void setReferredBy(String referredBy) {
        this.referredBy = referredBy;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public void setOwner(boolean owner) {
        isOwner = owner;
    }
}
