package dev.coms4156.project.backend.model;

/**
 * Request DTO for creating a review.
 */
public class ReviewRequest {
  private Integer rating;
  private Integer cleanliness;
  private String comment;

  public Integer getRating() {
    return rating;
  }

  public void setRating(Integer rating) {
    this.rating = rating;
  }

  public Integer getCleanliness() {
    return cleanliness;
  }

  public void setCleanliness(Integer cleanliness) {
    this.cleanliness = cleanliness;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }
}
