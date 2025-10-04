package dev.coms4156.project.backend.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Restroom.
 */
public class Restroom {
    private Long id;
    private String name;
    private String address;
    private double latitude;
    private double longitude;
    // hours in "HH:mm-HH:mm" 24h format (e.g., "08:00-20:00" or "00:00-23:59" for 24h)
    private String hours;
    // comma-separated for mock (e.g., "wheelchair,family,changing_table")
    private String amenities;
    private double avgRating;    // computed from reviews
    private long visitCount;     // simple popularity metric

    // not persisted externally; used in details or moderation
    private List<EditProposal> pendingEdits = new ArrayList<>();

    public Restroom() {}

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getHours() { return hours; }
    public void setHours(String hours) { this.hours = hours; }

    public String getAmenities() { return amenities; }
    public void setAmenities(String amenities) { this.amenities = amenities; }

    public double getAvgRating() { return avgRating; }
    public void setAvgRating(double avgRating) { this.avgRating = avgRating; }

    public long getVisitCount() { return visitCount; }
    public void setVisitCount(long visitCount) { this.visitCount = visitCount; }

    public List<EditProposal> getPendingEdits() { return pendingEdits; }
    public void setPendingEdits(List<EditProposal> pendingEdits) { this.pendingEdits = pendingEdits; }
}
