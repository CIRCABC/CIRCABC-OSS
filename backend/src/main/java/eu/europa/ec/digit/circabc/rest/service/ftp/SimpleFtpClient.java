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

/**
 * Abstraction over a simple FTP client used by CIRCABC to interact with a remote FTP server.
 *
 * <p>Implementations wrap the underlying ftp4j library to provide the small set of operations that
 * CIRCABC requires: establishing a connection, listing and locating remote files, downloading a
 * file locally, renaming a remote file and disconnecting. A single instance is expected to be
 * configured once through {@link #initParameters(String, Integer, String, String, String)} and then
 * reused for the subsequent file operations before {@link #logout()} is called.
 *
 * @author beaurpi
 */
public interface SimpleFtpClient {
  /**
   * Initialises the connection parameters and opens a session against the remote FTP server.
   *
   * @param host the FTP server host name or IP address
   * @param port the FTP server port
   * @param username the user name used to authenticate
   * @param password the password used to authenticate
   * @param path the remote working directory to change into once connected
   * @throws IllegalStateException if the client is already connected or in an invalid state
   * @throws IOException if an I/O error occurs while communicating with the server
   * @throws FTPIllegalReplyException if the server returns an unparsable reply
   * @throws FTPException if the server rejects the connection or authentication
   */
  void initParameters(
    String host,
    Integer port,
    String username,
    String password,
    String path
  )
    throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException;

  /**
   * Lists the names of the files available in the current remote directory.
   *
   * @return an array with the names of the remote files
   * @throws IllegalStateException if the client is not connected
   * @throws IOException if an I/O error occurs while communicating with the server
   * @throws FTPIllegalReplyException if the server returns an unparsable reply
   * @throws FTPException if the server reports an error
   * @throws FTPDataTransferException if an error occurs during the data transfer
   * @throws FTPAbortedException if the operation is aborted
   * @throws FTPListParseException if the server directory listing cannot be parsed
   */
  String[] listFiles()
    throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException, FTPListParseException;

  /** Disconnects from the FTP server, closing the current session. */
  void logout();

  /**
   * Verifies whether a given file is present on the remote server.
   *
   * @param fileName the name of the remote file to look for
   * @return {@code true} if a file with the given name exists on the server, {@code false} otherwise
   * @throws IllegalStateException if the client is not connected
   * @throws IOException if an I/O error occurs while communicating with the server
   * @throws FTPIllegalReplyException if the server returns an unparsable reply
   * @throws FTPException if the server reports an error
   * @throws FTPDataTransferException if an error occurs during the data transfer
   * @throws FTPAbortedException if the operation is aborted
   * @throws FTPListParseException if the server directory listing cannot be parsed
   */
  Boolean fileExists(String fileName)
    throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException, FTPListParseException;

  /**
   * Downloads a remote file to a local temporary file.
   *
   * @param filename the name of the remote file to download
   * @return the local {@link File} the remote content was downloaded into
   * @throws IllegalStateException if the client is not connected
   * @throws IOException if an I/O error occurs while communicating with the server
   * @throws FTPIllegalReplyException if the server returns an unparsable reply
   * @throws FTPException if the server reports an error
   * @throws FTPDataTransferException if an error occurs during the data transfer
   * @throws FTPAbortedException if the operation is aborted
   * @throws FTPListParseException if the server directory listing cannot be parsed
   */
  File downloadFile(String filename)
    throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException, FTPListParseException;

  /**
   * Renames a file on the remote FTP server.
   *
   * @param fileName the current name of the remote file
   * @param newFileName the new name to assign to the remote file
   * @throws IllegalStateException if the client is not connected
   * @throws IOException if an I/O error occurs while communicating with the server
   * @throws FTPIllegalReplyException if the server returns an unparsable reply
   * @throws FTPException if the server reports an error
   */
  void renameRemoteFile(String fileName, String newFileName)
    throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException;

  /**
   * Returns the name of the file currently associated with this client.
   *
   * @return the current file name, or {@code null} if none has been set
   */
  String getFileName();
}
