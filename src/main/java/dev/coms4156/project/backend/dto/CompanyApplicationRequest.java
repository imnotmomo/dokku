package dev.coms4156.project.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request payload for company account applications.
 */
public class CompanyApplicationRequest {

  @Schema(description = "Display name of the applying company", example = "Example Partner Inc.")
  private String companyName;

  public String getCompanyName() {
    return companyName;
  }

  public void setCompanyName(String companyName) {
    this.companyName = companyName;
  }
}
