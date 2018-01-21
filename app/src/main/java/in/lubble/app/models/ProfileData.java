package in.lubble.app.models;

import android.support.annotation.Nullable;

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
    @Nullable
    private List<PostData> postDataList;

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

    @Nullable
    public List<PostData> getPostDataList() {
        return postDataList;
    }

    public void setPostDataList(@Nullable List<PostData> postDataList) {
        this.postDataList = postDataList;
    }
}
