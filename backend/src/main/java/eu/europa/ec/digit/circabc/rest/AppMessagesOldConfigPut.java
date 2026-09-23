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
 * Alfresco webscript endpoint that updates the "old application messages" configuration flag.
 *
 * <p>As implied by the {@code Put} suffix in the class name, this endpoint handles an HTTP
 * {@code PUT} request. It toggles whether legacy (old) application messages are enabled by
 * delegating to {@link AppMessageApi#setEnableOldMessage(Boolean)}.
 *
 * <p>The request body is expected to carry a boolean value, which is extracted via
 * {@link AppMessageJsonParser#parseBoolean(WebScriptRequest)}. Access is restricted to
 * administrators: the caller must be either an Alfresco admin or a CIRCABC admin, otherwise the
 * request is rejected with an HTTP {@code 403 Forbidden} response.
 */
public class AppMessagesOldConfigPut extends CircabcDeclarativeWebScript {

  /** Logger used to report access-denied, I/O and unexpected errors during request handling. */
  static final Log logger = LogFactory.getLog(AppMessagesOldConfigPut.class);

  /** API used to update the application message configuration. */
  @Autowired
  private AppMessageApi appMessageApi;

  /** Service used to verify that the current user has administrative privileges. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the PUT request that enables or disables old application messages.
   *
   * <p>The method first verifies that the current user is an Alfresco or CIRCABC administrator.
   * It then parses a boolean value from the request body and, when present, applies it through
   * {@link AppMessageApi#setEnableOldMessage(Boolean)}. Errors are handled internally by setting
   * the appropriate HTTP status on the response.
   *
   * @param req the web script request; its body is expected to contain the boolean flag
   * @param status the response status, updated to {@code 403} on access denial or {@code 500} on
   *     server/parsing errors
   * @param cache the cache directives for the response
   * @return an (empty) model map on success, or {@code null} when an error status was set
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

      Boolean enableOld = AppMessageJsonParser.parseBoolean(req);

      if (enableOld != null) {
        appMessageApi.setEnableOldMessage(enableOld);
      }
    } catch (AccessDeniedException ade) {
      logger.error("Access denied", ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (IOException e) {
      logger.error("IO error while parsing request", e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Server error - invalid request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Unexpected error occurred", e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      return null; // NOSONAR
    }
    return model;
  }
}
