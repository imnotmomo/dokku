package dev.coms4156.project.backend.model;

import java.time.Instant;

/**
 * Represents a company account application.
 */
public class CompanyAccount {
  private Long id;
  private String subject;
  private String companyName;
  private String status;
  private Instant requestedAt;
  private Instant decidedAt;
  private String decidedBy;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getCompanyName() {
    return companyName;
  }

  public void setCompanyName(String companyName) {
    this.companyName = companyName;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Instant getRequestedAt() {
    return requestedAt;
  }

  public void setRequestedAt(Instant requestedAt) {
    this.requestedAt = requestedAt;
  }

  public Instant getDecidedAt() {
    return decidedAt;
  }

  public void setDecidedAt(Instant decidedAt) {
    this.decidedAt = decidedAt;
  }

  public String getDecidedBy() {
    return decidedBy;
  }

  public void setDecidedBy(String decidedBy) {
    this.decidedBy = decidedBy;
  }

  public boolean isApproved() {
    return "APPROVED".equalsIgnoreCase(status);
  }

  public boolean isPending() {
    return "PENDING".equalsIgnoreCase(status);
  }
}
