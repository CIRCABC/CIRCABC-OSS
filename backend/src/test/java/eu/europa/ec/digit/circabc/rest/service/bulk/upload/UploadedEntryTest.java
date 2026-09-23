package eu.europa.ec.digit.circabc.rest.service.bulk.upload;

import static org.junit.Assert.*;

import java.io.File;
import org.junit.Before;
import org.junit.Test;

public class UploadedEntryTest {

  private UploadedEntry entry;

  @Before
  public void setUp() {
    entry = new UploadedEntryImpl();
  }

  @Test
  public void testConstructor_whenFileNameAndPath_thenFieldsSet() {
    UploadedEntry e = new UploadedEntryImpl("doc.pdf", "/tmp/docs");
    assertEquals("doc.pdf", e.getFileName());
    assertEquals("/tmp/docs", e.getFilePath());
  }

  @Test
  public void testSetFilePath_whenUnixSeparator_thenBackslashesConverted() {
    entry.setFilePath("folder\\subfolder\\file.txt");
    String expected;
    if (File.separatorChar == '/') {
      expected = "folder/subfolder/file.txt";
    } else {
      expected = "folder\\subfolder\\file.txt";
    }
    assertEquals(expected, entry.getFilePath());
  }

  @Test
  public void testSetFilePath_whenForwardSlash_thenConvertedOnWindows() {
    entry.setFilePath("folder/subfolder/file.txt");
    String expected;
    if (File.separatorChar == '/') {
      expected = "folder/subfolder/file.txt";
    } else {
      expected = "folder\\subfolder\\file.txt";
    }
    assertEquals(expected, entry.getFilePath());
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
  public void testDefaultConstructor_thenAllFieldsNull() {
    assertNull(entry.getFilePath());
    assertNull(entry.getFileName());
    assertNull(entry.getStatus());
    assertNull(entry.getRemarks());
  }
}
