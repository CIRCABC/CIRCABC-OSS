package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

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
 * REST webscript endpoint that clears all explicit permissions granted to a
 * given authority on a node.
 *
 * <p>As implied by the {@code Delete} suffix in the class name, this endpoint
 * handles an HTTP {@code DELETE} request. The target node and authority are
 * supplied as URL template variables:
 *
 * <ul>
 *   <li>{@code id} &ndash; the identifier of the node whose permissions are
 *       being modified.</li>
 *   <li>{@code authority} &ndash; the authority (user or group) whose
 *       permissions on the node should be removed. Any URL-encoded dots
 *       ({@code %2E}) in the value are decoded back to {@code .} before use.</li>
 * </ul>
 *
 * <p>An optional {@code language} request parameter controls the content
 * locale used while processing the request; when omitted the webscript runs in
 * multilingual-aware mode.
 *
 * <p>The caller must hold library administration ({@code LIBADMIN}) or
 * newsgroup administration ({@code NWSADMIN}) permission on the node; otherwise
 * the request is rejected with HTTP {@code 403 Forbidden}. The actual removal
 * is delegated to {@link io.swagger.api.PermissionsApi#nodeIdPermissionsClear}.
 *
 * @see CircabcDeclarativeWebScript
 */
public class NodesAllPermissionsDelete extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(NodesAllPermissionsDelete.class);

  /** API used to clear the permissions held by an authority on a node. */
  @Autowired
  private PermissionsApi permissionsApi;

  /** Service used to verify that the current user is allowed to perform the operation. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the {@code DELETE} request by clearing every permission that the
   * requested authority holds on the target node.
   *
   * <p>The {@code id} and {@code authority} template variables identify the
   * node and the authority respectively, while the optional {@code language}
   * request parameter selects the content locale. Before performing the
   * deletion, the current user is checked for {@code LIBADMIN} or
   * {@code NWSADMIN} permission on the node.
   *
   * <p>On failure the method sets an appropriate HTTP status on
   * {@code status}, marks it as a redirect and returns {@code null}:
   * {@code 403} when access is denied, {@code 400} when the node reference is
   * invalid and {@code 500} for any other unexpected error.
   *
   * @param req the incoming web script request, providing the template
   *            variables and the optional {@code language} parameter
   * @param status the response status to be populated, in particular when an
   *               error occurs
   * @param cache the cache control settings for the response
   * @return an empty model map on success, or {@code null} if an error was
   *         handled and the corresponding error status was set
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
          "Impossible to delete the permission for authorities, not enough permissions"
        );
      }

      String clearAuthority = authority.replace("%2E", ".");

      this.permissionsApi.nodeIdPermissionsClear(id, clearAuthority);
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      logger.error(
        "Error deleting permissions for authorities - access denied",
        ade
      );
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error(ERROR_OCCURRED, inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      logger.error(
        "Error deleting permissions for authorities - invalid node reference",
        inre
      );
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(ERROR_OCCURRED, e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      logger.error("Unexpected error deleting permissions for authorities", e);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
    return model;
  }
}
