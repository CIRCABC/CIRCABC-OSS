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
 * REST webscript endpoint that updates the permission of a share on a given
 * space (folder) in the Alfresco repository.
 *
 * <p>Backing an HTTP {@code PUT} request (as implied by the {@code Put} suffix
 * of the class name), the endpoint changes the permission granted to another
 * Interest Group that a space has been shared with. It resolves the target
 * space from the {@code id} URL template variable and reads the request
 * parameters {@code igId} (the Interest Group the share applies to),
 * {@code permission} (the new permission to assign) and {@code notifyLeaders}
 * (whether the group leaders should be notified of the change).</p>
 *
 * <p>Before performing the change the endpoint verifies that the current user
 * holds the {@link LibraryPermissions#LIBMANAGEOWN} permission on the space;
 * otherwise the request is rejected as forbidden. The actual update is
 * delegated to {@link SpacesApi#changeSharePermission(String, String, String,
 * boolean)}.</p>
 *
 * @author schwerr
 */
public class SpacesIdShareUpdatePermissionPut extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(
    SpacesIdShareUpdatePermissionPut.class
  );

  /**
   * API providing the space-related business operations, including the share
   * permission update performed by this endpoint.
   */
  @Autowired
  private SpacesApi spacesApi;

  /**
   * Service used to check that the current user holds the library permissions
   * required to modify the share permission of the space.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the webscript request by updating the share permission of the
   * targeted space.
   *
   * <p>The space identifier is taken from the {@code id} URL template variable,
   * while the {@code igId}, {@code permission} and {@code notifyLeaders} values
   * are read from the request parameters. The method first ensures the current
   * user has the {@link LibraryPermissions#LIBMANAGEOWN} permission on the
   * space, then temporarily disables the multilingual property interceptor
   * while delegating the update to
   * {@link SpacesApi#changeSharePermission(String, String, String, boolean)}.
   * The original multilingual-aware state is always restored before returning.</p>
   *
   * @param req the webscript request; supplies the {@code id} template variable
   *            and the {@code igId}, {@code permission} and
   *            {@code notifyLeaders} request parameters
   * @param status the response status; set to {@code 403 Forbidden} when the
   *               user lacks the required permission and to
   *               {@code 406 Not Acceptable} for any other failure
   * @param cache the cache control directives for the response
   * @return a model map containing a {@code message} entry set to {@code "ok"}
   *         on success, or {@code null} when the request fails and an error
   *         status is set instead
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
          LibraryPermissions.LIBMANAGEOWN
        )
      ) {
        throw new AccessDeniedException(
          "Cannot change the permission of the share space, not enough permissions"
        );
      }

      MLPropertyInterceptor.setMLAware(false);

      String igId = req.getParameter("igId");

      String permission = req.getParameter("permission");

      boolean notifyLeaders = "true".equals(req.getParameter("notifyLeaders"));

      this.spacesApi.changeSharePermission(
        spaceId,
        igId,
        permission,
        notifyLeaders
      );

      model.put("message", "ok");
    } catch (AccessDeniedException ade) {
      logger.error(
        "Access denied when updating share permission for space: " + spaceId,
        ade
      );
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Error updating share permission for space: " + spaceId, e);
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
