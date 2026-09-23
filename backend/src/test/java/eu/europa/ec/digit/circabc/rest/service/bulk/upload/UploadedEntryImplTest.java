package eu.europa.ec.digit.circabc.rest.service.bulk.upload;

import static org.junit.Assert.*;

import java.io.File;
import org.junit.Before;
import org.junit.Test;

public class UploadedEntryImplTest {

  private UploadedEntryImpl entry;

  @Before
  public void setUp() {
    entry = new UploadedEntryImpl();
  }

  @Test
  public void testDefaultConstructor_thenAllFieldsNull() {
    assertNull(entry.getFilePath());
    assertNull(entry.getFileName());
    assertNull(entry.getStatus());
    assertNull(entry.getRemarks());
  }

  @Test
  public void testConstructor_whenFileNameAndPath_thenFieldsSet() {
    UploadedEntryImpl e = new UploadedEntryImpl("doc.pdf", "/tmp/docs");
    assertEquals("doc.pdf", e.getFileName());
    assertEquals("/tmp/docs", e.getFilePath());
  }

  @Test
  public void testSetFilePath_whenBackslashes_thenConvertedToSystemSeparator() {
    entry.setFilePath("folder\\subfolder\\file.txt");
    if (File.separatorChar == '/') {
      assertEquals("folder/subfolder/file.txt", entry.getFilePath());
    } else {
      assertEquals("folder\\subfolder\\file.txt", entry.getFilePath());
    }
  }

  @Test
  public void testSetFilePath_whenForwardSlashes_thenConvertedToSystemSeparator() {
    entry.setFilePath("folder/subfolder/file.txt");
    if (File.separatorChar == '/') {
      assertEquals("folder/subfolder/file.txt", entry.getFilePath());
    } else {
      assertEquals("folder\\subfolder\\file.txt", entry.getFilePath());
    }
  }

  @Test
  public void testSetFileName_thenGetFileName() {
    entry.setFileName("report.docx");
    assertEquals("report.docx", entry.getFileName());
  }

  @Test
  public void testSetStatus_thenGetStatus() {
    entry.setStatus("SUCCESS");
    assertEquals("SUCCESS", entry.getStatus());
  }

  @Test
  public void testSetRemarks_thenGetRemarks() {
    entry.setRemarks("Uploaded successfully");
    assertEquals("Uploaded successfully", entry.getRemarks());
  }

  @Test
  public void testSetFileName_whenNull_thenGetReturnsNull() {
    entry.setFileName(null);
    assertNull(entry.getFileName());
  }

  @Test
  public void testSetStatus_whenNull_thenGetReturnsNull() {
    entry.setStatus(null);
    assertNull(entry.getStatus());
  }

  @Test
  public void testSetRemarks_whenNull_thenGetReturnsNull() {
    entry.setRemarks(null);
    assertNull(entry.getRemarks());
  }
}
