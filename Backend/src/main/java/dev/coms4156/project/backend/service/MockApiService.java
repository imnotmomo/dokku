package dev.coms4156.project.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.coms4156.project.backend.model.EditProposal;
import dev.coms4156.project.backend.model.Restroom;
import dev.coms4156.project.backend.model.Review;
import dev.coms4156.project.backend.model.User;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;


/**
 * In-memory mock backing store and logic for the API.
 */
@Service
@Profile("mock")
public class MockApiService {

  private static final String TZ_NY = "America/New_York";

  private final Map<Long, Restroom> restrooms = new ConcurrentHashMap<>();

  public MockApiService() {
    seed();
  }

  private final Map<Long, List<Review>> reviewsByRestroom = new ConcurrentHashMap<>();
  private final Map<String, User> users = new ConcurrentHashMap<>();
  private final Map<String, String> tokenToUser = new ConcurrentHashMap<>();

  private final AtomicLong restroomSeq = new AtomicLong(1);
  private final AtomicLong reviewSeq = new AtomicLong(1);
  private final AtomicLong proposalSeq = new AtomicLong(1);


  private void seed() {
    createUser("admin@demo", "admin", "ADMIN");
    createUser("user@demo", "user", "USER");

    loadRestroomsFromJson();

    // additional restroom data is 976 and 977
    loadExtraRestroomData();
  }


  /**
   * Load restrooms from the JSON file in resources.
   */
  private void loadRestroomsFromJson() {
    try (InputStream is = Thread.currentThread().getContextClassLoader()
        .getResourceAsStream("mockdata/restrooms.json")) {
      if (is == null) {
        System.err.println("Failed to find mockdata/restrooms.json in resources.");
      } else {
        ObjectMapper mapper = new ObjectMapper();
        List<Restroom> restroomsList = mapper.readValue(is, new TypeReference<List<Restroom>>(){});
        // Convert list to map and update sequence counter
        for (Restroom restroom : restroomsList) {
          restrooms.put(restroom.getId(), restroom);

          if (restroom.getId() >= restroomSeq.get()) {
            restroomSeq.set(restroom.getId() + 1);
          }
        }
        
        System.out.println("Loaded " + restrooms.size() + " restrooms from JSON file");
      }
    } catch (Exception e) {
      System.err.println("Error loading restrooms from JSON: " + e.getMessage());
    }
  }


  private void loadExtraRestroomData() {
    System.out.println("Loading extra restroom data...");

    Restroom r1 = new Restroom();
    r1.setId(restroomSeq.getAndIncrement());
    r1.setName("Bryant Park Public Restroom");
    r1.setAddress("476 5th Ave, New York, NY 10018");
    r1.setLatitude(40.7536);
    r1.setLongitude(-73.9832);
    r1.setHours("08:00-18:00");
    r1.setAmenities(List.of("wheelchair", "family"));
    r1.setAvgRating(4.7);
    r1.setVisitCount(12L);
    restrooms.put(r1.getId(), r1);

    Restroom r2 = new Restroom();
    r2.setId(restroomSeq.getAndIncrement());
    r2.setName("Whole Foods Market - Bryant Park");
    r2.setAddress("1095 6th Ave, New York, NY 10036");
    r2.setLatitude(40.7530);
    r2.setLongitude(-73.9847);
    r2.setHours("07:00-22:00");
    r2.setAmenities(List.of("family"));
    r2.setAvgRating(4.2);
    r2.setVisitCount(6L);
    restrooms.put(r2.getId(), r2);
    
    addReviewInternal(makeReview(r1.getId(), "user@demo", 5, 5, "Very clean!"));
  }

  // ===== Users/Auth =====

  /**
   * Create a new user (mock).
   */
  public synchronized User createUser(final String username, final String password,
                                      final String role) {
    if (users.containsKey(username)) {
      throw new IllegalArgumentException("User already exists");
    }
    User u = new User();
    u.setUsername(username);
    u.setPassword(password);
    u.setRole(role);
    u.setToken(UUID.randomUUID().toString());
    u.setRefreshToken(UUID.randomUUID().toString());
    users.put(username, u);
    tokenToUser.put(u.getToken(), username);
    return sanitize(u);
  }

  /**
   * Login and rotate access token.
   */
  public User login(final String username, final String password) {
    User u = users.get(username);
    if (u == null || !Objects.equals(u.getPassword(), password)) {
      throw new IllegalArgumentException("Invalid credentials");
    }
    String newToken = UUID.randomUUID().toString();
    tokenToUser.remove(u.getToken());
    u.setToken(newToken);
    tokenToUser.put(newToken, username);
    return sanitize(u);
  }

  /**
   * Refresh access token.
   */
  public User refresh(final String refreshToken) {
    Optional<User> ou = users.values().stream()
            .filter(x -> Objects.equals(x.getRefreshToken(), refreshToken))
            .findFirst();
    if (ou.isEmpty()) {
      throw new IllegalArgumentException("Invalid refresh token");
    }
    User u = ou.get();
    String newToken = UUID.randomUUID().toString();
    tokenToUser.remove(u.getToken());
    u.setToken(newToken);
    tokenToUser.put(newToken, u.getUsername());
    return sanitize(u);
  }

  /**
   * Map token to user.
   */
  public User getUserFromToken(final String token) {
    String username = tokenToUser.get(token);
    if (username == null) {
      return null;
    }
    return sanitize(users.get(username));
  }

  private User sanitize(final User u) {
    if (u == null) {
      return null;
    }
    User s = new User();
    s.setUsername(u.getUsername());
    s.setRole(u.getRole());
    s.setToken(u.getToken());
    s.setRefreshToken(u.getRefreshToken());
    return s;
  }

  // ===== Restrooms =====

  /**
   * Submit a new restroom (mock).
   */
  public Restroom submitRestroom(final Restroom r) {
    r.setId(restroomSeq.getAndIncrement());
    r.setAvgRating(0.0);
    r.setVisitCount(0L);
    restrooms.put(r.getId(), r);
    return r;
  }

  /**
   * Get restroom by id and update avg rating from reviews.
   */
  public Restroom getRestroom(final Long id) {
    Restroom r = restrooms.get(id);
    if (r == null) {
      throw new NoSuchElementException("Restroom not found");
    }
    List<Review> rs = reviewsByRestroom.getOrDefault(id, Collections.emptyList());
    if (!rs.isEmpty()) {
      double avg = rs.stream().mapToInt(Review::getRating).average().orElse(0.0);
      r.setAvgRating(Math.round(avg * 10.0) / 10.0);
    }
    return r;
  }

  /**
   * Nearby search with ranking: rating desc, visits desc, distance asc.
   */
  public List<Restroom> getNearby(final double lat, final double lng, final double radiusMeters,
                                  final Boolean openNow, final Set<String> amenitiesFilter,
                                  final Integer limit) {
    List<Restroom> all = new ArrayList<>(restrooms.values());
    ZonedDateTime znow = ZonedDateTime.ofInstant(Instant.now(), ZoneId.of(TZ_NY));

    List<Restroom> filtered = all.stream()
            .filter(r -> distanceMeters(lat, lng, r.getLatitude(),
                    r.getLongitude()) <= radiusMeters)
            .filter(r -> openNow == null || isOpen(r.getHours(), znow.toLocalTime()))
            .filter(r -> amenitiesFilter == null || amenitiesFilter.isEmpty()
                    || hasAllAmenities(r.getAmenities(), amenitiesFilter))
            .sorted(Comparator
                    .comparing(Restroom::getAvgRating, Comparator.reverseOrder())
                    .thenComparing(Restroom::getVisitCount, Comparator.reverseOrder())
                    .thenComparing(r -> distanceMeters(lat, lng, r.getLatitude(),
                            r.getLongitude())))
            .collect(Collectors.toList());

    if (limit != null && limit > 0 && filtered.size() > limit) {
      return filtered.subList(0, limit);
    }
    return filtered;
  }

  /**
   * Record a visit.
   */
  public Map<String, Object> recordVisit(final Long id) {
    Restroom r = getRestroom(id);
    r.setVisitCount(r.getVisitCount() + 1);
    Map<String, Object> resp = new HashMap<>();
    resp.put("restroomId", id);
    resp.put("visitCount", r.getVisitCount());
    resp.put("visitedAt", Instant.now().toString());
    return resp;
  }

  // ===== Edit Proposals =====

  /**
   * Propose an edit.
   */
  public EditProposal proposeEdit(final Long restroomId, final EditProposal p) {
    p.setId(proposalSeq.getAndIncrement());
    p.setRestroomId(restroomId);
    p.setStatus("PENDING");
    p.setCreatedAt(Instant.now());
    Restroom r = getRestroom(restroomId);
    r.getPendingEdits().add(p);
    return p;
  }

  // ===== Reviews =====

  /**
   * Add a review.
   */
  public Review addReview(final Long restroomId, final String userId, final int rating,
                          final int cleanliness, final String comment) {
    getRestroom(restroomId);
    Review review = new Review();
    review.setId(reviewSeq.getAndIncrement());
    review.setRestroomId(restroomId);
    review.setUserId(userId);
    review.setRating(rating);
    review.setCleanliness(cleanliness);
    review.setComment(comment);
    review.setHelpfulVotes(0);
    review.setCreatedAt(Instant.now());
    addReviewInternal(review);
    return review;
  }

  private void addReviewInternal(final Review review) {
    reviewsByRestroom.computeIfAbsent(review.getRestroomId(), k -> new ArrayList<>()).add(review);
    Restroom r = restrooms.get(review.getRestroomId());
    if (r != null) {
      List<Review> rs = reviewsByRestroom.get(review.getRestroomId());
      double avg = rs.stream().mapToInt(Review::getRating).average().orElse(0.0);
      r.setAvgRating(Math.round(avg * 10.0) / 10.0);
    }
  }

  /**
   * Get reviews with sorting.
   */
  public List<Review> getReviews(final Long restroomId, final String sort) {
    List<Review> rs = new ArrayList<>(reviewsByRestroom.getOrDefault(restroomId,
            Collections.emptyList()));
    if ("helpful".equalsIgnoreCase(sort)) {
      rs.sort(Comparator.comparing(Review::getHelpfulVotes).reversed()
              .thenComparing(Review::getCreatedAt).reversed());
    } else {
      rs.sort(Comparator.comparing(Review::getCreatedAt).reversed());
    }
    return rs;
  }

  // ===== Helpers =====

  private boolean hasAllAmenities(final List<String> amenities, final Set<String> filter) {
    if (amenities == null || amenities.isEmpty()) {
      return false;
    }
    Set<String> have = amenities.stream()
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toSet());
    return have.containsAll(filter);
  }

  private boolean isOpen(final String hours, final LocalTime now) {
    if (hours == null || hours.isBlank()) {
      return true;
    }
    try {
      String[] parts = hours.split("-");
      LocalTime open = LocalTime.parse(parts[0], DateTimeFormatter.ofPattern("HH:mm"));
      LocalTime close = LocalTime.parse(parts[1], DateTimeFormatter.ofPattern("HH:mm"));
      if (close.isBefore(open)) {
        return !now.isBefore(open) || !now.isAfter(close);
      }
      return !now.isBefore(open) && !now.isAfter(close);
    } catch (Exception ex) {
      return true;
    }
  }

  private double distanceMeters(final double lat1, final double lon1,
                                final double lat2, final double lon2) {
    double earthRadiusMeters = 6_371_000.0;
    double dlat = Math.toRadians(lat2 - lat1);
    double dlon = Math.toRadians(lon2 - lon1);
    double a = Math.sin(dlat / 2) * Math.sin(dlat / 2)
            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
            * Math.sin(dlon / 2) * Math.sin(dlon / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return earthRadiusMeters * c;
  }

  private Review makeReview(final Long restroomId, final String userId, final int rating,
                            final int cleanliness, final String comment) {
    Review rv = new Review();
    rv.setId(reviewSeq.getAndIncrement());
    rv.setRestroomId(restroomId);
    rv.setUserId(userId);
    rv.setRating(rating);
    rv.setCleanliness(cleanliness);
    rv.setComment(comment);
    rv.setHelpfulVotes(ThreadLocalRandom.current().nextInt(5));
    rv.setCreatedAt(Instant.now());
    return rv;
  }
}
