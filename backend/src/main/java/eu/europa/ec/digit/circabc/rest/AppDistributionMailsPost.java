package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.AppMessageApi;
import io.swagger.model.db.DistributionEmailDAO;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.EmailJsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST webscript endpoint that handles the HTTP {@code POST} request used to register one or more
 * application distribution email addresses.
 *
 * <p>The JSON request body is expected to contain a collection of distribution email entries, which
 * are parsed via {@link EmailJsonParser#parseDistributionEmails(WebScriptRequest)} into a list of
 * {@link DistributionEmailDAO} objects. Authorization is enforced as follows:
 *
 * <ul>
 *   <li>If the request contains more than one email entry, the current user must be a CIRCABC
 *       administrator.
 *   <li>If the request contains exactly one email entry, the current user must either own that
 *       email address or be a CIRCABC administrator.
 *   <li>If the request contains no entries, the request is rejected as a bad request.
 * </ul>
 *
 * <p>On success the parsed emails are persisted through {@link AppMessageApi}. Failure cases are
 * translated into the appropriate HTTP status codes (403 for access denial, 400 for
 * malformed/unreadable/empty requests and 500 for unexpected errors).
 *
 * @author beaurpi
 */
public class AppDistributionMailsPost extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(AppDistributionMailsPost.class);

  /** API used to persist the application distribution email addresses. */
  @Autowired
  private AppMessageApi appMessageApi;

  /** Service used to check the current user's permissions and email ownership. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Parses the distribution emails from the request body, enforces the required authorization rules
   * and persists the emails.
   *
   * @param req the web script request whose body contains the distribution emails as JSON
   * @param status the response status, updated with an error code and message when the request is
   *     invalid, unauthorized or fails
   * @param cache the response cache directives
   * @return an empty model map on success, or {@code null} when an error status has been set on the
   *     response
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    try {
      List<DistributionEmailDAO> list = EmailJsonParser.parseDistributionEmails(
        req
      );

      if (list.size() > 1) {
        if (!currentUserPermissionCheckerService.isCircabcAdmin()) {
          throw new AccessDeniedException("Not enough permissions");
        }
      } else if (list.size() == 1) {
        if (
          !currentUserPermissionCheckerService.isCurrentUserEmailEqualTo(
            list.get(0).getEmailAddress()
          ) &&
          !currentUserPermissionCheckerService.isCircabcAdmin()
        ) {
          throw new AccessDeniedException("Not enough permissions");
        }
      } else {
        status.setCode(Status.STATUS_BAD_REQUEST);
        status.setMessage("Error in request, request is empty");
        status.setRedirect(true);
        return null; // NOSONAR
      }

      appMessageApi.addAppDistributionPostEmails(list);
    } catch (AccessDeniedException ade) {
      logger.error("Access denied to add distribution emails", ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (IOException e) {
      logger.error("Error reading request body", e);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Error in request, impossible to read request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (ParseException e) {
      logger.error("Error parsing JSON request", e);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Error in request, impossible to parse object request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Unexpected error adding distribution emails", e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      return null; // NOSONAR
    }

    return model;
  }
}
