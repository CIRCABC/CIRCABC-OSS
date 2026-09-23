package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.AppMessageApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.AppMessageJsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco webscript endpoint that updates the application message configuration.
 *
 * <p>Handles the HTTP {@code PUT} request (as implied by the {@code Put} suffix in the
 * class name) for the application messages configuration resource. The endpoint reads a
 * boolean flag from the request body indicating whether old (expired) application messages
 * should still be displayed, and persists it through the {@link AppMessageApi}.
 *
 * <p>Access is restricted to administrators: the caller must be either an Alfresco admin or
 * a CIRCABC admin. Requests from unauthorized users are rejected with an HTTP
 * {@code 403 Forbidden} response.
 *
 * <p>Key inputs:
 * <ul>
 *   <li>Request body containing the boolean {@code displayOldMessage} flag, parsed via
 *       {@link AppMessageJsonParser#parseBoolean(WebScriptRequest)}.</li>
 * </ul>
 */
public class AppMessagesConfigPut extends CircabcDeclarativeWebScript {

  /** Logger for this webscript endpoint. */
  static final Log logger = LogFactory.getLog(AppMessagesConfigPut.class);

  /** API used to read and update application message settings. */
  @Autowired
  private AppMessageApi appMessageApi;

  /** Service used to verify that the current user has the required admin privileges. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Executes the endpoint logic: validates that the current user is an administrator and,
   * if a boolean flag is provided in the request, updates the "display old message" setting.
   *
   * <p>On failure the method sets the appropriate HTTP status on {@code status}, enables
   * redirection to the status template and returns {@code null}:
   * <ul>
   *   <li>{@code 403 Forbidden} when the caller lacks admin permissions.</li>
   *   <li>{@code 500 Internal Server Error} when the request cannot be parsed or an
   *       unexpected error occurs.</li>
   * </ul>
   *
   * @param req the web script request, whose body carries the boolean configuration flag
   * @param status the response status to populate on success or failure
   * @param cache the cache control settings for the response
   * @return an empty model map on success, or {@code null} when an error status has been set
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
        !(currentUserPermissionCheckerService.isAlfrescoAdmin() ||
          currentUserPermissionCheckerService.isCircabcAdmin())
      ) {
        throw new AccessDeniedException("Not enough permissions");
      }

      Boolean displayOld = AppMessageJsonParser.parseBoolean(req);

      if (displayOld != null) {
        appMessageApi.setDisplayOldMessage(displayOld);
      }
    } catch (AccessDeniedException ade) {
      logger.error("Access denied to update message configuration", ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (IOException e) {
      logger.error("Error processing request", e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Server error - invalid request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Unexpected error updating message configuration", e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      return null; // NOSONAR
    }
    return model;
  }
}
