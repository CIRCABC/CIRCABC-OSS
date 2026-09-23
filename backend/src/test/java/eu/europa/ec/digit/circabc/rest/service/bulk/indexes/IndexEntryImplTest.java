package eu.europa.ec.digit.circabc.rest.service.bulk.indexes;

import static org.junit.Assert.*;

import org.junit.Test;

public class IndexEntryImplTest {

  @Test
  public void testConstructor_whenValidArgs_thenFieldsSet() {
    IndexEntryImpl entry = new IndexEntryImpl("header1", "value1");
    assertEquals("header1", entry.getHeaderName());
    assertEquals("value1", entry.getValue());
  }

  @Test
  public void testConstructor_whenNullArgs_thenFieldsNull() {
    IndexEntryImpl entry = new IndexEntryImpl(null, null);
    assertNull(entry.getHeaderName());
    assertNull(entry.getValue());
  }

  @Test
  public void testSetValue_whenCalled_thenUpdatesValue() {
    IndexEntryImpl entry = new IndexEntryImpl("header1", "original");
    entry.setValue("updated");
    assertEquals("updated", entry.getValue());
  }

  @Test
  public void testSetValue_whenNull_thenValueIsNull() {
    IndexEntryImpl entry = new IndexEntryImpl("header1", "original");
    entry.setValue(null);
    assertNull(entry.getValue());
  }

  @Test
  public void testImplementsIndexEntry() {
    IndexEntryImpl entry = new IndexEntryImpl("h", "v");
    assertTrue(entry instanceof IndexEntry);
  }
}
