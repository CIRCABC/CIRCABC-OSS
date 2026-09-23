package eu.europa.ec.digit.circabc.rest.service.compress;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import de.schlichtherle.util.zip.ZipEntry;
import org.junit.Before;
import org.junit.Test;

public class CompressedEntryTest {

  private CompressedEntry entry;
  private ZipEntry zipEntry;

  @Before
  public void setUp() {
    zipEntry = mock(ZipEntry.class);
    when(zipEntry.getName()).thenReturn("docs/readme.txt");
    when(zipEntry.getSize()).thenReturn(1024L);
    when(zipEntry.getCompressedSize()).thenReturn(512L);
    when(zipEntry.isDirectory()).thenReturn(false);
    when(zipEntry.getComment()).thenReturn("a comment");
    when(zipEntry.getCrc()).thenReturn(123456L);
    when(zipEntry.getTime()).thenReturn(1700000000L);
    entry = new CompressedEntryImpl(zipEntry);
  }

  @Test
  public void testGetFileName_whenFileEntry_thenReturnsName() {
    assertEquals("docs/readme.txt", entry.getFileName());
  }

  @Test
  public void testGetFileSize_whenFileEntry_thenReturnsSize() {
    assertEquals(1024L, entry.getFileSize());
  }

  @Test
  public void testGetFileCompressedSize_whenFileEntry_thenReturnsCompressedSize() {
    assertEquals(512L, entry.getFileCompressedSize());
  }

  @Test
  public void testIsDirectory_whenFileEntry_thenReturnsFalse() {
    assertFalse(entry.isDirectory());
  }

  @Test
  public void testIsDirectory_whenDirectoryEntry_thenReturnsTrue() {
    ZipEntry dirEntry = mock(ZipEntry.class);
    when(dirEntry.getName()).thenReturn("docs/");
    when(dirEntry.getSize()).thenReturn(0L);
    when(dirEntry.getCompressedSize()).thenReturn(0L);
    when(dirEntry.isDirectory()).thenReturn(true);
    when(dirEntry.getComment()).thenReturn(null);
    when(dirEntry.getCrc()).thenReturn(0L);
    when(dirEntry.getTime()).thenReturn(0L);

    CompressedEntry dirCompressedEntry = new CompressedEntryImpl(dirEntry);
    assertTrue(dirCompressedEntry.isDirectory());
  }

  @Test
  public void testGetComment_whenHasComment_thenReturnsComment() {
    assertEquals("a comment", entry.getComment());
  }

  @Test
  public void testGetComment_whenNullComment_thenReturnsNull() {
    ZipEntry noCommentEntry = mock(ZipEntry.class);
    when(noCommentEntry.getName()).thenReturn("file.txt");
    when(noCommentEntry.getSize()).thenReturn(0L);
    when(noCommentEntry.getCompressedSize()).thenReturn(0L);
    when(noCommentEntry.isDirectory()).thenReturn(false);
    when(noCommentEntry.getComment()).thenReturn(null);
    when(noCommentEntry.getCrc()).thenReturn(0L);
    when(noCommentEntry.getTime()).thenReturn(0L);

    CompressedEntry nullCommentEntry = new CompressedEntryImpl(noCommentEntry);
    assertNull(nullCommentEntry.getComment());
  }

  @Test
  public void testGetCrc_whenFileEntry_thenReturnsCrc() {
    assertEquals(123456L, entry.getCrc());
  }

  @Test
  public void testGetTime_whenFileEntry_thenReturnsTime() {
    assertEquals(1700000000L, entry.getTime());
  }

  @Test
  public void testToString_whenCalled_thenReturnsFileName() {
    assertEquals("docs/readme.txt", entry.toString());
  }
}
