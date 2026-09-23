package eu.europa.ec.digit.circabc.rest.service.bulk.indexes;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class IndexEntryTest {

  private IndexEntry indexEntry;

  @Before
  public void setUp() {
    indexEntry = new IndexEntryImpl("testHeader", "testValue");
  }

  @Test
  public void testGetHeaderName_whenCreated_thenReturnsHeaderName() {
    assertEquals("testHeader", indexEntry.getHeaderName());
  }

  @Test
  public void testGetValue_whenCreated_thenReturnsValue() {
    assertEquals("testValue", indexEntry.getValue());
  }

  @Test
  public void testSetValue_whenNewValue_thenValueIsUpdated() {
    indexEntry.setValue("newValue");
    assertEquals("newValue", indexEntry.getValue());
  }

  @Test
  public void testSetValue_whenNull_thenValueIsNull() {
    indexEntry.setValue(null);
    assertNull(indexEntry.getValue());
  }

  @Test
  public void testConstructor_whenNullHeaderName_thenGetHeaderNameReturnsNull() {
    IndexEntry entry = new IndexEntryImpl(null, "value");
    assertNull(entry.getHeaderName());
  }

  @Test
  public void testConstructor_whenEmptyStrings_thenReturnsEmptyStrings() {
    IndexEntry entry = new IndexEntryImpl("", "");
    assertEquals("", entry.getHeaderName());
    assertEquals("", entry.getValue());
  }
}
