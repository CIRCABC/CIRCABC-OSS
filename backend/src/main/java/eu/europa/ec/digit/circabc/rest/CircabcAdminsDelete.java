package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.CircabcApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST webscript endpoint that removes a user from the set of CIRCABC
 * administrators.
 *
 * <p>Mapped to an HTTP {@code DELETE} request (as implied by the {@code Delete}
 * suffix of the class name). The target user is identified by the
 * {@code userId} URL template variable and the removal is delegated to
 * {@link io.swagger.api.CircabcApi#circabcAdminsUserIdDelete(String)}.
 *
 * <p>Access is restricted: only a CIRCABC administrator or an Alfresco
 * administrator is allowed to perform the operation. Any other caller receives
 * an HTTP {@code 403 Forbidden} response.
 */
public class CircabcAdminsDelete extends CircabcDeclarativeWebScript {

  /** Logger used to report failures occurring while processing the request. */
  static final Log logger = LogFactory.getLog(CircabcAdminsDelete.class);

  /** API facade providing the CIRCABC administrator management operations. */
  @Autowired
  private CircabcApi circabcApi;

  /** Service used to check whether the current user has the required admin rights. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the incoming request to revoke CIRCABC administrator rights from a
   * user.
   *
   * <p>The current user's permissions are validated first; if they are neither a
   * CIRCABC nor an Alfresco administrator the response status is set to
   * {@link Status#STATUS_FORBIDDEN} and {@code null} is returned. Otherwise the
   * {@code userId} template variable is read from the request and the removal is
   * carried out through the {@link CircabcApi}. On success the status is set to
   * {@link Status#STATUS_OK} and the model contains {@code result=ok}; any
   * exception is logged and results in an
   * {@link Status#STATUS_INTERNAL_SERVER_ERROR} status.
   *
   * @param req the web script request, providing the {@code userId} URL template
   *     variable
   * @param status the response status to be populated by this method
   * @param cache the cache directives for the response
   * @return the model map used to render the response, or {@code null} when the
   *     caller is not authorized
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    try {
      boolean isAllowed =
        currentUserPermissionCheckerService.isCircabcAdmin() ||
        currentUserPermissionCheckerService.isAlfrescoAdmin();
      if (!isAllowed) {
        status.setCode(Status.STATUS_FORBIDDEN);
        status.setRedirect(true);
        return null; // NOSONAR
      }
      Map<String, String> templateVars = req
        .getServiceMatch()
        .getTemplateVars();
      String userId = templateVars.get("userId");
      circabcApi.circabcAdminsUserIdDelete(userId);
      status.setCode(Status.STATUS_OK);
      model.put("result", "ok");
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
    }
    return model;
  }
}
