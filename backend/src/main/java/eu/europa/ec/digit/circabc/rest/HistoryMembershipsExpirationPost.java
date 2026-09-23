package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.HistoryApi;
import io.swagger.model.UserMembershipsExpirationRequest;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.HistoryJsonParser;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco declarative web script that handles the HTTP {@code POST} request
 * used to register the expiration (revocation) of user memberships in the
 * history log.
 *
 * <p>The endpoint reads a JSON payload from the request body describing one or
 * more membership expirations (parsed into a list of
 * {@link io.swagger.model.UserMembershipsExpirationRequest}) and delegates the
 * persistence of these expiration records to {@link io.swagger.api.HistoryApi}.
 *
 * <p>Access is restricted to administrators: the caller must be either an
 * Alfresco administrator or a CIRCABC administrator. Any other caller results
 * in an HTTP {@code 403 Forbidden} response, while unexpected failures produce
 * an HTTP {@code 500 Internal Server Error} response.
 */
public class HistoryMembershipsExpirationPost
  extends CircabcDeclarativeWebScript
{

  /** Logger used to report access-denied and processing errors. */
  static final Log logger = LogFactory.getLog(
    HistoryMembershipsExpirationPost.class
  );

  /**
   * Service used to verify that the current user has administrator
   * privileges (Alfresco admin or CIRCABC admin) before registering an
   * expiration.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /** Business API that persists the membership expiration records. */
  @Autowired
  private HistoryApi historyApi;

  /**
   * Processes the incoming {@code POST} request: verifies the caller's
   * administrator permissions, parses the membership expiration requests from
   * the request body and registers them through {@link HistoryApi}.
   *
   * @param req the web script request; its body is expected to contain the
   *     JSON payload describing the user membership expirations to register
   * @param status the web script response status, used to signal
   *     {@code 403 Forbidden} on access denial or {@code 500 Internal Server
   *     Error} on unexpected failures
   * @param cache the cache control object for the response
   * @return an (empty) model map on success, or {@code null} when an error
   *     occurs and a redirect status has been set
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    try {
      if (
        !(this.currentUserPermissionCheckerService.isAlfrescoAdmin() ||
          this.currentUserPermissionCheckerService.isCircabcAdmin())
      ) {
        throw new AccessDeniedException(
          "Cannot register the revocation of the membership, not enough permissions"
        );
      }

      List<UserMembershipsExpirationRequest> request =
        HistoryJsonParser.parseUserMembershipsExpirationRequests(req);
      historyApi.registerExpiration(request);
    } catch (AccessDeniedException ade) {
      logger.error("Access denied for membership expiration registration", ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Error registering membership expiration", e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage(e.getMessage());
      status.setException(e);
      status.setRedirect(true);
      return null; // NOSONAR
    }
    return model;
  }
}
