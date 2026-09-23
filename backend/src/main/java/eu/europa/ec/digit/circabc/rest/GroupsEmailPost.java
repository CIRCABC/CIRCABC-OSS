package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.EmailApi;
import io.swagger.model.EmailDefinition;
import io.swagger.model.permissions.DirectoryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.EmailJsonParser;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco declarative webscript handling the HTTP {@code POST} request that sends an email to the
 * members of an Interest Group.
 *
 * <p>The Interest Group is identified by the {@code igId} template variable extracted from the
 * request URL, and the email content is provided as a JSON body parsed into an {@link
 * EmailDefinition}. Before delivery, the endpoint verifies that the current user holds directory
 * access permission ({@link DirectoryPermissions#DIRACCESS}) on the target group.
 *
 * <p>Possible outcomes:
 *
 * <ul>
 *   <li>Success: the email is dispatched via {@link EmailApi#groupsIdEmailPost(String,
 *       EmailDefinition)} and an empty model is returned.
 *   <li>Insufficient permissions: responds with HTTP {@code 403 Forbidden}.
 *   <li>Any other failure: responds with HTTP {@code 406 Not Acceptable}.
 * </ul>
 *
 * @author beaurpi
 */
public class GroupsEmailPost extends CircabcDeclarativeWebScript {

  /** API used to send the email to the Interest Group members. */
  @Autowired
  private EmailApi emailApi;

  /** Service used to verify that the current user has the required directory permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Sends an email to the members of the Interest Group identified in the request URL.
   *
   * <p>Reads the {@code igId} template variable, checks that the current user has {@link
   * DirectoryPermissions#DIRACCESS} permission on that group, parses the request body into an
   * {@link EmailDefinition} and delegates the delivery to {@link EmailApi}. Errors are translated
   * into the appropriate HTTP status codes on the response ({@code 403} for access denials, {@code
   * 406} for any other failure).
   *
   * @param req the webscript request; must expose the {@code igId} template variable and carry the
   *     JSON email definition as its body
   * @param status the webscript response status, updated with the relevant HTTP status code and
   *     message when an error occurs
   * @param cache the webscript response cache directives
   * @return an empty model map on success, or {@code null} when an error occurs and the response
   *     has been redirected with an error status
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
        !this.currentUserPermissionCheckerService.hasAnyOfDirectoryPermission(
          igId,
          DirectoryPermissions.DIRACCESS
        )
      ) {
        throw new AccessDeniedException(
          "Cannot contact users because user does not have enough permissions"
        );
      }
      EmailDefinition body = EmailJsonParser.parse(req);
      this.emailApi.groupsIdEmailPost(igId, body);
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
