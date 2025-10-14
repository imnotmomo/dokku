package dev.coms4156.project.backend.controller;

import dev.coms4156.project.backend.model.Company;
import dev.coms4156.project.backend.service.db.CompanyDbService;
import dev.coms4156.project.backend.service.db.UserDbService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Company lookup endpoints.
 */
@RestController
@RequestMapping("/v1/companies")
public class CompanyController {

  private final CompanyDbService companyDbService;
  private final UserDbService userDbService;

  @Autowired
  public CompanyController(CompanyDbService companyDbService, UserDbService userDbService) {
    this.companyDbService = companyDbService;
    this.userDbService = userDbService;
  }

  /**
   * List all companies or search by partial name (case-insensitive).
   */
  @GetMapping
  public ResponseEntity<?> list(@RequestParam(value = "name", required = false) String name) {
    if (name == null || name.isBlank()) {
      List<Company> companies = companyDbService.findAll();
      return ResponseEntity.ok(companies);
    }
    List<Company> matches = companyDbService.searchByName(name);
    return ResponseEntity.ok(matches);
  }

  /**
   * Lookup company by identifier.
   */
  @GetMapping("/{id}")
  public ResponseEntity<?> getById(@PathVariable Long id) {
    return companyDbService.findById(id)
        .<ResponseEntity<?>>map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  /**
   * Allows an authenticated user to associate their account with a company.
   */
  @PostMapping("/{id}/assign")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<?> assignCompany(@PathVariable Long id,
                                         @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal) {
    Optional<Company> company = companyDbService.findById(id);
    if (company.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    String subject = resolveSubject(principal);
    if (subject == null || subject.isBlank()) {
      return ResponseEntity.status(401).body(Map.of("error", "Unable to resolve user subject"));
    }
    userDbService.assignCompany(subject, id);
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
