package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.AresBridgeApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco Declarative Web Script that handles the HTTP {@code DELETE} operation for removing an
 * external (ARES Bridge) repository association from an interest group.
 *
 * <p>The endpoint expects two URL template variables:
 *
 * <ul>
 *   <li>{@code id} &ndash; the identifier of the interest group whose external repository is being
 *       removed;
 *   <li>{@code repoId} &ndash; the identifier of the external repository to delete.
 * </ul>
 *
 * <p>Access is restricted to group administrators of the given interest group. If the current user
 * is not a group administrator, the endpoint responds with HTTP {@code 403 Forbidden}. When both
 * template variables are present, the deletion is delegated to {@link AresBridgeApi}. Any failure
 * during the deletion is logged and reported as HTTP {@code 400 Bad Request}.
 */
public class ExternalRepositoryIdDelete extends CircabcDeclarativeWebScript {

  /** API used to delete the external repository association via the ARES Bridge integration. */
  @Autowired
  private AresBridgeApi aresBridgeApi;

  /** Service used to verify that the current user has group administrator rights. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Deletes the external repository identified by the {@code repoId} template variable from the
   * interest group identified by the {@code id} template variable.
   *
   * <p>The current user must be a group administrator of the interest group; otherwise the response
   * status is set to {@link Status#STATUS_FORBIDDEN} and {@code null} is returned. If the deletion
   * fails, the error is logged and the response status is set to {@link Status#STATUS_BAD_REQUEST}.
   *
   * @param req the web script request, providing the {@code id} and {@code repoId} template
   *     variables
   * @param status the response status, updated to reflect forbidden access or a bad request on
   *     failure
   * @param cache the cache directive for the response
   * @return an empty model map on success, or {@code null} when access is denied or the deletion
   *     fails
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("id");
    String repoId = templateVars.get("repoId");

    if (!currentUserPermissionCheckerService.isGroupAdmin(id)) {
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    }

    try {
      if (id != null && repoId != null) {
        this.aresBridgeApi.deleteExternalRepository(id, repoId);
      }
    } catch (Exception e) {
      logger.error(ERROR_OCCURRED, e);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    }

    return model;
  }
}
