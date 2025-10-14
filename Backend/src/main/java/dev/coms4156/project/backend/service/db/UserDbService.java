package dev.coms4156.project.backend.service.db;

import dev.coms4156.project.backend.model.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Database service for OAuth-backed users and their roles.
 */
@Service
public class UserDbService {

  private final JdbcTemplate jdbcTemplate;

  @Autowired
  public UserDbService(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  /**
   * Create or update a user record from OAuth profile attributes.
   */
  public void upsertUser(String subject,
                         String email,
                         String displayName,
                         String pictureUrl) {
    if (subject == null || subject.isBlank()) {
      throw new IllegalArgumentException("subject must not be blank");
    }
    int updated = jdbcTemplate.update("""
        UPDATE users
           SET email = ?,
               display_name = ?,
               picture_url = ?,
               last_login_at = CURRENT_TIMESTAMP,
               updated_at = CURRENT_TIMESTAMP
         WHERE subject = ?
        """,
        email,
        displayName,
        pictureUrl,
        subject);
    if (updated == 0) {
      jdbcTemplate.update("""
          INSERT INTO users (subject, email, display_name, picture_url, last_login_at)
          VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
          """,
          subject,
          email,
          displayName,
          pictureUrl);
    }
  }

  /**
   * Replace all roles for a subject with the provided set (normalized to upper-case).
   */
  public void replaceRoles(String subject, Set<String> roles) {
    jdbcTemplate.update("DELETE FROM user_roles WHERE subject = ?", subject);
    if (roles == null || roles.isEmpty()) {
      return;
    }
    Set<String> normalized = roles.stream()
        .filter(role -> role != null && !role.isBlank())
        .map(role -> role.toUpperCase())
        .collect(Collectors.toCollection(LinkedHashSet::new));
    if (normalized.isEmpty()) {
      return;
    }
    String insertSql = "INSERT INTO user_roles (subject, role) VALUES (?, ?)";
    List<Object[]> params = normalized.stream()
        .map(role -> new Object[]{subject, role})
        .toList();
    jdbcTemplate.batchUpdate(insertSql, params);
  }

  /**
   * Fetch all roles for a subject.
   */
  public Set<String> getRoles(String subject) {
    String sql = "SELECT role FROM user_roles WHERE subject = ? ORDER BY role ASC";
    return new LinkedHashSet<>(jdbcTemplate.query(sql,
        (rs, rowNum) -> rs.getString("role"),
        subject));
  }

  /**
   * Load a user, including roles.
   */
  public Optional<User> findBySubject(String subject) {
    String sql = """
        SELECT subject, email, display_name, picture_url, company_id,
            last_login_at, created_at, updated_at
        FROM users
        WHERE subject = ?
        """;
    try {
      User user = jdbcTemplate.queryForObject(sql, this::mapUser, subject);
      if (user != null) {
        user.setRoles(getRoles(subject));
      }
      return Optional.ofNullable(user);
    } catch (EmptyResultDataAccessException ex) {
      return Optional.empty();
    }
  }

  private User mapUser(ResultSet rs, int rowNum) throws SQLException {
    User user = new User();
    user.setSubject(rs.getString("subject"));
    user.setEmail(rs.getString("email"));
    user.setDisplayName(rs.getString("display_name"));
    user.setPictureUrl(rs.getString("picture_url"));
    long companyId = rs.getLong("company_id");
    if (!rs.wasNull()) {
      user.setCompanyId(companyId);
    }
    user.setLastLoginAt(toInstant(rs.getTimestamp("last_login_at")));
    user.setCreatedAt(toInstant(rs.getTimestamp("created_at")));
    user.setUpdatedAt(toInstant(rs.getTimestamp("updated_at")));
    return user;
  }

  private Instant toInstant(Timestamp ts) {
    return ts == null ? null : ts.toInstant();
  }

  /**
   * Returns the company identifier linked to the user, if any.
   */
  public Optional<Long> findCompanyId(String subject) {
    String sql = "SELECT company_id FROM users WHERE subject = ?";
    try {
      Long value = jdbcTemplate.queryForObject(sql, Long.class, subject);
      return value == null ? Optional.empty() : Optional.of(value);
    } catch (EmptyResultDataAccessException ex) {
      return Optional.empty();
    }
  }

  /**
   * Assigns a company to the user and ensures the COMPANY role is present.
   */
  public void assignCompany(String subject, Long companyId) {
    if (companyId == null) {
      return;
    }
    jdbcTemplate.update("""
        UPDATE users
           SET company_id = ?,
               updated_at = CURRENT_TIMESTAMP
         WHERE subject = ?
        """,
        companyId,
        subject);
    Set<String> roles = new LinkedHashSet<>(getRoles(subject));
    roles.add("COMPANY");
    replaceRoles(subject, roles);
  }
}
