package dev.coms4156.project.backend.controller;

import dev.coms4156.project.backend.model.EditProposal;
import dev.coms4156.project.backend.model.Restroom;
import dev.coms4156.project.backend.model.User;
import dev.coms4156.project.backend.service.MockApiService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Bathroom endpoints: submit, nearby, details, propose edit, visit.
 */
@RestController
@RequestMapping("/v1/bathrooms")
public class RestroomController {

  private final MockApiService svc;

  /**
   * Constructor for DI.
   *
   * @param svc mock service
   */
  public RestroomController(final MockApiService svc) {
    this.svc = svc;
  }

  /**
   * Submit a new restroom (mock: accepted right away).
   *
   * @param restroom minimal restroom payload
   * @param auth optional auth (ignored here)
   * @return created restroom
   */
  @Operation(
      summary = "Submit a new restroom",
      description = "Creates a restroom from the provided basics; other fields are auto-populated.")
  @ApiResponses({
      @ApiResponse(responseCode = "201",
          description = "Restroom created",
          content = @Content(schema = @Schema(implementation = Restroom.class))),
      @ApiResponse(responseCode = "400", description = "Invalid request payload")
  })
  @PostMapping
  public ResponseEntity<Restroom> submit(@RequestBody final Restroom restroom,
                                         @RequestHeader(value = "Authorization", required = false)
                                         final String auth) {
    Restroom toCreate = new Restroom();
    toCreate.setName(restroom.getName());
    toCreate.setAddress(restroom.getAddress());
    toCreate.setLatitude(restroom.getLatitude());
    toCreate.setLongitude(restroom.getLongitude());
    toCreate.setHours(restroom.getHours());
    toCreate.setAmenities(restroom.getAmenities());
    Restroom saved = svc.submitRestroom(toCreate);
    return ResponseEntity.status(201).body(saved);
  }

  /**
   * Nearby search with optional filters.
   */
  @Operation(
      summary = "Find nearby restrooms",
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
    return ResponseEntity.ok(svc.getNearby(lat, lng, radius, openNow, amSet, limit));
  }

  /**
   * Bathroom details with top helpful reviews preview.
   */
  @Operation(
      summary = "Get restroom details",
      description = "Fetches metadata and up to three helpful reviews for the restroom identifier.")
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
      dto.put("topReviews", svc.getReviews(id, "helpful").stream()
              .limit(3).collect(Collectors.toList()));
      return ResponseEntity.ok(dto);
    } catch (NoSuchElementException ex) {
      return ResponseEntity.status(404).body(Map.of("error", ex.getMessage()));
    }
  }

  /**
   * Propose an edit to a restroom (auth required).
   */
  @Operation(
      summary = "Propose restroom edits",
      description = "Submits restroom edit suggestions; requires a bearer token.")
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
  @Operation(
      summary = "Record a restroom visit",
      description = "Increments restroom visit metrics and requires a bearer token for the user.")
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
