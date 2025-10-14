package dev.coms4156.project.backend.model;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * User DTO sourced from OAuth profiles and persisted in the database.
 */
@SuppressWarnings("PMD.DataClass")
public class User {
  private String subject;
  private String email;
  private String displayName;
  private String pictureUrl;
  private Instant lastLoginAt;
  private Instant createdAt;
  private Instant updatedAt;
  private Set<String> roles = new LinkedHashSet<>();

  public String getSubject() {
    return subject;
  }

  public void setSubject(final String subject) {
    this.subject = subject;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(final String email) {
    this.email = email;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(final String displayName) {
    this.displayName = displayName;
  }

  public String getPictureUrl() {
    return pictureUrl;
  }

  public void setPictureUrl(final String pictureUrl) {
    this.pictureUrl = pictureUrl;
  }

  public Instant getLastLoginAt() {
    return lastLoginAt;
  }

  public void setLastLoginAt(final Instant lastLoginAt) {
    this.lastLoginAt = lastLoginAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(final Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(final Instant updatedAt) {
    this.updatedAt = updatedAt;
  }

  public Set<String> getRoles() {
    return roles;
  }

  public void setRoles(final Set<String> roles) {
    this.roles = roles;
  }
}
