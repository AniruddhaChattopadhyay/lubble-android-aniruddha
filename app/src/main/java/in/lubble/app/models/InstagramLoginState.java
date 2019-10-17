package in.lubble.app.models;

public class InstagramLoginState {
    public String insta_handle;

    public InstagramLoginState() {

    }


    public InstagramLoginState(String insta_handle) {
        this.insta_handle = insta_handle;
    }

    public String getInsta_handle() {
        return insta_handle;
    }

    public void setInsta_handle(String insta_handle) {
        this.insta_handle = insta_handle;
    }

}