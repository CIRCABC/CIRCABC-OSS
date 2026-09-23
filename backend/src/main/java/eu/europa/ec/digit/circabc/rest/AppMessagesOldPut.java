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
 * Alfresco Declarative Web Script backing the HTTP {@code PUT} endpoint that updates an existing
 * ("old") application-wide message in CIRCABC.
 *
 * <p>The {@code PUT} HTTP method is implied by the {@code Put} suffix in the class name. The request
 * body is expected to contain a JSON representation of an {@link io.swagger.model.AppMessage}, which
 * is deserialized via {@link io.swagger.util.parsers.AppMessageJsonParser} and forwarded to
 * {@link io.swagger.api.AppMessageApi#udpateOldAppMessage(io.swagger.model.AppMessage)}.
 *
 * <p>Access is restricted to administrators: the caller must be either an Alfresco administrator or
 * a CIRCABC administrator. Callers lacking these privileges receive an HTTP {@code 403 Forbidden}
 * response, while malformed input yields an HTTP {@code 400 Bad Request} and unexpected failures an
 * HTTP {@code 500 Internal Server Error}.
 */
public class AppMessagesOldPut extends CircabcDeclarativeWebScript {

  /** Logger used to report access, input and unexpected errors during message updates. */
  static final Log logger = LogFactory.getLog(AppMessagesOldPut.class);

  /** API used to perform the update of the existing application message. */
  @Autowired
  private AppMessageApi appMessageApi;

  /** Service used to verify that the current user holds administrator privileges. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the {@code PUT} request that updates an existing application message.
   *
   * <p>The method first checks that the current user is an Alfresco or CIRCABC administrator, then
   * parses the {@link io.swagger.model.AppMessage} from the request body and delegates the update to
   * {@link AppMessageApi#udpateOldAppMessage(io.swagger.model.AppMessage)}. On error, the appropriate
   * HTTP status is set on {@code status} and {@code null} is returned so the framework renders the
   * error response.
   *
   * @param req the web script request whose body carries the JSON {@link io.swagger.model.AppMessage}
   *     to update
   * @param status the response status, populated with an error code and message when the update fails
   *     (403 for insufficient permissions, 400 for bad input, 500 for unexpected errors)
   * @param cache the cache control settings for the response
   * @return an empty model map on success, or {@code null} when an error occurs and an error status
   *     has been set
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
      appMessageApi.udpateOldAppMessage(template);
    } catch (AccessDeniedException ade) {
      logger.error("Access denied when updating old app message", ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (IOException e) {
      logger.error("IO error when updating old app message", e);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Problem with object");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (ParseException e) {
      logger.error("Parse error when updating old app message", e);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad JSON format");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Unexpected error when updating old app message", e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      return null; // NOSONAR
    }

    return model;
  }
}
