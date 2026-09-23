/**
 * Copyright 2006 European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
/**
 *
 */
package eu.europa.ec.digit.circabc.rest.service.ftp;

import it.sauronsoftware.ftp4j.*;
import java.io.File;
import java.io.IOException;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Default {@link SimpleFtpClient} implementation backed by the ftp4j library.
 *
 * <p>This class wraps an {@link FTPClient} to provide a small, high-level API for the FTP
 * operations required by CIRCABC: connecting/authenticating against a remote FTP server,
 * listing files, checking for a file's existence, downloading a file to a temporary local file,
 * renaming a remote file and disconnecting.
 *
 * <p>A single instance keeps its own connection state and the parsed target location (the
 * {@link #directory} and {@link #filename} derived from the configured path). Instances are
 * therefore stateful and not thread-safe; a new instance should be used per FTP session and
 * {@link #logout()} should be called once the session is no longer needed.
 *
 * @author beaurpi
 */
public class SimpleFtpClientImpl implements SimpleFtpClient {

  /** A logger for the class */
  private static final Log logger = LogFactory.getLog(
    SimpleFtpClientImpl.class
  );

  /** Host name or IP address of the remote FTP server. */
  private String host;

  /** TCP port used to reach the remote FTP server. */
  private Integer port;

  /** User name used to authenticate against the FTP server. */
  private String username;

  /** Password used to authenticate against the FTP server. */
  private String password;

  /**
   * The full path configured for this client. It may point to a directory or to a file and is
   * parsed on {@link #initParameters} into {@link #directory} and, when a file name is present,
   * {@link #filename}.
   */
  private String path;

  /** Underlying ftp4j client that performs the actual FTP protocol exchanges. */
  private FTPClient ftpClient;

  /** Remote working directory derived from {@link #path}. */
  private String directory;

  /** Remote file name derived from {@link #path}, when the path targets a file. */
  private String filename;

  /**
   * Configures the client and opens a passive-mode FTP connection to the remote server.
   *
   * <p>After connecting and logging in with the supplied credentials, the given {@code path} is
   * parsed: if it contains a {@code '/'} the leading part is treated as the remote directory (which
   * becomes the current working directory) and, when the trailing segment looks like a file name,
   * it is stored as the file name. When the path has no slash it is treated as a bare file name in
   * the root directory. An empty path leaves the connection at its default location.
   *
   * @param host the host name or IP address of the FTP server
   * @param port the TCP port of the FTP server
   * @param username the user name used to authenticate
   * @param password the password used to authenticate
   * @param path the remote path to a directory or file; may be empty
   * @throws IllegalStateException if the client is already connected
   * @throws IOException if an I/O error occurs while communicating with the server
   * @throws FTPIllegalReplyException if the server replies in a way that violates the protocol
   * @throws FTPException if the server rejects the connection, login or directory change
   */
  @Override
  public void initParameters(
    String host,
    Integer port,
    String username,
    String password,
    String path
  )
    throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
    String resolvedHost = FtpDestinationValidator.validateAndResolveHost(
      host,
      port
    );

    this.host = host;
    this.port = port;
    this.username = username;
    this.password = password;
    this.path = path;

    ftpClient = new FTPClient();

    ftpClient.connect(resolvedHost, port);
    ftpClient.setPassive(true);
    ftpClient.login(username, password);

    if (!path.isEmpty()) {
      if (path.contains("/")) {
        int lastIndexOfSlash = path.lastIndexOf('/');

        this.directory = path.substring(0, lastIndexOfSlash);

        if (lastIndexOfSlash < path.lastIndexOf('.')) {
          this.setFileName(path.substring(lastIndexOfSlash + 1));
        }
        ftpClient.changeDirectory(directory);
      } else {
        this.setFileName(path);
        this.directory = "";
      }
    }
  }

  /**
   * Indicates whether the client currently holds an open connection to the FTP server.
   *
   * @return {@code true} if the underlying client is connected, {@code false} otherwise
   */
  private Boolean isConfigured() {
    return ftpClient.isConnected();
  }

  /**
   * Lists the names of the files available in the current remote directory.
   *
   * @return an array of remote file names, or {@code null} if the client is not connected
   * @throws IllegalStateException if the client is not in a state that allows listing
   * @throws IOException if an I/O error occurs while communicating with the server
   * @throws FTPIllegalReplyException if the server replies in a way that violates the protocol
   * @throws FTPException if the server rejects the listing request
   * @throws FTPDataTransferException if an error occurs during the data transfer
   * @throws FTPAbortedException if the operation is aborted
   * @throws FTPListParseException if the returned listing cannot be parsed
   */
  @Override
  public String[] listFiles()
    throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException, FTPListParseException {
    String[] listOfFiles = null;

    if (Boolean.TRUE.equals(isConfigured())) {
      listOfFiles = ftpClient.listNames();
    }

    return listOfFiles;
  }

  /** @return the host */
  public String getHost() {
    return host;
  }

  /** @param host the host to set */
  public void setHost(String host) {
    this.host = host;
  }

  /** @return the port */
  public Integer getPort() {
    return port;
  }

  /** @param port the port to set */
  public void setPort(Integer port) {
    this.port = port;
  }

  /** @return the username */
  public String getUsername() {
    return username;
  }

  /** @param username the username to set */
  public void setUsername(String username) {
    this.username = username;
  }

  /** @return the password */
  public String getPassword() {
    return password;
  }

  /** @param password the password to set */
  public void setPassword(String password) {
    this.password = password;
  }

  /** @return the path */
  public String getPath() {
    return path;
  }

  /** @param path the path to set */
  public void setPath(String path) {
    this.path = path;
  }

  /** @return the ftpClient */
  public FTPClient getFtpClient() {
    return ftpClient;
  }

  /** @param ftpClient the ftpClient to set */
  public void setFtpClient(FTPClient ftpClient) {
    this.ftpClient = ftpClient;
  }

  /** @return the directory */
  public String getDirectory() {
    return directory;
  }

  /** @param directory the directory to set */
  public void setDirectory(String directory) {
    this.directory = directory;
  }

  /** @param filename the filename to set */
  public void setFileName(String filename) {
    this.filename = filename;
  }

  /**
   * Disconnects from the FTP server, sending the quit command to allow a graceful shutdown.
   *
   * <p>Any exception raised while disconnecting is caught and logged rather than propagated, so
   * this method can be safely called during cleanup.
   */
  @Override
  public void logout() {
    try {
      ftpClient.disconnect(true);
    } catch (
      IllegalStateException
      | FTPException
      | FTPIllegalReplyException
      | IOException e
    ) {
      if (logger.isErrorEnabled()) {
        logger.error(e.getMessage(), e);
      }
    }
  }

  /**
   * Checks whether a file with the given name exists in the current remote directory.
   *
   * @param fileName the remote file name to look for
   * @return {@code true} if a file with that exact name is present, {@code false} otherwise
   * @throws IllegalStateException if the client is not in a state that allows listing
   * @throws IOException if an I/O error occurs while communicating with the server
   * @throws FTPIllegalReplyException if the server replies in a way that violates the protocol
   * @throws FTPException if the server rejects the listing request
   * @throws FTPDataTransferException if an error occurs during the data transfer
   * @throws FTPAbortedException if the operation is aborted
   * @throws FTPListParseException if the returned listing cannot be parsed
   */
  @Override
  public Boolean fileExists(String fileName)
    throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException, FTPListParseException {
    boolean result = false;

    for (String file : ftpClient.listNames()) {
      if (file.equals(fileName)) {
        result = true;
        break;
      }
    }

    return result;
  }

  /**
   * Downloads a remote file into a newly created local temporary file.
   *
   * <p>The temporary file is always created (using {@link TempFileProvider}); the remote content is
   * only transferred into it when the file name is not empty and the file actually exists on the
   * server. The caller is responsible for deleting the temporary file once it is no longer needed.
   *
   * @param filename the name of the remote file to download
   * @return the local temporary {@link File}; it may be empty if the remote file was not found
   * @throws IllegalStateException if the client is not in a state that allows the transfer
   * @throws IOException if an I/O error occurs while communicating with the server
   * @throws FTPIllegalReplyException if the server replies in a way that violates the protocol
   * @throws FTPException if the server rejects the download request
   * @throws FTPDataTransferException if an error occurs during the data transfer
   * @throws FTPAbortedException if the operation is aborted
   * @throws FTPListParseException if the listing used to check existence cannot be parsed
   */
  public File downloadFile(String filename)
    throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException, FTPListParseException {
    File localFile = TempFileProvider.createTempFile(filename, ".tmp");

    if (!filename.isEmpty() && Boolean.TRUE.equals(fileExists(filename))) {
      ftpClient.download(filename, localFile);
    }

    return localFile;
  }

  /**
   * Renames a file on the remote FTP server.
   *
   * @param fileName the current name of the remote file
   * @param newFileName the new name to assign to the remote file
   * @throws IllegalStateException if the client is not in a state that allows the rename
   * @throws IOException if an I/O error occurs while communicating with the server
   * @throws FTPIllegalReplyException if the server replies in a way that violates the protocol
   * @throws FTPException if the server rejects the rename request
   */
  @Override
  public void renameRemoteFile(String fileName, String newFileName)
    throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
    ftpClient.rename(fileName, newFileName);
  }

  /**
   * Returns the remote file name currently associated with this client.
   *
   * @return the remote file name, or {@code null} if none was resolved from the configured path
   */
  @Override
  public String getFileName() {
    return filename;
  }
}
