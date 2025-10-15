package dev.coms4156.project.backend.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for Review model class.
 */
public class ReviewUnitTests {

  @Test
  public void shouldSetAndGetId() {
    Review review = new Review();

    review.setId(123L);

    assertEquals(123L, review.getId());
  }

  @Test
  public void shouldSetAndGetRestroomId() {
    Review review = new Review();

    review.setRestroomId(456L);

    assertEquals(456L, review.getRestroomId());
  }

  @Test
  public void shouldSetAndGetUserId() {
    Review review = new Review();
    String expectedUserId = "user@example.com";

    review.setUserId(expectedUserId);

    assertEquals(expectedUserId, review.getUserId());
  }

  @Test
  public void shouldSetAndGetRating() {
    Review review = new Review();

    review.setRating(5);

    assertEquals(5, review.getRating());
  }

  @Test
  public void shouldSetAndGetCleanliness() {
    Review review = new Review();

    review.setCleanliness(4);

    assertEquals(4, review.getCleanliness());
  }

  @Test
  public void shouldSetAndGetComment() {
    Review review = new Review();
    String expectedComment = "Very clean and well-maintained!";

    review.setComment(expectedComment);

    assertEquals(expectedComment, review.getComment());
  }

  @Test
  public void shouldSetAndGetHelpfulVotes() {
    Review review = new Review();

    review.setHelpfulVotes(15);

    assertEquals(15, review.getHelpfulVotes());
  }

  @Test
  public void shouldSetAndGetCreatedAt() {
    Review review = new Review();
    Instant expectedTime = Instant.now();

    review.setCreatedAt(expectedTime);

    assertEquals(expectedTime, review.getCreatedAt());
  }

  @Test
  public void shouldHandleNullId() {
    Review review = new Review();

    review.setId(null);

    assertNull(review.getId());
  }

  @Test
  public void shouldHandleNullRestroomId() {
    Review review = new Review();

    review.setRestroomId(null);

    assertNull(review.getRestroomId());
  }

  @Test
  public void shouldHandleNullUserId() {
    Review review = new Review();

    review.setUserId(null);

    assertNull(review.getUserId());
  }

  @Test
  public void shouldHandleNullComment() {
    Review review = new Review();

    review.setComment(null);

    assertNull(review.getComment());
  }

  @Test
  public void shouldHandleNullCreatedAt() {
    Review review = new Review();

    review.setCreatedAt(null);

    assertNull(review.getCreatedAt());
  }

  @Test
  public void shouldHandleEmptyComment() {
    Review review = new Review();

    review.setComment("");

    assertEquals("", review.getComment());
  }

  @Test
  public void shouldHandleBlankComment() {
    Review review = new Review();

    review.setComment("   ");

    assertEquals("   ", review.getComment());
  }

  @Test
  public void shouldAcceptValidRatingRange() {
    Review review = new Review();

    // Test typical 1-5 range
    review.setRating(1);
    assertEquals(1, review.getRating());

    review.setRating(3);
    assertEquals(3, review.getRating());

    review.setRating(5);
    assertEquals(5, review.getRating());
  }

  @Test
  public void shouldAcceptValidCleanlinessRange() {
    Review review = new Review();

    //test typical 1-5 range
    review.setCleanliness(1);
    assertEquals(1, review.getCleanliness());

    review.setCleanliness(3);
    assertEquals(3, review.getCleanliness());

    review.setCleanliness(5);
    assertEquals(5, review.getCleanliness());
  }

  @Test
  public void shouldAcceptZeroHelpfulVotes() {
    Review review = new Review();

    review.setHelpfulVotes(0);

    assertEquals(0, review.getHelpfulVotes());
  }

  @Test
  public void shouldAcceptNegativeHelpfulVotes() {
    Review review = new Review();

    //model doesn't validate - could be used for downvotes
    review.setHelpfulVotes(-5);

    assertEquals(-5, review.getHelpfulVotes());
  }

  @Test
  public void shouldAcceptHighHelpfulVotes() {
    Review review = new Review();

    review.setHelpfulVotes(1000);

    assertEquals(1000, review.getHelpfulVotes());
  }

  @Test
  public void shouldCreateCompleteReview() {
    Review review = new Review();
    Instant now = Instant.now();

    review.setId(1L);
    review.setRestroomId(100L);
    review.setUserId("reviewer@example.com");
    review.setRating(4);
    review.setCleanliness(5);
    review.setComment("Excellent facilities!");
    review.setHelpfulVotes(12);
    review.setCreatedAt(now);

    assertEquals(1L, review.getId());
    assertEquals(100L, review.getRestroomId());
    assertEquals("reviewer@example.com", review.getUserId());
    assertEquals(4, review.getRating());
    assertEquals(5, review.getCleanliness());
    assertEquals("Excellent facilities!", review.getComment());
    assertEquals(12, review.getHelpfulVotes());
    assertEquals(now, review.getCreatedAt());
  }

  @Test
  public void shouldCreateMinimalReview() {
    Review review = new Review();

    review.setRestroomId(100L);
    review.setUserId("reviewer@example.com");
    review.setRating(3);
    review.setCleanliness(3);

    assertEquals(100L, review.getRestroomId());
    assertEquals("reviewer@example.com", review.getUserId());
    assertEquals(3, review.getRating());
    assertEquals(3, review.getCleanliness());

    //other fields should be null/default
    assertNull(review.getId());
    assertNull(review.getComment());
    assertNull(review.getCreatedAt());
    assertEquals(0, review.getHelpfulVotes()); //int defaults to 0
  }

  @Test
  public void shouldHandleLongComment() {
    Review review = new Review();
    String longComment = "This restroom exceeded all expectations. ".repeat(20);

    review.setComment(longComment);

    assertEquals(longComment, review.getComment());
  }

  @Test
  public void shouldHandleSpecialCharactersInComment() {
    Review review = new Review();
    String specialComment = "Amazing! 🚻 Clean & accessible ♿ 5⭐";

    review.setComment(specialComment);

    assertEquals(specialComment, review.getComment());
  }

  @Test
  public void shouldHandleSpecialCharactersInUserId() {
    Review review = new Review();
    String specialUserId = "user+test@example.com";

    review.setUserId(specialUserId);

    assertEquals(specialUserId, review.getUserId());
  }

  @Test
  public void shouldHandleInstantPrecision() {
    Review review = new Review();
    Instant preciseTime = Instant.parse("2023-12-25T10:15:30.123456789Z");

    review.setCreatedAt(preciseTime);

    assertEquals(preciseTime, review.getCreatedAt());
  }

  @Test
  public void shouldInitializeWithDefaultValues() {
    Review review = new Review();

    assertNull(review.getId());
    assertNull(review.getRestroomId());
    assertNull(review.getUserId());
    assertEquals(0, review.getRating()); //int defaults to 0
    assertEquals(0, review.getCleanliness()); //int defaults to 0
    assertNull(review.getComment());
    assertEquals(0, review.getHelpfulVotes()); //int defaults to 0
    assertNull(review.getCreatedAt());
  }

  @Test
  public void shouldAcceptRatingOutsideTypicalRange() {
    Review review = new Review();

    //model doesn't validate - validation happens elsewhere
    review.setRating(0);
    assertEquals(0, review.getRating());

    review.setRating(10);
    assertEquals(10, review.getRating());

    review.setRating(-1);
    assertEquals(-1, review.getRating());
  }

  @Test
  public void shouldAcceptCleanlinessOutsideTypicalRange() {
    Review review = new Review();

    //model doesn't validate - validation happens elsewhere
    review.setCleanliness(0);
    assertEquals(0, review.getCleanliness());

    review.setCleanliness(10);
    assertEquals(10, review.getCleanliness());

    review.setCleanliness(-1);
    assertEquals(-1, review.getCleanliness());
  }
}
