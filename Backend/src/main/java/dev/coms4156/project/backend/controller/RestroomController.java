package dev.coms4156.project.backend.controller;

import dev.coms4156.project.backend.model.EditProposal;
import dev.coms4156.project.backend.model.Restroom;
import dev.coms4156.project.backend.service.MockApiService;
import dev.coms4156.project.backend.service.db.RestroomDbService;
import dev.coms4156.project.backend.service.db.ReviewDbService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import java.time.Instant;
import java.util.HashMap;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Bathroom endpoints: submit, nearby, details, propose edit, visit.
 */
@RestController
@RequestMapping("/v1/bathrooms")
public class RestroomController {

  private static final String ROLE_USER_EXPRESSION = "hasRole('USER')";
  private static final String ERROR_KEY = "error";

  private final MockApiService mockService;
  private final RestroomDbService dbService;
  private final ReviewDbService reviewDbService;
  private final boolean useMock;

  /**
   * Constructor for DI.
   *
   * @param mockService mock service implementation (optional)
   * @param dbService database service implementation
   * @param reviewDbService review database service for getting top reviews
   * @param env Spring environment to check active profile
   */
  public RestroomController(
      @Autowired(required = false) final MockApiService mockService,
      @Autowired(required = false) final RestroomDbService dbService,
      @Autowired(required = false) final ReviewDbService reviewDbService,
      final Environment env) {
    this.mockService = mockService;
    this.dbService = dbService;
    this.reviewDbService = reviewDbService;
    this.useMock = mockService != null && Arrays.asList(env.getActiveProfiles()).contains("mock");
  }

  /**
   * Submit a new restroom (mock: accepted right away).
   *
   * @param restroom minimal restroom payload
   * @return created restroom
   */
  @Operation(
      summary = "Submit a new restroom, login required",
      description = "Creates a restroom from the provided basics; other fields are auto-populated.")
  @ApiResponses({
      @ApiResponse(responseCode = "201",
          description = "Restroom created",
          content = @Content(schema = @Schema(implementation = Restroom.class))),
      @ApiResponse(responseCode = "400", description = "Invalid request payload")
  })
  @PostMapping
  @PreAuthorize(ROLE_USER_EXPRESSION)
  public ResponseEntity<Restroom> submit(@RequestBody final Restroom restroom) {
    Restroom toCreate = new Restroom();
    toCreate.setName(restroom.getName());
    toCreate.setAddress(restroom.getAddress());
    toCreate.setLatitude(restroom.getLatitude());
    toCreate.setLongitude(restroom.getLongitude());
    toCreate.setHours(restroom.getHours());
    toCreate.setAmenities(restroom.getAmenities());
    Restroom saved = useMock ? mockService.submitRestroom(toCreate) : dbService.create(toCreate);
    return ResponseEntity.status(201).body(saved);
  }

  /**
   * Nearby search with optional filters.
   */
  @Operation(
      summary = "Find nearby restrooms, no login needed",
      description = "Returns restrooms filtered by radius, open status, amenities, and limit.")
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
    if (useMock) {
      return ResponseEntity.ok(mockService.getNearby(lat, lng, radius, openNow, amSet, limit));
    } else {
      return ResponseEntity.ok(dbService.getNearby(lat, lng, radius, openNow, amSet, limit));
    }
  }

  /**
   * Bathroom details with top helpful reviews preview.
   */
  @Operation(
      summary = "Get restroom details, no login needed",
      description = "Fetches metadata and up to three helpful reviews for the restroom identifier.")
  @GetMapping("/{id}")
  public ResponseEntity<?> details(@PathVariable final Long id) {
    try {
      Restroom r = useMock ? mockService.getRestroom(id) : dbService.getById(id).orElseThrow(() -> new NoSuchElementException("Restroom not found"));
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
      dto.put("topReviews", useMock 
              ? mockService.getReviews(id, "helpful").stream().limit(3).collect(Collectors.toList())
              : reviewDbService.getByRestroomId(id, "helpful").stream().limit(3).collect(Collectors.toList()));
      return ResponseEntity.ok(dto);
    } catch (NoSuchElementException ex) {
      return ResponseEntity.status(404).body(Map.of(ERROR_KEY, ex.getMessage()));
    }
  }

  /**
   * Propose an edit to a restroom (auth required).
   */
  @Operation(
      summary = "Propose restroom edits",
      description = "Submit restroom edit suggestions, need a user token.")
  @PatchMapping("/{id}")
  @PreAuthorize(ROLE_USER_EXPRESSION)
  public ResponseEntity<?> propose(
      @PathVariable final Long id,
      @RequestBody final EditProposal p,
      @AuthenticationPrincipal final OAuth2AuthenticatedPrincipal principal) {
    try {
      p.setProposerUserId(resolveUserIdentifier(principal));
      EditProposal created = useMock ? mockService.proposeEdit(id, p) : null;
      return ResponseEntity.status(202).body(created);
    } catch (NoSuchElementException ex) {
      return ResponseEntity.status(404).body(Map.of(ERROR_KEY, ex.getMessage()));
    }
  }

  /**
   * Record a user visit (auth required).
   */
  @Operation(
      summary = "Record a restroom visit",
      description = "Increments restroom visit counts, need a user token.")
  @PostMapping("/{id}/visit")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<?> visit(
      @PathVariable final Long id,
      @AuthenticationPrincipal final OAuth2AuthenticatedPrincipal principal) {
    resolveUserIdentifier(principal);
    try {
      if (useMock) {
        return ResponseEntity.ok(mockService.recordVisit(id));
      } else {
        dbService.incrementVisitCount(id);
        Map<String, Object> response = new HashMap<>();
        response.put("restroomId", id);
        response.put("visitCount", dbService.getById(id).map(Restroom::getVisitCount).orElse(0L));
        response.put("visitedAt", Instant.now().toString());
        return ResponseEntity.ok(response);
      }
    } catch (NoSuchElementException ex) {
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
