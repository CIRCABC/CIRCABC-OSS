package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.NotificationsApi;
import io.swagger.model.NotificationStatus;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.model.permissions.NewsGroupPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST webscript endpoint that updates the notification subscription status of an
 * authority (user or group) on a given node.
 *
 * <p>The class name implies an HTTP {@code POST} on a node-scoped URL of the form
 * {@code .../nodes/{id}/notification/updateStatus}. It is wired as an Alfresco
 * declarative webscript via the Spring context and extends
 * {@link CircabcDeclarativeWebScript}.
 *
 * <p>Key inputs:
 * <ul>
 *   <li>{@code id} &ndash; the node identifier, taken from the URL template variables.</li>
 *   <li>{@code authority} &ndash; request parameter identifying the user or group whose
 *       notification status is being changed.</li>
 *   <li>{@code status} &ndash; request parameter for the desired notification status.
 *       Values {@code "true"} or {@code "subscribed"} (case-insensitive) map to
 *       {@link NotificationStatus#SUBSCRIBED}; any other value maps to
 *       {@link NotificationStatus#UNSUBSCRIBED}.</li>
 * </ul>
 *
 * <p>The caller must hold library admin, newsgroup admin, or group admin rights on the
 * node; otherwise an {@link AccessDeniedException} is raised and the response is set to
 * HTTP 403. Invalid node references result in HTTP 400 and any other failure in HTTP 500.
 */
public class NodesIdNotificationUpdateStatusPost
  extends CircabcDeclarativeWebScript
{

  /** Logger for this webscript. */
  static final Log logger = LogFactory.getLog(
    NodesIdNotificationUpdateStatusPost.class
  );

  /** API used to apply the notification status change on the node for an authority. */
  @Autowired
  private NotificationsApi notificationsApi;

  /** Service used to verify that the current user has the required admin permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the request: validates the caller's permissions on the node, resolves the
   * desired notification status from the request parameters, and delegates the update to
   * {@link NotificationsApi#setNotificationStatus}.
   *
   * <p>While applying the change, ML awareness is temporarily disabled and always restored
   * to its previous value in a {@code finally} block. On error, the appropriate HTTP status
   * code is set on {@code status} and {@code null} is returned to trigger a redirect to the
   * error response.
   *
   * @param req the webscript request; supplies the {@code id} URL template variable and the
   *            {@code authority} and {@code status} parameters
   * @param status the response status, updated with the appropriate HTTP code on failure
   * @param cache the response cache directives
   * @return an empty model map on success, or {@code null} when an error occurs and a
   *         redirect to the error response is required
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    boolean mlAware = MLPropertyInterceptor.isMLAware();

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("id");

    try {
      if (
        !(this.currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
            id,
            LibraryPermissions.LIBADMIN
          ) ||
          this.currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
            id,
            NewsGroupPermissions.NWSADMIN
          ) ||
          this.currentUserPermissionCheckerService.isGroupAdmin(id))
      ) {
        throw new AccessDeniedException(
          "Impossible to remove notification configuration, not enough permission"
        );
      }

      String authority = req.getParameter("authority");

      String notificationStatus = req.getParameter("status");
      NotificationStatus mappedNotificationStatus =
        NotificationStatus.UNSUBSCRIBED;
      if (
        "true".equalsIgnoreCase(notificationStatus) ||
        "subscribed".equalsIgnoreCase(notificationStatus)
      ) {
        mappedNotificationStatus = NotificationStatus.SUBSCRIBED;
      }

      MLPropertyInterceptor.setMLAware(false);

      this.notificationsApi.setNotificationStatus(
        id,
        authority,
        mappedNotificationStatus
      );
    } catch (AccessDeniedException ade) {
      logger.error(
        "Access denied when updating notification status for id: " + id,
        ade
      );
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error(
        "Invalid node reference when updating notification status for id: " +
          id,
        inre
      );
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Error when updating notification status for id: " + id, e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }
}
