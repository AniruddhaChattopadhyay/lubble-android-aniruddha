package in.lubble.app.profile;

public class InstagramLoginState {
    public String insta_handle;
    //public String insta_pro_pic;
    public boolean insta_linked;
    public InstagramLoginState(String insta_handle, boolean insta_linked) {
        this.insta_handle = insta_handle;
        //this.insta_pro_pic = insta_pro_pic;
        this.insta_linked = insta_linked;
    }

    public String getInsta_handle() {
        return insta_handle;
    }

}
