package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.NotificationsApi;
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
 * REST webscript endpoint that removes the notification configuration of a node.
 *
 * <p>Backing the HTTP {@code DELETE} operation (as implied by the {@code Delete} suffix of the
 * class name), this endpoint deletes the notification settings associated with a given node for a
 * specific authority. The node identifier is taken from the {@code id} URL template variable and
 * the authority whose notification is removed is provided through the {@code authority} request
 * parameter.
 *
 * <p>Access is restricted: the current user must hold either the {@link
 * LibraryPermissions#LIBADMIN} library permission or the {@link NewsGroupPermissions#NWSADMIN}
 * newsgroup permission on the target node. Otherwise an {@link AccessDeniedException} is raised and
 * translated into an HTTP {@code 403 Forbidden} response.
 *
 * @see CircabcDeclarativeWebScript
 * @see NotificationsApi
 */
public class NodesIdNotificationDelete extends CircabcDeclarativeWebScript {

  /** Logger used to report errors that occur while removing the notification configuration. */
  static final Log logger = LogFactory.getLog(NodesIdNotificationDelete.class);

  /** API used to perform the actual removal of the node's notification configuration. */
  @Autowired
  private NotificationsApi notificationsApi;

  /** Service used to check whether the current user has the required permissions on the node. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the request to remove a node's notification configuration.
   *
   * <p>Extracts the node {@code id} from the URL template variables and the {@code authority} from
   * the request parameters, verifies that the current user has administrative permission on the
   * node (library {@code LIBADMIN} or newsgroup {@code NWSADMIN}), and delegates the removal to the
   * {@link NotificationsApi}. Multilingual property awareness is temporarily disabled during the
   * operation and restored afterwards.
   *
   * @param req the incoming web script request; supplies the {@code id} template variable and the
   *     {@code authority} parameter
   * @param status the response status, updated with the appropriate HTTP status code when an error
   *     occurs
   * @param cache the cache directive for the response
   * @return an empty model map on success, or {@code null} when an error occurs and the status has
   *     been set to an error code
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
          ))
      ) {
        throw new AccessDeniedException(
          "Impossible to remove notification configuration, not enough permission"
        );
      }

      String authority = req.getParameter("authority");

      MLPropertyInterceptor.setMLAware(false);

      this.notificationsApi.removeNotification(id, authority);
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error("Access denied: " + ade.getMessage(), ade);
      }
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error(ERROR_OCCURRED, inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error("Invalid node reference: " + inre.getMessage(), inre);
      }
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(ERROR_OCCURRED, e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error(
          "Unexpected error during notification deletion: " + e.getMessage(),
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
