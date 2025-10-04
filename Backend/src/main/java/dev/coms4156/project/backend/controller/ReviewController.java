package dev.coms4156.project.backend.controller;

import dev.coms4156.project.backend.model.Review;
import dev.coms4156.project.backend.model.User;
import dev.coms4156.project.backend.service.MockApiService;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Review endpoints for a restroom.
 */
@RestController
@RequestMapping("/v1/bathrooms/{id}/reviews")
public class ReviewController {

  private final MockApiService svc;

  /**
   * Constructor for DI.
   *
   * @param svc mock service
   */
  public ReviewController(final MockApiService svc) {
    this.svc = svc;
  }

  /**
   * Create a review (auth required).
   */
  @PostMapping
  public ResponseEntity<?> addReview(@PathVariable final Long id,
                                     @RequestBody final Map<String, Object> body,
                                     @RequestHeader(value = "Authorization", required = false)
                                     final String auth) {
    User u = getUserFromAuthHeader(auth);
    if (u == null) {
      return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
    }

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
    } catch (Exception ex) {
      return ResponseEntity.status(404).body(Map.of("error", ex.getMessage()));
    }
  }

  /**
   * List reviews for a restroom.
   *
   * @param id restroom id
   * @param sort recent|helpful
   * @return list of reviews
   */
  @GetMapping
  public ResponseEntity<?> list(@PathVariable final Long id,
                                @RequestParam(defaultValue = "recent") final String sort) {
    try {
      return ResponseEntity.ok(svc.getReviews(id, sort));
    } catch (Exception ex) {
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