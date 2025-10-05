package dev.coms4156.project.backend.controller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Returns info about the current authenticated user.
 */
@RestController
public class UserController {

  /**
   * Returns the calling user's profile derived from the Bearer token.
   *
   * @return user or 401
   */
  @GetMapping("/v1/me")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<?> me(
      @AuthenticationPrincipal final OAuth2AuthenticatedPrincipal principal) {
    String email = principal.getAttribute("email");
    String name = principal.getAttribute("name");
    boolean isAdmin = principal.getAuthorities().stream()
        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    Map<String, Object> profile = Map.of(
        "subject", principal.getName(),
        "email", email,
        "name", name,
        "hasRoleAdmin", isAdmin);
    return ResponseEntity.ok(profile);
  }
}
