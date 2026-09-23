package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.FavouritesApi;
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
 * REST webscript endpoint that removes a node from a user's list of favourites.
 *
 * <p>The class name implies an HTTP {@code DELETE} request. It handles the
 * removal of the favourite identified by {@code nodeId} from the favourites of
 * the user identified by {@code userId}, both provided as URL template
 * variables (e.g. {@code /users/{userId}/favourites/{nodeId}}).
 *
 * <p>A user may only delete their own favourites: the endpoint verifies that
 * the currently authenticated user matches {@code userId}, otherwise the
 * request is rejected with an HTTP 403 (Forbidden). An invalid node reference
 * results in an HTTP 400 (Bad request).
 *
 * @author beaurpi
 */
public class UsersFavouritesDelete extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(UsersFavouritesDelete.class);

  /**
   * API providing the business logic to manage a user's favourites.
   */
  @Autowired
  private FavouritesApi favouritesApi;

  /**
   * Service used to verify that the current user matches the target user,
   * ensuring a user can only delete their own favourites.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Deletes the favourite identified by {@code nodeId} from the favourites of
   * the user identified by {@code userId}.
   *
   * <p>Both identifiers are read from the request URL template variables. The
   * method first checks that the current user is the same as {@code userId};
   * if not, access is denied. When both identifiers are present, the removal is
   * delegated to {@link FavouritesApi}.
   *
   * @param req the web script request, providing the {@code userId} and
   *            {@code nodeId} URL template variables
   * @param status the web script response status; set to
   *               {@link Status#STATUS_BAD_REQUEST} on an invalid node
   *               reference or {@link Status#STATUS_FORBIDDEN} when the current
   *               user tries to delete another user's favourite
   * @param cache the cache directives for the response
   * @return an empty model map on success, or {@code null} when an error
   *         occurred and a redirect status has been set
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

      if (!currentUserPermissionCheckerService.isCurrentUserEqualTo(userId)) {
        throw new AccessDeniedException(
          "Cannot delete user favourite of somebody else"
        );
      }

      String nodeId = templateVars.get("nodeId");
      if ((userId != null) && (nodeId != null)) {
        this.favouritesApi.usersUserIdFavouritesNodeIdDelete(userId, nodeId);
      }
    } catch (InvalidNodeRefException inre) {
      logger.error(ERROR_OCCURRED, inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    }

    return model;
  }
}
