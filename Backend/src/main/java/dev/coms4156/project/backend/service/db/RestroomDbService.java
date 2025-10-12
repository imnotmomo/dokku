package dev.coms4156.project.backend.service.db;

import dev.coms4156.project.backend.model.Restroom;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;



/**
 * Database service for Restroom entity.
 */
@Service
@Profile("!mock")
public class RestroomDbService {
  private final JdbcTemplate jdbcTemplate;

  @Autowired
  public RestroomDbService(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  /**
   * Get restroom by ID.
   */
  public Optional<Restroom> getById(Long id) {
    String sql = "SELECT * FROM restroom WHERE id = ?";
    try {
      return Optional.of(jdbcTemplate.queryForObject(sql, this::mapRestroom, id));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  /**
   * Get nearby restrooms within radius.
   */
  public List<Restroom> getNearby(double lat, double lng, double radiusMeters, 
                                  Boolean openNow, Set<String> amenitiesFilter, Integer limit) {
    String sql = """
      SELECT *,
          (6371000 * 2 * asin(sqrt(
              sin(radians((latitude - ?) / 2)) * sin(radians((latitude - ?) / 2)) +
              cos(radians(?)) * cos(radians(latitude)) *
              sin(radians((longitude - ?) / 2)) * sin(radians((longitude - ?) / 2))
          ))) as distance
      FROM restroom
      WHERE (6371000 * 2 * asin(sqrt(
              sin(radians((latitude - ?) / 2)) * sin(radians((latitude - ?) / 2)) +
              cos(radians(?)) * cos(radians(latitude)) *
              sin(radians((longitude - ?) / 2)) * sin(radians((longitude - ?) / 2))
          ))) <= ?
      ORDER BY distance ASC
      LIMIT ?
        """;

    return jdbcTemplate.query(sql, this::mapRestroom, 
      lat, lat, lat, lng, lng,           // First distance calculation (SELECT)
      lat, lat, lat, lng, lng,           // Second distance calculation (WHERE)
      radiusMeters,                      // Radius filter
      limit != null ? limit : 10);       // Limit
  }

  /**
   * Create a new restroom.
   */
  public Restroom create(Restroom restroom) {
    String sql = """
        INSERT INTO restroom (name, address, latitude, longitude, hours, amenities, 
        avg_rating, visit_count)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        RETURNING id
        """;

    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(connection -> {
      PreparedStatement ps = connection.prepareStatement(sql, new String[] { "id" });
      ps.setString(1, restroom.getName());
      ps.setString(2, restroom.getAddress());
      ps.setDouble(3, restroom.getLatitude());
      ps.setDouble(4, restroom.getLongitude());
      ps.setString(5, restroom.getHours());
      ps.setArray(6, connection.createArrayOf("text",
          restroom.getAmenities().toArray(new String[0])));
      ps.setDouble(7, restroom.getAvgRating());
      ps.setLong(8, restroom.getVisitCount());
      return ps;
    }, keyHolder);

    restroom.setId(keyHolder.getKey().longValue());
    return restroom;
  }

  /**
   * Update visit count.
   */
  public void incrementVisitCount(Long id) {
    String sql = "UPDATE restroom SET visit_count = visit_count + 1 WHERE id = ?";
    jdbcTemplate.update(sql, id);
  }

  /**
   * Update average rating.
   */
  public void updateAverageRating(Long id, double newRating) {
    String sql = "UPDATE restroom SET avg_rating = ? WHERE id = ?";
    jdbcTemplate.update(sql, newRating, id);
  }

  /**
   * Map database row to Restroom object.
   */
  @SuppressWarnings({"PMD.UnusedFormalParameter"})
  private Restroom mapRestroom(ResultSet rs, int rowNum) throws SQLException {
    Restroom restroom = new Restroom();
    restroom.setId(rs.getLong("id"));
    restroom.setName(rs.getString("name"));
    restroom.setAddress(rs.getString("address"));
    restroom.setLatitude(rs.getDouble("latitude"));
    restroom.setLongitude(rs.getDouble("longitude"));
    restroom.setHours(rs.getString("hours"));
    restroom.setAmenities(Arrays.asList((String[]) rs.getArray("amenities").getArray()));
    restroom.setAvgRating(rs.getDouble("avg_rating"));
    restroom.setVisitCount(rs.getLong("visit_count"));
    return restroom;
  }
}
