package dev.coms4156.project.backend.controller;

import dev.coms4156.project.backend.dto.RestroomCreateRequest;
import dev.coms4156.project.backend.dto.RestroomEditProposalRequest;
import dev.coms4156.project.backend.model.EditProposal;
import dev.coms4156.project.backend.model.Restroom;
import dev.coms4156.project.backend.service.db.EditProposalDbService;
import dev.coms4156.project.backend.service.db.RestroomDbService;
import dev.coms4156.project.backend.service.db.ReviewDbService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
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

  private static final String ROLE_MEMBER_EXPRESSION =
      "hasAnyRole('USER','THIRD_PARTY_INTEGRATION','ADMIN')";
  private static final String ERROR_KEY = "error";

  private final RestroomDbService restroomDbService;
  private final ReviewDbService reviewDbService;
  private final EditProposalDbService editProposalDbService;

  /**
   * Constructor for DI.
   *
   * @param restroomDbService database service implementation
   * @param reviewDbService review database service for getting top reviews
   * @param editProposalDbService edit proposal database service
   */
  public RestroomController(
      @Autowired final RestroomDbService restroomDbService,
      @Autowired final ReviewDbService reviewDbService,
      @Autowired final EditProposalDbService editProposalDbService) {
    this.restroomDbService = restroomDbService;
    this.reviewDbService = reviewDbService;
    this.editProposalDbService = editProposalDbService;
  }

  /**
   * Submit a new restroom to the database.
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
  @PreAuthorize(ROLE_MEMBER_EXPRESSION)
  public ResponseEntity<?> submit(@RequestBody final RestroomCreateRequest request) {
    if (request.getName() == null || request.getName().isBlank()
        || request.getLatitude() == null || request.getLongitude() == null) {
      return ResponseEntity.badRequest()
          .body(Map.of(ERROR_KEY, "name, latitude, and longitude are required"));
    }
    Restroom toCreate = new Restroom();
    toCreate.setName(request.getName());
    toCreate.setAddress(request.getAddress());
    toCreate.setLatitude(request.getLatitude());
    toCreate.setLongitude(request.getLongitude());
    toCreate.setHours(request.getHours());
    toCreate.setAmenities(request.getAmenities());
    Restroom saved = restroomDbService.create(toCreate);
    return ResponseEntity.status(201).body(saved);
  }

  /**
   * Nearby search with optional filters (auth required).
   */
  @Operation(
      summary = "Find nearby restrooms, login required",
      description = "Returns restrooms filtered by radius, open status, amenities, and limit.")
  @GetMapping("/nearby")
  @PreAuthorize(ROLE_MEMBER_EXPRESSION)
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
    return ResponseEntity.ok(
        restroomDbService.getNearby(lat, lng, radius, openNow, amSet, limit));
  }

  /**
   * Bathroom details with top helpful reviews preview (auth required).
   */
  @Operation(
      summary = "Get restroom details, login required",
      description = "Fetches metadata and up to three helpful reviews for the restroom identifier.")
  @GetMapping("/{id}")
  @PreAuthorize(ROLE_MEMBER_EXPRESSION)
  public ResponseEntity<?> details(@PathVariable final Long id) {
    try {
      Restroom r = restroomDbService.getById(id)
          .orElseThrow(() -> new NoSuchElementException("Restroom not found"));
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
      dto.put("topReviews", reviewDbService.getByRestroomId(id, "helpful")
          .stream().limit(3).collect(Collectors.toList()));
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
  @PreAuthorize(ROLE_MEMBER_EXPRESSION)
  public ResponseEntity<?> propose(
      @PathVariable final Long id,
      @RequestBody final RestroomEditProposalRequest request,
      @AuthenticationPrincipal final OAuth2AuthenticatedPrincipal principal) {
    try {
      restroomDbService.getById(id)
          .orElseThrow(() -> new NoSuchElementException("Restroom not found"));
      String subject = resolveUserIdentifier(principal);
      if (subject == null || subject.isBlank()) {
        return ResponseEntity.status(401).body(Map.of(ERROR_KEY, "Unable to resolve user subject"));
      }
      EditProposal proposal = new EditProposal();
      proposal.setRestroomId(id);
      proposal.setProposedName(request.getProposedName());
      proposal.setProposedAddress(request.getProposedAddress());
      proposal.setProposedHours(request.getProposedHours());
      proposal.setProposedAmenities(request.getProposedAmenities());
      proposal.setProposerUserId(subject);
      proposal.setStatus("PENDING");
      EditProposal created = editProposalDbService.create(proposal);
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
  @PreAuthorize(ROLE_MEMBER_EXPRESSION)
  public ResponseEntity<?> visit(
      @PathVariable final Long id,
      @AuthenticationPrincipal final OAuth2AuthenticatedPrincipal principal) {
    String subject = resolveUserIdentifier(principal);
    if (subject == null || subject.isBlank()) {
      return ResponseEntity.status(401).body(Map.of(ERROR_KEY, "Unable to resolve user subject"));
    }
    try {
      restroomDbService.getById(id)
          .orElseThrow(() -> new NoSuchElementException("Restroom not found"));
      restroomDbService.incrementVisitCount(id);
      Map<String, Object> response = new HashMap<>();
      response.put("restroomId", id);
      response.put("visitCount",
          restroomDbService.getById(id).map(Restroom::getVisitCount).orElse(0L));
      response.put("visitedAt", Instant.now().toString());
      return ResponseEntity.ok(response);
    } catch (NoSuchElementException ex) {
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
