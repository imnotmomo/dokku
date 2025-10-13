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
  public void shouldReturnWelcomePageForIndexPath() {
    //same method handles both "/" and "/index"
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
    assertTrue(body.contains("Login with Google"));
  }

  @Test
  public void shouldContainExampleApiLink() {
    ResponseEntity<String> response = controller.index();
    String body = response.getBody();

    assertNotNull(body);
    assertTrue(body.contains("/v1/bathrooms/nearby"));
    assertTrue(body.contains("lat=40.7536"));
    assertTrue(body.contains("lng=-73.9832"));
    assertTrue(body.contains("radius=2000"));
  }

  @Test
  public void shouldContainSwaggerUiLink() {
    ResponseEntity<String> response = controller.index();
    String body = response.getBody();

    assertNotNull(body);
    assertTrue(body.contains("/swagger-ui/index.html"));
    assertTrue(body.contains("Swagger UI"));
  }

  @Test
  public void shouldReturnValidHtmlContent() {
    ResponseEntity<String> response = controller.index();
    String body = response.getBody();

    assertNotNull(body);
    assertTrue(body.contains("<p>"));
    assertTrue(body.contains("</p>"));
    assertTrue(body.contains("<a href="));
    assertTrue(body.contains("Welcome!"));
  }

  @Test
  public void shouldContainAllExpectedSections() {
    ResponseEntity<String> response = controller.index();
    String body = response.getBody();

    assertNotNull(body);

    assertTrue(body.contains("Welcome!"));

    assertTrue(body.contains("Login with Google"));

    assertTrue(body.contains("Try the example query"));

    assertTrue(body.contains("visit"));
    assertTrue(body.contains("Swagger UI"));
  }

  @Test
  public void shouldHaveCorrectContentType() {
    ResponseEntity<String> response = controller.index();

    assertEquals(MediaType.TEXT_HTML, response.getHeaders().getContentType());
  }

  @Test
  public void shouldHaveCorrectStatusCode() {
    ResponseEntity<String> response = controller.index();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(200, response.getStatusCodeValue());
  }

  @Test
  public void shouldContainProperlyEncodedAmpersands() {
    ResponseEntity<String> response = controller.index();
    String body = response.getBody();

    assertNotNull(body);
    assertTrue(body.contains("&amp;"));
  }

  @Test
  public void shouldContainValidUrlParameters() {
    ResponseEntity<String> response = controller.index();
    String body = response.getBody();

    assertNotNull(body);

    //check that the example URL contains proper parameters
    assertTrue(body.contains("lat=40.7536"));
    assertTrue(body.contains("lng=-73.9832"));
    assertTrue(body.contains("radius=2000"));

    //parameters should be properly formatted
    assertTrue(body.contains("?lat=") || body.contains("&amp;lat="));
  }

  @Test
  public void shouldHaveConsistentResponse() {
    //test that multiple calls return the same content
    ResponseEntity<String> response1 = controller.index();
    ResponseEntity<String> response2 = controller.index();

    assertEquals(response1.getStatusCode(), response2.getStatusCode());
    assertEquals(response1.getHeaders().getContentType(), response2.getHeaders().getContentType());
    assertEquals(response1.getBody(), response2.getBody());
  }

  @Test
  public void shouldNotReturnNullBody() {
    ResponseEntity<String> response = controller.index();

    assertNotNull(response.getBody());
    assertFalse(response.getBody().isEmpty());
    assertFalse(response.getBody().isBlank());
  }

  @Test
  public void shouldContainValidHtmlStructure() {
    ResponseEntity<String> response = controller.index();
    String body = response.getBody();

    assertNotNull(body);

    //should have proper HTML paragraph structure
    int openParaTags = countOccurrences(body, "<p>");
    int closeParaTags = countOccurrences(body, "</p>");
    assertEquals(openParaTags, closeParaTags, "Opening and closing <p> tags should match");

    //should have proper HTML anchor structure
    int openAnchorTags = countOccurrences(body, "<a href=");
    int closeAnchorTags = countOccurrences(body, "</a>");
    assertEquals(openAnchorTags, closeAnchorTags, "Opening and closing <a> tags should match");
  }

  @Test
  public void shouldContainExpectedLinks() {
    ResponseEntity<String> response = controller.index();
    String body = response.getBody();

    assertNotNull(body);

    //should contain at least 3 links: Google OAuth, example query, Swagger UI
    int linkCount = countOccurrences(body, "<a href=");
    assertTrue(linkCount >= 3, "Should contain at least 3 links");
  }

  @Test
  public void shouldHaveReadableContent() {
    ResponseEntity<String> response = controller.index();
    String body = response.getBody();

    assertNotNull(body);

    //should contain user-friendly text
    assertTrue(body.toLowerCase().contains("welcome"));
    assertTrue(body.toLowerCase().contains("login"));
    assertTrue(body.toLowerCase().contains("try"));
    assertTrue(body.toLowerCase().contains("visit"));
  }

  @Test
  public void shouldContainBryantParkCoordinates() {
    ResponseEntity<String> response = controller.index();
    String body = response.getBody();

    assertNotNull(body);

    //example uses Bryant Park coordinates
    assertTrue(body.contains("40.7536")); //Bryant Park latitude
    assertTrue(body.contains("-73.9832")); //Bryant Park longitude
  }

  @Test
  public void shouldContainReasonableRadius() {
    ResponseEntity<String> response = controller.index();
    String body = response.getBody();

    assertNotNull(body);

    //should contain a reasonable search radius
    assertTrue(body.contains("2000"));
  }

  @Test
  public void shouldNotContainScriptTags() {
    ResponseEntity<String> response = controller.index();
    String body = response.getBody();

    assertNotNull(body);

    //should not contain any script tags (security)
    assertFalse(body.toLowerCase().contains("<script"));
    assertFalse(body.toLowerCase().contains("javascript:"));
  }

  @Test
  public void shouldNotContainSensitiveInformation() {
    ResponseEntity<String> response = controller.index();
    String body = response.getBody();

    assertNotNull(body);

    //should not contain any sensitive information
    assertFalse(body.toLowerCase().contains("password"));
    assertFalse(body.toLowerCase().contains("secret"));
    assertFalse(body.toLowerCase().contains("key"));
    assertFalse(body.toLowerCase().contains("token"));
  }

  //helper method to count occurrences of a substring
  private int countOccurrences(String text, String substring) {
    int count = 0;
    int index = 0;
    while ((index = text.indexOf(substring, index)) != -1) {
      count++;
      index += substring.length();
    }
    return count;
  }
}
