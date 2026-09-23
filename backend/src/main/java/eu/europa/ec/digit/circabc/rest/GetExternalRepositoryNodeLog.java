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
 * Alfresco webscript endpoint that retrieves the activity log of a node stored
 * in an external repository (ARES bridge integration).
 *
 * <p>The class name is prefixed with {@code Get}, which implies this endpoint is
 * bound to an HTTP {@code GET} request. It reads the target node identifier
 * ({@code id}) and the external repository identifier ({@code repoId}) from the
 * request URL template variables, verifies that the current user holds Alfresco
 * read permission on the node, and delegates to {@link AresBridgeApi#nodeLog}
 * to fetch the log entries.
 *
 * <p>Response behavior:
 * <ul>
 *   <li>On success the returned model exposes the log entries under the
 *       {@code "logs"} key, rendered by the associated FreeMarker template.</li>
 *   <li>If the current user lacks read permission, the response is set to
 *       HTTP {@code 403 Forbidden}.</li>
 *   <li>If the underlying API call fails, the response is set to
 *       HTTP {@code 400 Bad Request}.</li>
 * </ul>
 */
public class GetExternalRepositoryNodeLog extends DeclarativeWebScript {

  /** Logger used to record errors raised while resolving the external node log. */
  static final Log logger = LogFactory.getLog(
    GetExternalRepositoryNodeLog.class
  );

  /** API bridge used to query the external (ARES) repository for node logs. */
  private AresBridgeApi aresBridgeApi;

  /** Service used to verify the current user's Alfresco permissions on a node. */
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the webscript request: resolves the external repository node log for
   * the node identified by the URL template variables.
   *
   * <p>The {@code id} (Alfresco node identifier) and {@code repoId} (external
   * repository identifier) are read from the request's service match template
   * variables. Access is only granted when the current user has Alfresco read
   * permission on the node.
   *
   * @param req the webscript request, providing the {@code id} and
   *     {@code repoId} template variables
   * @param status the response status, updated to {@code 403} when access is
   *     denied or {@code 400} when the log retrieval fails
   * @param cache the cache directives for the response
   * @return a model map containing the log entries under the {@code "logs"} key
   *     on success, or {@code null} when access is denied or an error occurs
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

    if (!currentUserPermissionCheckerService.hasAlfrescoReadPermission(id)) {
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    }

    try {
      model.put("logs", this.aresBridgeApi.nodeLog(id, repoId));
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
   * Returns the service used to check the current user's Alfresco permissions.
   *
   * @return the current-user permission checker service
   */
  public CurrentUserPermissionCheckerService getCurrentUserPermissionCheckerService() {
    return currentUserPermissionCheckerService;
  }

  /**
   * Sets the service used to check the current user's Alfresco permissions.
   *
   * @param currentUserPermissionCheckerService the permission checker service to inject
   */
  public void setCurrentUserPermissionCheckerService(
    CurrentUserPermissionCheckerService currentUserPermissionCheckerService
  ) {
    this.currentUserPermissionCheckerService =
      currentUserPermissionCheckerService;
  }

  /**
   * Returns the ARES bridge API used to query external repository node logs.
   *
   * @return the ARES bridge API
   */
  public AresBridgeApi getAresBridgeApi() {
    return aresBridgeApi;
  }

  /**
   * Sets the ARES bridge API used to query external repository node logs.
   *
   * @param aresBridgeApi the ARES bridge API to inject
   */
  public void setAresBridgeApi(AresBridgeApi aresBridgeApi) {
    this.aresBridgeApi = aresBridgeApi;
  }
}
