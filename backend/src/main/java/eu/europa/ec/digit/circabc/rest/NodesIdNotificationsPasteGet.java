package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.NotificationsApi;
import io.swagger.model.PasteNotificationsState;
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
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco Web Script endpoint that handles HTTP {@code GET} requests to
 * retrieve the "paste" notification configuration of a given node.
 *
 * <p>The endpoint is bound to a URL carrying an {@code id} template variable
 * identifying the target node. It resolves the current paste notification
 * state for that node and exposes it in the response model.
 *
 * <p>Access is restricted to group administrators of the node: callers that
 * are not group admins receive an HTTP {@code 403 Forbidden} response. An
 * invalid node reference results in an HTTP {@code 400 Bad Request}, and any
 * other unexpected failure results in an HTTP {@code 500 Internal Server
 * Error}.
 *
 * <p>Key inputs:
 * <ul>
 *   <li>{@code id} (URL template variable) &ndash; the identifier of the node
 *       whose paste notification state is requested.</li>
 * </ul>
 *
 * <p>Response model entries:
 * <ul>
 *   <li>{@code pasteEnabled} &ndash; whether paste notifications are enabled.</li>
 *   <li>{@code pasteAllEnabled} &ndash; whether "paste all" notifications are
 *       enabled.</li>
 * </ul>
 */
public class NodesIdNotificationsPasteGet extends DeclarativeWebScript {

  /** Logger used to report access, validation and unexpected errors. */
  static final Log logger = LogFactory.getLog(
    NodesIdNotificationsPasteGet.class
  );

  /** API providing access to node notification configuration operations. */
  @Autowired
  private NotificationsApi notificationsApi;

  /** Service used to verify that the current user is a group administrator. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Retrieves the paste notification state for the node identified by the
   * {@code id} URL template variable and populates the response model.
   *
   * <p>The method first checks that the current user is a group administrator
   * for the node. It temporarily disables multilingual (ML) property
   * interception while reading the notification state and restores the
   * previous ML-aware flag afterwards.
   *
   * <p>On error the appropriate HTTP status is set on {@code status} and
   * {@code null} is returned so the framework renders the error response.
   *
   * @param req the web script request; expected to contain the {@code id}
   *            template variable identifying the target node
   * @param status the response status object, updated with an error code and
   *               message when the request cannot be fulfilled
   * @param cache the response cache directives
   * @return a model map containing the {@code pasteEnabled} and
   *         {@code pasteAllEnabled} flags on success, or {@code null} when an
   *         error occurred and an error status was set
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
          "Impossible to get the notification configuration, not enough permission"
        );
      }

      MLPropertyInterceptor.setMLAware(false);

      PasteNotificationsState state =
        this.notificationsApi.getPasteNotificationsState(id);

      model.put("pasteEnabled", state.isPasteEnabled());
      model.put("pasteAllEnabled", state.isPasteAllEnabled());
    } catch (AccessDeniedException ade) {
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error("Access denied: " + ade.getMessage(), ade);
      }
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error("Invalid node reference: " + inre.getMessage(), inre);
      }
      return null; // NOSONAR
    } catch (Exception e) {
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error(
          "Unexpected error during paste notifications check: " +
            e.getMessage(),
          e
        );
      }
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }
}
