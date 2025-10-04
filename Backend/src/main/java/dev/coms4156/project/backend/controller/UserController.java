package dev.coms4156.project.backend.controller;

import dev.coms4156.project.backend.model.User;
import dev.coms4156.project.backend.service.MockApiService;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * Returns info about the current authenticated user.
 */
@RestController
public class UserController {

  private final MockApiService svc;

  /**
   * Constructor for DI.
   *
   * @param svc mock service
   */
  public UserController(final MockApiService svc) {
    this.svc = svc;
  }

  /**
   * Returns the calling user's profile derived from the Bearer token.
   *
   * @param auth Authorization header
   * @return user or 401
   */
  @GetMapping("/v1/me")
  public ResponseEntity<?> me(
          @RequestHeader(value = "Authorization", required = false) final String auth) {
    User u = getUserFromAuthHeader(auth);
    if (u == null) {
      return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
    }
    return ResponseEntity.ok(u);
  }

  private User getUserFromAuthHeader(final String auth) {
    if (auth == null || !auth.startsWith("Bearer ")) {
      return null;
    }
    String token = auth.substring("Bearer ".length()).trim();
    return svc.getUserFromToken(token);
  }
}