package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.FavouritesApi;
import io.swagger.model.SimpleId;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.SimpleIdJsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco declarative web script handling the HTTP {@code POST} request that adds a node to a
 * user's list of favourites.
 *
 * <p>The endpoint is bound to a user identified by the {@code userId} URL template variable and
 * expects a JSON body describing the node to favourite (parsed into a {@link SimpleId}). Before
 * performing any change it verifies that the currently authenticated user matches {@code userId},
 * so a user can only manage their own favourites; otherwise an {@link AccessDeniedException} is
 * raised and the request is rejected.
 *
 * <p>The actual persistence is delegated to {@link FavouritesApi#usersUserIdFavouritesPost(String,
 * SimpleId)}. Bad input (invalid node reference, malformed JSON body) results in a
 * {@link Status#STATUS_BAD_REQUEST}, while a permission violation results in a
 * {@link Status#STATUS_FORBIDDEN}.
 *
 * @author beaurpi
 */
public class UsersFavouritesPost extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(UsersFavouritesPost.class);

  /** API used to add the requested node to the target user's favourites. */
  @Autowired
  private FavouritesApi favouritesApi;

  /** Service used to ensure the authenticated user only alters their own favourites. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Adds the node described in the request body to the favourites of the user identified by the
   * {@code userId} URL template variable.
   *
   * <p>The method first checks that the authenticated user is the same as {@code userId}; if not,
   * the request is denied. When the check passes and {@code userId} is present, the request body is
   * parsed into a {@link SimpleId} and forwarded to the favourites API.
   *
   * @param req the web script request; provides the {@code userId} template variable and the JSON
   *     payload identifying the node to favourite
   * @param status the response status, set to {@link Status#STATUS_BAD_REQUEST} on invalid input or
   *     {@link Status#STATUS_FORBIDDEN} when the user tries to modify another user's favourites
   * @param cache the cache directives for the response
   * @return an empty model map on success, or {@code null} when the request is rejected and a
   *     redirect status has been set
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
          "Cannot add a user favourite of somebody else"
        );
      }

      if (userId != null) {
        SimpleId id = SimpleIdJsonParser.parse(req);
        this.favouritesApi.usersUserIdFavouritesPost(userId, id);
      }
    } catch (InvalidNodeRefException | ParseException | IOException inre) {
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
