package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.HistoryApi;
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
 * Alfresco webscript endpoint that recovers a previously removed group
 * membership for a user.
 *
 * <p>This class handles an HTTP {@code POST} request (implied by the
 * {@code Post} suffix in the class name). The endpoint expects the target
 * user, interest group and profile to be supplied as URL template variables
 * ({@code userId}, {@code groupId} and {@code profileId}) and delegates the
 * recovery operation to {@link io.swagger.api.HistoryApi}.
 *
 * <p>Access is restricted: only an Alfresco administrator, a CIRCABC
 * administrator, or a director/administrator of the target interest group is
 * allowed to recover a membership. Callers lacking the required permission
 * receive an HTTP {@code 403 Forbidden} response, while unexpected failures
 * result in an HTTP {@code 500 Internal Server Error}.
 */
public class HistoryGroupMembershipRecoverablePost
  extends CircabcDeclarativeWebScript
{

  /** Logger used to report access denials and recovery failures. */
  static final Log logger = LogFactory.getLog(
    HistoryGroupMembershipRecoverablePost.class
  );

  /**
   * Service used to check whether the current user has the permissions
   * required to recover a group membership (Alfresco admin, CIRCABC admin or
   * interest group director/administrator).
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /** API providing the membership recovery business logic. */
  @Autowired
  private HistoryApi historyApi;

  /**
   * Executes the membership recovery request.
   *
   * <p>Reads the {@code userId}, {@code groupId} and {@code profileId} URL
   * template variables, verifies that the current user is authorized, and then
   * recovers the membership through {@link io.swagger.api.HistoryApi}. On an
   * authorization failure the response status is set to
   * {@link Status#STATUS_FORBIDDEN}; on any other error it is set to
   * {@link Status#STATUS_INTERNAL_SERVER_ERROR}.
   *
   * @param req the web script request; supplies the {@code userId},
   *            {@code groupId} and {@code profileId} URL template variables
   * @param status the response status object, updated with an error code and
   *               message when the recovery cannot be performed
   * @param cache the response cache control object
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
    String userId = templateVars.get("userId");
    String groupId = templateVars.get("groupId");
    String profileId = templateVars.get("profileId");

    try {
      if (
        !(this.currentUserPermissionCheckerService.isAlfrescoAdmin() ||
          this.currentUserPermissionCheckerService.isCircabcAdmin() ||
          this.currentUserPermissionCheckerService.isInterestGroupDirAdmin(
            groupId
          ))
      ) {
        throw new AccessDeniedException(
          "Cannot recover the membership, not enough permissions"
        );
      }

      historyApi.recoverMembershipFromGroup(userId, groupId, profileId);
    } catch (AccessDeniedException ade) {
      logger.error(
        "Access denied for membership recovery. userId: " +
          userId +
          ", groupId: " +
          groupId +
          ", profileId: " +
          profileId,
        ade
      );
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(
        "Error recovering membership. userId: " +
          userId +
          ", groupId: " +
          groupId +
          ", profileId: " +
          profileId,
        e
      );
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage(e.getMessage());
      status.setException(e);
      status.setRedirect(true);
      return null; // NOSONAR
    }
    return model;
  }
}
