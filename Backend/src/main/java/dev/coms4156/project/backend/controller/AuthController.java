package dev.coms4156.project.backend.controller;

import dev.coms4156.project.backend.model.User;
import dev.coms4156.project.backend.service.MockApiService;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Mock authentication endpoints: signup, login, refresh.
 */
@RestController
@RequestMapping("/v1/auth")
public class AuthController {

  private final MockApiService svc;

  /**
   * Constructor for DI.
   * @param svc mock service
   */
  public AuthController(final MockApiService svc) {
    this.svc = svc;
  }

  /**
   * Sign up a new user.
   * @param body JSON with username, password
   * @return user with tokens
   */
  @PostMapping("/signup")
  public ResponseEntity<?> signup(@RequestBody final Map<String, String> body) {
    String username = body.get("username");
    String password = body.get("password");
    if (username == null || password == null) {
      return ResponseEntity.badRequest().body(Map.of("error", "username and password required"));
    }
    try {
      User u = svc.createUser(username, password, "USER");
      return ResponseEntity.ok(u);
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
  }

  /**
   * Login with credentials.
   * @param body JSON with username, password
   * @return user with new token
   */
  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody final Map<String, String> body) {
    try {
      User u = svc.login(body.get("username"), body.get("password"));
      return ResponseEntity.ok(u);
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.status(401).body(Map.of("error", ex.getMessage()));
    }
  }

  /**
   * Refresh an access token.
   * @param body JSON with refreshToken
   * @return user with rotated token
   */
  @PostMapping("/refresh")
  public ResponseEntity<?> refresh(@RequestBody final Map<String, String> body) {
    try {
      User u = svc.refresh(body.get("refreshToken"));
      return ResponseEntity.ok(u);
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.status(401).body(Map.of("error", ex.getMessage()));
    }
  }
}