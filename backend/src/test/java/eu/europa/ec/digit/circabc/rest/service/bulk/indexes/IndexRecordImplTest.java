package eu.europa.ec.digit.circabc.rest.service.bulk.indexes;

import static org.junit.Assert.*;

import java.io.File;
import org.junit.Before;
import org.junit.Test;

public class IndexRecordImplTest {

  private IndexRecordImpl record;

  @Before
  public void setUp() {
    record = new IndexRecordImpl(5);
  }

  @Test
  public void testGetRowNumber() {
    assertEquals(5, record.getRowNumber());
  }

  @Test
  public void testSetAndGetTitle() {
    record.setTitle("My Title");
    assertEquals("My Title", record.getTitle());
  }

  @Test
  public void testSetTitle_overwritesExistingValue() {
    record.setTitle("First");
    record.setTitle("Second");
    assertEquals("Second", record.getTitle());
  }

  @Test
  public void testGetTitle_whenNotSet_returnsEmptyString() {
    assertEquals("", record.getTitle());
  }

  @Test
  public void testSetName_replacesBackslashOnUnix() {
    record.setName("path\\to\\file.txt");
    String expected =
      File.separatorChar == '/' ? "path/to/file.txt" : "path\\to\\file.txt";
    assertEquals(expected, record.getName());
  }

  @Test
  public void testSetName_replacesForwardSlashOnWindows() {
    record.setName("path/to/file.txt");
    String expected =
      File.separatorChar == '/' ? "path/to/file.txt" : "path\\to\\file.txt";
    assertEquals(expected, record.getName());
  }

  @Test
  public void testGetEntry_whenExists_returnsEntry() {
    record.setAuthor("John");
    IndexEntry entry = record.getEntry(IndexHeaderColumn.AUTHOR);
    assertNotNull(entry);
    assertEquals("John", entry.getValue());
  }

  @Test
  public void testGetEntry_whenNotExists_returnsNull() {
    assertNull(record.getEntry(IndexHeaderColumn.AUTHOR));
  }

  @Test
  public void testAddIndexEntry() {
    IndexEntry entry = new IndexEntryImpl("CUSTOM", "val");
    record.addIndexEntry(entry);
    assertEquals(1, record.getIndexEntries().size());
    assertEquals("val", record.getIndexEntries().get(0).getValue());
  }

  @Test
  public void testGetDynamicProperty_whenNotSet_returnsEmptyString() {
    assertEquals("", record.getDynamicProperty(3));
  }

  @Test
  public void testSetAndGetDynamicProperty() {
    record.setDynamicProperty(1, "dynValue");
    assertEquals("dynValue", record.getDynamicProperty(1));
  }

  @Test
  public void testSetGeneric_withNullValue_returnsEmptyString() {
    record.setDescription(null);
    assertEquals("", record.getDescription());
  }

  @Test
  public void testToString_containsRowNumber() {
    record.setTitle("T");
    String result = record.toString();
    assertTrue(result.contains("Row Number=5"));
    assertTrue(result.contains("TITLE=T"));
  }
}
