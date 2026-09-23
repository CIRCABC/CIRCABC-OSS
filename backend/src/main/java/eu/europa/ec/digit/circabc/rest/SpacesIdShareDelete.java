package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.SpacesApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco declarative webscript that removes a share (sharing relationship) from a space.
 *
 * <p>The class name follows the {@code SpacesIdShareDelete} convention, which maps to the
 * HTTP {@code DELETE} method on a space share resource. The space is identified by the
 * {@code id} URL template variable, and the shared Interest Group to detach is provided via
 * the {@code sharedIGId} request parameter.</p>
 *
 * <p>Behavior:</p>
 * <ul>
 *   <li>Verifies that the current user has Alfresco delete permission on the target space;
 *       otherwise the request is rejected with HTTP {@code 403 Forbidden}.</li>
 *   <li>Temporarily disables the {@link MLPropertyInterceptor} multilingual awareness while
 *       recording pre-delete state and delegating the removal to {@link SpacesApi#deleteShare}.</li>
 *   <li>On success, returns a model containing a {@code message} entry set to {@code "ok"}.</li>
 *   <li>On any other error, responds with HTTP {@code 406 Not Acceptable}.</li>
 * </ul>
 *
 * @author schwerr
 */
public class SpacesIdShareDelete extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(SpacesIdShareDelete.class);

  /** Service exposing space operations, including share removal. */
  @Autowired
  private SpacesApi spacesApi;

  /** Service used to check whether the current user holds the required permissions on a node. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the share deletion request for a given space.
   *
   * <p>Reads the space {@code id} from the URL template variables and the {@code sharedIGId}
   * from the request parameters, checks delete permissions, and removes the corresponding
   * share via {@link SpacesApi#deleteShare(String, String)}. Multilingual property awareness
   * is disabled for the duration of the operation and restored in the {@code finally} block.</p>
   *
   * @param req    the incoming webscript request; supplies the space {@code id} template
   *               variable and the {@code sharedIGId} parameter
   * @param status the response status object, updated to {@code 403} when permissions are
   *               insufficient or {@code 406} when an unexpected error occurs
   * @param cache  the response cache control object
   * @return a model map containing a {@code message} entry set to {@code "ok"} on success, or
   *         {@code null} when the request fails and an error status/redirect has been set
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
        !this.currentUserPermissionCheckerService.hasAlfrescoDeletePermission(
          spaceId
        )
      ) {
        throw new AccessDeniedException(
          "Cannot delete the shared spaces, not enough permissions"
        );
      }

      String sharedIGId = req.getParameter("sharedIGId");

      MLPropertyInterceptor.setMLAware(false);
      this.recordBeforeDelete(spaceId);
      this.spacesApi.deleteShare(spaceId, sharedIGId);

      model.put("message", "ok");
    } catch (AccessDeniedException ade) {
      logger.error(
        "Access denied deleting shared space with id: " + spaceId,
        ade
      );
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Error deleting shared space with id: " + spaceId, e);
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
