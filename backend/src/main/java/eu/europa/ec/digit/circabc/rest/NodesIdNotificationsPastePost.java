package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.NotificationsApi;
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
 * Alfresco web script endpoint handling the HTTP {@code POST} request that toggles the
 * "paste" notification configuration of a given node.
 *
 * <p>The endpoint enables or disables the notifications that are sent when content is pasted
 * into the node identified by the {@code id} template variable. It accepts two request
 * parameters:
 *
 * <ul>
 *   <li>{@code pasteEnable} - {@code "true"} to enable paste notifications, any other value
 *       disables them.</li>
 *   <li>{@code pasteAllEnable} - {@code "true"} to enable paste notifications for all users,
 *       any other value disables them.</li>
 * </ul>
 *
 * <p>Only group administrators of the target node are allowed to change this configuration;
 * requests from other users are rejected with an HTTP {@code 403 Forbidden} status. The actual
 * update is delegated to {@link NotificationsApi#setPasteNotificationsState(String, boolean, boolean)}.
 */
public class NodesIdNotificationsPastePost extends CircabcDeclarativeWebScript {

  /** Logger used to report access, validation and unexpected errors for this endpoint. */
  static final Log logger = LogFactory.getLog(
    NodesIdNotificationsPastePost.class
  );

  /** API used to update the paste notification state of a node. */
  @Autowired
  private NotificationsApi notificationsApi;

  /** Service used to verify that the current user has group administrator permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Updates the paste notification configuration for the node referenced by the {@code id}
   * template variable.
   *
   * <p>The method verifies that the current user is a group administrator, then applies the
   * {@code pasteEnable} and {@code pasteAllEnable} request parameters through the
   * {@link NotificationsApi}. Multilingual property interception is temporarily disabled while
   * the update is performed and restored afterwards. On error the response status is set
   * accordingly and {@code null} is returned.
   *
   * @param req the web script request; provides the {@code id} template variable and the
   *     {@code pasteEnable} / {@code pasteAllEnable} parameters
   * @param status the web script response status, updated to reflect forbidden, bad request or
   *     internal server error conditions
   * @param cache the response cache directives
   * @return an empty model map on success, or {@code null} when an error occurred and the
   *     response status has been set to an error code
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
      if (!(this.currentUserPermissionCheckerService.isGroupAdmin(id))) {
        throw new AccessDeniedException(
          "Impossible to edit the notification configuration, not enough permission"
        );
      }

      MLPropertyInterceptor.setMLAware(false);

      boolean pasteEnable = "true".equals(req.getParameter("pasteEnable"));
      boolean pasteAllEnable = "true".equals(
        req.getParameter("pasteAllEnable")
      );

      this.notificationsApi.setPasteNotificationsState(
        id,
        pasteEnable,
        pasteAllEnable
      );
    } catch (AccessDeniedException ade) {
      logger.error(
        "Access denied when setting paste notifications state for node: " + id,
        ade
      );
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error(
        "Invalid node reference when setting paste notifications state for node: " +
          id,
        inre
      );
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(
        "Unexpected error when setting paste notifications state for node: " +
          id,
        e
      );
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
