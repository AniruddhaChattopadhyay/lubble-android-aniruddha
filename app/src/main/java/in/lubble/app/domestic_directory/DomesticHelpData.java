package in.lubble.app.domestic_directory;

/**
 * Created by ishaan on 25/3/18.
 */

public class DomesticHelpData {

    private String name;
    private long phone = 0;
    private String category;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getPhone() {
        return phone;
    }

    public void setPhone(long phoneNo) {
        this.phone = phoneNo;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
