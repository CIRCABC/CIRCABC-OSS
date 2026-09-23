package eu.europa.ec.digit.circabc.rest.service.compress;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import de.schlichtherle.util.zip.ZipEntry;
import org.junit.Test;

public class CompressedEntryImplTest {

  @Test
  public void testConstructor_whenValidZipEntry_thenFieldsPopulated() {
    ZipEntry zipEntry = mock(ZipEntry.class);
    when(zipEntry.getName()).thenReturn("docs/readme.txt");
    when(zipEntry.getSize()).thenReturn(1024L);
    when(zipEntry.getCompressedSize()).thenReturn(512L);
    when(zipEntry.getComment()).thenReturn("test comment");
    when(zipEntry.isDirectory()).thenReturn(false);
    when(zipEntry.getCrc()).thenReturn(12345L);
    when(zipEntry.getTime()).thenReturn(1000000L);

    CompressedEntryImpl entry = new CompressedEntryImpl(zipEntry);

    assertEquals("docs/readme.txt", entry.getFileName());
    assertEquals(1024L, entry.getFileSize());
    assertEquals(512L, entry.getFileCompressedSize());
    assertEquals("test comment", entry.getComment());
    assertEquals(12345L, entry.getCrc());
    assertEquals(1000000L, entry.getTime());
    assertFalse(entry.isDirectory());
  }

  @Test
  public void testIsDirectory_whenDirectoryEntry_thenReturnsTrue() {
    ZipEntry zipEntry = mock(ZipEntry.class);
    when(zipEntry.getName()).thenReturn("docs/");
    when(zipEntry.isDirectory()).thenReturn(true);

    CompressedEntryImpl entry = new CompressedEntryImpl(zipEntry);

    assertTrue(entry.isDirectory());
  }

  @Test
  public void testToString_returnsFileName() {
    ZipEntry zipEntry = mock(ZipEntry.class);
    when(zipEntry.getName()).thenReturn("file.txt");

    CompressedEntryImpl entry = new CompressedEntryImpl(zipEntry);

    assertEquals("file.txt", entry.toString());
  }

  @Test
  public void testConstructor_whenNullComment_thenCommentIsNull() {
    ZipEntry zipEntry = mock(ZipEntry.class);
    when(zipEntry.getName()).thenReturn("empty.dat");
    when(zipEntry.getComment()).thenReturn(null);
    when(zipEntry.getSize()).thenReturn(-1L);
    when(zipEntry.getCompressedSize()).thenReturn(-1L);
    when(zipEntry.getCrc()).thenReturn(-1L);
    when(zipEntry.getTime()).thenReturn(-1L);
    when(zipEntry.isDirectory()).thenReturn(false);

    CompressedEntryImpl entry = new CompressedEntryImpl(zipEntry);

    assertEquals("empty.dat", entry.getFileName());
    assertEquals(-1L, entry.getFileSize());
    assertEquals(-1L, entry.getFileCompressedSize());
    assertNull(entry.getComment());
    assertEquals(-1L, entry.getCrc());
  }
}
