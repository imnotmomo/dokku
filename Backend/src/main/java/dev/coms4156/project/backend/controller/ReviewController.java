package dev.coms4156.project.backend.controller;

import dev.coms4156.project.backend.model.Review;
import dev.coms4156.project.backend.model.User;
import dev.coms4156.project.backend.service.MockApiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/bathrooms/{id}/reviews")
public class ReviewController {

  private final MockApiService svc;

  public ReviewController(MockApiService svc) {
    this.svc = svc;
  }

  // POST /v1/bathrooms/{id}/reviews — {rating(1-5), cleanliness(1-5), comment}
  @PostMapping
  public ResponseEntity<?> addReview(@PathVariable Long id,
                                     @RequestBody Map<String, Object> body,
                                     @RequestHeader(value = "Authorization", required = false) String auth) {
    User u = getUserFromAuthHeader(auth);
    if (u == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

    Integer rating = (Integer) body.get("rating");
    Integer cleanliness = (Integer) body.get("cleanliness");
    String comment = (String) body.get("comment");

    if (rating == null || cleanliness == null) {
      return ResponseEntity.badRequest().body(Map.of("error", "rating and cleanliness required"));
    }
    if (rating < 1 || rating > 5 || cleanliness < 1 || cleanliness > 5) {
      return ResponseEntity.badRequest().body(Map.of("error", "rating/cleanliness must be 1-5"));
    }

    try {
      Review r = svc.addReview(id, u.getUsername(), rating, cleanliness, comment);
      return ResponseEntity.status(201).body(r);
    } catch (Exception e) {
      return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
    }
  }

  // GET /v1/bathrooms/{id}/reviews?sort=recent|helpful
  @GetMapping
  public ResponseEntity<?> list(@PathVariable Long id,
                                @RequestParam(defaultValue = "recent") String sort) {
    try {
      return ResponseEntity.ok(svc.getReviews(id, sort));
    } catch (Exception e) {
      return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
    }
  }

  private User getUserFromAuthHeader(String auth) {
    if (auth == null || !auth.startsWith("Bearer ")) return null;
    String token = auth.substring("Bearer ".length()).trim();
    return svc.getUserFromToken(token);
  }
}