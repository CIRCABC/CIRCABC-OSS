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
 * Alfresco Web Script endpoint that clears the membership history logs of a
 * user within an interest group.
 *
 * <p>The class name implies an HTTP {@code DELETE} operation. The endpoint
 * expects two URL template variables:
 *
 * <ul>
 *   <li>{@code groupId} &ndash; the identifier of the interest group whose
 *       membership logs are targeted;</li>
 *   <li>{@code userId} &ndash; the identifier of the user whose membership
 *       logs should be removed.</li>
 * </ul>
 *
 * <p>Access is restricted: only an Alfresco administrator, a CIRCABC
 * administrator or an administrator of the given group may perform the
 * deletion. When the caller lacks the required privileges the endpoint
 * responds with HTTP 403 (Forbidden); any other failure results in HTTP 500
 * (Internal Server Error). On success the underlying
 * {@link io.swagger.api.HistoryApi#cleanMembershipsLogs(String, String)} call
 * removes the logs and an empty model is returned.
 */
public class HistoryMembershipsLogsDelete extends DeclarativeWebScript {

  /** Logger used to report access-denied and processing errors. */
  static final Log logger = LogFactory.getLog(
    HistoryMembershipsLogsDelete.class
  );

  /**
   * Service used to verify whether the current user has the administrative
   * privileges required to delete membership logs.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /** API providing the history operations, including membership log cleanup. */
  @Autowired
  private HistoryApi historyApi;

  /**
   * Handles the request to delete the membership history logs of a user in a
   * group.
   *
   * <p>Reads the {@code groupId} and {@code userId} template variables from the
   * request, checks that the current user is an Alfresco admin, a CIRCABC admin
   * or an admin of the group, and then delegates to
   * {@link io.swagger.api.HistoryApi#cleanMembershipsLogs(String, String)}.
   * Errors are translated into the appropriate HTTP status codes rather than
   * being propagated.
   *
   * @param req the web script request; supplies the {@code groupId} and
   *     {@code userId} template variables
   * @param status the response status object, set to 403 when permissions are
   *     insufficient or 500 when an unexpected error occurs
   * @param cache the cache control object for the response
   * @return an empty model map on success, or {@code null} when the request
   *     fails and a redirect status has been set
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);
    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String groupId = templateVars.get("groupId");
    String userId = templateVars.get("userId");

    try {
      if (
        !(this.currentUserPermissionCheckerService.isAlfrescoAdmin() ||
          this.currentUserPermissionCheckerService.isCircabcAdmin() ||
          this.currentUserPermissionCheckerService.isGroupAdmin(groupId))
      ) {
        throw new AccessDeniedException(
          "Cannot clean the logs of the membership, not enough permissions"
        );
      }

      historyApi.cleanMembershipsLogs(groupId, userId);
    } catch (AccessDeniedException ade) {
      logger.error(
        "Access denied for membership logs deletion. groupId: " +
          groupId +
          ", userId: " +
          userId,
        ade
      );
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(
        "Error deleting membership logs. groupId: " +
          groupId +
          ", userId: " +
          userId,
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
