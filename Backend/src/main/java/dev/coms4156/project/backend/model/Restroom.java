package dev.coms4156.project.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Restroom DTO for API responses and requests.
 */
@SuppressWarnings("PMD.DataClass")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Restroom {
  private Long id;
  private String name;
  private String address;
  private double latitude;
  private double longitude;
  private String hours;
  private List<String> amenities = new ArrayList<>();
  private double avgRating;
  private long visitCount;
  private List<EditProposal> pendingEdits = new ArrayList<>();

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

  public String getAddress() {
    return address;
  }

  public void setAddress(final String address) {
    this.address = address;
  }

  public double getLatitude() {
    return latitude;
  }

  public void setLatitude(final double latitude) {
    this.latitude = latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public void setLongitude(final double longitude) {
    this.longitude = longitude;
  }

  public String getHours() {
    return hours;
  }

  public void setHours(final String hours) {
    this.hours = hours;
  }

  public List<String> getAmenities() {
    return Collections.unmodifiableList(amenities);
  }

  public void setAmenities(final List<String> amenities) {
    this.amenities = amenities == null ? new ArrayList<>() : new ArrayList<>(amenities);
  }

  public double getAvgRating() {
    return avgRating;
  }

  public void setAvgRating(final double avgRating) {
    this.avgRating = avgRating;
  }

  public long getVisitCount() {
    return visitCount;
  }

  public void setVisitCount(final long visitCount) {
    this.visitCount = visitCount;
  }

  public List<EditProposal> getPendingEdits() {
    return pendingEdits;
  }

  public void setPendingEdits(final List<EditProposal> pendingEdits) {
    this.pendingEdits = pendingEdits;
  }
}
