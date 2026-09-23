package eu.europa.ec.digit.circabc.rest.service.compress;

import static org.junit.Assert.*;

import org.junit.Test;

public class CompressedEntriesTest {

  @Test
  public void testIsMarkerInterface() {
    assertEquals(0, CompressedEntries.class.getDeclaredMethods().length);
  }

  @Test
  public void testCanBeImplemented() {
    CompressedEntries instance = new CompressedEntries() {};
    assertNotNull(instance);
    assertTrue(instance instanceof CompressedEntries);
  }
}
