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
 * Alfresco webscript endpoint handling the HTTP {@code POST} request that associates an external
 * (ARES) repository with a CIRCABC interest group.
 *
 * <p>It is bound to the URL {@code /circabc/groups/{id}/repositories/{repoId}} and requires an
 * authenticated user. The {@code id} path variable identifies the interest group and {@code repoId}
 * identifies the external repository to link. Only a group administrator of the target group is
 * allowed to perform the operation; otherwise the request is rejected with an HTTP
 * {@code 403 Forbidden} response.
 *
 * <p>When authorized, the request is delegated to {@link AresBridgeApi#addExternalRepositories} to
 * register the external repository. Failures during that delegation are reported as an HTTP
 * {@code 400 Bad Request} response.
 */
public class ExternalRepositoryIdPost extends CircabcDeclarativeWebScript {

  /** Bridge API used to register the external (ARES) repository against the interest group. */
  @Autowired
  private AresBridgeApi aresBridgeApi;

  /** Service used to verify that the current user is an administrator of the target group. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the {@code POST} request that links an external repository to an interest group.
   *
   * <p>Reads the {@code id} (interest group identifier) and {@code repoId} (external repository
   * identifier) from the URL template variables. If the current user is not an administrator of the
   * group, the response status is set to {@code 403 Forbidden}. Otherwise the association is
   * delegated to the {@link AresBridgeApi}; any error during that call results in a
   * {@code 400 Bad Request} status.
   *
   * @param req the web script request carrying the {@code id} and {@code repoId} template variables
   * @param status the response status, updated to {@code 403}/{@code 400} on failure
   * @param cache the cache control directives for the response
   * @return an empty model map on success, or {@code null} when the request is rejected (forbidden
   *     or bad request)
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
      if (id != null) {
        this.aresBridgeApi.addExternalRepositories(id, repoId);
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
