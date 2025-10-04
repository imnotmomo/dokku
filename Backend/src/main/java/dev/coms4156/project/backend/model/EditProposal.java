package dev.coms4156.project.backend.model;

import java.time.Instant;

/**
 * Edit proposal for restroom data.
 */
@SuppressWarnings("PMD.DataClass")
public class EditProposal {
    private Long id;
    private Long restroomId;
    private String proposedName;
    private String proposedAddress;
    private String proposedHours;
    private String proposedAmenities;
    private String proposerUserId;
    private String status; // PENDING, APPROVED, REJECTED
    private Instant createdAt;

    public Long getId() { return id; }
    public void setId(final Long id) { this.id = id; }

    public Long getRestroomId() { return restroomId; }
    public void setRestroomId(final Long restroomId) { this.restroomId = restroomId; }

    public String getProposedName() { return proposedName; }
    public void setProposedName(final String proposedName) { this.proposedName = proposedName; }

    public String getProposedAddress() { return proposedAddress; }
    public void setProposedAddress(final String proposedAddress) { this.proposedAddress = proposedAddress; }

    public String getProposedHours() { return proposedHours; }
    public void setProposedHours(final String proposedHours) { this.proposedHours = proposedHours; }

    public String getProposedAmenities() { return proposedAmenities; }
    public void setProposedAmenities(final String proposedAmenities) {
        this.proposedAmenities = proposedAmenities;
    }

    public String getProposerUserId() { return proposerUserId; }
    public void setProposerUserId(final String proposerUserId) { this.proposerUserId = proposerUserId; }

    public String getStatus() { return status; }
    public void setStatus(final String status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(final Instant createdAt) { this.createdAt = createdAt; }
}