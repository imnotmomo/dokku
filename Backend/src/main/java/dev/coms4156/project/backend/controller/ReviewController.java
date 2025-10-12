package dev.coms4156.project.backend.controller;

import dev.coms4156.project.backend.model.Review;
import dev.coms4156.project.backend.model.ReviewRequest;
import dev.coms4156.project.backend.service.MockApiService;
import dev.coms4156.project.backend.service.db.ReviewDbService;
import dev.coms4156.project.backend.service.db.RestroomDbService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import java.util.Arrays;
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

  private static final String ROLE_USER_EXPRESSION = "hasRole('USER')";
  private static final String ERROR_KEY = "error";

  private final MockApiService mockService;
  private final ReviewDbService reviewDbService;
  private final RestroomDbService restroomDbService;
  private final boolean useMock;

  /**
   * Constructor for DI.
   *
   * @param mockService mock service implementation (optional)
   * @param reviewDbService review database service
   * @param restroomDbService restroom database service
   * @param env Spring environment to check active profile
   */
  public ReviewController(
      @Autowired(required = false) final MockApiService mockService,
      @Autowired(required = false) final ReviewDbService reviewDbService,
      @Autowired(required = false) final RestroomDbService restroomDbService,
      final Environment env) {
    this.mockService = mockService;
    this.reviewDbService = reviewDbService;
    this.restroomDbService = restroomDbService;
    this.useMock = mockService != null && Arrays.asList(env.getActiveProfiles()).contains("mock");
  }

  /**
   * Create a review (auth required).
   */
  @PostMapping
  @PreAuthorize(ROLE_USER_EXPRESSION)
  public ResponseEntity<?> addReview(
      @PathVariable final Long id,
      @RequestBody final ReviewRequest body,
      @AuthenticationPrincipal final OAuth2AuthenticatedPrincipal principal) {
    String reviewer = resolveUserIdentifier(principal);

    if (body.getRating() == null || body.getCleanliness() == null) {
      return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, "rating and cleanliness required"));
    }
    if (body.getRating() < 1 || body.getRating() > 5
        || body.getCleanliness() < 1 || body.getCleanliness() > 5) {
      return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, "rating/cleanliness must be 1-5"));
    }

    try {
      Review r;
      if (useMock) {
        r = mockService.addReview(id, reviewer, body.getRating(),
            body.getCleanliness(), body.getComment());
      } else {
        // Create review using database service
        Review review = new Review();
        review.setRestroomId(id);
        review.setUserId(reviewer);
        review.setRating(body.getRating());
        review.setCleanliness(body.getCleanliness());
        review.setComment(body.getComment());
        review.setHelpfulVotes(0);
        r = reviewDbService.create(review);
      }
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
  public ResponseEntity<?> list(@PathVariable final Long id,
                                @RequestParam(defaultValue = "recent") final String sort) {
    try {
      if (useMock) {
        return ResponseEntity.ok(mockService.getReviews(id, sort));
      } else {
        return ResponseEntity.ok(reviewDbService.getByRestroomId(id, sort));
      }
    } catch (Exception ex) {
      return ResponseEntity.status(404).body(Map.of(ERROR_KEY, ex.getMessage()));
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
