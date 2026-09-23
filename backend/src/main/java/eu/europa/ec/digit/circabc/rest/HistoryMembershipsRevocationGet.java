package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.HistoryApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco Declarative Web Script backing the HTTP {@code GET} endpoint that
 * retrieves the history of membership revocations.
 *
 * <p>Access is restricted to administrators: the caller must be either an
 * Alfresco administrator or a CIRCABC administrator, otherwise the endpoint
 * responds with HTTP {@code 403 Forbidden}.</p>
 *
 * <p>The results are paginated. Two optional query parameters control the
 * pagination:</p>
 * <ul>
 *   <li>{@code limit} &ndash; the maximum number of revocation entries to
 *   return (defaults to {@code 25} when absent);</li>
 *   <li>{@code page} &ndash; the zero-based page index to return
 *   (defaults to {@code 0} when absent).</li>
 * </ul>
 *
 * <p>On success the model exposes the retrieved revocations under the
 * {@code revocations} key for rendering by the associated FreeMarker
 * template.</p>
 */
public class HistoryMembershipsRevocationGet extends DeclarativeWebScript {

  /** Name of the request parameter controlling the page size. */
  private static final String LIMIT = "limit";

  /** Logger used to report access-denied and unexpected errors. */
  static final Log logger = LogFactory.getLog(
    HistoryMembershipsRevocationGet.class
  );

  /** Service used to verify that the current user has administrator rights. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /** API providing access to the membership revocation history. */
  @Autowired
  private HistoryApi historyApi;

  /**
   * Handles the {@code GET} request, retrieving a paginated list of membership
   * revocations for administrators.
   *
   * <p>Reads the optional {@code limit} and {@code page} query parameters,
   * defaulting to {@code 25} and {@code 0} respectively, and populates the
   * response model with the {@code revocations} entry. If the current user is
   * neither an Alfresco nor a CIRCABC administrator, the response status is set
   * to {@code 403 Forbidden}; any other failure results in a
   * {@code 500 Internal Server Error}.</p>
   *
   * @param req the web script request, providing the {@code limit} and
   *            {@code page} query parameters
   * @param status the web script response status, updated to reflect access
   *               denial or internal errors
   * @param cache the cache directives for the response
   * @return a model map containing the {@code revocations} entry on success,
   *         or {@code null} when an error status has been set
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

      String limitStr = req.getParameter(LIMIT);
      int limit = 25;
      if (limitStr != null) {
        limit = Integer.parseInt(limitStr);
      }

      String pageStr = req.getParameter("page");
      int page = 0;
      if (pageStr != null) {
        page = Integer.parseInt(pageStr);
      }

      model.put("revocations", historyApi.getRevocations(limit, page));
    } catch (AccessDeniedException ade) {
      logger.error(
        "Access denied for getting revocations. limit: " +
          (req.getParameter(LIMIT) != null ? req.getParameter(LIMIT) : "25") +
          ", page: " +
          (req.getParameter("page") != null ? req.getParameter("page") : "0"),
        ade
      );
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(
        "Error getting revocations. limit: " +
          (req.getParameter(LIMIT) != null ? req.getParameter(LIMIT) : "25") +
          ", page: " +
          (req.getParameter("page") != null ? req.getParameter("page") : "0"),
        e
      );
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage(e.getMessage());
      status.setException(e);
      status.setRedirect(true);
      return null; // NOSONAR
    }
    return model;
  }
}
