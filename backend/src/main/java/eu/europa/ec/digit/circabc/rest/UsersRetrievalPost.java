package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.UsersApi;
import io.swagger.model.User;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.FileParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco webscript endpoint that handles the HTTP {@code POST} request used to
 * retrieve a list of users from an uploaded spreadsheet.
 *
 * <p>The endpoint expects the request body to contain an Excel file describing a
 * partial user list (see {@link FileParser#parseExcelUserListPartial}). Each parsed
 * entry is resolved against the CIRCABC user store via
 * {@link UsersApi#retrieveUserList(List)} and the resulting {@link User} objects are
 * returned in the response model under the {@code "users"} key.</p>
 *
 * <p>Access is restricted to administrators: the caller must be either a CIRCABC
 * administrator or an Alfresco administrator, otherwise the request is rejected with
 * an HTTP {@code 403 Forbidden} status.</p>
 */
public class UsersRetrievalPost extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(UsersRetrievalPost.class);

  /**
   * API used to resolve and retrieve the {@link User} entries parsed from the
   * uploaded spreadsheet.
   */
  @Autowired
  private UsersApi usersApi;

  /**
   * Service used to verify that the current caller has the administrative
   * privileges (CIRCABC admin or Alfresco admin) required to invoke this endpoint.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the incoming {@code POST} request: enforces administrative access,
   * parses the uploaded Excel file into a partial list of users and resolves them
   * into full {@link User} objects.
   *
   * <p>On success the returned model contains a single {@code "users"} entry holding
   * the resolved user list. On failure the method sets the appropriate HTTP status
   * on {@code status} and returns {@code null}:</p>
   * <ul>
   *   <li>{@code 400 Bad Request} when the parsed query is invalid or empty.</li>
   *   <li>{@code 403 Forbidden} when the caller is not an administrator.</li>
   *   <li>{@code 500 Internal Server Error} when the file cannot be read or an
   *       unexpected error occurs.</li>
   * </ul>
   *
   * @param req the web script request carrying the uploaded Excel file
   * @param status the response status to be populated on error
   * @param cache the cache directives for the response
   * @return a model map containing the resolved {@code "users"} list, or
   *         {@code null} if an error occurred and a status/redirect was set
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    try {
      if (
        !(currentUserPermissionCheckerService.isCircabcAdmin() ||
          currentUserPermissionCheckerService.isAlfrescoAdmin())
      ) {
        throw new AccessDeniedException("Method not allowed");
      } else {
        List<User> users = FileParser.parseExcelUserListPartial(req);
        model.put("users", usersApi.retrieveUserList(users));
      }
    } catch (InvalidArgumentException inre) {
      logger.error("Invalid argument in users retrieval", inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Empty Query");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (AccessDeniedException ade) {
      logger.error("Access denied in users retrieval", ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (IOException e) {
      logger.error("Error reading the file in users retrieval", e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Error reading the file");
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Unexpected error in users retrieval", e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("An unexpected error occurred");
      return null; // NOSONAR
    }

    return model;
  }
}
