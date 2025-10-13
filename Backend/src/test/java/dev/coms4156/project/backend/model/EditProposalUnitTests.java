package dev.coms4156.project.backend.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for EditProposal model class.
 */
public class EditProposalUnitTests {

  @Test
  public void shouldSetAndGetId() {
    EditProposal proposal = new EditProposal();

    proposal.setId(123L);

    assertEquals(123L, proposal.getId());
  }

  @Test
  public void shouldSetAndGetRestroomId() {
    EditProposal proposal = new EditProposal();

    proposal.setRestroomId(456L);

    assertEquals(456L, proposal.getRestroomId());
  }

  @Test
  public void shouldSetAndGetProposedName() {
    EditProposal proposal = new EditProposal();
    String expectedName = "Updated Restroom Name";

    proposal.setProposedName(expectedName);

    assertEquals(expectedName, proposal.getProposedName());
  }

  @Test
  public void shouldSetAndGetProposedAddress() {
    EditProposal proposal = new EditProposal();
    String expectedAddress = "123 Updated St, New York, NY 10001";

    proposal.setProposedAddress(expectedAddress);

    assertEquals(expectedAddress, proposal.getProposedAddress());
  }

  @Test
  public void shouldSetAndGetProposedHours() {
    EditProposal proposal = new EditProposal();
    String expectedHours = "09:00-20:00";

    proposal.setProposedHours(expectedHours);

    assertEquals(expectedHours, proposal.getProposedHours());
  }

  @Test
  public void shouldSetAndGetProposedAmenities() {
    EditProposal proposal = new EditProposal();
    String expectedAmenities = "wheelchair,family,baby-changing";

    proposal.setProposedAmenities(expectedAmenities);

    assertEquals(expectedAmenities, proposal.getProposedAmenities());
  }

  @Test
  public void shouldSetAndGetProposerUserId() {
    EditProposal proposal = new EditProposal();
    String expectedUserId = "proposer@example.com";

    proposal.setProposerUserId(expectedUserId);

    assertEquals(expectedUserId, proposal.getProposerUserId());
  }

  @Test
  public void shouldSetAndGetStatus() {
    EditProposal proposal = new EditProposal();
    String expectedStatus = "PENDING";

    proposal.setStatus(expectedStatus);

    assertEquals(expectedStatus, proposal.getStatus());
  }

  @Test
  public void shouldSetAndGetCreatedAt() {
    EditProposal proposal = new EditProposal();
    Instant expectedTime = Instant.now();

    proposal.setCreatedAt(expectedTime);

    assertEquals(expectedTime, proposal.getCreatedAt());
  }

  @Test
  public void shouldHandleNullId() {
    EditProposal proposal = new EditProposal();

    proposal.setId(null);

    assertNull(proposal.getId());
  }

  @Test
  public void shouldHandleNullRestroomId() {
    EditProposal proposal = new EditProposal();

    proposal.setRestroomId(null);

    assertNull(proposal.getRestroomId());
  }

  @Test
  public void shouldHandleNullProposedName() {
    EditProposal proposal = new EditProposal();

    proposal.setProposedName(null);

    assertNull(proposal.getProposedName());
  }

  @Test
  public void shouldHandleNullProposedAddress() {
    EditProposal proposal = new EditProposal();

    proposal.setProposedAddress(null);

    assertNull(proposal.getProposedAddress());
  }

  @Test
  public void shouldHandleNullProposedHours() {
    EditProposal proposal = new EditProposal();

    proposal.setProposedHours(null);

    assertNull(proposal.getProposedHours());
  }

  @Test
  public void shouldHandleNullProposedAmenities() {
    EditProposal proposal = new EditProposal();

    proposal.setProposedAmenities(null);

    assertNull(proposal.getProposedAmenities());
  }

  @Test
  public void shouldHandleNullProposerUserId() {
    EditProposal proposal = new EditProposal();

    proposal.setProposerUserId(null);

    assertNull(proposal.getProposerUserId());
  }

  @Test
  public void shouldHandleNullStatus() {
    EditProposal proposal = new EditProposal();

    proposal.setStatus(null);

    assertNull(proposal.getStatus());
  }

  @Test
  public void shouldHandleNullCreatedAt() {
    EditProposal proposal = new EditProposal();

    proposal.setCreatedAt(null);

    assertNull(proposal.getCreatedAt());
  }

  @Test
  public void shouldHandleEmptyStringFields() {
    EditProposal proposal = new EditProposal();

    proposal.setProposedName("");
    proposal.setProposedAddress("");
    proposal.setProposedHours("");
    proposal.setProposedAmenities("");
    proposal.setProposerUserId("");
    proposal.setStatus("");

    assertEquals("", proposal.getProposedName());
    assertEquals("", proposal.getProposedAddress());
    assertEquals("", proposal.getProposedHours());
    assertEquals("", proposal.getProposedAmenities());
    assertEquals("", proposal.getProposerUserId());
    assertEquals("", proposal.getStatus());
  }

  @Test
  public void shouldHandleBlankStringFields() {
    EditProposal proposal = new EditProposal();

    proposal.setProposedName("   ");
    proposal.setProposedAddress("   ");
    proposal.setStatus("   ");

    assertEquals("   ", proposal.getProposedName());
    assertEquals("   ", proposal.getProposedAddress());
    assertEquals("   ", proposal.getStatus());
  }

  @Test
  public void shouldAcceptValidStatusValues() {
    EditProposal proposal = new EditProposal();

    proposal.setStatus("PENDING");
    assertEquals("PENDING", proposal.getStatus());

    proposal.setStatus("APPROVED");
    assertEquals("APPROVED", proposal.getStatus());

    proposal.setStatus("REJECTED");
    assertEquals("REJECTED", proposal.getStatus());
  }

  @Test
  public void shouldAcceptInvalidStatusValues() {
    EditProposal proposal = new EditProposal();

    // Model doesn't validate - validation happens elsewhere
    proposal.setStatus("INVALID_STATUS");
    assertEquals("INVALID_STATUS", proposal.getStatus());

    proposal.setStatus("pending"); // lowercase
    assertEquals("pending", proposal.getStatus());
  }

  @Test
  public void shouldCreateCompleteEditProposal() {
    EditProposal proposal = new EditProposal();
    Instant now = Instant.now();

    proposal.setId(1L);
    proposal.setRestroomId(100L);
    proposal.setProposedName("New Restroom Name");
    proposal.setProposedAddress("456 New St, Brooklyn, NY 11201");
    proposal.setProposedHours("08:00-22:00");
    proposal.setProposedAmenities("wheelchair,family,baby-changing,gender-neutral");
    proposal.setProposerUserId("editor@example.com");
    proposal.setStatus("PENDING");
    proposal.setCreatedAt(now);

    assertEquals(1L, proposal.getId());
    assertEquals(100L, proposal.getRestroomId());
    assertEquals("New Restroom Name", proposal.getProposedName());
    assertEquals("456 New St, Brooklyn, NY 11201", proposal.getProposedAddress());
    assertEquals("08:00-22:00", proposal.getProposedHours());
    assertEquals("wheelchair,family,baby-changing,gender-neutral", proposal.getProposedAmenities());
    assertEquals("editor@example.com", proposal.getProposerUserId());
    assertEquals("PENDING", proposal.getStatus());
    assertEquals(now, proposal.getCreatedAt());
  }

  @Test
  public void shouldCreatePartialEditProposal() {
    EditProposal proposal = new EditProposal();

    //only updating name and hours
    proposal.setRestroomId(100L);
    proposal.setProposedName("Updated Name Only");
    proposal.setProposedHours("24/7");
    proposal.setProposerUserId("editor@example.com");
    proposal.setStatus("PENDING");

    assertEquals(100L, proposal.getRestroomId());
    assertEquals("Updated Name Only", proposal.getProposedName());
    assertEquals("24/7", proposal.getProposedHours());
    assertEquals("editor@example.com", proposal.getProposerUserId());
    assertEquals("PENDING", proposal.getStatus());

    //other fields should be null
    assertNull(proposal.getId());
    assertNull(proposal.getProposedAddress());
    assertNull(proposal.getProposedAmenities());
    assertNull(proposal.getCreatedAt());
  }

  @Test
  public void shouldHandleLongProposedFields() {
    EditProposal proposal = new EditProposal();
    String longName = "Very Long Restroom Name ".repeat(10);
    String longAddress = "123 Very Long Street Name That Goes On And On ".repeat(5);
    String longAmenities = "wheelchair,family,baby-changing,gender-neutral,".repeat(20);

    proposal.setProposedName(longName);
    proposal.setProposedAddress(longAddress);
    proposal.setProposedAmenities(longAmenities);

    assertEquals(longName, proposal.getProposedName());
    assertEquals(longAddress, proposal.getProposedAddress());
    assertEquals(longAmenities, proposal.getProposedAmenities());
  }

  @Test
  public void shouldHandleSpecialCharactersInFields() {
    EditProposal proposal = new EditProposal();

    proposal.setProposedName("Café & Restaurant Restroom 🚻");
    proposal.setProposedAddress("123 Münch St, São Paulo, Brazil");
    proposal.setProposerUserId("user+test@example.com");

    assertEquals("Café & Restaurant Restroom 🚻", proposal.getProposedName());
    assertEquals("123 Münch St, São Paulo, Brazil", proposal.getProposedAddress());
    assertEquals("user+test@example.com", proposal.getProposerUserId());
  }

  @Test
  public void shouldHandleVariousHourFormats() {
    EditProposal proposal = new EditProposal();

    proposal.setProposedHours("24/7");
    assertEquals("24/7", proposal.getProposedHours());

    proposal.setProposedHours("Mon-Fri: 9AM-5PM, Sat-Sun: 10AM-6PM");
    assertEquals("Mon-Fri: 9AM-5PM, Sat-Sun: 10AM-6PM", proposal.getProposedHours());

    proposal.setProposedHours("Closed");
    assertEquals("Closed", proposal.getProposedHours());
  }

  @Test
  public void shouldHandleInstantPrecision() {
    EditProposal proposal = new EditProposal();
    Instant preciseTime = Instant.parse("2023-12-25T10:15:30.123456789Z");

    proposal.setCreatedAt(preciseTime);

    assertEquals(preciseTime, proposal.getCreatedAt());
  }

  @Test
  public void shouldInitializeWithNullValues() {
    EditProposal proposal = new EditProposal();

    assertNull(proposal.getId());
    assertNull(proposal.getRestroomId());
    assertNull(proposal.getProposedName());
    assertNull(proposal.getProposedAddress());
    assertNull(proposal.getProposedHours());
    assertNull(proposal.getProposedAmenities());
    assertNull(proposal.getProposerUserId());
    assertNull(proposal.getStatus());
    assertNull(proposal.getCreatedAt());
  }

  @Test
  public void shouldHandleStatusTransitions() {
    EditProposal proposal = new EditProposal();

    // Simulate status workflow
    proposal.setStatus("PENDING");
    assertEquals("PENDING", proposal.getStatus());

    proposal.setStatus("APPROVED");
    assertEquals("APPROVED", proposal.getStatus());

    //could also go to REJECTED
    proposal.setStatus("REJECTED");
    assertEquals("REJECTED", proposal.getStatus());
  }
}
