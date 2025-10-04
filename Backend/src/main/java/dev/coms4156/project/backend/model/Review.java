package dev.coms4156.project.backend.model;

import java.time.Instant;

public class Review {
    private Long id;
    private Long restroomId;
    private String userId;   // username/email from mock auth
    private int rating;      // 1-5
    private int cleanliness; // 1-5
    private String comment;
    private int helpfulVotes;
    private Instant createdAt;

    public Review() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRestroomId() { return restroomId; }
    public void setRestroomId(Long restroomId) { this.restroomId = restroomId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public int getCleanliness() { return cleanliness; }
    public void setCleanliness(int cleanliness) { this.cleanliness = cleanliness; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public int getHelpfulVotes() { return helpfulVotes; }
    public void setHelpfulVotes(int helpfulVotes) { this.helpfulVotes = helpfulVotes; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}