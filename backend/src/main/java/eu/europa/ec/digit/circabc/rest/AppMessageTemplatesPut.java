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
 * Alfresco declarative web script that handles the HTTP {@code PUT} request for updating an
 * application message template (a banner/notice shown across the CIRCABC application).
 *
 * <p>The endpoint expects the template payload as JSON in the request body, which is deserialized
 * into an {@link AppMessage} by {@link AppMessageJsonParser}. Access is restricted to Alfresco
 * administrators or CIRCABC administrators; any other caller receives an HTTP 403 (Forbidden)
 * response.
 *
 * <p>In addition to persisting the template, the endpoint accepts an optional {@code notification}
 * request parameter. When that parameter is {@code true} and the updated template is enabled, a
 * notification for the template is triggered via {@link AppMessageApi#notifyTemplate(AppMessage)}.
 */
public class AppMessageTemplatesPut extends CircabcDeclarativeWebScript {

  /** Logger used to report access, parsing and processing errors for this endpoint. */
  static final Log logger = LogFactory.getLog(AppMessageTemplatesPut.class);

  /** API used to update the application message template and to trigger its notification. */
  @Autowired
  private AppMessageApi appMessageApi;

  /** Service used to verify that the current user has administrator privileges. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Updates an application message template from the JSON payload of the request.
   *
   * <p>Verifies that the current user is an Alfresco or CIRCABC administrator, parses the request
   * body into an {@link AppMessage}, persists it, and optionally sends a notification when the
   * {@code notification} request parameter is {@code true} and the template is enabled.
   *
   * <p>Instead of throwing, error conditions are translated into HTTP status codes on the supplied
   * {@code status} object and the method returns {@code null}:
   *
   * <ul>
   *   <li>403 (Forbidden) when the caller lacks administrator permissions;
   *   <li>400 (Bad Request) when the request body cannot be read or contains malformed JSON;
   *   <li>500 (Internal Server Error) for any other unexpected failure.
   * </ul>
   *
   * @param req the web script request, providing the JSON body and the optional
   *     {@code notification} parameter
   * @param status the response status, set to the appropriate error code on failure
   * @param cache the cache control for the response
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

      AppMessage template = AppMessageJsonParser.parse(req);
      appMessageApi.updateAppMessageTemplate(template);

      String notifyStr = req.getParameter("notification");
      if (
        !"".equals(notifyStr) &&
        Boolean.parseBoolean(notifyStr) &&
        Boolean.TRUE.equals(template.getEnabled())
      ) {
        appMessageApi.notifyTemplate(template);
      }
    } catch (AccessDeniedException ade) {
      logger.error("Access denied to update message template", ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (IOException e) {
      logger.error("Error reading template data", e);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Problem with object");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (ParseException e) {
      logger.error("Error parsing template JSON", e);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad JSON format");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Unexpected error updating message template", e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      return null; // NOSONAR
    }

    return model;
  }
}
