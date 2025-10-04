package dev.coms4156.project.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple landing route to verify the service is up.
 */
@RestController
public class RouteController {

  /**
   * Returns a short hint about how to try the API.
   * @return Welcome message
   */
  @GetMapping({"/", "/index"})
  public String index() {
    return "Welcome! Try: GET /v1/bathrooms/nearby?lat=40.7536&lng=-73.9832&radius=2000";
  }
}