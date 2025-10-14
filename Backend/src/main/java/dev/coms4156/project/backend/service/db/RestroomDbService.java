package dev.coms4156.project.backend.service.db;

import dev.coms4156.project.backend.model.Restroom;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;



/**
 * Database service for Restroom entity.
 */
@Service
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
    String sql = """
        SELECT r.*, c.name AS company_name
          FROM restroom r
          LEFT JOIN company c ON c.id = r.company_id
         WHERE r.id = ?
        """;
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
      SELECT r.*, c.name AS company_name,
          (6371000 * 2 * asin(sqrt(
              sin(radians((r.latitude - ?) / 2)) * sin(radians((r.latitude - ?) / 2)) +
              cos(radians(?)) * cos(radians(r.latitude)) *
              sin(radians((r.longitude - ?) / 2)) * sin(radians((r.longitude - ?) / 2))
          ))) as distance
      FROM restroom r
      LEFT JOIN company c ON c.id = r.company_id
      WHERE (6371000 * 2 * asin(sqrt(
              sin(radians((r.latitude - ?) / 2)) * sin(radians((r.latitude - ?) / 2)) +
              cos(radians(?)) * cos(radians(r.latitude)) *
              sin(radians((r.longitude - ?) / 2)) * sin(radians((r.longitude - ?) / 2))
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
        INSERT INTO restroom (name, address, latitude, longitude, company_id, hours_json, amenities, 
        avg_rating, visit_count)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        RETURNING id
        """;

    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(connection -> {
      PreparedStatement ps = connection.prepareStatement(sql, new String[] { "id" });
      ps.setString(1, restroom.getName());
      ps.setString(2, restroom.getAddress());
      ps.setDouble(3, restroom.getLatitude());
      ps.setDouble(4, restroom.getLongitude());
      if (restroom.getCompanyId() != null) {
        ps.setLong(5, restroom.getCompanyId());
      } else {
        ps.setNull(5, java.sql.Types.BIGINT);
      }
      ps.setString(6, restroom.getHoursJson());
      ps.setArray(7, connection.createArrayOf("text",
          restroom.getAmenities().toArray(new String[0])));
      ps.setDouble(8, restroom.getAvgRating());
      ps.setLong(9, restroom.getVisitCount());
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
    long companyId = rs.getLong("company_id");
    if (!rs.wasNull()) {
      restroom.setCompanyId(companyId);
    }
    restroom.setCompanyName(rs.getString("company_name"));
    restroom.setHoursJson(rs.getString("hours_json"));
    Array amenitiesArray = rs.getArray("amenities");
    if (amenitiesArray != null) {
      Object arrayObject = amenitiesArray.getArray();
      if (arrayObject instanceof String[] strings) {
        restroom.setAmenities(Arrays.asList(strings));
      } else if (arrayObject instanceof Object[] objects) {
        restroom.setAmenities(Arrays.stream(objects)
            .filter(obj -> obj != null)
            .map(Object::toString)
            .toList());
      } else {
        restroom.setAmenities(List.of());
      }
    } else {
      restroom.setAmenities(List.of());
    }
    restroom.setAvgRating(rs.getDouble("avg_rating"));
    restroom.setVisitCount(rs.getLong("visit_count"));
    return restroom;
  }
}
