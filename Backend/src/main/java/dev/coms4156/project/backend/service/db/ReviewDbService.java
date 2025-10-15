package dev.coms4156.project.backend.service.db;

import dev.coms4156.project.backend.model.Review;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;


/**
 * Database service for Review entity.
 */
@Service
public class ReviewDbService {
  private final JdbcTemplate jdbcTemplate;
  private final RestroomDbService restroomDbService;

  @Autowired
  public ReviewDbService(JdbcTemplate jdbcTemplate, RestroomDbService restroomDbService) {
    this.jdbcTemplate = jdbcTemplate;
    this.restroomDbService = restroomDbService;
  }

  /**
   * Add a new review.
   */
  public Review create(Review review) {
    String sql = """
        INSERT INTO review (restroom_id, user_id, rating, cleanliness, comment, helpful_votes)
        VALUES (?, ?, ?, ?, ?, ?)
        """;

    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(connection -> {
      PreparedStatement ps = connection.prepareStatement(sql, new String[] { "id" });
      ps.setLong(1, review.getRestroomId());
      ps.setString(2, review.getUserId());
      ps.setInt(3, review.getRating());
      ps.setInt(4, review.getCleanliness());
      ps.setString(5, review.getComment());
      ps.setInt(6, review.getHelpfulVotes());
      return ps;
    }, keyHolder);

    Number generatedId = keyHolder.getKey();
    if (generatedId != null) {
      review.setId(generatedId.longValue());
      Timestamp createdAt = jdbcTemplate.queryForObject(
          "SELECT created_at FROM review WHERE id = ?",
          Timestamp.class,
          review.getId());
      if (createdAt != null) {
        review.setCreatedAt(createdAt.toInstant());
      }
    }

    // Update restroom average rating
    updateRestroomRating(review.getRestroomId());

    return review;
  }

  /**
   * Get reviews for a restroom.
   */
  public List<Review> getByRestroomId(Long restroomId, String sort) {
    String orderBy = "helpful".equalsIgnoreCase(sort)
        ? "helpful_votes DESC, created_at DESC"
        : "created_at DESC";

    String sql = "SELECT * FROM review WHERE restroom_id = ? ORDER BY " + orderBy;
    return jdbcTemplate.query(sql, this::mapReview, restroomId);
  }

  /**
   * Update helpful votes.
   */
  public void incrementHelpfulVotes(Long id) {
    String sql = "UPDATE review SET helpful_votes = helpful_votes + 1 WHERE id = ?";
    jdbcTemplate.update(sql, id);
  }

  /**
   * Calculate and update restroom average rating.
   */
  private void updateRestroomRating(Long restroomId) {
    String sql = "SELECT AVG(rating) FROM review WHERE restroom_id = ?";
    Double avgRating = jdbcTemplate.queryForObject(sql, Double.class, restroomId);
    if (avgRating != null) {
      restroomDbService.updateAverageRating(restroomId, avgRating);
    }
  }

  /**
   * Map database row to Review object.
   */
  private Review mapReview(ResultSet rs, int rowNum) throws SQLException {
    if (rowNum < 0) {
      throw new SQLException("Row index must not be negative");
    }
    Review review = new Review();
    review.setId(rs.getLong("id"));
    review.setRestroomId(rs.getLong("restroom_id"));
    review.setUserId(rs.getString("user_id"));
    review.setRating(rs.getInt("rating"));
    review.setCleanliness(rs.getInt("cleanliness"));
    review.setComment(rs.getString("comment"));
    review.setHelpfulVotes(rs.getInt("helpful_votes"));
    review.setCreatedAt(rs.getTimestamp("created_at").toInstant());
    return review;
  }
}
