package dev.coms4156.project.backend.controller;

import dev.coms4156.project.backend.model.User;
import dev.coms4156.project.backend.service.db.UserDbService;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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

  private static final String ROLE_USER_EXPRESSION = "hasRole('USER')";
  private final UserDbService userDbService;

  public UserController(UserDbService userDbService) {
    this.userDbService = userDbService;
  }

  /**
   * Returns the calling user's profile derived from the Bearer token.
   *
   * @return user or 401
   */
  @GetMapping("/v1/me")
  @PreAuthorize(ROLE_USER_EXPRESSION)
  public ResponseEntity<?> me(
      @AuthenticationPrincipal final OAuth2AuthenticatedPrincipal principal) {
    String subject = resolveSubject(principal);
    if (subject == null || subject.isBlank()) {
      return ResponseEntity.status(401).body(Map.of("error", "Unable to resolve user subject"));
    }
    Optional<User> dbUser = userDbService.findBySubject(subject);
    if (dbUser.isPresent()) {
      User user = dbUser.get();
      return ResponseEntity.ok(Map.of(
          "subject", user.getSubject(),
          "email", user.getEmail(),
          "name", user.getDisplayName(),
          "roles", user.getRoles()
      ));
    }
    String email = principal.getAttribute("email");
    String name = principal.getAttribute("name");
    Set<String> roles = principal.getAuthorities().stream()
        .map(auth -> auth.getAuthority())
        .collect(Collectors.toSet());
    return ResponseEntity.ok(Map.of(
        "subject", subject,
        "email", email,
        "name", name,
        "roles", roles
    ));
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
