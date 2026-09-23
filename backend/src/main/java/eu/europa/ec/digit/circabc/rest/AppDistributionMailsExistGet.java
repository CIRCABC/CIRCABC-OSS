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
 * Alfresco Web Script endpoint that handles HTTP {@code GET} requests to
 * retrieve the distribution email a user is subscribed to for application
 * messages.
 *
 * <p>The endpoint expects a {@code userId} template variable in the request
 * URL and returns the subscribed distribution email address for that user.
 * Access is restricted: the request is only served when the current user is an
 * Alfresco administrator, a CIRCABC administrator, or the same user identified
 * by the {@code userId} template variable. Otherwise an
 * {@link org.alfresco.repo.security.permissions.AccessDeniedException} is raised
 * and the response is answered with an HTTP 403 (Forbidden) status.</p>
 *
 * <p>The resolved email address is exposed to the response template under the
 * {@code email} model key.</p>
 *
 * @author beaurpi
 */
public class AppDistributionMailsExistGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(
    AppDistributionMailsExistGet.class
  );

  /**
   * API used to resolve the distribution email a user is subscribed to.
   */
  @Autowired
  private AppMessageApi appMessageApi;

  /**
   * Service used to verify the current user's permissions (administrator roles
   * or identity match) before serving the request.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the incoming {@code GET} request and builds the response model
   * containing the subscribed distribution email for the requested user.
   *
   * <p>The {@code userId} is read from the request template variables. Access is
   * granted only to Alfresco administrators, CIRCABC administrators, or the user
   * themselves; any other caller results in a {@code 403 Forbidden} response.
   * Unexpected failures result in a {@code 500 Internal Server Error} response.</p>
   *
   * @param req the web script request, providing the {@code userId} template
   *     variable
   * @param status the response status, updated to {@code 403} on access denial
   *     or {@code 500} on unexpected errors
   * @param cache the cache directives for the response
   * @return a model map containing the {@code email} entry on success, or
   *     {@code null} when the request is rejected or fails
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();

    try {
      if (
        !(currentUserPermissionCheckerService.isAlfrescoAdmin() ||
          currentUserPermissionCheckerService.isCircabcAdmin() ||
          currentUserPermissionCheckerService.isCurrentUserEqualTo(
            templateVars.get("userId")
          ))
      ) {
        throw new AccessDeniedException("");
      }

      model.put(
        "email",
        appMessageApi.getSubscribedDistributionEmail(templateVars.get("userId"))
      );
    } catch (AccessDeniedException ade) {
      logger.error("User does not have access rights", ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Unexpected error when getting distribution email", e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      return null; // NOSONAR
    }

    return model;
  }
}
