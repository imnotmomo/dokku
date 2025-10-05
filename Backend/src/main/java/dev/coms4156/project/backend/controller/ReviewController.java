package dev.coms4156.project.backend.controller;

import dev.coms4156.project.backend.model.Review;
import dev.coms4156.project.backend.model.ReviewRequest;
import dev.coms4156.project.backend.service.MockApiService;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<?> addReview(
      @PathVariable final Long id,
      @RequestBody final ReviewRequest body,
      @AuthenticationPrincipal final OAuth2AuthenticatedPrincipal principal) {
    String reviewer = resolveUserIdentifier(principal);

    if (body.getRating() == null || body.getCleanliness() == null) {
      return ResponseEntity.badRequest().body(Map.of("error", "rating and cleanliness required"));
    }
    if (body.getRating() < 1 || body.getRating() > 5
        || body.getCleanliness() < 1 || body.getCleanliness() > 5) {
      return ResponseEntity.badRequest().body(Map.of("error", "rating/cleanliness must be 1-5"));
    }

    try {
      Review r = svc.addReview(id, reviewer, body.getRating(),
          body.getCleanliness(), body.getComment());
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

  private String resolveUserIdentifier(final OAuth2AuthenticatedPrincipal principal) {
    if (principal == null) {
      return "anonymous";
    }
    String email = principal.getAttribute("email");
    if (email != null && !email.isBlank()) {
      return email;
    }
    return principal.getName();
  }
}
