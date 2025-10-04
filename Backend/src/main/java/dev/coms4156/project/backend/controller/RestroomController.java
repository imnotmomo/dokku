package dev.coms4156.project.backend.controller;

import dev.coms4156.project.backend.model.EditProposal;
import dev.coms4156.project.backend.model.Restroom;
import dev.coms4156.project.backend.model.User;
import dev.coms4156.project.backend.service.MockApiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/bathrooms")
public class RestroomController {

  private final MockApiService svc;

  public RestroomController(MockApiService svc) {
    this.svc = svc;
  }

  // POST /v1/bathrooms — submit new bathroom (→ pending)
  @PostMapping
  public ResponseEntity<?> submit(@RequestBody Restroom r,
                                  @RequestHeader(value = "Authorization", required = false) String auth) {
    // Optional auth (allow anonymous submissions in mock)
    Restroom saved = svc.submitRestroom(r);
    return ResponseEntity.status(201).body(saved);
  }

  // GET /v1/bathrooms/nearby?lat=..&lng=..&radius=..&openNow=..&amenities=..&limit=..
  @GetMapping("/nearby")
  public ResponseEntity<?> nearby(@RequestParam double lat,
                                  @RequestParam double lng,
                                  @RequestParam(defaultValue = "1500") double radius,
                                  @RequestParam(required = false) Boolean openNow,
                                  @RequestParam(required = false) String amenities,
                                  @RequestParam(required = false) Integer limit) {

    Set<String> amSet = null;
    if (amenities != null && !amenities.isBlank()) {
      amSet = Arrays.stream(amenities.split(","))
              .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
    }
    return ResponseEntity.ok(svc.getNearby(lat, lng, radius, openNow, amSet, limit));
  }

  // GET /v1/bathrooms/{id}
  @GetMapping("/{id}")
  public ResponseEntity<?> details(@PathVariable Long id) {
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
      // include a couple of top reviews by helpfulness for preview
      dto.put("topReviews", svc.getReviews(id, "helpful").stream().limit(3).collect(Collectors.toList()));
      return ResponseEntity.ok(dto);
    } catch (NoSuchElementException e) {
      return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
    }
  }

  // PATCH /v1/bathrooms/{id} — propose edits (→ submission)
  @PatchMapping("/{id}")
  public ResponseEntity<?> propose(@PathVariable Long id,
                                   @RequestBody EditProposal p,
                                   @RequestHeader(value = "Authorization", required = false) String auth) {
    User u = getUserFromAuthHeader(auth);
    if (u == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
    try {
      p.setProposerUserId(u.getUsername());
      EditProposal created = svc.proposeEdit(id, p);
      return ResponseEntity.status(202).body(created);
    } catch (NoSuchElementException e) {
      return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
    }
  }

  // POST /v1/bathrooms/{id}/visit — record visit
  @PostMapping("/{id}/visit")
  public ResponseEntity<?> visit(@PathVariable Long id,
                                 @RequestHeader(value = "Authorization", required = false) String auth) {
    User u = getUserFromAuthHeader(auth);
    if (u == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
    try {
      return ResponseEntity.ok(svc.recordVisit(id));
    } catch (NoSuchElementException e) {
      return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
    }
  }

  private User getUserFromAuthHeader(String auth) {
    if (auth == null || !auth.startsWith("Bearer ")) return null;
    String token = auth.substring("Bearer ".length()).trim();
    return svc.getUserFromToken(token);
  }
}