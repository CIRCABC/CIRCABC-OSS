package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.EmailApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco webscript endpoint that sends an email to the leaders of an Interest
 * Group.
 *
 * <p>This endpoint handles an HTTP {@code POST} request (as implied by the
 * {@code Post} suffix in the class name). The target Interest Group is
 * identified by the {@code igId} template variable extracted from the request
 * URL, and the email body is read from the raw request content.
 *
 * <p>Before sending, the caller's access to the Interest Group is verified via
 * {@link CurrentUserPermissionCheckerService#canAccessInterestGroup(String)}.
 * If the caller lacks sufficient permissions the request is rejected with an
 * HTTP {@code 403 Forbidden} status; any other failure results in an HTTP
 * {@code 406 Not Acceptable} status. The actual delivery is delegated to
 * {@link EmailApi#groupsIdLeadersEmailPost(String, String)}.
 *
 * @author beaurpi
 */
public class GroupsLeadersEmailPost extends CircabcDeclarativeWebScript {

  /** API used to perform the email delivery to the Interest Group leaders. */
  @Autowired
  private EmailApi emailApi;

  /**
   * Service used to verify that the current user is allowed to access the
   * targeted Interest Group before an email is sent.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the incoming request to email the leaders of an Interest Group.
   *
   * <p>Reads the {@code igId} template variable from the request URL, checks the
   * current user's access to that Interest Group, then reads the request body
   * and delegates the send operation to {@link EmailApi}. On an access failure
   * the response status is set to {@code 403 Forbidden}; on any other failure it
   * is set to {@code 406 Not Acceptable} and {@code null} is returned.
   *
   * @param req the web script request, providing the {@code igId} template
   *     variable and the email body as request content
   * @param status the response status, updated to reflect success or the type of
   *     failure encountered
   * @param cache the cache directives for the response (unused)
   * @return an empty model map on success, or {@code null} when an error occurs
   *     and an error status has been set
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String igId = templateVars.get("igId");

    try {
      if (
        !this.currentUserPermissionCheckerService.canAccessInterestGroup(igId)
      ) {
        throw new AccessDeniedException(
          "Cannot contact users because user does not have enough permissions"
        );
      }
      String body = req.getContent().getContent();
      this.emailApi.groupsIdLeadersEmailPost(igId, body);
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(ERROR_OCCURRED, e);
      status.setCode(Status.STATUS_NOT_ACCEPTABLE);
      status.setMessage(e.getMessage());
      status.setException(e);
      status.setRedirect(true);
      return null; // NOSONAR
    }

    return model;
  }
}
