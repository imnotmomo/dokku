package dev.coms4156.project.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * Routecontroller.
 */
@RestController
public class RouteController {

  @GetMapping({"/", "/index"})
  public String index() {
    return "Welcome! Try GET /v1/bathrooms/nearby?lat=40.7536&lng=-73.9832&radius=2000";
  }

}
