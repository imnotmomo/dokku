package dev.coms4156.project.backend.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple landing route to verify the service is up.
 */
@RestController
public class RouteController {

  /**
   * Returns a short hint about how to try the API.
   *
   * @return Welcome message
   */
  @GetMapping({"/", "/index"})
  public ResponseEntity<String> index() {
    String body = """
        <p>Welcome!</p>
        <p><a href='/oauth2/authorization/google'>Login with Google to post/update a restroom or review</a></p>
        <p>Try the example query:
          <a href='/v1/bathrooms/nearby?lat=40.7536&amp;lng=-73.9832&amp;radius=2000'>
            GET /v1/bathrooms/nearby?lat=40.7536&amp;lng=-73.9832&amp;radius=2000
          </a>
        </p>
        <p>or visit <a href='/swagger-ui/index.html'>Swagger UI</a></p>
        """;
    return ResponseEntity.ok()
        .contentType(MediaType.TEXT_HTML)
        .body(body);
  }
}
