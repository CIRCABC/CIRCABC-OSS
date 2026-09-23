/**
 *
 */
package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.SpacesApi;
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
 * Alfresco Declarative Web Script backing the HTTP {@code GET} endpoint that
 * computes the total size of a folder (space) in the Library service.
 *
 * <p>The target folder is identified by the {@code id} template variable taken
 * from the request URL. Before computing the size the endpoint verifies that
 * the current user holds at least {@link LibraryPermissions#LIBACCESS} on the
 * folder; otherwise an {@link AccessDeniedException} is raised and the response
 * is returned with HTTP status {@code 403 Forbidden}.</p>
 *
 * <p>Any other failure while computing the size results in HTTP status
 * {@code 406 Not Acceptable}. On success the computed size (in bytes) is
 * exposed in the response model under the {@code result} key, which the
 * associated FreeMarker template renders as JSON.</p>
 *
 * <p>Multilingual property interception ({@link MLPropertyInterceptor}) is
 * temporarily disabled during the size computation and restored to its
 * previous state afterwards.</p>
 *
 * @author trifapa
 */
public class SpacesFolderSizeGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(SpacesFolderSizeGet.class);

  /** API providing space (folder) operations, including folder size computation. */
  @Autowired
  private SpacesApi spacesApi;

  /** Service used to verify that the current user has the required library permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the web script request: resolves the folder id from the URL,
   * checks the current user's library permissions and, if allowed, computes
   * the folder size.
   *
   * <p>On an access-denial the response status is set to
   * {@code 403 Forbidden}; on any other error it is set to
   * {@code 406 Not Acceptable}. In both error cases {@code null} is returned
   * so the framework renders the configured status template. On success the
   * model contains the folder size under the {@code result} key.</p>
   *
   * @param req the web script request; the folder id is read from the
   *     {@code id} URL template variable
   * @param status the web script response status, updated with the appropriate
   *     HTTP code, message and redirect flag on error
   * @param cache the web script response cache control settings
   * @return the model map holding the folder size under {@code result}, or
   *     {@code null} if the request failed (permission denied or other error)
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String folderId = templateVars.get("id");
    boolean mlAware = MLPropertyInterceptor.isMLAware();

    try {
      if (
        !this.currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
          folderId,
          LibraryPermissions.LIBACCESS
        )
      ) {
        throw new AccessDeniedException(
          "Cannot get the folder size, not enough permissions"
        );
      }

      MLPropertyInterceptor.setMLAware(false);

      int folderSize = this.spacesApi.getFolderSize(folderId);

      model.put("result", folderSize);
    } catch (AccessDeniedException ade) {
      logger.error("Access denied for folder with id: " + folderId, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(
        "Error getting folder size for folder with id: " + folderId,
        e
      );
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
