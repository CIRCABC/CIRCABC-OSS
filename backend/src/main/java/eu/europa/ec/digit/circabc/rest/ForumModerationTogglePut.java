package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.ForumsApi;
import io.swagger.model.permissions.NewsGroupPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco webscript endpoint that toggles the moderation settings of a forum
 * (newsgroup).
 *
 * <p>Mapped to an HTTP {@code PUT} request (as implied by the {@code Put}
 * suffix in the class name). The endpoint expects the target forum node
 * identifier as the {@code id} template variable in the URL and two optional
 * request parameters:
 *
 * <ul>
 *   <li>{@code enable} - when equal to {@code "true"}, moderation is enabled;
 *       otherwise it is disabled.</li>
 *   <li>{@code acceptAll} - when equal to {@code "true"}, all pending posts are
 *       accepted as part of the toggle operation.</li>
 * </ul>
 *
 * <p>The current user must hold the {@link NewsGroupPermissions#NWSMODERATE}
 * permission on the target node; otherwise the request is rejected with an
 * HTTP 403 (Forbidden) response.
 */
public class ForumModerationTogglePut extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(ForumModerationTogglePut.class);

  /**
   * API used to perform forum (newsgroup) operations, including toggling
   * moderation on a forum node.
   */
  @Autowired
  private ForumsApi forumsApi;

  /**
   * Service used to verify that the current user holds the required
   * newsgroup permissions on the target forum node.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the request to toggle a forum's moderation settings.
   *
   * <p>Reads the forum node id from the URL template variables, checks that the
   * current user has the {@link NewsGroupPermissions#NWSMODERATE} permission,
   * and then applies the moderation change using the {@code enable} and
   * {@code acceptAll} request parameters.
   *
   * @param req the incoming webscript request; provides the {@code id}
   *            template variable and the {@code enable}/{@code acceptAll}
   *            parameters
   * @param status the response status, set to an error code (403 or 400) when
   *               the operation cannot be completed
   * @param cache the webscript cache directives for the response
   * @return a model map containing a {@code "message"} entry set to
   *         {@code "ok"} on success, or {@code null} when an error occurs and
   *         the response is redirected to a status page
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
      String id = templateVars.get("id");

      if (
        !this.currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
          id,
          NewsGroupPermissions.NWSMODERATE
        )
      ) {
        throw new AccessDeniedException(
          "Current Authority cannot change the moderation of the forum, not enough permission"
        );
      }

      boolean enable = "true".equals(req.getParameter("enable"));
      boolean acceptAll = "true".equals(req.getParameter("acceptAll"));

      this.forumsApi.toggleModeration(id, enable, acceptAll);

      model.put("message", "ok");
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error(ERROR_OCCURRED, inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidTypeException ite) {
      logger.error(ERROR_OCCURRED, ite);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad noderef type");
      status.setRedirect(true);
      return null; // NOSONAR
    }

    return model;
  }
}
