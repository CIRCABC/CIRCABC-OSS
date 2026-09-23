package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.UsersApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco webscript endpoint that handles the bulk invitation of users into an
 * Interest Group (IG).
 *
 * <p>As implied by the {@code Post} suffix in the class name, this endpoint is bound
 * to the HTTP {@code POST} method. It reads a bulk-invite payload from the request
 * body and delegates the actual invitation logic to {@link UsersApi#bulkInviteUsers}.
 *
 * <p>Key inputs:
 * <ul>
 *   <li>{@code igId} (query parameter) &ndash; the identifier of the target Interest
 *       Group node the users are invited to; must not be {@code null} or blank.</li>
 *   <li>{@code createNewProfiles} (query parameter) &ndash; whether new profiles
 *       should be created for invitees that do not yet exist ({@code "true"} to
 *       enable).</li>
 *   <li>{@code notifyUsers} (query parameter) &ndash; whether invited users should be
 *       notified ({@code "true"} to enable).</li>
 *   <li>request body &ndash; the bulk invite data describing the users to invite.</li>
 * </ul>
 *
 * <p>The caller must hold Alfresco read permission on the {@code igId} node; otherwise
 * an {@link AccessDeniedException} is raised and the response is set to
 * {@code 403 Forbidden}.
 *
 * @see CircabcDeclarativeWebScript
 * @see UsersApi#bulkInviteUsers
 */
public class BulkInviteUsersPost extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(BulkInviteUsersPost.class);

  /**
   * API providing user-related business operations, including the bulk invite logic
   * this endpoint delegates to.
   */
  @Autowired
  private UsersApi usersApi;

  /**
   * Service used to verify that the current user holds the required Alfresco
   * permissions on the target Interest Group node before performing the invitation.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Processes the bulk-invite request.
   *
   * <p>Validates the {@code igId} parameter, checks that the current user has read
   * permission on the corresponding node, reads the bulk-invite payload from the
   * request body and delegates to {@link UsersApi#bulkInviteUsers}. ML (multilingual)
   * property awareness is disabled for the duration of the call and restored
   * afterwards.
   *
   * <p>On success the returned model contains {@code result = 1}. On failure the HTTP
   * status is set accordingly and {@code null} is returned:
   * <ul>
   *   <li>{@code 400 Bad Request} for an {@link InvalidNodeRefException};</li>
   *   <li>{@code 403 Forbidden} for an {@link AccessDeniedException};</li>
   *   <li>{@code 406 Not Acceptable} for any other error.</li>
   * </ul>
   *
   * @param req the web script request, providing the {@code igId},
   *            {@code createNewProfiles} and {@code notifyUsers} parameters and the
   *            bulk-invite request body
   * @param status the response status to be populated on error
   * @param cache the cache directives for the response
   * @return a model map containing {@code result = 1} on success, or {@code null} if an
   *         error occurred and the status has been set accordingly
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    boolean mlAware = MLPropertyInterceptor.isMLAware();

    MLPropertyInterceptor.setMLAware(false);

    try {
      String igId = req.getParameter("igId");

      if ((igId == null) || igId.trim().isEmpty()) {
        throw new IllegalArgumentException("'igId' cannot be empty.");
      }

      if (
        !this.currentUserPermissionCheckerService.hasAlfrescoReadPermission(
          igId
        )
      ) {
        throw new AccessDeniedException("No access on node: " + igId);
      }

      boolean createNewProfiles = req
        .getParameter("createNewProfiles")
        .equals("true");
      boolean notifyUsers = req.getParameter("notifyUsers").equals("true");

      String bulkInviteDataBody = req.getContent().getContent();

      this.usersApi.bulkInviteUsers(
        bulkInviteDataBody,
        igId,
        createNewProfiles,
        notifyUsers
      );

      model.put("result", 1);
    } catch (InvalidNodeRefException inre) {
      logger.error("Invalid node reference when bulk inviting users", inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (AccessDeniedException ade) {
      logger.error("Access denied when bulk inviting users", ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Unexpected error occurred while bulk inviting users", e);
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
