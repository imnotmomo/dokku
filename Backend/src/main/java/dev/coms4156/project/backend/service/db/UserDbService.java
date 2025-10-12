package dev.coms4156.project.backend.service.db;

import dev.coms4156.project.backend.model.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;


/**
 * Database service for User entity.
 */
@Service
@Profile("!mock")
public class UserDbService {
  private final JdbcTemplate jdbcTemplate;

  @Autowired
  public UserDbService(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  /**
   * Create a new user.
   */
  public User create(User user) {
    String sql = """
        INSERT INTO users (username, password, role, token, refresh_token)
        VALUES (?, ?, ?, ?, ?)
        """;

    jdbcTemplate.update(sql,
        user.getUsername(),
        user.getPassword(),
        user.getRole(),
        user.getToken(),
        user.getRefreshToken());

    return user;
  }

  /**
   * Find user by username.
   */
  public Optional<User> findByUsername(String username) {
    String sql = "SELECT * FROM users WHERE username = ?";
    try {
      return Optional.of(jdbcTemplate.queryForObject(sql, this::mapUser, username));
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  /**
   * Find user by token.
   */
  public Optional<User> findByToken(String token) {
    String sql = "SELECT * FROM users WHERE token = ?";
    try {
      return Optional.of(jdbcTemplate.queryForObject(sql, this::mapUser, token));
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  /**
   * Find user by refresh token.
   */
  public Optional<User> findByRefreshToken(String refreshToken) {
    String sql = "SELECT * FROM users WHERE refresh_token = ?";
    try {
      return Optional.of(jdbcTemplate.queryForObject(sql, this::mapUser, refreshToken));
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  /**
   * Update user's tokens.
   */
  public void updateTokens(String username, String newToken, String newRefreshToken) {
    String sql = "UPDATE users SET token = ?, refresh_token = ? WHERE username = ?";
    jdbcTemplate.update(sql, newToken, newRefreshToken, username);
  }

  /**
   * Map database row to User object.
   */
  @SuppressWarnings("PMD.UnusedFormalParameter")
  private User mapUser(ResultSet rs, int rowNum) throws SQLException {
    User user = new User();
    user.setUsername(rs.getString("username"));
    user.setPassword(rs.getString("password"));
    user.setRole(rs.getString("role"));
    user.setToken(rs.getString("token"));
    user.setRefreshToken(rs.getString("refresh_token"));
    return user;
  }
}
