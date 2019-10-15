package in.lubble.app.models;

import androidx.annotation.Nullable;
import com.google.firebase.database.Exclude;

import in.lubble.app.utils.DateTimeUtils;

import java.util.Calendar;

/**
 * Created by ishaangarg on 01/11/17.
 */

public class ProfileData {

    private String id = "";
    private ProfileInfo info;
    private InstagramLoginState instagram =null;
    private String profilePic;
    private String coverPic;
    private String locality;
    private String bio;
    private int gender = -1; //0 male / 1 female / 2 other
    private String jobTitle;
    private String company;
    private String school;
    private String phone;
    private String token;
    private String referredBy;
    private long birthdate = 0L;
    private boolean isOwner;
    private boolean isAgePublic = true;
    private boolean isDeleted;
    private long coins;
    private long likes = 0L;
    private ProfileAddress profileAddress;
    @Exclude
    private String groupFlair;

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof ProfileData) {
            ProfileData objectToCompare = (ProfileData) obj;
            if (this.id.equalsIgnoreCase(objectToCompare.getId())) {
                return true;
            }
            return false;
        }
        return super.equals(obj);
    }

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

    public ProfileInfo getInfo() {
        return info;
    }
    public InstagramLoginState getInstagram() {
        return instagram;
    }

    public void setInstagram(InstagramLoginState instagram) {
        this.instagram = instagram;
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

    public boolean getIsOwner() {
        return isOwner;
    }

    public void setIsOwner(boolean owner) {
        isOwner = owner;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public int getGender() {
        return gender;
    }

    public String getGenderText() {
        switch (gender) {
            case 0:
                return "Male";
            case 1:
                return "Female";
            case 2:
                return "Other";
            default:
                return "";
        }
    }

    public static String getGenderText(int pos) {
        switch (pos) {
            case 0:
                return "Male";
            case 1:
                return "Female";
            case 2:
                return "Other";
            default:
                return "";
        }
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public long getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(long birthdate) {
        this.birthdate = birthdate;
    }

    public int getAge() {
        Calendar dob = Calendar.getInstance();
        dob.setTimeInMillis(birthdate);
        return DateTimeUtils.getAge(dob);
    }

    public boolean getIsAgePublic() {
        return isAgePublic;
    }

    public void setIsAgePublic(boolean agePublic) {
        isAgePublic = agePublic;
    }

    public long getCoins() {
        return coins;
    }

    public void setCoins(long coins) {
        this.coins = coins;
    }

    public long getLikes() {
        return likes;
    }

    public void setLikes(long likes) {
        this.likes = likes;
    }

    public ProfileAddress getProfileAddress() {
        return profileAddress;
    }

    public void setProfileAddress(ProfileAddress profileAddress) {
        this.profileAddress = profileAddress;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Exclude
    @Nullable
    public String getGroupFlair() {
        return groupFlair;
    }

    @Exclude
    public void setGroupFlair(String groupFlair) {
        this.groupFlair = groupFlair;
    }

    public boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}
