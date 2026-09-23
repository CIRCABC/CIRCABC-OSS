package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.AppMessageApi;
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
 * REST webscript endpoint that retrieves a single application message template
 * by its identifier.
 *
 * <p>As implied by the {@code Get} suffix in the class name, this endpoint
 * handles an HTTP {@code GET} request. The template identifier is supplied as a
 * path variable named {@code id} in the webscript URL, and is parsed as an
 * integer before being passed to the {@link AppMessageApi}.
 *
 * <p>Access is restricted to administrators: the caller must be either an
 * Alfresco administrator or a CIRCABC administrator. Requests from users
 * without those privileges are rejected with an HTTP {@code 403 Forbidden}
 * response. Any other failure results in an HTTP {@code 500 Internal Server
 * Error}. On success, the resolved template is placed in the response model
 * under the {@code message} key for rendering by the associated FreeMarker
 * template.
 */
public class AppMessageTemplateGet extends DeclarativeWebScript {

  /** Logger used to record access-denied and error conditions for this endpoint. */
  static final Log logger = LogFactory.getLog(AppMessageTemplateGet.class);

  /** API providing access to application message templates. */
  @Autowired
  private AppMessageApi appMessageApi;

  /** Service used to verify that the current user holds the required admin privileges. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the incoming GET request by looking up the application message
   * template identified by the {@code id} path variable.
   *
   * <p>The current user must be an Alfresco administrator or a CIRCABC
   * administrator; otherwise the response status is set to {@code 403
   * Forbidden}. Unexpected errors set the status to {@code 500 Internal Server
   * Error}. In both failure cases the method returns {@code null} and marks the
   * status as a redirect so the framework renders the appropriate status page.
   *
   * @param req the web script request; supplies the {@code id} path variable
   *            identifying the template to retrieve
   * @param status the response status, updated to reflect forbidden or error
   *               conditions when the lookup cannot be completed
   * @param cache the cache directives for the response
   * @return a model map containing the resolved template under the
   *         {@code message} key on success, or {@code null} when the request is
   *         forbidden or an error occurs
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);
    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();

    String id = templateVars.get("id");

    try {
      if (
        !(currentUserPermissionCheckerService.isAlfrescoAdmin() ||
          currentUserPermissionCheckerService.isCircabcAdmin())
      ) {
        throw new AccessDeniedException("Not enough permissions");
      }

      model.put(
        "message",
        appMessageApi.getAppMessageTemplate(Integer.parseInt(id))
      );
    } catch (AccessDeniedException ade) {
      logger.error(
        "Access denied when getting app message template with id: " + id,
        ade
      );
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Error getting app message template with id: " + id, e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      return null; // NOSONAR
    }

    return model;
  }
}
