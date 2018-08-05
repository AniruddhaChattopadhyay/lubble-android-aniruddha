package in.lubble.app.marketplace;

import com.google.gson.annotations.SerializedName;

public class RatingData {

    @SerializedName("id")
    private int ratingId;
    @SerializedName("rating")
    private int starRating;
    @SerializedName("review")
    private String review;

    public int getRatingId() {
        return ratingId;
    }

    public void setRatingId(int ratingId) {
        this.ratingId = ratingId;
    }

    public int getStarRating() {
        return starRating;
    }

    public void setStarRating(int starRating) {
        this.starRating = starRating;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }
}
