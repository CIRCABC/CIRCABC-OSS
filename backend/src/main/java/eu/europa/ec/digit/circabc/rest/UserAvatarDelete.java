package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.UsersApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco declarative web script backing the {@code DELETE} endpoint that removes a user's avatar.
 *
 * <p>The class name suffix {@code Delete} maps to the HTTP {@code DELETE} method. The target user is
 * identified by the {@code userId} template variable taken from the request URL. Deletion is only
 * permitted when the caller is the same user as {@code userId} or an Alfresco administrator;
 * otherwise the request is rejected with {@code 403 Forbidden}.</p>
 *
 * <p>On success the response model contains a {@code message} entry set to {@code "ok"}. Error
 * conditions are translated into HTTP status codes: {@code 403} for access denial, {@code 400} for
 * an invalid node reference and {@code 500} for any other failure.</p>
 */
public class UserAvatarDelete extends CircabcDeclarativeWebScript {

  /**
   * Logger used to record errors raised while processing the request.
   */
  static final Log logger = LogFactory.getLog(UserAvatarDelete.class);

  /**
   * API providing user-related operations, including avatar removal.
   */
  @Autowired
  private UsersApi usersApi;

  /**
   * Service used to verify that the caller is allowed to delete the targeted user's avatar
   * (i.e. the caller is the same user or an Alfresco administrator).
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the avatar deletion request.
   *
   * <p>Reads the {@code userId} template variable, verifies that the current user is authorized to
   * delete that user's avatar, and delegates the removal to {@link UsersApi#removeAvatar(String)}.
   * When authorization fails or an error occurs, the appropriate HTTP status is set on
   * {@code status} and {@code null} is returned so the web script framework renders the status
   * response.</p>
   *
   * @param req the web script request, exposing the {@code userId} template variable
   * @param status the response status to populate on error
   * @param cache the response cache directives (unused)
   * @return a model containing a {@code message} entry set to {@code "ok"} on success, or
   *         {@code null} when an error status has been set
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("userId");

    try {
      if (
        !(currentUserPermissionCheckerService.isCurrentUserEqualTo(id) ||
          currentUserPermissionCheckerService.isAlfrescoAdmin())
      ) {
        throw new AccessDeniedException(
          "Impossible to delete the avatar of somebody else"
        );
      }

      this.usersApi.removeAvatar(id);
      model.put("message", "ok");
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error(inre.getMessage(), inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      return null; // NOSONAR
    }

    return model;
  }
}
