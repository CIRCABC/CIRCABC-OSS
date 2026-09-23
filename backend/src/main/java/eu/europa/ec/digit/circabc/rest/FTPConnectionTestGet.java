package eu.europa.ec.digit.circabc.rest;

import eu.europa.ec.digit.circabc.rest.exception.FtpConnectionException;
import eu.europa.ec.digit.circabc.rest.service.ftp.SimpleFtpClient;
import eu.europa.ec.digit.circabc.rest.service.ftp.SimpleFtpClientImpl;
import io.swagger.util.CurrentUserPermissionCheckerService;
import it.sauronsoftware.ftp4j.*;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco read-only (HTTP GET) web script endpoint that verifies whether a
 * remote FTP location can be reached with the supplied credentials.
 *
 * <p>The endpoint opens a connection to the given FTP server, authenticates
 * with the provided username and password, navigates to the requested path and
 * optionally checks for the presence of a target file. It is intended to let
 * administrators validate FTP configuration before persisting it.
 *
 * <p>Request parameters (read from the query string):
 * <ul>
 *   <li>{@code host} &ndash; FTP server host name or IP address.</li>
 *   <li>{@code port} &ndash; FTP server port (parsed as an integer).</li>
 *   <li>{@code username} &ndash; FTP account user name.</li>
 *   <li>{@code password} &ndash; FTP account password.</li>
 *   <li>{@code filePath} &ndash; path (and optional file name) to test on the
 *       remote server.</li>
 * </ul>
 *
 * <p>Access is restricted to directory, category or CIRCABC administrators.
 * The JSON response model exposes a single {@code result} entry encoding the
 * outcome of the connection test (see {@link #testConnection}).
 */
public class FTPConnectionTestGet extends DeclarativeWebScript {

  /** Logger used to report FTP connection and validation problems. */
  static final Log logger = LogFactory.getLog(FTPConnectionTestGet.class);

  /**
   * Service used to verify that the current user holds sufficient
   * administrative permissions to run the FTP connection test.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Attempts to establish an FTP connection with the given parameters and, when
   * a file name is present in the path, checks whether that file exists on the
   * remote server.
   *
   * @param host the FTP server host name or IP address
   * @param port the FTP server port
   * @param username the FTP account user name
   * @param password the FTP account password
   * @param path the remote path (optionally including a file name) to test
   * @return a status code describing the outcome:
   *         {@code 1} when the connection succeeds (and any target file exists),
   *         {@code 0} when the connection succeeds but the target file is
   *         missing, {@code -1} when an FTP-level error occurs (e.g. a wrong
   *         path) and {@code -2} for any other failure
   */
  public static int testConnection(
    String host,
    int port,
    String username,
    String password,
    String path
  ) {
    SimpleFtpClient ftpTest = new SimpleFtpClientImpl();
    try {
      ftpTest.initParameters(host, port, username, password, path);
      return checkFileExists(ftpTest);
    } catch (FTPException e) {
      logError(
        "Problem during testing FTP connection, ftp issue -> wrong path ?",
        e
      );
      return -1;
    } catch (Exception e) {
      logError("Problem during testing FTP connection", e);
      return -2;
    }
  }

  /**
   * Checks whether the file referenced by the FTP client's configured path
   * exists on the remote server.
   *
   * @param ftpTest the initialised FTP client used to query the remote server
   * @return {@code 0} when a file name is configured but the file does not
   *         exist, otherwise {@code 1}
   * @throws FtpConnectionException if an unexpected error occurs while checking
   *         for the file
   * @throws FTPException if an FTP protocol error occurs during the check
   */
  private static int checkFileExists(SimpleFtpClient ftpTest)
    throws FtpConnectionException, FTPException {
    try {
      String fileName = ftpTest.getFileName();
      if (
        fileName != null &&
        !fileName.isEmpty() &&
        Boolean.FALSE.equals(ftpTest.fileExists(fileName))
      ) {
        return 0;
      }
      return 1;
    } catch (FTPException e) {
      throw e;
    } catch (Exception e) {
      throw new FtpConnectionException("Error checking file existence", e);
    }
  }

  /**
   * Logs the given message and exception at error level, guarded by the
   * logger's error-enabled check.
   *
   * @param message the descriptive error message to log
   * @param e the exception associated with the error
   */
  private static void logError(String message, Exception e) {
    if (logger.isErrorEnabled()) {
      logger.error(message, e);
    }
  }

  /**
   * Handles the web script request: validates the caller's permissions, reads
   * the FTP parameters from the request and runs the connection test.
   *
   * <p>ML (multilingual) property interception is temporarily disabled for the
   * duration of the call and restored afterwards.
   *
   * @param req the web script request carrying the {@code host}, {@code port},
   *        {@code username}, {@code password} and {@code filePath} parameters
   * @param status the web script response status, updated on error conditions
   * @param cache the web script cache directives
   * @return a model map containing the {@code result} status code of the
   *         connection test, or {@code null} when an error status
   *         (bad request or forbidden) is set on the response
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);
    boolean mlAware = MLPropertyInterceptor.isMLAware();
    MLPropertyInterceptor.setMLAware(false);

    validatePermission();

    try {
      int connStatus = testConnection(
        req.getParameter("host"),
        parsePort(req.getParameter("port")),
        req.getParameter("username"),
        req.getParameter("password"),
        req.getParameter("filePath")
      );
      model.put("result", connStatus);
    } catch (InvalidNodeRefException inre) {
      return handleError(status, Status.STATUS_BAD_REQUEST, "Bad request");
    } catch (AccessDeniedException ade) {
      return handleError(status, Status.STATUS_FORBIDDEN, "Access denied");
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
    return model;
  }

  /**
   * Ensures the current user is a directory, category or CIRCABC administrator.
   *
   * @throws AccessDeniedException if the current user lacks the required
   *         administrative permissions
   */
  private void validatePermission() {
    if (
      !this.currentUserPermissionCheckerService.isCurrentUserDirAdminOrCategoryAdminOrCircabcAdmin()
    ) {
      throw new AccessDeniedException("The user don't have enough permissions");
    }
  }

  /**
   * Parses the given port string into an integer.
   *
   * @param portString the raw port value from the request
   * @return the parsed port number, or {@code 0} when the value is not a valid
   *         integer
   */
  private int parsePort(String portString) {
    try {
      return Integer.parseInt(portString);
    } catch (NumberFormatException e) {
      if (logger.isErrorEnabled()) {
        logger.error("Invalid port number: " + portString);
      }
      return 0;
    }
  }

  /**
   * Populates the response status with the given error code and message and
   * marks it as a redirect so the framework renders the corresponding error
   * response.
   *
   * @param status the web script response status to update
   * @param code the HTTP status code to set
   * @param message the human-readable error message to set
   * @return {@code null}, signalling that no model should be rendered
   */
  private Map<String, Object> handleError(
    Status status,
    int code,
    String message
  ) {
    status.setCode(code);
    status.setMessage(message);
    status.setRedirect(true);
    return null; // NOSONAR
  }
}
