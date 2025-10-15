package dev.coms4156.project.backend.service.db;

import dev.coms4156.project.backend.model.CompanyAccount;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Persistence layer for company account applications.
 */
@Service
public class CompanyAccountDbService {

  private final JdbcTemplate jdbcTemplate;

  @Autowired
  public CompanyAccountDbService(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  /**
   * Replace any existing application for the subject with a new pending request.
   *
   * @param subject OAuth subject identifier
   * @param companyName submitted company name
   * @return the persisted application
   */
  public CompanyAccount createPending(String subject, String companyName) {
    jdbcTemplate.update("DELETE FROM third_party_account WHERE subject = ?", subject);
    jdbcTemplate.update("""
        INSERT INTO third_party_account (subject, company_name, status)
        VALUES (?, ?, 'PENDING')
        """,
        subject,
        companyName);
    return findBySubject(subject).orElseThrow();
  }

  /**
   * Find an application by OAuth subject.
   *
   * @param subject OAuth subject identifier
   * @return optional application
   */
  public Optional<CompanyAccount> findBySubject(String subject) {
    String sql = "SELECT * FROM third_party_account WHERE subject = ?";
    try {
      return Optional.ofNullable(jdbcTemplate.queryForObject(sql, this::map, subject));
    } catch (Exception ex) {
      return Optional.empty();
    }
  }

  /**
   * Find an application by its primary key identifier.
   *
   * @param id application identifier
   * @return optional application
   */
  public Optional<CompanyAccount> findById(Long id) {
    String sql = "SELECT * FROM third_party_account WHERE id = ?";
    try {
      return Optional.ofNullable(jdbcTemplate.queryForObject(sql, this::map, id));
    } catch (Exception ex) {
      return Optional.empty();
    }
  }

  /**
   * Retrieve applications filtered by a specific status.
   *
   * @param status status value to match
   * @return ordered list of matching applications
   */
  public List<CompanyAccount> findByStatus(String status) {
    String sql = "SELECT * FROM third_party_account WHERE status = ? ORDER BY requested_at";
    return jdbcTemplate.query(sql, this::map, status.toUpperCase(Locale.ROOT));
  }

  /**
   * Retrieve all applications ordered by request date.
   *
   * @return ordered list of applications
   */
  public List<CompanyAccount> findAll() {
    String sql = "SELECT * FROM third_party_account ORDER BY requested_at";
    return jdbcTemplate.query(sql, this::map);
  }

  /**
   * Update the status of an application.
   *
   * @param id application identifier
   * @param status new status value
   * @param decidedBy subject of the decider
   */
  public void updateStatus(Long id, String status, String decidedBy) {
    String normalized = status == null ? null : status.toUpperCase(Locale.ROOT);
    jdbcTemplate.update("""
        UPDATE third_party_account
           SET status = ?,
               decided_at = CURRENT_TIMESTAMP,
               decided_by = ?
         WHERE id = ?
        """,
        normalized,
        decidedBy,
        id);
  }

  private CompanyAccount map(ResultSet rs, int rowNum) throws SQLException {
    if (rowNum < 0) {
      throw new SQLException("Row index must not be negative");
    }
    CompanyAccount account = new CompanyAccount();
    account.setId(rs.getLong("id"));
    account.setSubject(rs.getString("subject"));
    account.setCompanyName(rs.getString("company_name"));
    account.setStatus(rs.getString("status"));
    Timestamp requested = rs.getTimestamp("requested_at");
    if (requested != null) {
      account.setRequestedAt(requested.toInstant());
    }
    Timestamp decided = rs.getTimestamp("decided_at");
    if (decided != null) {
      account.setDecidedAt(decided.toInstant());
    }
    account.setDecidedBy(rs.getString("decided_by"));
    return account;
  }
}
