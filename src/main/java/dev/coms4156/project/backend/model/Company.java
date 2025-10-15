package dev.coms4156.project.backend.model;

import java.time.Instant;

/**
 * Company/Operator entity.
 */
@SuppressWarnings("PMD.DataClass")
public class Company {
  private Long id;
  private String name;
  private Instant createdAt;

  public Long getId() {
    return id;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(final Instant createdAt) {
    this.createdAt = createdAt;
  }
}
