package dev.coms4156.project.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
  private String hoursJson;
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
    return hoursJson;
  }

  public void setHours(final String hours) {
    this.hoursJson = hours;
  }

  public String getHoursJson() {
    return hoursJson;
  }

  public void setHoursJson(final String hoursJson) {
    this.hoursJson = hoursJson;
  }

  public List<String> getAmenities() {
    return Collections.unmodifiableList(amenities);
  }

  public void setAmenities(final List<String> amenities) {
    this.amenities = amenities == null ? new ArrayList<>() : new ArrayList<>(amenities);
  }

  /**
   * Support binding amenities from either a JSON array or a comma-separated string.
   */
  @JsonSetter("amenities")
  public void setAmenitiesJson(final Object amenitiesJson) {
    if (amenitiesJson == null) {
      setAmenities(null);
      return;
    }
    if (amenitiesJson instanceof List<?> list) {
      List<String> normalized = list.stream()
          .map(Object::toString)
          .map(String::trim)
          .filter(s -> !s.isEmpty())
          .collect(Collectors.toList());
      setAmenities(normalized);
      return;
    }
    if (amenitiesJson instanceof String str) {
      List<String> normalized = str.isBlank()
          ? List.of()
          : Arrays.stream(str.split(","))
              .map(String::trim)
              .filter(s -> !s.isEmpty())
              .collect(Collectors.toList());
      setAmenities(normalized);
      return;
    }
    throw new IllegalArgumentException("Unsupported amenities format");
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
