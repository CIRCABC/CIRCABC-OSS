package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.HistoryApi;
import io.swagger.model.UserRevocationRequest;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.HistoryJsonParser;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST webscript endpoint that registers the revocation of a user's membership
 * in the history/audit trail.
 *
 * <p>As implied by the {@code Post} suffix in the class name, this endpoint
 * handles an HTTP {@code POST} request. The request body is expected to be a
 * JSON payload describing the membership revocation to record, which is parsed
 * into a {@link io.swagger.model.UserRevocationRequest} via
 * {@link io.swagger.util.parsers.HistoryJsonParser#parseRevocationRequest}.</p>
 *
 * <p>Access is restricted: only an Alfresco administrator or a CIRCABC
 * administrator may register a revocation. Any other caller results in an
 * HTTP {@code 403 Forbidden} response. The recording itself is delegated to
 * {@link io.swagger.api.HistoryApi#registerRevocation}.</p>
 */
public class HistoryMembershipsRevocationPost
  extends CircabcDeclarativeWebScript
{

  /** Logger used to report access-denied and processing errors. */
  static final Log logger = LogFactory.getLog(
    HistoryMembershipsRevocationPost.class
  );

  /**
   * Service used to verify that the current user has the administrative
   * privileges (Alfresco admin or CIRCABC admin) required to register a
   * membership revocation.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /** Business API that persists the membership revocation in the history. */
  @Autowired
  private HistoryApi historyApi;

  /**
   * Handles the POST request: validates that the caller is an administrator,
   * parses the revocation request from the request body and registers it in
   * the history.
   *
   * <p>On an authorization failure the response status is set to
   * {@code 403 Forbidden}; on any other failure it is set to
   * {@code 500 Internal Server Error}. In both error cases {@code null} is
   * returned so that the framework renders the configured status page.</p>
   *
   * @param req    the incoming web script request carrying the JSON revocation
   *               payload
   * @param status the response status to be populated on success or failure
   * @param cache  the cache directives for the response
   * @return an empty model map on success, or {@code null} when an error
   *         status (403 or 500) has been set and a redirect is requested
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

      UserRevocationRequest request = HistoryJsonParser.parseRevocationRequest(
        req
      );
      historyApi.registerRevocation(request);
    } catch (AccessDeniedException ade) {
      logger.error("Access denied for membership revocation registration", ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Error registering membership revocation", e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage(e.getMessage());
      status.setException(e);
      status.setRedirect(true);
      return null; // NOSONAR
    }
    return model;
  }
}
