package in.lubble.app.models.airtable_pojo;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class AirtableBooksFields implements Serializable {

    private static final long serialVersionUID = -11006212659862214L;
    @SerializedName("ListTime")
    private String listTime;
    @SerializedName("Title")
    private String title;
    @SerializedName("Author")
    private String author;
    @SerializedName("Owner")
    private String owner;
    @SerializedName("Borrower")
    private List<String> borrowerList;
    @SerializedName("Lubble")
    private String lubble;
    @SerializedName("isbn")
    private String isbn;
    @SerializedName("Photo")
    private String photo;

    public String getListTime() {
        return listTime;
    }

    public void setListTime(String listTime) {
        this.listTime = listTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getLubble() {
        return lubble;
    }

    public void setLubble(String lubble) {
        this.lubble = lubble;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public List<String> getBorrower() {
        return borrowerList;
    }

    public void setBorrower(List<String> borrowerList) {
        this.borrowerList = borrowerList;
    }
}
