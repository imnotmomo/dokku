package dev.coms4156.project.backend.controller;

import dev.coms4156.project.backend.dto.CompanyApplicationRequest;
import dev.coms4156.project.backend.model.CompanyAccount;
import dev.coms4156.project.backend.service.db.CompanyAccountDbService;
import dev.coms4156.project.backend.service.db.UserDbService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints for managing company account applications.
 */
@RestController
@RequestMapping("/v1/companies")
public class CompanyController {

  private static final String ROLE_USER = "hasRole('USER')";
  private static final String ROLE_ADMIN = "hasRole('ADMIN')";
  private static final String ROLE_MEMBER = "hasAnyRole('USER','THIRD_PARTY_INTEGRATION','ADMIN')";

  private final CompanyAccountDbService companyAccountDbService;
  private final UserDbService userDbService;

  @Autowired
  public CompanyController(CompanyAccountDbService companyAccountDbService,
                           UserDbService userDbService) {
    this.companyAccountDbService = companyAccountDbService;
    this.userDbService = userDbService;
  }

  /**
   * Submit or update a pending third-party application for the authenticated user.
   *
   * @param payload request payload containing the company name
   * @param principal authenticated principal
   * @return 202 with application or error response
   */
  @Operation(summary = "Apply for company access",
      description = "Creates or replaces a pending company application for the current user.")
  @ApiResponses({
      @ApiResponse(responseCode = "202", description = "Application recorded",
          content = @Content(schema = @Schema(implementation = CompanyAccount.class))),
      @ApiResponse(responseCode = "400", description = "Missing or invalid company name"),
      @ApiResponse(responseCode = "401", description = "User not authenticated")
  })
  @PostMapping("/apply")
  @PreAuthorize(ROLE_USER)
  public ResponseEntity<?> apply(@RequestBody CompanyApplicationRequest payload,
                                 @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal) {
    String companyName = payload == null ? null : payload.getCompanyName();
    if (companyName == null || companyName.isBlank()) {
      return ResponseEntity.badRequest().body(Map.of("error", "companyName is required"));
    }
    companyName = companyName.trim();
    String subject = resolveSubject(principal);
    if (subject == null || subject.isBlank()) {
      return ResponseEntity.status(401).body(Map.of("error", "Unable to resolve user subject"));
    }
    CompanyAccount account = companyAccountDbService.createPending(subject, companyName);
    return ResponseEntity.accepted().body(account);
  }

  /**
   * Retrieve the current user's application if one has been submitted.
   *
   * @param principal authenticated principal
   * @return application or empty response
   */
  @Operation(summary = "Get my company application",
      description = "Returns the current user's company application, if any.")
  @GetMapping("/me")
  @PreAuthorize(ROLE_MEMBER)
  public ResponseEntity<?> myApplication(
      @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal) {
    String subject = resolveSubject(principal);
    if (subject == null || subject.isBlank()) {
      return ResponseEntity.status(401).body(Map.of("error", "Unable to resolve user subject"));
    }
    return companyAccountDbService.findBySubject(subject)
        .<ResponseEntity<?>>map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.noContent().build());
  }

  /**
   * List third-party applications, restricted to approved entries for non-admin users.
   *
   * @param principal authenticated principal
   * @return list of applications visible to the caller
   */
  @Operation(summary = "List company applications",
      description = "Lists approved companies for general users. Admins receive all applications.")
  @GetMapping
  @PreAuthorize(ROLE_MEMBER)
  public ResponseEntity<?> list(@AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal) {
    boolean isAdmin = principal != null && principal.getAuthorities().stream()
        .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));

    List<CompanyAccount> accounts = isAdmin
        ? companyAccountDbService.findAll()
        : companyAccountDbService.findByStatus("APPROVED");
    return ResponseEntity.ok(accounts);
  }

  /**
   * Approve an application and promote the applicant to third-party integration role.
   *
   * @param id application identifier
   * @param principal authenticated administrator
   * @return 204 empty response on success or 404 if not found
   */
  @Operation(summary = "Approve company application",
      description = "Admin only: approves an application and grants company role.")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Application approved"),
      @ApiResponse(responseCode = "404", description = "Application not found")
  })
  @PostMapping("/{id}/approve")
  @PreAuthorize(ROLE_ADMIN)
  public ResponseEntity<?> approve(
      @PathVariable Long id,
      @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal) {
    Optional<CompanyAccount> accountOpt = companyAccountDbService.findById(id);
    if (accountOpt.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    CompanyAccount account = accountOpt.get();
    companyAccountDbService.updateStatus(id, "APPROVED", resolveSubject(principal));
    Set<String> roles = new LinkedHashSet<>(userDbService.getRoles(account.getSubject()));
    roles.remove("USER");
    roles.add("THIRD_PARTY_INTEGRATION");
    userDbService.replaceRoles(account.getSubject(), roles);
    return ResponseEntity.noContent().build();
  }

  /**
   * Deny an application and revert the applicant to regular user status.
   *
   * @param id application identifier
   * @param principal authenticated administrator
   * @return 204 empty response on success or 404 if not found
   */
  @Operation(summary = "Deny company application",
      description = "Admin only: denies an application and restores USER role.")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Application denied"),
      @ApiResponse(responseCode = "404", description = "Application not found")
  })
  @PostMapping("/{id}/deny")
  @PreAuthorize(ROLE_ADMIN)
  public ResponseEntity<?> deny(
      @PathVariable Long id,
      @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal) {
    Optional<CompanyAccount> accountOpt = companyAccountDbService.findById(id);
    if (accountOpt.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    CompanyAccount account = accountOpt.get();
    companyAccountDbService.updateStatus(id, "DENIED", resolveSubject(principal));
    Set<String> roles = new LinkedHashSet<>(userDbService.getRoles(account.getSubject()));
    roles.remove("THIRD_PARTY_INTEGRATION");
    roles.add("USER");
    userDbService.replaceRoles(account.getSubject(), roles);
    return ResponseEntity.noContent().build();
  }

  private String resolveSubject(OAuth2AuthenticatedPrincipal principal) {
    if (principal == null) {
      return null;
    }
    String subject = principal.getAttribute("sub");
    if (subject == null || subject.isBlank()) {
      subject = principal.getName();
    }
    return subject;
  }
}
