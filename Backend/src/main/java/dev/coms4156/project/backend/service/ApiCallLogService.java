package dev.coms4156.project.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Persists API call audit records.
 */
@Service
public class ApiCallLogService {

  private final JdbcTemplate jdbcTemplate;

  @Autowired
  public ApiCallLogService(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  /**
   * Record an API invocation for auditing purposes.
   */
  public void record(String userSubject,
                     String httpMethod,
                     String requestedUrl,
                     int responseStatus,
                     long durationMs) {
    String sql = """
        INSERT INTO api_call_log (user_subject, http_method, requested_url, response_status, duration_ms)
        VALUES (?, ?, ?, ?, ?)
        """;
    jdbcTemplate.update(sql, userSubject, httpMethod, requestedUrl, responseStatus, durationMs);
  }
}
