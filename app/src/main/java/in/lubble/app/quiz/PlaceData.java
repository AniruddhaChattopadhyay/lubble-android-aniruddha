package in.lubble.app.quiz;

public class PlaceData {

    private Long ambience;
    private Long budget;
    private Long cuisine;
    private String name;
    private String pic;
    private String type;
    private double distance;
    private float rating;

    public Long getAmbience() {
        return ambience;
    }

    public void setAmbience(Long ambience) {
        this.ambience = ambience;
    }

    public Long getBudget() {
        return budget;
    }

    public void setBudget(Long budget) {
        this.budget = budget;
    }

    public Long getCuisine() {
        return cuisine;
    }

    public void setCuisine(Long cuisine) {
        this.cuisine = cuisine;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getDistanceString() {
        return String.valueOf(Math.round(this.distance)) + "m";
    }

}
