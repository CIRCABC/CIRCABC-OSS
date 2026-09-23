package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.AresBridgeApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Read-only ({@code GET}) Alfresco webscript endpoint that retrieves the
 * synchronization/interaction log entries between a CIRCABC interest group and
 * an external repository (ARES bridge).
 *
 * <p>The HTTP method is {@code GET}, as implied by the {@code Get} prefix in
 * the class name. The endpoint expects two path template variables:
 * <ul>
 *   <li>{@code id} &mdash; the identifier of the interest group whose external
 *       repository log is requested;</li>
 *   <li>{@code repoId} &mdash; the identifier of the external repository.</li>
 * </ul>
 *
 * <p>Access is restricted: only a group administrator of the given group
 * {@code id} may retrieve the log. When the caller is not a group admin the
 * endpoint responds with {@code 403 Forbidden}; if the underlying ARES bridge
 * call fails it responds with {@code 400 Bad Request}. On success the resolved
 * log entries are exposed to the response template under the {@code logs}
 * model key.
 */
public class GetExternalRepositoryGroupLog extends DeclarativeWebScript {

  /** Logger used to record errors raised while fetching the group log. */
  static final Log logger = LogFactory.getLog(
    GetExternalRepositoryGroupLog.class
  );

  /** API bridge used to query the external (ARES) repository group log. */
  private AresBridgeApi aresBridgeApi;

  /** Service used to verify that the current user is administrator of the group. */
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the webscript request: validates that the current user is an
   * administrator of the target group and, if so, retrieves the external
   * repository log for the given group and repository.
   *
   * @param req the webscript request; supplies the {@code id} and
   *            {@code repoId} path template variables
   * @param status the response status; set to {@code 403 Forbidden} when the
   *               caller is not a group administrator, or to
   *               {@code 400 Bad Request} when the ARES bridge call fails
   * @param cache the response cache directives
   * @return a model map containing the {@code logs} entry on success, or
   *         {@code null} when access is denied or an error occurs
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
      model.put("logs", this.aresBridgeApi.groupLog(id, repoId));
    } catch (Exception e) {
      logger.error(ERROR_OCCURRED, e);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    }
    return model;
  }

  /**
   * Returns the service used to check whether the current user is a group
   * administrator.
   *
   * @return the current user permission checker service
   */
  public CurrentUserPermissionCheckerService getCurrentUserPermissionCheckerService() {
    return currentUserPermissionCheckerService;
  }

  /**
   * Injects the service used to check whether the current user is a group
   * administrator.
   *
   * @param currentUserPermissionCheckerService the current user permission
   *                                            checker service to set
   */
  public void setCurrentUserPermissionCheckerService(
    CurrentUserPermissionCheckerService currentUserPermissionCheckerService
  ) {
    this.currentUserPermissionCheckerService =
      currentUserPermissionCheckerService;
  }

  /**
   * Returns the ARES bridge API used to retrieve the external repository log.
   *
   * @return the ARES bridge API
   */
  public AresBridgeApi getAresBridgeApi() {
    return aresBridgeApi;
  }

  /**
   * Injects the ARES bridge API used to retrieve the external repository log.
   *
   * @param aresBridgeApi the ARES bridge API to set
   */
  public void setAresBridgeApi(AresBridgeApi aresBridgeApi) {
    this.aresBridgeApi = aresBridgeApi;
  }
}
