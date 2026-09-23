package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.UsersApi;
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
 * Alfresco Web Script endpoint that handles HTTP {@code GET} requests to retrieve the
 * preferences of a given user.
 *
 * <p>The endpoint expects a {@code userId} template variable in the request URL and returns
 * the corresponding user preference in the response model under the {@code preference} key.
 * For security reasons, a user is only allowed to fetch their own preferences: the current
 * authenticated user must match the requested {@code userId}, otherwise the request is
 * rejected with an HTTP 403 (Forbidden) status. Any other unexpected failure results in an
 * HTTP 500 (Internal Server Error) status.
 *
 * @author beaurpi
 */
public class UserPreferenceGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(UserPreferenceGet.class);

  /**
   * API used to resolve and load the requested user's preferences.
   */
  @Autowired
  private UsersApi usersApi;

  /**
   * Service used to verify that the current authenticated user is authorized to access the
   * requested user's data (i.e. that they are the same user).
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Executes the web script logic for retrieving a user's preferences.
   *
   * <p>Reads the {@code userId} template variable from the request, verifies that it matches
   * the current authenticated user, and, if authorized, places the resolved preference into
   * the response model under the {@code preference} key. Authorization failures set an HTTP
   * 403 status while any other error sets an HTTP 500 status; in both error cases {@code null}
   * is returned so that the error status is rendered instead of the model.
   *
   * @param req the web script request, providing the {@code userId} template variable
   * @param status the response status, updated to reflect authorization or unexpected errors
   * @param cache the cache directives for the response
   * @return a model map containing the {@code preference} entry, or {@code null} if an error
   *     occurred and an error status was set instead
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
      String userId = templateVars.get("userId");
      if (!(currentUserPermissionCheckerService.isCurrentUserEqualTo(userId))) {
        throw new AccessDeniedException(
          "Cannot get user feed dashboard of somebody else"
        );
      }
      if (userId != null) {
        model.put("preference", usersApi.getUserPreference(userId));
      }
    } catch (AccessDeniedException ade) {
      logger.error("Access denied when getting user preference", ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Unexpected error when getting user preference", e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      return null; // NOSONAR
    }

    return model;
  }
}
