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
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST webscript endpoint that deletes a CIRCABC user.
 *
 * <p>Backs an HTTP {@code DELETE} request (as implied by the {@code Delete}
 * suffix in the class name) on a user resource identified by the {@code userId}
 * path template variable. The user to remove is resolved from the request URI
 * and deleted through {@link io.swagger.api.UsersApi#usersUserIdDelete(String)}.
 *
 * <p>Access is restricted: only CIRCABC administrators or Alfresco
 * administrators are allowed to delete users. Any other caller results in an
 * HTTP {@code 403 Forbidden} response. A missing/unknown user yields an HTTP
 * {@code 404 Not Found}, and any other failure yields an HTTP
 * {@code 500 Internal Server Error}.
 *
 * @see CircabcDeclarativeWebScript
 */
public class UserIdDelete extends CircabcDeclarativeWebScript {

  /** Logger used to report deletion failures and access violations. */
  static final Log logger = LogFactory.getLog(UserIdDelete.class);

  /** API used to perform the actual user deletion in the repository. */
  @Autowired
  private UsersApi usersApi;

  /** Service used to check whether the current user has administrator rights. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Deletes the user identified by the {@code userId} path variable.
   *
   * <p>Verifies that the current user is a CIRCABC or Alfresco administrator
   * before delegating the deletion to {@link UsersApi}. On error, sets the
   * appropriate HTTP status on {@code status}, marks the response as a redirect
   * and returns {@code null}.
   *
   * @param req the web script request; supplies the {@code userId} template
   *     variable identifying the user to delete
   * @param status the response status, populated with an error code
   *     (404 / 403 / 500) when the deletion cannot be completed
   * @param cache the response cache directives (unused)
   * @return an empty model map on success, or {@code null} when an error status
   *     has been set on the response
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
        !currentUserPermissionCheckerService.isCircabcAdmin() &&
        !currentUserPermissionCheckerService.isAlfrescoAdmin()
      ) {
        throw new AccessDeniedException(
          "Only CIRCABC or Alfresco administrators can delete users"
        );
      }

      String userId = templateVars.get("userId");
      if (userId != null) {
        this.usersApi.usersUserIdDelete(userId);
      }
    } catch (IllegalArgumentException iae) {
      logger.error("User not found", iae);
      status.setCode(Status.STATUS_NOT_FOUND);
      status.setMessage("User not found");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (AccessDeniedException ade) {
      logger.error("Access denied", ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Error deleting user", e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      return null; // NOSONAR
    }

    return model;
  }
}
