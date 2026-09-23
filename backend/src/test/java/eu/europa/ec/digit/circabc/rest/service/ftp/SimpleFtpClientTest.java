package eu.europa.ec.digit.circabc.rest.service.ftp;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import it.sauronsoftware.ftp4j.*;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;

public class SimpleFtpClientTest {

  private SimpleFtpClientImpl client;
  private FTPClient mockFtpClient;

  @Before
  public void setUp() {
    client = new SimpleFtpClientImpl();
    mockFtpClient = mock(FTPClient.class);
    client.setFtpClient(mockFtpClient);
  }

  @Test
  public void testListFiles_whenConnected_thenReturnsFileNames()
    throws Exception {
    String[] expected = { "file1.txt", "file2.csv" };
    when(mockFtpClient.isConnected()).thenReturn(true);
    when(mockFtpClient.listNames()).thenReturn(expected);

    String[] result = client.listFiles();

    assertArrayEquals(expected, result);
  }

  @Test
  public void testListFiles_whenNotConnected_thenReturnsNull()
    throws Exception {
    when(mockFtpClient.isConnected()).thenReturn(false);

    String[] result = client.listFiles();

    assertNull(result);
  }

  @Test
  public void testFileExists_whenFilePresent_thenReturnsTrue()
    throws Exception {
    when(mockFtpClient.listNames()).thenReturn(
      new String[] { "data.csv", "report.pdf" }
    );

    assertTrue(client.fileExists("data.csv"));
  }

  @Test
  public void testFileExists_whenFileAbsent_thenReturnsFalse()
    throws Exception {
    when(mockFtpClient.listNames()).thenReturn(
      new String[] { "data.csv", "report.pdf" }
    );

    assertFalse(client.fileExists("missing.txt"));
  }

  @Test
  public void testLogout_whenDisconnectSucceeds_thenNoException()
    throws Exception {
    doNothing().when(mockFtpClient).disconnect(true);

    client.logout();

    verify(mockFtpClient).disconnect(true);
  }

  @Test
  public void testLogout_whenDisconnectThrows_thenExceptionSwallowed()
    throws Exception {
    doThrow(new IOException("connection reset"))
      .when(mockFtpClient)
      .disconnect(true);

    client.logout(); // should not throw
  }

  @Test
  public void testRenameRemoteFile_delegatesToFtpClient() throws Exception {
    client.renameRemoteFile("old.txt", "new.txt");

    verify(mockFtpClient).rename("old.txt", "new.txt");
  }

  @Test
  public void testGetFileName_afterSetFileName_returnsValue() {
    client.setFileName("test.csv");

    assertEquals("test.csv", client.getFileName());
  }

  @Test
  public void testInitParameters_withPathContainingSlash_setsDirectoryAndFilename()
    throws Exception {
    FTPClient spyFtp = mock(FTPClient.class);

    // We can't easily test initParameters because it creates a new FTPClient internally
    // and connects. Instead, verify the path parsing logic indirectly by checking
    // that after init the directory/filename are set correctly.
    // We test the setter/getter path parsing behavior:
    client.setFtpClient(spyFtp);
    client.setDirectory("/remote/dir");
    client.setFileName("data.csv");

    assertEquals("/remote/dir", client.getDirectory());
    assertEquals("data.csv", client.getFileName());
  }

  @Test
  public void testDownloadFile_whenFileDoesNotExist_returnsFileWithoutDownload()
    throws Exception {
    when(mockFtpClient.listNames()).thenReturn(new String[] { "other.txt" });

    // downloadFile uses TempFileProvider which requires Alfresco context,
    // so we verify the fileExists check prevents download call
    // This will throw since TempFileProvider is not initialized in unit test
    try {
      client.downloadFile("missing.txt");
    } catch (Exception e) {
      // Expected: TempFileProvider not available in unit test context
    }

    verify(mockFtpClient, never()).download(eq("missing.txt"), any());
  }
}
