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
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco Web Script backing the HTTP {@code GET} endpoint that reports whether a former member of
 * an Interest Group can be recovered (i.e. re-added) from the group's membership history.
 *
 * <p>The endpoint resolves the {@code userId} and {@code groupId} URL template variables and, after
 * confirming that the caller has sufficient privileges (Alfresco administrator, CIRCABC
 * administrator, or director/administrator of the target Interest Group), delegates to
 * {@link HistoryApi#isRecoverableFromGroup(String, String)}. The resulting recovery flag is exposed
 * to the response template under the {@code recoveryOption} model key.
 *
 * <p>Error handling: a permission failure results in an HTTP {@code 403 Forbidden} response, while
 * any other failure results in an HTTP {@code 406 Not Acceptable} response; in both cases the model
 * is not rendered.
 */
public class HistoryGroupMembershipRecoverableGet extends DeclarativeWebScript {

  /** Logger used to record permission denials and unexpected failures during recovery checks. */
  static final Log logger = LogFactory.getLog(
    HistoryGroupMembershipRecoverableGet.class
  );

  /** Service used to verify the current user's administrative privileges over the group. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /** Business API providing history-related operations, including recoverability checks. */
  @Autowired
  private HistoryApi historyApi;

  /**
   * Handles the incoming {@code GET} request and determines whether the given user can be recovered
   * into the given Interest Group from its membership history.
   *
   * <p>Reads the {@code userId} and {@code groupId} template variables from the request URL,
   * enforces that the caller is an Alfresco admin, a CIRCABC admin, or a director/administrator of
   * the target group, and then queries {@link HistoryApi#isRecoverableFromGroup(String, String)}.
   *
   * @param req the web script request; its service match must supply the {@code userId} and {@code
   *     groupId} template variables
   * @param status the response status, updated to {@code 403} on access denial or {@code 406} on
   *     other errors
   * @param cache the response cache directives
   * @return a model map containing the {@code recoveryOption} boolean flag on success, or {@code
   *     null} when an error occurs and a redirect status has been set
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

    try {
      if (
        !(this.currentUserPermissionCheckerService.isAlfrescoAdmin() ||
          this.currentUserPermissionCheckerService.isCircabcAdmin() ||
          this.currentUserPermissionCheckerService.isInterestGroupDirAdmin(
            groupId
          ))
      ) {
        throw new AccessDeniedException(
          "Cannot verify if the user is recoverable, not enough permissions"
        );
      }

      model.put(
        "recoveryOption",
        historyApi.isRecoverableFromGroup(userId, groupId)
      );
    } catch (AccessDeniedException ade) {
      logger.error(
        "Access denied for user recovery check. userId: " +
          userId +
          ", groupId: " +
          groupId,
        ade
      );
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(
        "Error checking if user is recoverable. userId: " +
          userId +
          ", groupId: " +
          groupId,
        e
      );
      status.setCode(Status.STATUS_NOT_ACCEPTABLE);
      status.setMessage(e.getMessage());
      status.setException(e);
      status.setRedirect(true);
      return null; // NOSONAR
    }
    return model;
  }
}
