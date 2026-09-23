package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.GroupsApi;
import io.swagger.api.NotificationsApi;
import io.swagger.model.InterestGroup;
import io.swagger.model.permissions.DirectoryPermissions;
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
 * REST webscript endpoint that removes a member (user) from an Interest Group.
 *
 * <p>The class name follows the CIRCABC convention {@code <Entity><Method>}, so
 * this endpoint handles the HTTP {@code DELETE} method for a member of a group.
 * It is bound to a URL that carries two path variables:
 *
 * <ul>
 *   <li>{@code igId} - the identifier of the target Interest Group;</li>
 *   <li>{@code userId} - the identifier of the user to remove.</li>
 * </ul>
 *
 * <p>Behavior:
 *
 * <ol>
 *   <li>Authorizes the request: the caller must either hold the
 *       {@link DirectoryPermissions#DIRMANAGEMEMBERS} permission on the group or
 *       be the very user being removed (self-removal). Otherwise an
 *       {@link AccessDeniedException} is raised.</li>
 *   <li>Cleans up any Newsgroup and/or Library notification subscriptions the
 *       user held within the group (DIGITCIRCABC-5060).</li>
 *   <li>Removes the user from the Interest Group.</li>
 * </ol>
 *
 * <p>On error it does not throw to the caller but sets an appropriate HTTP
 * status on the response: {@code 403 Forbidden} when access is denied and
 * {@code 400 Bad Request} when a referenced node is invalid.
 *
 * @see CircabcDeclarativeWebScript
 */
public class GroupsMembersDelete extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GroupsMembersDelete.class);

  /** API used to look up the Interest Group and to remove the member. */
  @Autowired
  private GroupsApi groupsApi;

  /** Service used to verify the current user's permissions on the group. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /** API used to inspect and remove the user's notification subscriptions. */
  @Autowired
  private NotificationsApi notificationsApi;

  /**
   * Executes the member removal.
   *
   * <p>Reads the {@code igId} and {@code userId} path variables from the
   * request, enforces the required permissions, removes any Newsgroup/Library
   * notification subscriptions the user has, and finally removes the user from
   * the Interest Group.
   *
   * @param req the web script request; supplies the {@code igId} and
   *            {@code userId} template variables
   * @param status the response status, updated to {@code 403} on access denial
   *               or {@code 400} on an invalid node reference
   * @param cache the response cache control settings
   * @return an empty model map on success, or {@code null} when an error status
   *         has been set on the response
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("igId");
    String userId = templateVars.get("userId");

    try {
      if (
        !(this.currentUserPermissionCheckerService.hasAnyOfDirectoryPermission(
            id,
            DirectoryPermissions.DIRMANAGEMEMBERS
          ) ||
          this.currentUserPermissionCheckerService.isCurrentUserEqualTo(userId))
      ) {
        throw new AccessDeniedException(
          "Not enough rights for deleting a user"
        );
      }

      //DIGITCIRCABC-5060 If the user subscribed to Notifications from Newsgroup and/or Library, remove the subscription(s)
      //-->
      InterestGroup ig = groupsApi.getInterestGroup(id);

      //check if we need to remove Newsgroup notifications for this user
      String newsGroupId = ig.getNewsgroupId();
      if (
        this.notificationsApi.isUsersubscribedForNotification(
          newsGroupId,
          userId
        )
      ) {
        this.notificationsApi.removeNotification(newsGroupId, userId);
      }
      //check if we need to remove library notifications for this user
      String libraryId = ig.getLibraryId();
      if (
        this.notificationsApi.isUsersubscribedForNotification(libraryId, userId)
      ) {
        this.notificationsApi.removeNotification(libraryId, userId);
      }
      //<--

      //remove the user from the Interest Group
      this.groupsApi.groupsIdMembersUserIdDelete(id, userId);
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
    }

    return model;
  }
}
