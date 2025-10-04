package dev.coms4156.project.backend.model;

import java.time.Instant;

/**
 * Review DTO.
 */
@SuppressWarnings("PMD.DataClass")
public class Review {
  private Long id;
  private Long restroomId;
  private String userId;
  private int rating;
  private int cleanliness;
  private String comment;
  private int helpfulVotes;
  private Instant createdAt;

  public Long getId() {
    return id;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public Long getRestroomId() {
    return restroomId;
  }

  public void setRestroomId(final Long restroomId) {
    this.restroomId = restroomId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(final String userId) {
    this.userId = userId;
  }

  public int getRating() {
    return rating;
  }

  public void setRating(final int rating) {
    this.rating = rating;
  }

  public int getCleanliness() {
    return cleanliness;
  }

  public void setCleanliness(final int cleanliness) {
    this.cleanliness = cleanliness;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(final String comment) {
    this.comment = comment;
  }

  public int getHelpfulVotes() {
    return helpfulVotes;
  }

  public void setHelpfulVotes(final int helpfulVotes) {
    this.helpfulVotes = helpfulVotes;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(final Instant createdAt) {
    this.createdAt = createdAt;
  }
}