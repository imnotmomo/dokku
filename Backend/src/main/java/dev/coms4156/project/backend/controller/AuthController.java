package dev.coms4156.project.backend.controller;

import dev.coms4156.project.backend.model.User;
import dev.coms4156.project.backend.service.MockApiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {

  private final MockApiService svc;

  public AuthController(MockApiService svc) {
    this.svc = svc;
  }

  @PostMapping("/signup")
  public ResponseEntity<?> signup(@RequestBody Map<String, String> body) {
    String username = body.get("username");
    String password = body.get("password");
    if (username == null || password == null) {
      return ResponseEntity.badRequest().body(Map.of("error", "username and password required"));
    }
    try {
      User u = svc.createUser(username, password, "USER");
      return ResponseEntity.ok(u);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
    try {
      User u = svc.login(body.get("username"), body.get("password"));
      return ResponseEntity.ok(u);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
    }
  }

  @PostMapping("/refresh")
  public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {
    try {
      User u = svc.refresh(body.get("refreshToken"));
      return ResponseEntity.ok(u);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
    }
  }
}