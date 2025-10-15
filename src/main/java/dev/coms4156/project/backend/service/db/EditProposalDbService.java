package dev.coms4156.project.backend.service.db;

import dev.coms4156.project.backend.model.EditProposal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;


/**
 * Database service for EditProposal entity.
 */
@Service
public class EditProposalDbService {
  private final JdbcTemplate jdbcTemplate;

  @Autowired
  public EditProposalDbService(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  /**
   * Create a new edit proposal.
   */
  public EditProposal create(EditProposal proposal) {
    String sql = """
        INSERT INTO edit_proposal (
          restroom_id, proposed_name, proposed_address, proposed_hours,
          proposed_amenities, proposer_user_id, status
        )
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(connection -> {
      PreparedStatement ps = connection.prepareStatement(sql, new String[] { "id" });
      ps.setLong(1, proposal.getRestroomId());
      ps.setString(2, proposal.getProposedName());
      ps.setString(3, proposal.getProposedAddress());
      ps.setString(4, proposal.getProposedHours());
      ps.setString(5, proposal.getProposedAmenities());
      ps.setString(6, proposal.getProposerUserId());
      ps.setString(7, proposal.getStatus());
      return ps;
    }, keyHolder);

    Number generatedId = keyHolder.getKey();
    if (generatedId != null) {
      proposal.setId(generatedId.longValue());
      Timestamp createdAt = jdbcTemplate.queryForObject(
          "SELECT created_at FROM edit_proposal WHERE id = ?",
          Timestamp.class,
          proposal.getId());
      if (createdAt != null) {
        proposal.setCreatedAt(createdAt.toInstant());
      }
    }

    return proposal;
  }

  /**
   * Get all pending edits for a restroom.
   */
  public List<EditProposal> getPendingByRestroomId(Long restroomId) {
    String sql = """
        SELECT * FROM edit_proposal
        WHERE restroom_id = ? AND status = 'PENDING'
        ORDER BY created_at DESC
        """;
    return jdbcTemplate.query(sql, this::mapEditProposal, restroomId);
  }

  /**
   * Update proposal status.
   */
  public void updateStatus(Long id, String status) {
    String sql = "UPDATE edit_proposal SET status = ? WHERE id = ?";
    jdbcTemplate.update(sql, status, id);
  }

  /**
   * Get proposal by ID.
   */
  public Optional<EditProposal> getById(Long id) {
    String sql = "SELECT * FROM edit_proposal WHERE id = ?";
    try {
      return Optional.of(jdbcTemplate.queryForObject(sql, this::mapEditProposal, id));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  /**
   * Map database row to EditProposal object.
   */
  private EditProposal mapEditProposal(ResultSet rs, int rowNum) throws SQLException {
    if (rowNum < 0) {
      throw new SQLException("Row index must not be negative");
    }
    EditProposal proposal = new EditProposal();
    proposal.setId(rs.getLong("id"));
    proposal.setRestroomId(rs.getLong("restroom_id"));
    proposal.setProposedName(rs.getString("proposed_name"));
    proposal.setProposedAddress(rs.getString("proposed_address"));
    proposal.setProposedHours(rs.getString("proposed_hours"));
    proposal.setProposedAmenities(rs.getString("proposed_amenities"));
    proposal.setProposerUserId(rs.getString("proposer_user_id"));
    proposal.setStatus(rs.getString("status"));
    proposal.setCreatedAt(rs.getTimestamp("created_at").toInstant());
    return proposal;
  }
}
