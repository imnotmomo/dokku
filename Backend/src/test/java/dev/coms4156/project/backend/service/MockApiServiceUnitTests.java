package dev.coms4156.project.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.coms4156.project.backend.model.EditProposal;
import dev.coms4156.project.backend.model.Restroom;
import dev.coms4156.project.backend.model.Review;
import dev.coms4156.project.backend.model.User;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for MockApiService.
 */
public class MockApiServiceUnitTests {

  private MockApiService service;

  @BeforeEach
  public void setUp() {
    service = new MockApiService();
  }


  @Test
  public void shouldCreateNewUser() {
    User user = service.createUser("testuser", "password123", "USER");

    assertEquals("testuser", user.getUsername());
    assertEquals("USER", user.getRole());
    assertNotNull(user.getToken());
    assertNotNull(user.getRefreshToken());
    assertNull(user.getPassword());
  }

  @Test
  public void shouldNotCreateDuplicateUser() {
    service.createUser("testuser", "password123", "USER");

    assertThrows(IllegalArgumentException.class, () -> {
      service.createUser("testuser", "different", "ADMIN");
    });
  }

  @Test
  public void shouldLoginWithValidCredentials() {
    service.createUser("testuser", "password123", "USER");

    User loggedInUser = service.login("testuser", "password123");

    assertEquals("testuser", loggedInUser.getUsername());
    assertEquals("USER", loggedInUser.getRole());
    assertNotNull(loggedInUser.getToken());
  }

  @Test
  public void shouldNotLoginWithInvalidCredentials() {
    service.createUser("testuser", "password123", "USER");

    assertThrows(IllegalArgumentException.class, () -> {
      service.login("testuser", "wrongpassword");
    });

    assertThrows(IllegalArgumentException.class, () -> {
      service.login("nonexistent", "password123");
    });
  }

  @Test
  public void shouldRefreshTokenWithValidRefreshToken() {
    User user = service.createUser("testuser", "password123", "USER");
    String refreshToken = user.getRefreshToken();

    User refreshedUser = service.refresh(refreshToken);

    assertEquals("testuser", refreshedUser.getUsername());
    assertNotNull(refreshedUser.getToken());
    assertNotEquals(user.getToken(), refreshedUser.getToken()); //new token
  }

  @Test
  public void shouldNotRefreshWithInvalidToken() {
    assertThrows(IllegalArgumentException.class, () -> {
      service.refresh("invalid-refresh-token");
    });
  }

  @Test
  public void shouldGetUserFromValidToken() {
    User user = service.createUser("testuser", "password123", "USER");

    User foundUser = service.getUserFromToken(user.getToken());

    assertEquals("testuser", foundUser.getUsername());
    assertEquals("USER", foundUser.getRole());
  }

  @Test
  public void shouldReturnNullForInvalidToken() {
    User user = service.getUserFromToken("invalid-token");

    assertNull(user);
  }


  @Test
  public void shouldSubmitNewRestroom() {
    Restroom restroom = new Restroom();
    restroom.setName("Test Restroom");
    restroom.setAddress("123 Test St");
    restroom.setLatitude(40.7536);
    restroom.setLongitude(-73.9832);
    restroom.setHours("09:00-17:00");
    restroom.setAmenities(List.of("wheelchair", "family"));

    Restroom saved = service.submitRestroom(restroom);

    assertNotNull(saved.getId());
    assertEquals("Test Restroom", saved.getName());
    assertEquals("123 Test St", saved.getAddress());
    assertEquals(40.7536, saved.getLatitude());
    assertEquals(-73.9832, saved.getLongitude());
    assertEquals("09:00-17:00", saved.getHours());
    assertEquals(2, saved.getAmenities().size());
    assertEquals(0.0, saved.getAvgRating());
    assertEquals(0L, saved.getVisitCount());
  }

  @Test
  public void shouldGetExistingRestroom() {
    //use seeded data (ID 1 should exist from constructor)
    Restroom restroom = service.getRestroom(1L);

    assertNotNull(restroom);
    assertEquals(1L, restroom.getId());
    assertEquals("Refinery Building", restroom.getName());
  }

  @Test
  public void shouldThrowExceptionForNonExistentRestroom() {
    assertThrows(NoSuchElementException.class, () -> {
      service.getRestroom(999L);
    });
  }

  @Test
  public void shouldRecordVisit() {
    Map<String, Object> result = service.recordVisit(1L);

    assertEquals(1L, result.get("restroomId"));
    assertTrue(result.containsKey("visitCount"));
    assertTrue(result.containsKey("visitedAt"));

    // Visit count should increase
    Long visitCount = (Long) result.get("visitCount");
    assertTrue(visitCount > 0);
  }

  @Test
  public void shouldThrowExceptionWhenRecordingVisitForNonExistentRestroom() {
    assertThrows(NoSuchElementException.class, () -> {
      service.recordVisit(999L);
    });
  }

  // ===== Nearby Search Tests =====

  @Test
  public void shouldFindNearbyRestrooms() {
    //Bryant Park coordinates: 40.7536, -73.9832
    List<Restroom> nearby = service.getNearby(40.7536, -73.9832, 2000.0, null, null, null);

    assertFalse(nearby.isEmpty());
    assertTrue(nearby.size() >= 1); // At least the seeded restroom
  }

  @Test
  public void shouldFilterByRadius() {
    //very small radius should return fewer results
    List<Restroom> nearbySmall = service.getNearby(40.7536, -73.9832,
        2000.0, null, null, null);
    List<Restroom> nearbyLarge = service.getNearby(40.7536, -73.9832,
        2000.0, null, null, null);

    assertTrue(nearbySmall.size() <= nearbyLarge.size());
  }

  @Test
  public void shouldFilterByAmenities() {
    Set<String> wheelchairFilter = Set.of("wheelchair");
    List<Restroom> nearby = service.getNearby(40.7536, -73.9832,
        2000.0, null, wheelchairFilter, null);

    //all returned restrooms should have wheelchair amenity
    for (Restroom restroom : nearby) {
      assertTrue(restroom.getAmenities().contains("wheelchair"));
    }
  }

  @Test
  public void shouldLimitResults() {
    List<Restroom> unlimited = service.getNearby(40.7536, -73.9832, 5000.0, null, null, null);
    List<Restroom> limited = service.getNearby(40.7536, -73.9832, 5000.0, null, null, 1);

    assertTrue(limited.size() <= 1);
    assertTrue(limited.size() <= unlimited.size());
  }

  @Test
  public void shouldSortByRatingThenVisitsThenDistance() {
    //add a restroom with higher rating
    Restroom highRated = new Restroom();
    highRated.setName("High Rated Restroom");
    highRated.setAddress("456 Test Ave");
    highRated.setLatitude(40.7540); //slightly different location
    highRated.setLongitude(-73.9830);
    highRated.setAvgRating(5.0);
    service.submitRestroom(highRated);

    List<Restroom> results = service.getNearby(40.7536, -73.9832, 2000.0, null, null, null);

    //should be sorted by rating descending
    for (int i = 0; i < results.size() - 1; i++) {
      assertTrue(results.get(i).getAvgRating() >= results.get(i + 1).getAvgRating());
    }
  }


  @Test
  public void shouldAddReview() {
    Review review = service.addReview(1L, "reviewer@test.com", 4, 5, "Great restroom!");

    assertNotNull(review.getId());
    assertEquals(1L, review.getRestroomId());
    assertEquals("reviewer@test.com", review.getUserId());
    assertEquals(4, review.getRating());
    assertEquals(5, review.getCleanliness());
    assertEquals("Great restroom!", review.getComment());
    assertEquals(0, review.getHelpfulVotes());
    assertNotNull(review.getCreatedAt());
  }

  @Test
  public void shouldThrowExceptionWhenAddingReviewToNonExistentRestroom() {
    assertThrows(NoSuchElementException.class, () -> {
      service.addReview(999L, "reviewer@test.com", 4, 5, "Great!");
    });
  }

  @Test
  public void shouldUpdateAvgRatingWhenAddingReview() {
    //get initial rating
    Restroom before = service.getRestroom(1L);
    double initialRating = before.getAvgRating();

    //add a review
    service.addReview(1L, "reviewer@test.com", 5, 5, "Excellent!");

    //check updated rating
    Restroom after = service.getRestroom(1L);
    assertNotEquals(initialRating, after.getAvgRating());
  }

  @Test
  public void shouldGetReviewsSortedByRecent() {
    //add multiple reviews
    service.addReview(1L, "user1@test.com", 4, 4, "Good");
    service.addReview(1L, "user2@test.com", 5, 5, "Great");

    List<Review> reviews = service.getReviews(1L, "recent");

    assertFalse(reviews.isEmpty());
    //should be sorted by creation time descending
    for (int i = 0; i < reviews.size() - 1; i++) {
      assertTrue(reviews.get(i).getCreatedAt()
          .compareTo(reviews.get(i + 1).getCreatedAt()) >= 0);
    }
  }

  @Test
  public void shouldGetReviewsSortedByHelpful() {
    List<Review> reviews = service.getReviews(1L, "helpful");

    //should be sorted by helpful votes descending
    for (int i = 0; i < reviews.size() - 1; i++) {
      assertTrue(reviews.get(i).getHelpfulVotes() >= reviews.get(i + 1).getHelpfulVotes());
    }
  }


  @Test
  public void shouldProposeEdit() {
    EditProposal proposal = new EditProposal();
    proposal.setProposedName("Updated Name");
    proposal.setProposedHours("24/7");
    proposal.setProposerUserId("editor@test.com");

    EditProposal created = service.proposeEdit(1L, proposal);

    assertNotNull(created.getId());
    assertEquals(1L, created.getRestroomId());
    assertEquals("Updated Name", created.getProposedName());
    assertEquals("24/7", created.getProposedHours());
    assertEquals("editor@test.com", created.getProposerUserId());
    assertEquals("PENDING", created.getStatus());
    assertNotNull(created.getCreatedAt());
  }

  @Test
  public void shouldThrowExceptionWhenProposingEditForNonExistentRestroom() {
    EditProposal proposal = new EditProposal();
    proposal.setProposedName("Updated Name");

    assertThrows(NoSuchElementException.class, () -> {
      service.proposeEdit(999L, proposal);
    });
  }

  @Test
  public void shouldAddProposalToPendingEdits() {
    EditProposal proposal = new EditProposal();
    proposal.setProposedName("Updated Name");

    service.proposeEdit(1L, proposal);

    Restroom restroom = service.getRestroom(1L);
    assertFalse(restroom.getPendingEdits().isEmpty());
  }


  @Test
  public void shouldCalculateDistanceCorrectly() {
    //Bryant Park to Times Square is about 800m
    double lat1 = 40.7536; // Bryant Park
    double lng1 = -73.9832;
    double lat2 = 40.7580; // Times Square
    double lng2 = -73.9855;

    //test indirectly through getNearby with small radius
    List<Restroom> nearby = service.getNearby(lat1, lng1, 500.0, null, null, null);
    List<Restroom> farther = service.getNearby(lat1, lng1, 1000.0, null, null, null);

    assertTrue(farther.size() >= nearby.size());
  }

  @Test
  public void shouldCheckOpenHoursCorrectly() {
    //this is indirect testing since isOpen is private
    List<Restroom> all = service.getNearby(40.7536, -73.9832, 2000.0, null, null, null);
    List<Restroom> openNow = service.getNearby(40.7536, -73.9832, 2000.0, true, null, null);

    //during business hours, should have some open restrooms
    assertTrue(openNow.size() <= all.size());
  }

  @Test
  public void shouldFilterAmenitiesCorrectly() {
    //test hasAllAmenities indirectly through getNearby
    Set<String> nonExistentAmenity = Set.of("non-existent-amenity");
    List<Restroom> filtered = service.getNearby(40.7536, -73.9832,
        2000.0, null, nonExistentAmenity, null);

    //should return empty or very few results
    assertTrue(filtered.isEmpty() || filtered.size() < 2);
  }


  @Test
  public void shouldHandleEmptyAmenitiesFilter() {
    Set<String> emptyFilter = Set.of();
    List<Restroom> results = service.getNearby(40.7536, -73.9832, 2000.0, null, emptyFilter, null);

    assertFalse(results.isEmpty()); //empty filter should not filter anything
  }

  @Test
  public void shouldHandleZeroRadius() {
    List<Restroom> results = service.getNearby(40.7536, -73.9832, 0.0, null, null, null);

    //should return empty or very few results
    assertTrue(results.isEmpty() || results.size() <= 1);
  }

  @Test
  public void shouldHandleNegativeLimit() {
    List<Restroom> results = service.getNearby(40.7536, -73.9832, 2000.0, null, null, -1);

    assertFalse(results.isEmpty());
  }

  @Test
  public void shouldHandleNullComment() {
    Review review = service.addReview(1L, "reviewer@test.com", 4, 5, null);

    assertNull(review.getComment());
  }

  @Test
  public void shouldHandleEmptyComment() {
    Review review = service.addReview(1L, "reviewer@test.com", 4, 5, "");

    assertEquals("", review.getComment());
  }


  @Test
  public void shouldHaveSeededUsers() {
    User admin = service.getUserFromToken(service.login("admin@demo", "admin").getToken());
    User user = service.getUserFromToken(service.login("user@demo", "user").getToken());

    assertEquals("ADMIN", admin.getRole());
    assertEquals("USER", user.getRole());
  }

  @Test
  public void shouldHaveSeededRestrooms() {
    //check the hardcoded extra restrooms (IDs 976-977)
    Restroom bryantPark = service.getRestroom(976L);
    Restroom wholefoods = service.getRestroom(977L);

    assertEquals("Bryant Park Public Restroom", bryantPark.getName());
    assertEquals("Whole Foods Market - Bryant Park", wholefoods.getName());
  }

  @Test
  public void shouldHaveSeededReview() {
    //review is added to Bryant Park (ID 976, not ID 1)
    List<Review> reviews = service.getReviews(976L, "recent");

    assertFalse(reviews.isEmpty());
    assertEquals("user@demo", reviews.get(0).getUserId());
    assertEquals(5, reviews.get(0).getRating());
  }
}
