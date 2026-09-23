package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.GroupsApi;
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
 * REST webscript endpoint that removes the membership expiration date for a
 * user within an Interest Group.
 *
 * <p>The class name implies an HTTP <b>DELETE</b> request. Given an Interest
 * Group identifier and a user identifier (both supplied as URL template
 * variables {@code igId} and {@code userId}), it clears any expiration date
 * previously set on that user's membership, effectively making the membership
 * permanent again.</p>
 *
 * <p>Before performing the operation, the current user must hold the
 * {@link DirectoryPermissions#DIRMANAGEMEMBERS} permission on the target
 * Interest Group; otherwise the request is rejected with an HTTP
 * {@code 403 Forbidden}. Invalid node references result in an HTTP
 * {@code 400 Bad Request}.</p>
 *
 * @see CircabcDeclarativeWebScript
 * @see GroupsApi#groupsIdMembersUserIdExpirationDelete(String, String)
 */
public class GroupsMembersExpirationDelete extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(
    GroupsMembersExpirationDelete.class
  );

  /** API providing group and membership operations, including expiration handling. */
  @Autowired
  private GroupsApi groupsApi;

  /** Service used to verify that the current user holds the required directory permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the request to delete a member's expiration date within an
   * Interest Group.
   *
   * <p>Reads the {@code igId} and {@code userId} URL template variables,
   * checks that the current user has the
   * {@link DirectoryPermissions#DIRMANAGEMEMBERS} permission on the group,
   * and then removes the expiration date associated with the given member.</p>
   *
   * @param req    the web script request, providing the {@code igId} and
   *               {@code userId} template variables
   * @param status the response status; set to {@code 403} on access denial or
   *               {@code 400} on an invalid node reference
   * @param cache  the response cache directives
   * @return an empty model map on success, or {@code null} when the request
   *         fails and a redirect status has been set
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
        !this.currentUserPermissionCheckerService.hasAnyOfDirectoryPermission(
          id,
          DirectoryPermissions.DIRMANAGEMEMBERS
        )
      ) {
        throw new AccessDeniedException(
          "Not enough rights for deleting expiration date "
        );
      }

      this.groupsApi.groupsIdMembersUserIdExpirationDelete(id, userId);
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
