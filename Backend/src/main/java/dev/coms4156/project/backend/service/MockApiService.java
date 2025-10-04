package dev.coms4156.project.backend.service;

import dev.coms4156.project.backend.model.EditProposal;
import dev.coms4156.project.backend.model.Restroom;
import dev.coms4156.project.backend.model.Review;
import dev.coms4156.project.backend.model.User;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class MockApiService {

    private final Map<Long, Restroom> restrooms = new ConcurrentHashMap<>();
    private final Map<Long, List<Review>> reviewsByRestroom = new ConcurrentHashMap<>();
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Map<String, String> tokenToUser = new ConcurrentHashMap<>();

    private final AtomicLong restroomSeq = new AtomicLong(1);
    private final AtomicLong reviewSeq = new AtomicLong(1);
    private final AtomicLong proposalSeq = new AtomicLong(1);

    public MockApiService() {
        seed();
    }

    private void seed() {
        // Seed one admin and one user
        createUser("admin@demo", "admin", "ADMIN");
        createUser("user@demo", "user", "USER");

        // Seed restrooms
        Restroom r1 = new Restroom();
        r1.setId(restroomSeq.getAndIncrement());
        r1.setName("Bryant Park Public Restroom");
        r1.setAddress("476 5th Ave, New York, NY 10018");
        r1.setLatitude(40.7536);
        r1.setLongitude(-73.9832);
        r1.setHours("08:00-18:00");
        r1.setAmenities("wheelchair,family");
        r1.setAvgRating(4.7);
        r1.setVisitCount(12);
        restrooms.put(r1.getId(), r1);

        Restroom r2 = new Restroom();
        r2.setId(restroomSeq.getAndIncrement());
        r2.setName("Whole Foods Market - Bryant Park");
        r2.setAddress("1095 6th Ave, New York, NY 10036");
        r2.setLatitude(40.7530);
        r2.setLongitude(-73.9847);
        r2.setHours("07:00-22:00");
        r2.setAmenities("family");
        r2.setAvgRating(4.2);
        r2.setVisitCount(6);
        restrooms.put(r2.getId(), r2);

        // Seed a review
        addReviewInternal(makeReview(r1.getId(),"user@demo",5,5,"Very clean!"));
    }

    private Review makeReview(Long restroomId, String userId, int rating, int clean, String cmt) {
        Review rv = new Review();
        rv.setId(reviewSeq.getAndIncrement());
        rv.setRestroomId(restroomId);
        rv.setUserId(userId);
        rv.setRating(rating);
        rv.setCleanliness(clean);
        rv.setComment(cmt);
        rv.setHelpfulVotes(new Random().nextInt(5));
        rv.setCreatedAt(Instant.now());
        return rv;
    }

    // ===== Users/Auth =====
    public synchronized User createUser(String username, String password, String role) {
        if (users.containsKey(username)) {
            throw new IllegalArgumentException("User already exists");
        }
        User u = new User();
        u.setUsername(username);
        u.setPassword(password);
        u.setRole(role);
        // issue tokens
        u.setToken(UUID.randomUUID().toString());
        u.setRefreshToken(UUID.randomUUID().toString());
        users.put(username, u);
        tokenToUser.put(u.getToken(), username);
        return sanitize(u);
    }

    public User login(String username, String password) {
        User u = users.get(username);
        if (u == null || !Objects.equals(u.getPassword(), password)) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        // rotate token
        String newToken = UUID.randomUUID().toString();
        tokenToUser.remove(u.getToken());
        u.setToken(newToken);
        tokenToUser.put(newToken, username);
        return sanitize(u);
    }

    public User refresh(String refreshToken) {
        Optional<User> ou = users.values().stream()
                .filter(x -> Objects.equals(x.getRefreshToken(), refreshToken))
                .findFirst();
        if (ou.isEmpty()) throw new IllegalArgumentException("Invalid refresh token");
        User u = ou.get();
        String newToken = UUID.randomUUID().toString();
        tokenToUser.remove(u.getToken());
        u.setToken(newToken);
        tokenToUser.put(newToken, u.getUsername());
        return sanitize(u);
    }

    public User getUserFromToken(String token) {
        String username = tokenToUser.get(token);
        if (username == null) return null;
        return sanitize(users.get(username));
    }

    private User sanitize(User u) {
        if (u == null) return null;
        User s = new User();
        s.setUsername(u.getUsername());
        s.setRole(u.getRole());
        s.setToken(u.getToken());
        s.setRefreshToken(u.getRefreshToken());
        return s;
    }

    // ===== Restrooms =====
    public Restroom submitRestroom(Restroom r) {
        r.setId(restroomSeq.getAndIncrement());
        r.setAvgRating(0.0);
        r.setVisitCount(0);
        restrooms.put(r.getId(), r);
        return r;
    }

    public Restroom getRestroom(Long id) {
        Restroom r = restrooms.get(id);
        if (r == null) throw new NoSuchElementException("Restroom not found");
        // recompute avg rating
        List<Review> rs = reviewsByRestroom.getOrDefault(id, Collections.emptyList());
        if (!rs.isEmpty()) {
            double avg = rs.stream().mapToInt(Review::getRating).average().orElse(0.0);
            r.setAvgRating(Math.round(avg * 10.0) / 10.0);
        }
        return r;
    }

    public List<Restroom> getNearby(double lat, double lng, double radiusMeters,
                                    Boolean openNow, Set<String> amenitiesFilter, Integer limit) {
        List<Restroom> all = new ArrayList<>(restrooms.values());
        Instant now = Instant.now();
        ZonedDateTime znow = ZonedDateTime.ofInstant(now, ZoneId.of("America/New_York"));

        List<Restroom> filtered = all.stream()
                .filter(r -> distanceMeters(lat, lng, r.getLatitude(), r.getLongitude()) <= radiusMeters)
                .filter(r -> openNow == null || isOpen(r.getHours(), znow.toLocalTime()))
                .filter(r -> amenitiesFilter == null || amenitiesFilter.isEmpty() ||
                        hasAllAmenities(r.getAmenities(), amenitiesFilter))
                .sorted(Comparator
                        // simple "best" ranking: rating (desc), then visits (desc), then distance (asc)
                        .comparing(Restroom::getAvgRating, Comparator.reverseOrder())
                        .thenComparing(Restroom::getVisitCount, Comparator.reverseOrder())
                        .thenComparing(r -> distanceMeters(lat, lng, r.getLatitude(), r.getLongitude()))
                ).collect(Collectors.toList());

        if (limit != null && limit > 0 && filtered.size() > limit) {
            return filtered.subList(0, limit);
        }
        return filtered;
    }

    public Map<String, Object> recordVisit(Long id) {
        Restroom r = getRestroom(id);
        r.setVisitCount(r.getVisitCount() + 1);
        Map<String, Object> resp = new HashMap<>();
        resp.put("restroomId", id);
        resp.put("visitCount", r.getVisitCount());
        resp.put("visitedAt", Instant.now().toString());
        return resp;
    }

    // ===== Edit Proposals =====
    public EditProposal proposeEdit(Long restroomId, EditProposal p) {
        Restroom r = getRestroom(restroomId);
        p.setId(proposalSeq.getAndIncrement());
        p.setRestroomId(restroomId);
        p.setStatus("PENDING");
        p.setCreatedAt(Instant.now());
        r.getPendingEdits().add(p);
        return p;
    }

    // ===== Reviews =====
    public Review addReview(Long restroomId, String userId, int rating, int cleanliness, String comment) {
        getRestroom(restroomId); // ensure exists
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

    private void addReviewInternal(Review review) {
        reviewsByRestroom.computeIfAbsent(review.getRestroomId(), k -> new ArrayList<>()).add(review);
        // recalc avg
        Restroom r = restrooms.get(review.getRestroomId());
        if (r != null) {
            List<Review> rs = reviewsByRestroom.get(review.getRestroomId());
            double avg = rs.stream().mapToInt(Review::getRating).average().orElse(0.0);
            r.setAvgRating(Math.round(avg * 10.0) / 10.0);
        }
    }

    public List<Review> getReviews(Long restroomId, String sort) {
        List<Review> rs = new ArrayList<>(reviewsByRestroom.getOrDefault(restroomId, Collections.emptyList()));
        if ("helpful".equalsIgnoreCase(sort)) {
            rs.sort(Comparator.comparing(Review::getHelpfulVotes).reversed()
                    .thenComparing(Review::getCreatedAt).reversed());
        } else {
            // default: recent
            rs.sort(Comparator.comparing(Review::getCreatedAt).reversed());
        }
        return rs;
    }

    // ===== Helpers =====
    private boolean hasAllAmenities(String commaSeparated, Set<String> filter) {
        if (commaSeparated == null || commaSeparated.isBlank()) return false;
        Set<String> have = Arrays.stream(commaSeparated.split(","))
                .map(String::trim).filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
        return have.containsAll(filter);
    }

    private boolean isOpen(String hours, LocalTime now) {
        if (hours == null || hours.isBlank()) return true; // assume open if unknown
        try {
            String[] parts = hours.split("-");
            LocalTime open = LocalTime.parse(parts[0], DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime close = LocalTime.parse(parts[1], DateTimeFormatter.ofPattern("HH:mm"));
            if (close.isBefore(open)) {
                // overnight window (e.g., 22:00-06:00)
                return !now.isBefore(open) || !now.isAfter(close);
            }
            return !now.isBefore(open) && !now.isAfter(close);
        } catch (Exception e) {
            return true;
        }
    }

    private double distanceMeters(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371000.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2)*Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))*
                        Math.sin(dLon/2)*Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }
}