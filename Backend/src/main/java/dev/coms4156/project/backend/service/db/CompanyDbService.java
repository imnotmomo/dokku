package dev.coms4156.project.backend.service.db;

import dev.coms4156.project.backend.model.Company;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Database access for companies/operators.
 */
@Service
public class CompanyDbService {

  private final JdbcTemplate jdbcTemplate;

  @Autowired
  public CompanyDbService(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  /**
   * List all companies.
   */
  public List<Company> findAll() {
    String sql = "SELECT id, name, created_at FROM company ORDER BY name";
    return jdbcTemplate.query(sql, this::mapCompany);
  }

  /**
   * Find a company by name (case insensitive).
   */
  public List<Company> searchByName(String query) {
    String sql = """
        SELECT id, name, created_at
          FROM company
         WHERE LOWER(name) LIKE LOWER(?)
         ORDER BY name
        """;
    String like = "%" + query + "%";
    return jdbcTemplate.query(sql, this::mapCompany, like);
  }

  /**
   * Find by identifier.
   */
  public Optional<Company> findById(Long id) {
    String sql = "SELECT id, name, created_at FROM company WHERE id = ?";
    try {
      return Optional.ofNullable(jdbcTemplate.queryForObject(sql, this::mapCompany, id));
    } catch (Exception ex) {
      return Optional.empty();
    }
  }

  private Company mapCompany(ResultSet rs, int rowNum) throws SQLException {
    Company company = new Company();
    company.setId(rs.getLong("id"));
    company.setName(rs.getString("name"));
    var ts = rs.getTimestamp("created_at");
    if (ts != null) {
      company.setCreatedAt(ts.toInstant());
    }
    return company;
  }
}
