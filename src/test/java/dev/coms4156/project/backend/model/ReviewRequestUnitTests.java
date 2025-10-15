package dev.coms4156.project.backend.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for ReviewRequest model class.
 */
public class ReviewRequestUnitTests {

  @Test
  public void shouldSetAndGetRating() {
    ReviewRequest request = new ReviewRequest();

    request.setRating(5);

    assertEquals(5, request.getRating());
  }

  @Test
  public void shouldSetAndGetCleanliness() {
    ReviewRequest request = new ReviewRequest();

    request.setCleanliness(4);

    assertEquals(4, request.getCleanliness());
  }

  @Test
  public void shouldSetAndGetComment() {
    ReviewRequest request = new ReviewRequest();
    String expectedComment = "Very clean and well-maintained restroom!";

    request.setComment(expectedComment);

    assertEquals(expectedComment, request.getComment());
  }

  @Test
  public void shouldHandleNullRating() {
    ReviewRequest request = new ReviewRequest();

    request.setRating(null);

    assertNull(request.getRating());
  }

  @Test
  public void shouldHandleNullCleanliness() {
    ReviewRequest request = new ReviewRequest();

    request.setCleanliness(null);

    assertNull(request.getCleanliness());
  }

  @Test
  public void shouldHandleNullComment() {
    ReviewRequest request = new ReviewRequest();

    request.setComment(null);

    assertNull(request.getComment());
  }

  @Test
  public void shouldHandleEmptyComment() {
    ReviewRequest request = new ReviewRequest();

    request.setComment("");

    assertEquals("", request.getComment());
  }

  @Test
  public void shouldHandleBlankComment() {
    ReviewRequest request = new ReviewRequest();

    request.setComment("   ");

    assertEquals("   ", request.getComment());
  }

  @Test
  public void shouldAcceptValidRatingRange() {
    ReviewRequest request = new ReviewRequest();

    //test boundary values (assuming 1-5 range based on controller validation)
    request.setRating(1);
    assertEquals(1, request.getRating());

    request.setRating(5);
    assertEquals(5, request.getRating());

    request.setRating(3);
    assertEquals(3, request.getRating());
  }

  @Test
  public void shouldAcceptValidCleanlinessRange() {
    ReviewRequest request = new ReviewRequest();

    //test boundary values (assuming 1-5 range based on controller validation)
    request.setCleanliness(1);
    assertEquals(1, request.getCleanliness());

    request.setCleanliness(5);
    assertEquals(5, request.getCleanliness());

    request.setCleanliness(3);
    assertEquals(3, request.getCleanliness());
  }

  @Test
  public void shouldAcceptRatingOutsideTypicalRange() {
    ReviewRequest request = new ReviewRequest();

    //the model itself doesn't validate - validation happens in controller
    request.setRating(0);
    assertEquals(0, request.getRating());

    request.setRating(10);
    assertEquals(10, request.getRating());

    request.setRating(-1);
    assertEquals(-1, request.getRating());
  }

  @Test
  public void shouldAcceptCleanlinessOutsideTypicalRange() {
    ReviewRequest request = new ReviewRequest();

    //the model itself doesn't validate - validation happens in controller
    request.setCleanliness(0);
    assertEquals(0, request.getCleanliness());

    request.setCleanliness(10);
    assertEquals(10, request.getCleanliness());

    request.setCleanliness(-1);
    assertEquals(-1, request.getCleanliness());
  }

  @Test
  public void shouldCreateCompleteReviewRequest() {
    ReviewRequest request = new ReviewRequest();

    request.setRating(4);
    request.setCleanliness(5);
    request.setComment("Great facilities, very clean!");

    assertEquals(4, request.getRating());
    assertEquals(5, request.getCleanliness());
    assertEquals("Great facilities, very clean!", request.getComment());
  }

  @Test
  public void shouldCreateMinimalReviewRequest() {
    ReviewRequest request = new ReviewRequest();

    request.setRating(3);
    request.setCleanliness(3);

    assertEquals(3, request.getRating());
    assertEquals(3, request.getCleanliness());
    assertNull(request.getComment());
  }

  @Test
  public void shouldHandleLongComment() {
    ReviewRequest request = new ReviewRequest();
    String longComment = "This is a very long comment that describes "
        + "the restroom experience in great detail. ".repeat(10);

    request.setComment(longComment);

    assertEquals(longComment, request.getComment());
  }

  @Test
  public void shouldHandleSpecialCharactersInComment() {
    ReviewRequest request = new ReviewRequest();
    String specialComment = "Great restroom! 👍 Clean & well-maintained. 5/5 ⭐⭐⭐⭐⭐";

    request.setComment(specialComment);

    assertEquals(specialComment, request.getComment());
  }

  @Test
  public void shouldInitializeWithNullValues() {
    ReviewRequest request = new ReviewRequest();

    assertNull(request.getRating());
    assertNull(request.getCleanliness());
    assertNull(request.getComment());
  }
}
