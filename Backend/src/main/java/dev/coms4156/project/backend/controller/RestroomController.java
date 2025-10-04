package dev.coms4156.project.backend.controller;

import dev.coms4156.project.backend.model.EditProposal;
import dev.coms4156.project.backend.model.Restroom;
import dev.coms4156.project.backend.model.User;
import dev.coms4156.project.backend.service.MockApiService;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Bathroom endpoints: submit, nearby, details, propose edit, visit.
 */
@RestController
@RequestMapping("/v1/bathrooms")
public class RestroomController {

  private final MockApiService svc;

  /**
   * Constructor for DI.
   * @param svc mock service
   */
  public RestroomController(final MockApiService svc) {
    this.svc = svc;
  }

  /**
   * Submit a new restroom (mock: accepted right away).
   * @param r restroom
   * @param auth optional auth (ignored here)
   * @return created restroom
   */
  @PostMapping
  public ResponseEntity<?> submit(@RequestBody final Restroom r,
                                  @RequestHeader(value = "Authorization", required = false)
                                  final String auth) {
    Restroom saved = svc.submitRestroom(r);
    return ResponseEntity.status(201).body(saved);
  }

  /**
   * Nearby search with optional filters.
   */
  @GetMapping("/nearby")
  public ResponseEntity<?> nearby(@RequestParam final double lat,
                                  @RequestParam final double lng,
                                  @RequestParam(defaultValue = "1500") final double radius,
                                  @RequestParam(required = false) final Boolean openNow,
                                  @RequestParam(required = false) final String amenities,
                                  @RequestParam(required = false) final Integer limit) {
    Set<String> amSet = null;
    if (amenities != null && !amenities.isBlank()) {
      amSet = Arrays.stream(amenities.split(","))
              .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
    }
    return ResponseEntity.ok(svc.getNearby(lat, lng, radius, openNow, amSet, limit));
  }

  /**
   * Bathroom details with top helpful reviews preview.
   */
  @GetMapping("/{id}")
  public ResponseEntity<?> details(@PathVariable final Long id) {
    try {
      Restroom r = svc.getRestroom(id);
      Map<String, Object> dto = new LinkedHashMap<>();
      dto.put("id", r.getId());
      dto.put("name", r.getName());
      dto.put("address", r.getAddress());
      dto.put("latitude", r.getLatitude());
      dto.put("longitude", r.getLongitude());
      dto.put("hours", r.getHours());
      dto.put("amenities", r.getAmenities());
      dto.put("avg_rating", r.getAvgRating());
      dto.put("visitCount", r.getVisitCount());
      dto.put("topReviews", svc.getReviews(id, "helpful").stream().limit(3).collect(Collectors.toList()));
      return ResponseEntity.ok(dto);
    } catch (NoSuchElementException ex) {
      return ResponseEntity.status(404).body(Map.of("error", ex.getMessage()));
    }
  }

  /**
   * Propose an edit to a restroom (auth required).
   */
  @PatchMapping("/{id}")
  public ResponseEntity<?> propose(@PathVariable final Long id,
                                   @RequestBody final EditProposal p,
                                   @RequestHeader(value = "Authorization", required = false)
                                   final String auth) {
    User u = getUserFromAuthHeader(auth);
    if (u == null) {
      return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
    }
    try {
      p.setProposerUserId(u.getUsername());
      EditProposal created = svc.proposeEdit(id, p);
      return ResponseEntity.status(202).body(created);
    } catch (NoSuchElementException ex) {
      return ResponseEntity.status(404).body(Map.of("error", ex.getMessage()));
    }
  }

  /**
   * Record a user visit (auth required).
   */
  @PostMapping("/{id}/visit")
  public ResponseEntity<?> visit(@PathVariable final Long id,
                                 @RequestHeader(value = "Authorization", required = false)
                                 final String auth) {
    User u = getUserFromAuthHeader(auth);
    if (u == null) {
      return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
    }
    try {
      return ResponseEntity.ok(svc.recordVisit(id));
    } catch (NoSuchElementException ex) {
      return ResponseEntity.status(404).body(Map.of("error", ex.getMessage()));
    }
  }

  private User getUserFromAuthHeader(final String auth) {
    if (auth == null || !auth.startsWith("Bearer ")) {
      return null;
    }
    String token = auth.substring("Bearer ".length()).trim();
    return svc.getUserFromToken(token);
  }
}