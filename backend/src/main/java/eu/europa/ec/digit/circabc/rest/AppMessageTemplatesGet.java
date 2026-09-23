package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.AppMessageApi;
import io.swagger.model.PagedAppMessages;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco webscript endpoint that handles the HTTP {@code GET} request for
 * retrieving the paginated list of application message templates.
 *
 * <p>Access is restricted to administrators: the caller must be either an
 * Alfresco administrator or a CIRCABC administrator, otherwise the endpoint
 * responds with HTTP {@code 403 Forbidden}.
 *
 * <p>Supported query parameters:
 * <ul>
 *   <li>{@code page} - the 1-based page index to return (defaults to {@code 1}).</li>
 *   <li>{@code limit} - the maximum number of templates per page (defaults to {@code 25}).</li>
 * </ul>
 *
 * <p>On success the response model exposes the {@code messages} entry (a
 * {@link io.swagger.model.PagedAppMessages} instance) and a {@code total}
 * entry with the total number of available templates.
 */
public class AppMessageTemplatesGet extends DeclarativeWebScript {

  /** Logger used to report parameter parsing issues and request failures. */
  static final Log logger = LogFactory.getLog(AppMessageTemplatesGet.class);

  /** API providing access to the application message templates. */
  @Autowired
  private AppMessageApi appMessageApi;

  /** Service used to verify the current user's administrator privileges. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Executes the GET request, returning the paginated application message
   * templates for administrator callers.
   *
   * <p>The {@code page} and {@code limit} query parameters are read from the
   * request (falling back to their defaults when absent or invalid). Access is
   * validated before the templates are fetched; on failure the appropriate
   * HTTP error status is set on the response.
   *
   * @param req the web script request, providing the {@code page} and
   *            {@code limit} query parameters
   * @param status the response status, updated when access is denied or an
   *               internal error occurs
   * @param cache the response cache directives
   * @return a model map containing the {@code messages} and {@code total}
   *         entries on success, or {@code null} when an error status has been
   *         set on the response
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    int page = parseIntParam(req.getParameter("page"), 1);
    int limit = parseIntParam(req.getParameter("limit"), 25);

    try {
      validateAdminAccess();
      PagedAppMessages messages = appMessageApi.getAppMessageTemplates(
        page,
        limit
      );
      model.put("messages", messages);
      model.put("total", messages.getTotal());
    } catch (AccessDeniedException ade) {
      return handleAccessDenied(status, ade);
    } catch (Exception e) {
      return handleInternalError(status, e);
    }

    return model;
  }

  /**
   * Parses an integer request parameter, falling back to a default value when
   * the value is missing or not a valid integer.
   *
   * @param value the raw parameter value, may be {@code null} or empty
   * @param defaultValue the value returned when {@code value} is absent or
   *                     cannot be parsed as an integer
   * @return the parsed integer, or {@code defaultValue} if parsing is not
   *         possible
   */
  private int parseIntParam(String value, int defaultValue) {
    if (value == null || value.isEmpty()) {
      return defaultValue;
    }
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      if (logger.isErrorEnabled()) {
        logger.error("Wrong numeric value: " + value, e);
      }
      return defaultValue;
    }
  }

  /**
   * Verifies that the current user is allowed to access the message templates.
   *
   * @throws AccessDeniedException if the current user is neither an Alfresco
   *                               administrator nor a CIRCABC administrator
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
   * Logs the access denial and configures the response with an HTTP
   * {@code 403 Forbidden} status.
   *
   * @param status the response status to update
   * @param ade the access denied exception that triggered this handling
   * @return {@code null}, signalling that the error status has been set
   */
  private Map<String, Object> handleAccessDenied(
    Status status,
    AccessDeniedException ade
  ) {
    if (logger.isErrorEnabled()) {
      logger.error("Access denied in AppMessageTemplatesGet", ade);
    }
    status.setCode(Status.STATUS_FORBIDDEN);
    status.setMessage("Access denied");
    status.setRedirect(true);
    return null; // NOSONAR
  }

  /**
   * Logs the unexpected error and configures the response with an HTTP
   * {@code 500 Internal Server Error} status.
   *
   * @param status the response status to update
   * @param e the exception that triggered this handling
   * @return {@code null}, signalling that the error status has been set
   */
  private Map<String, Object> handleInternalError(Status status, Exception e) {
    if (logger.isErrorEnabled()) {
      logger.error("Error in AppMessageTemplatesGet", e);
    }
    status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
    status.setMessage("Internal server error");
    status.setRedirect(true);
    return null; // NOSONAR
  }
}
