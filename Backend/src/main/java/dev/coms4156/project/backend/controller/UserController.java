package dev.coms4156.project.backend.controller;

import dev.coms4156.project.backend.model.User;
import dev.coms4156.project.backend.service.MockApiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class UserController {

  private final MockApiService svc;

  public UserController(MockApiService svc) {
    this.svc = svc;
  }

  @GetMapping("/v1/me")
  public ResponseEntity<?> me(@RequestHeader(value = "Authorization", required = false) String auth) {
    User u = getUserFromAuthHeader(auth);
    if (u == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
    return ResponseEntity.ok(u);
  }

  private User getUserFromAuthHeader(String auth) {
    if (auth == null || !auth.startsWith("Bearer ")) return null;
    String token = auth.substring("Bearer ".length()).trim();
    return svc.getUserFromToken(token);
  }
}