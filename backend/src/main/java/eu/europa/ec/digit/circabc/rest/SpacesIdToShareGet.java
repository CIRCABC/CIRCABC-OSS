package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.SpacesApi;
import io.swagger.model.ShareIGsAndPermissions;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Read-only Alfresco webscript endpoint that retrieves the list of Interest
 * Groups (IGs) and the associated permissions that a given space can be shared
 * with.
 *
 * <p>The class name follows the {@code <Entity><Method>} convention, so this
 * handles an HTTP {@code GET} request. The target space is identified by the
 * {@code id} path (template) variable extracted from the request URL.</p>
 *
 * <p>Behavior:
 * <ul>
 *   <li>Verifies that the current user holds at least
 *       {@link LibraryPermissions#LIBACCESS} on the space; otherwise the
 *       response is set to {@code 403 Forbidden}.</li>
 *   <li>Temporarily disables the {@link MLPropertyInterceptor} multilingual
 *       awareness while resolving the shareable IGs and permissions, restoring
 *       the previous state afterwards.</li>
 *   <li>On success, exposes the resolved {@link ShareIGsAndPermissions} in the
 *       model under the {@code igsAndPermissions} key for rendering by the
 *       FreeMarker template.</li>
 *   <li>Any other failure results in a {@code 406 Not Acceptable} response.</li>
 * </ul>
 *
 * @author schwerr
 */
public class SpacesIdToShareGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(SpacesIdToShareGet.class);

  /** API providing space-related operations, including resolution of the IGs and permissions a space can be shared with. */
  @Autowired
  private SpacesApi spacesApi;

  /** Service used to check whether the current user holds the required library permissions on the target space. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the GET request: resolves the Interest Groups and permissions that
   * the requested space can be shared with.
   *
   * <p>The space identifier is read from the {@code id} path variable. Access
   * is granted only if the current user has the required library permission.
   * The multilingual property interceptor is disabled for the duration of the
   * lookup and restored in the {@code finally} block.</p>
   *
   * @param req    the web script request; supplies the {@code id} template
   *               variable identifying the target space
   * @param status the response status, updated to {@code 403} when the user
   *               lacks permission or {@code 406} when an unexpected error
   *               occurs
   * @param cache  the cache directives for the response
   * @return a model map containing the {@code igsAndPermissions} entry
   *         ({@link ShareIGsAndPermissions}) on success, or {@code null} when
   *         an error status has been set on the response
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String spaceId = templateVars.get("id");
    boolean mlAware = MLPropertyInterceptor.isMLAware();

    try {
      if (
        !this.currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
          spaceId,
          LibraryPermissions.LIBACCESS
        )
      ) {
        throw new AccessDeniedException(
          "Cannot get the shares, not enough permissions"
        );
      }

      MLPropertyInterceptor.setMLAware(false);

      ShareIGsAndPermissions igsAndPermissions =
        this.spacesApi.getShareIGsAndPermissions(spaceId);

      model.put("igsAndPermissions", igsAndPermissions);
    } catch (AccessDeniedException ade) {
      logger.error(
        "Access denied when retrieving shares for space: " + spaceId,
        ade
      );
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Error retrieving shares for space: " + spaceId, e);
      status.setCode(Status.STATUS_NOT_ACCEPTABLE);
      status.setMessage(e.getMessage());
      status.setException(e);
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }
}
