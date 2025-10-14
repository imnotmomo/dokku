package dev.coms4156.project.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Unit tests for RouteController.
 */
public class RouteControllerUnitTests {

  private RouteController controller;

  @BeforeEach
  public void setUp() {
    controller = new RouteController();
  }

  @Test
  public void shouldReturnWelcomePageForRootPath() {
    ResponseEntity<String> response = controller.index();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(MediaType.TEXT_HTML, response.getHeaders().getContentType());
    assertNotNull(response.getBody());
  }

  @Test
  public void shouldContainGoogleLoginLink() {
    ResponseEntity<String> response = controller.index();
    String body = response.getBody();

    assertNotNull(body);
    assertTrue(body.contains("/oauth2/authorization/google"));
  }

  @Test
  public void shouldMentionCompanyConversionEndpoint() {
    ResponseEntity<String> response = controller.index();
    String body = response.getBody();

    assertNotNull(body);
    assertTrue(body.contains("/v1/companies"));
    assertTrue(body.contains("/v1/companies/{id}/assign"));
  }

  @Test
  public void shouldNotContainSensitiveInformation() {
    ResponseEntity<String> response = controller.index();
    String body = response.getBody();

    assertNotNull(body);

    assertFalse(body.toLowerCase().contains("password"));
    assertFalse(body.toLowerCase().contains("secret"));
    assertFalse(body.toLowerCase().contains("token"));
  }
}
