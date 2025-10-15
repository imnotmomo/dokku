package dev.coms4156.project.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;

/**
 * Payload for creating a new restroom; excludes server-managed fields such as id and ratings.
 */
public class RestroomCreateRequest {

  @Schema(description = "Restroom name", example = "Name of the bathroom", required = true)
  private String name;

  @Schema(description = "Street address or descriptor", example = "Address of the bathroom")
  private String address;

  @Schema(description = "Latitude in decimal degrees", example = "40.7128", required = true)
  private Double latitude;

  @Schema(description = "Longitude in decimal degrees", example = "-74.0060", required = true)
  private Double longitude;

  @Schema(description = "Operating hours serialized as JSON",
      example = "{\"mon\":\"09:00-17:00\",\"tue\":\"09:00-17:00\",\"wed\":\"09:00-17:00\","
          + "\"thu\":\"09:00-17:00\",\"fri\":\"09:00-17:00\",\"sat\":\"09:00-17:00\","
          + "\"sun\":\"09:00-17:00\"}")
  private String hours;

  @Schema(description = "List of amenities",
      example = "[\"Amenities 1\", \"Amenities 2 etc.\"]")
  private List<String> amenities = new ArrayList<>();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public Double getLatitude() {
    return latitude;
  }

  public void setLatitude(Double latitude) {
    this.latitude = latitude;
  }

  public Double getLongitude() {
    return longitude;
  }

  public void setLongitude(Double longitude) {
    this.longitude = longitude;
  }

  public String getHours() {
    return hours;
  }

  public void setHours(String hours) {
    this.hours = hours;
  }

  public List<String> getAmenities() {
    return amenities;
  }

  public void setAmenities(List<String> amenities) {
    this.amenities = amenities == null ? new ArrayList<>() : new ArrayList<>(amenities);
  }
}
