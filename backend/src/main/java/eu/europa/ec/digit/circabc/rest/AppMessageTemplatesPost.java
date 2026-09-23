package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.AppMessageApi;
import io.swagger.model.AppMessage;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.AppMessageJsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco webscript endpoint that handles the HTTP {@code POST} request for creating a new
 * application message template.
 *
 * <p>An application message template ({@link AppMessage}) is the payload used to broadcast
 * notices to CIRCABC users (for example maintenance banners or announcements). This endpoint:
 *
 * <ul>
 *   <li>Restricts access to Alfresco or CIRCABC administrators only.</li>
 *   <li>Parses the {@link AppMessage} template from the JSON request body.</li>
 *   <li>Persists the template through {@link AppMessageApi#addAppMessageTemplate(AppMessage)}.</li>
 *   <li>Optionally triggers a notification when the request carries a truthy
 *       {@code notification} parameter and the template is enabled.</li>
 * </ul>
 *
 * <p>Key inputs:
 *
 * <ul>
 *   <li>JSON request body describing the {@link AppMessage} template.</li>
 *   <li>Optional {@code notification} request parameter ({@code true}/{@code false}) that
 *       controls whether an immediate notification is sent for the template.</li>
 * </ul>
 *
 * <p>On failure the endpoint sets the appropriate HTTP status code: {@code 403 Forbidden} when
 * the caller lacks administrator rights, {@code 400 Bad Request} for malformed or unparseable
 * payloads, and {@code 500 Internal Server Error} for any other error.
 */
public class AppMessageTemplatesPost extends CircabcDeclarativeWebScript {

  /** Logger used to report errors raised while handling the request. */
  static final Log logger = LogFactory.getLog(AppMessageTemplatesPost.class);

  /** API providing the business operations for creating and notifying application messages. */
  @Autowired
  private AppMessageApi appMessageApi;

  /** Service used to verify that the current user holds the required administrator privileges. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the incoming POST request: validates administrator access, parses the
   * {@link AppMessage} template from the request body, persists it and optionally notifies it.
   *
   * @param req the webscript request containing the JSON template body and the optional
   *     {@code notification} parameter
   * @param status the response status, populated with an error code when processing fails
   * @param cache the cache directives for the response
   * @return an (empty) model map on success, or {@code null} after an error status has been set
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);
    try {
      validateAdminAccess();
      AppMessage template = AppMessageJsonParser.parse(req);
      appMessageApi.addAppMessageTemplate(template);
      notifyIfRequired(req, template);
    } catch (AccessDeniedException ade) {
      return handleError(status, Status.STATUS_FORBIDDEN, "Access denied", ade);
    } catch (IOException e) {
      return handleError(
        status,
        Status.STATUS_BAD_REQUEST,
        "Problem with object",
        e
      );
    } catch (ParseException e) {
      return handleError(
        status,
        Status.STATUS_BAD_REQUEST,
        "Bad JSON format",
        e
      );
    } catch (Exception e) {
      return handleError(
        status,
        Status.STATUS_INTERNAL_SERVER_ERROR,
        "Internal server error",
        e
      );
    }
    return model;
  }

  /**
   * Ensures the current user is either an Alfresco administrator or a CIRCABC administrator.
   *
   * @throws AccessDeniedException if the current user has neither administrator role
   */
  private void validateAdminAccess() {
    if (
      !currentUserPermissionCheckerService.isAlfrescoAdmin() &&
      !currentUserPermissionCheckerService.isCircabcAdmin()
    ) {
      throw new AccessDeniedException("Not enough permissions");
    }
  }

  /**
   * Sends a notification for the given template when requested by the caller and allowed by the
   * template state. A notification is dispatched only when the {@code notification} request
   * parameter is present and evaluates to {@code true} and the template is enabled.
   *
   * @param req the webscript request carrying the optional {@code notification} parameter
   * @param template the parsed template that may be notified
   */
  private void notifyIfRequired(WebScriptRequest req, AppMessage template) {
    String notifyStr = req.getParameter("notification");
    boolean shouldNotify =
      !"".equals(notifyStr) && Boolean.parseBoolean(notifyStr);
    if (shouldNotify && Boolean.TRUE.equals(template.getEnabled())) {
      appMessageApi.notifyTemplate(template);
    }
  }

  /**
   * Logs the given error and populates the response status so the client receives the supplied
   * HTTP code and message.
   *
   * @param status the response status to update
   * @param code the HTTP status code to set on the response
   * @param message the human-readable error message to log and return to the client
   * @param e the exception that caused the error, used for logging
   * @return {@code null}, signalling that the error status has been set and no model is returned
   */
  private Map<String, Object> handleError(
    Status status,
    int code,
    String message,
    Exception e
  ) {
    if (logger.isErrorEnabled()) {
      logger.error(message + " in AppMessageTemplatesPost", e);
    }
    status.setCode(code);
    status.setMessage(message);
    status.setRedirect(true);
    return null; // NOSONAR
  }
}
