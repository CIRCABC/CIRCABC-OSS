package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.PermissionsApi;
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
 * REST endpoint that removes a single permission grant from a node.
 *
 * <p>This Alfresco Declarative Web Script backs an HTTP {@code DELETE} request (as implied by the
 * {@code Delete} suffix in the class name). It targets a specific permission assigned to a specific
 * authority (user or group) on a given node.
 *
 * <p>The node reference, the authority and the permission to revoke are supplied as URL template
 * variables ({@code id}, {@code authority} and {@code permission}). An optional {@code language}
 * request parameter controls the content locale and multilingual (ML) awareness while the operation
 * is performed.
 *
 * <p>Before deleting, the caller must hold administrative rights on the node, i.e. either the
 * {@link LibraryPermissions#LIBADMIN} library permission or the
 * {@link NewsGroupPermissions#NWSADMIN} newsgroup permission; otherwise the request is rejected.
 *
 * <p>Error handling maps failures to HTTP status codes: insufficient rights yield
 * {@code 403 Forbidden}, an invalid node reference yields {@code 400 Bad Request}, and any other
 * failure yields {@code 500 Internal Server Error}.
 */
public class NodesPermissionsDelete extends CircabcDeclarativeWebScript {

  /**
   * Fragment reused when building log messages, joining the node id and the authority.
   */
  private static final String AND_AUTHORITY = " and authority ";

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(NodesPermissionsDelete.class);

  /**
   * API used to perform the actual permission removal on the node.
   */
  @Autowired
  private PermissionsApi permissionsApi;

  /**
   * Service used to verify that the current user holds the administrative permissions required to
   * revoke a permission on the target node.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the DELETE request by revoking the given permission for the given authority on the
   * specified node.
   *
   * <p>Reads the {@code id}, {@code authority} and {@code permission} URL template variables and the
   * optional {@code language} request parameter. When {@code language} is absent, ML awareness is
   * enabled; otherwise the content and UI locales are set from it and ML awareness is disabled. The
   * previous ML-awareness state is always restored before returning.
   *
   * <p>The current user must have library administrator ({@link LibraryPermissions#LIBADMIN}) or
   * newsgroup administrator ({@link NewsGroupPermissions#NWSADMIN}) rights on the node; otherwise the
   * response is set to {@code 403 Forbidden}.
   *
   * @param req the web script request, providing the URL template variables and request parameters
   * @param status the response status, updated with an error code when the deletion cannot be
   *     performed
   * @param cache the cache directives for the response
   * @return an empty model map on success, or {@code null} when an error status (403, 400 or 500)
   *     has been set and the response is redirected
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
    String permission = templateVars.get("permission");
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
          "Impossible to delete the permission for authorities, not enough permissions"
        );
      }

      this.permissionsApi.nodeIdPermissionsDelete(id, authority, permission);
    } catch (AccessDeniedException ade) {
      logger.error(
        "Access denied when deleting permissions for node " +
          id +
          AND_AUTHORITY +
          authority,
        ade
      );
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error(
        "Invalid node reference when deleting permissions for node " +
          id +
          AND_AUTHORITY +
          authority,
        inre
      );
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(
        "Unexpected error when deleting permissions for node " +
          id +
          AND_AUTHORITY +
          authority,
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
