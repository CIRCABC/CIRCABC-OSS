package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.UsersApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco Declarative Web Script backing the HTTP {@code GET} endpoint that
 * retrieves the memberships of a given user.
 *
 * <p>The target user is identified by the {@code userId} URL template variable.
 * Access is restricted: the memberships are returned only when the current
 * caller is the requested user themselves, an Alfresco administrator, or a
 * CIRCABC administrator; otherwise an {@link AccessDeniedException} is raised
 * and the response is set to {@link Status#STATUS_FORBIDDEN}.</p>
 *
 * <p>Supported request parameters:</p>
 * <ul>
 *   <li>{@code language} (optional) &mdash; when provided, the content and UI
 *       locale are switched to this language and multilingual awareness is
 *       disabled so that values are resolved for that locale; when absent,
 *       multilingual awareness is enabled.</li>
 *   <li>{@code lightMode} (optional) &mdash; boolean flag controlling whether a
 *       reduced ("light") representation of the memberships is returned.
 *       Defaults to {@code true} when omitted.</li>
 * </ul>
 *
 * <p>On success the resolved membership data is placed in the response model
 * under the {@code membership} key for rendering by the associated FreeMarker
 * template.</p>
 *
 * @author beaurpi
 */
public class UserMembershipsGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(UserMembershipsGet.class);

  /**
   * API used to resolve the memberships of the requested user.
   */
  @Autowired
  private UsersApi usersApi;

  /**
   * Service used to determine the identity and privileges of the current caller
   * (self, Alfresco administrator or CIRCABC administrator) in order to
   * authorise the request.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the web script request, resolving and returning the memberships of
   * the user identified by the {@code userId} URL template variable.
   *
   * <p>The method configures the multilingual/locale context based on the
   * optional {@code language} parameter, verifies that the caller is authorised
   * to view the target user's memberships, and populates the model accordingly.
   * The previous multilingual-awareness state is always restored before
   * returning. Error conditions are translated into the appropriate HTTP status
   * codes ({@link Status#STATUS_BAD_REQUEST}, {@link Status#STATUS_FORBIDDEN} or
   * {@link Status#STATUS_INTERNAL_SERVER_ERROR}), in which case {@code null} is
   * returned and the response is redirected to the corresponding status page.</p>
   *
   * @param req the incoming web script request, providing the {@code userId}
   *            template variable and the {@code language} and {@code lightMode}
   *            request parameters
   * @param status the response status, updated with an HTTP error code when the
   *               request cannot be fulfilled
   * @param cache the cache directives for the response
   * @return a model map containing the resolved memberships under the
   *         {@code membership} key, or {@code null} when an error status has
   *         been set on the response
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    String language = req.getParameter("language");
    boolean mlAware = MLPropertyInterceptor.isMLAware();

    if (language == null) {
      MLPropertyInterceptor.setMLAware(true);
    } else {
      Locale locale = Locale.of(language);
      I18NUtil.setContentLocale(locale);
      I18NUtil.setLocale(locale);
      MLPropertyInterceptor.setMLAware(false);
    }

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    try {
      String userId = templateVars.get("userId");
      String lightModeStr = req.getParameter("lightMode");
      boolean lightMode = true;
      if (lightModeStr != null) {
        lightMode = Boolean.parseBoolean(lightModeStr);
      }

      if (userId != null) {
        if (
          currentUserPermissionCheckerService.isCurrentUserEqualTo(userId) ||
          currentUserPermissionCheckerService.isAlfrescoAdmin() ||
          currentUserPermissionCheckerService.isCircabcAdmin()
        ) {
          model.put(
            "membership",
            this.usersApi.getUserMembership(userId, lightMode)
          );
        } else {
          throw new AccessDeniedException("Operation is not allowed");
        }
      }
    } catch (InvalidNodeRefException inre) {
      logger.error(
        "Invalid node reference when getting user memberships",
        inre
      );
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (AccessDeniedException ade) {
      logger.error("Access denied when getting user memberships", ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Unexpected error when getting user memberships", e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }
}
