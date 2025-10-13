package dev.coms4156.project.backend.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for Restroom model class.
 */
public class RestroomUnitTests {

  @Test
  public void testBasicGettersAndSetters() {
    Restroom restroom = new Restroom();

    restroom.setId(123L);
    restroom.setName("Test Restroom");
    restroom.setAddress("123 Test St");
    restroom.setLatitude(40.7536);
    restroom.setLongitude(-73.9832);
    restroom.setHours("08:00-18:00");
    restroom.setAvgRating(4.5);
    restroom.setVisitCount(42L);

    assertEquals(123L, restroom.getId());
    assertEquals("Test Restroom", restroom.getName());
    assertEquals("123 Test St", restroom.getAddress());
    assertEquals(40.7536, restroom.getLatitude());
    assertEquals(-73.9832, restroom.getLongitude());
    assertEquals("08:00-18:00", restroom.getHours());
    assertEquals(4.5, restroom.getAvgRating());
    assertEquals(42L, restroom.getVisitCount());
  }

  @Test
  public void testSetAmenitiesWithList() {
    Restroom restroom = new Restroom();
    List<String> amenities = List.of("wheelchair", "family");

    restroom.setAmenities(amenities);

    assertEquals(2, restroom.getAmenities().size());
    assertTrue(restroom.getAmenities().contains("wheelchair"));
    assertTrue(restroom.getAmenities().contains("family"));
  }

  @Test
  public void testSetAmenitiesJsonWithCommaSeparatedString() {
    Restroom restroom = new Restroom();

    restroom.setAmenitiesJson("wheelchair,family,baby-changing");

    assertEquals(3, restroom.getAmenities().size());
    assertTrue(restroom.getAmenities().contains("wheelchair"));
    assertTrue(restroom.getAmenities().contains("family"));
    assertTrue(restroom.getAmenities().contains("baby-changing"));
  }

  @Test
  public void testSetAmenitiesJsonWithWhitespace() {
    Restroom restroom = new Restroom();

    restroom.setAmenitiesJson("  wheelchair  ,  family  ");

    assertEquals(2, restroom.getAmenities().size());
    assertTrue(restroom.getAmenities().contains("wheelchair"));
    assertTrue(restroom.getAmenities().contains("family"));
  }

  @Test
  public void testSetAmenitiesJsonWithEmptyString() {
    Restroom restroom = new Restroom();

    restroom.setAmenitiesJson("");

    assertTrue(restroom.getAmenities().isEmpty());
  }

  @Test
  public void testSetAmenitiesJsonWithNull() {
    Restroom restroom = new Restroom();

    restroom.setAmenitiesJson(null);

    assertNotNull(restroom.getAmenities());
    assertTrue(restroom.getAmenities().isEmpty());
  }

  @Test
  public void testSetAmenitiesJsonWithInvalidType() {
    Restroom restroom = new Restroom();

    assertThrows(IllegalArgumentException.class, () -> {
      restroom.setAmenitiesJson(123);
    });
  }

  @Test
  public void testAmenitiesListIsUnmodifiable() {
    Restroom restroom = new Restroom();
    restroom.setAmenities(List.of("wheelchair"));

    assertThrows(UnsupportedOperationException.class, () -> {
      restroom.getAmenities().add("family");
    });
  }

  @Test
  public void testSetAmenitiesWithNullList() {
    Restroom restroom = new Restroom();

    restroom.setAmenities(null);

    assertNotNull(restroom.getAmenities());
    assertTrue(restroom.getAmenities().isEmpty());
  }
}
