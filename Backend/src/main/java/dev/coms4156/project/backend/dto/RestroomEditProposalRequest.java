package dev.coms4156.project.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Payload for proposing edits to an existing restroom.
 */
public class RestroomEditProposalRequest {

  @Schema(description = "Updated restroom name", example = "Updated Bathroom Name")
  private String proposedName;

  @Schema(description = "Updated address", example = "123 Main St, New York, NY")
  private String proposedAddress;

  @Schema(description = "Updated hours serialized as JSON",
      example = "{\"mon\":\"09:00-18:00\",\"tue\":\"09:00-18:00\"}")
  private String proposedHours;

  @Schema(description = "Updated amenities description",
      example = "Fully Accessible; Changing Table")
  private String proposedAmenities;

  public String getProposedName() {
    return proposedName;
  }

  public void setProposedName(String proposedName) {
    this.proposedName = proposedName;
  }

  public String getProposedAddress() {
    return proposedAddress;
  }

  public void setProposedAddress(String proposedAddress) {
    this.proposedAddress = proposedAddress;
  }

  public String getProposedHours() {
    return proposedHours;
  }

  public void setProposedHours(String proposedHours) {
    this.proposedHours = proposedHours;
  }

  public String getProposedAmenities() {
    return proposedAmenities;
  }

  public void setProposedAmenities(String proposedAmenities) {
    this.proposedAmenities = proposedAmenities;
  }
}
