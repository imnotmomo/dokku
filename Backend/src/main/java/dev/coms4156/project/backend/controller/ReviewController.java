package dev.coms4156.project.backend.controller;

import dev.coms4156.project.backend.model.Review;
import dev.coms4156.project.backend.model.ReviewRequest;
import dev.coms4156.project.backend.service.db.RestroomDbService;
import dev.coms4156.project.backend.service.db.ReviewDbService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
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

  private static final String ROLE_MEMBER_EXPRESSION =
      "hasAnyRole('USER','THIRD_PARTY_INTEGRATION','ADMIN')";
  private static final String ERROR_KEY = "error";

  private final ReviewDbService reviewDbService;
  private final RestroomDbService restroomDbService;

  /**
   * Constructor for DI.
   *
   * @param reviewDbService review database service
   * @param restroomDbService restroom database service
   */
  public ReviewController(
      @Autowired final ReviewDbService reviewDbService,
      @Autowired final RestroomDbService restroomDbService) {
    this.reviewDbService = reviewDbService;
    this.restroomDbService = restroomDbService;
  }

  /**
   * Create a review (auth required).
   */
  @PostMapping
  @PreAuthorize(ROLE_MEMBER_EXPRESSION)
  public ResponseEntity<?> addReview(
      @PathVariable final Long id,
      @RequestBody final ReviewRequest body,
      @AuthenticationPrincipal final OAuth2AuthenticatedPrincipal principal) {
    String reviewer = resolveUserIdentifier(principal);

    if (reviewer == null || reviewer.isBlank()) {
      return ResponseEntity.status(401).body(Map.of(ERROR_KEY, "Unable to resolve user subject"));
    }

    if (body.getRating() == null || body.getCleanliness() == null) {
      return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, "rating and cleanliness required"));
    }
    if (body.getRating() < 1 || body.getRating() > 5
        || body.getCleanliness() < 1 || body.getCleanliness() > 5) {
      return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, "rating/cleanliness must be 1-5"));
    }

    try {
      restroomDbService.getById(id)
          .orElseThrow(() -> new IllegalArgumentException("Restroom not found"));
      Review review = new Review();
      review.setRestroomId(id);
      review.setUserId(reviewer);
      review.setRating(body.getRating());
      review.setCleanliness(body.getCleanliness());
      review.setComment(body.getComment());
      review.setHelpfulVotes(0);
      Review r = reviewDbService.create(review);
      return ResponseEntity.status(201).body(r);
    } catch (Exception ex) {
      return ResponseEntity.status(404).body(Map.of(ERROR_KEY, ex.getMessage()));
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
  @PreAuthorize(ROLE_MEMBER_EXPRESSION)
  public ResponseEntity<?> list(@PathVariable final Long id,
                                @RequestParam(defaultValue = "recent") final String sort) {
    try {
      restroomDbService.getById(id)
          .orElseThrow(() -> new IllegalArgumentException("Restroom not found"));
      return ResponseEntity.ok(reviewDbService.getByRestroomId(id, sort));
    } catch (Exception ex) {
      return ResponseEntity.status(404).body(Map.of(ERROR_KEY, ex.getMessage()));
    }
  }

  private String resolveUserIdentifier(final OAuth2AuthenticatedPrincipal principal) {
    if (principal == null) {
      return null;
    }
    String subject = principal.getAttribute("sub");
    if (subject == null || subject.isBlank()) {
      subject = principal.getName();
    }
    if ((subject == null || subject.isBlank()) && principal.getAttribute("email") != null) {
      subject = principal.getAttribute("email");
    }
    return subject;
  }
}
