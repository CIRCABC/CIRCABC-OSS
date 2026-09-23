package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.NotificationsApi;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.model.permissions.NewsGroupPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST webscript endpoint that removes the notification configuration of a
 * specific authority (user or group) on a given node.
 *
 * <p>The class name follows the CIRCABC convention {@code <Entity><Method>},
 * implying an HTTP {@code DELETE} on a nodes/authority/notifications resource.
 * The endpoint expects two URL template variables: {@code id} (the node
 * identifier) and {@code authority} (the user or group whose notification
 * settings are to be deleted). An optional {@code language} request parameter
 * controls locale-aware content handling.</p>
 *
 * <p>Before deleting, the caller must hold either the Library admin permission
 * ({@link LibraryPermissions#LIBADMIN}) or the Newsgroup admin permission
 * ({@link NewsGroupPermissions#NWSADMIN}) on the target node; otherwise an
 * {@link AccessDeniedException} is raised and the response is set to
 * {@code 403 Forbidden}.</p>
 *
 * @see CircabcDeclarativeWebScript
 * @see NotificationsApi
 */
public class NodesAuthorityNotificationsDelete
  extends CircabcDeclarativeWebScript
{

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(
    NodesAuthorityNotificationsDelete.class
  );

  /**
   * API used to perform the actual removal of the notification configuration
   * for a node/authority pair.
   */
  @Autowired
  private NotificationsApi notificationsApi;

  /**
   * Service used to verify that the current user holds the required Library or
   * Newsgroup administration permissions on the target node.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the request to delete the notification configuration for the given
   * authority on the given node.
   *
   * <p>Reads the {@code id} and {@code authority} URL template variables and an
   * optional {@code language} request parameter (used to configure the content
   * locale and multilingual awareness). After confirming the current user has
   * the required administration permission, it delegates the removal to
   * {@link NotificationsApi#nodesIdNotificationsAuthorityDelete(String, String)}.
   * The multilingual awareness flag is always restored in the {@code finally}
   * block.</p>
   *
   * @param req the web script request, providing the {@code language} parameter
   *            and the {@code id} / {@code authority} template variables
   * @param status the response status, set to {@code 403}, {@code 400} or
   *               {@code 500} when an error occurs
   * @param cache the cache directives for the response
   * @return an (empty) model map on success or after an
   *         {@link InvalidNodeRefException}; {@code null} when an access-denied
   *         or unexpected error triggers a redirect to the error status
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);
    String language = req.getParameter("language");
    boolean mlAware = MLPropertyInterceptor.isMLAware();

    if (language == null) {
      MLPropertyInterceptor.setMLAware(true);
    } else {
      Locale locale = Locale.of(language);
      I18NUtil.setContentLocale(locale);
      I18NUtil.setLocale(locale);
      MLPropertyInterceptor.setMLAware(false);
    }

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("id");
    String authority = templateVars.get("authority");

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
      this.notificationsApi.nodesIdNotificationsAuthorityDelete(id, authority);
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      logger.error(
        "Error removing notification configuration - access denied",
        ade
      );
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error(ERROR_OCCURRED, inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      logger.error(
        "Error removing notification configuration - invalid node reference",
        inre
      );
      return new HashMap<>();
    } catch (Exception e) {
      logger.error(ERROR_OCCURRED, e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      logger.error("Unexpected error removing notification configuration", e);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
    return model;
  }
}
