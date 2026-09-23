package eu.europa.ec.digit.circabc.rest.service.ftp;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import it.sauronsoftware.ftp4j.FTPClient;
import org.junit.Before;
import org.junit.Test;

public class SimpleFtpClientImplTest {

  private SimpleFtpClientImpl client;
  private FTPClient mockFtpClient;

  @Before
  public void setUp() {
    client = new SimpleFtpClientImpl();
    mockFtpClient = mock(FTPClient.class);
    client.setFtpClient(mockFtpClient);
  }

  @Test
  public void testListFiles_whenConnected_thenReturnsFileList()
    throws Exception {
    String[] expected = { "file1.txt", "file2.txt" };
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
      new String[] { "a.txt", "b.txt" }
    );

    assertTrue(client.fileExists("b.txt"));
  }

  @Test
  public void testFileExists_whenFileAbsent_thenReturnsFalse()
    throws Exception {
    when(mockFtpClient.listNames()).thenReturn(
      new String[] { "a.txt", "b.txt" }
    );

    assertFalse(client.fileExists("c.txt"));
  }

  @Test
  public void testRenameRemoteFile_delegatesToFtpClient() throws Exception {
    client.renameRemoteFile("old.txt", "new.txt");

    verify(mockFtpClient).rename("old.txt", "new.txt");
  }

  @Test
  public void testLogout_disconnectsClient() throws Exception {
    client.logout();

    verify(mockFtpClient).disconnect(true);
  }

  @Test
  public void testLogout_whenExceptionThrown_doesNotPropagate()
    throws Exception {
    doThrow(new IllegalStateException("test"))
      .when(mockFtpClient)
      .disconnect(true);

    client.logout(); // should not throw
  }

  @Test
  public void testGetFileName_returnsSetValue() {
    client.setFileName("report.csv");

    assertEquals("report.csv", client.getFileName());
  }

  @Test
  public void testGettersAndSetters_workCorrectly() {
    client.setHost("ftp.example.com");
    client.setPort(21);
    client.setUsername("user");
    client.setPassword("pass");
    client.setPath("some/path");
    client.setDirectory("some");

    assertEquals("ftp.example.com", client.getHost());
    assertEquals(Integer.valueOf(21), client.getPort());
    assertEquals("user", client.getUsername());
    assertEquals("pass", client.getPassword());
    assertEquals("some/path", client.getPath());
    assertEquals("some", client.getDirectory());
    assertSame(mockFtpClient, client.getFtpClient());
  }
}
