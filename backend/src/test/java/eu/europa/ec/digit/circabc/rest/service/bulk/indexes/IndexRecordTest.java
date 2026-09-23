package eu.europa.ec.digit.circabc.rest.service.bulk.indexes;

import static org.junit.Assert.*;

import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class IndexRecordTest {

  private IndexRecordImpl record;

  @Before
  public void setUp() {
    record = new IndexRecordImpl(5);
  }

  @Test
  public void testGetRowNumber_returnsConstructorValue() {
    assertEquals(5, record.getRowNumber());
  }

  @Test
  public void testAddIndexEntry_andGetIndexEntries() {
    IndexEntry entry = new IndexEntryImpl("TITLE", "myTitle");
    record.addIndexEntry(entry);

    List<IndexEntry> entries = record.getIndexEntries();
    assertEquals(1, entries.size());
    assertEquals("TITLE", entries.get(0).getHeaderName());
    assertEquals("myTitle", entries.get(0).getValue());
  }

  @Test
  public void testGetEntry_whenExists_returnsEntry() {
    record.addIndexEntry(new IndexEntryImpl("AUTHOR", "John"));
    IndexEntry result = record.getEntry("AUTHOR");
    assertNotNull(result);
    assertEquals("John", result.getValue());
  }

  @Test
  public void testGetEntry_whenNotExists_returnsNull() {
    assertNull(record.getEntry("NONEXISTENT"));
  }

  @Test
  public void testSetName_replacesBackslashesOnUnix() {
    record.setName("path\\to\\file.txt");
    // On Unix (File.separatorChar == '/'), backslashes become forward slashes
    // On Windows (File.separatorChar == '\\'), forward slashes become backslashes
    String name = record.getName();
    assertNotNull(name);
    assertFalse(name.isEmpty());
  }

  @Test
  public void testSetTitle_andGetTitle() {
    record.setTitle("Test Title");
    assertEquals("Test Title", record.getTitle());
  }

  @Test
  public void testSetTitle_overwritesExistingValue() {
    record.setTitle("First");
    record.setTitle("Second");
    assertEquals("Second", record.getTitle());
    // Should not duplicate entries
    int count = 0;
    for (IndexEntry e : record.getIndexEntries()) {
      if (e.getHeaderName().equals(IndexHeaderColumn.TITLE)) count++;
    }
    assertEquals(1, count);
  }

  @Test
  public void testGetGeneric_whenNoEntry_returnsEmptyString() {
    assertEquals("", record.getDescription());
  }

  @Test
  public void testGetGeneric_whenEntryHasNullValue_returnsEmptyString() {
    record.addIndexEntry(new IndexEntryImpl(IndexHeaderColumn.KEYWORDS, null));
    assertEquals("", record.getKeywords());
  }

  @Test
  public void testDynamicProperty_setAndGet() {
    record.setDynamicProperty(3, "dynValue");
    assertEquals("dynValue", record.getDynamicProperty(3));
  }

  @Test
  public void testDynamicProperty_whenNotSet_returnsEmptyString() {
    assertEquals("", record.getDynamicProperty(99));
  }

  @Test
  public void testToString_containsRowNumber() {
    record.setTitle("MyDoc");
    String str = record.toString();
    assertTrue(str.contains("Row Number=5"));
    assertTrue(str.contains("TITLE=MyDoc"));
  }
}
