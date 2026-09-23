package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.AppMessageApi;
import io.swagger.model.PagedEmails;
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
 * Alfresco Web Script endpoint that handles HTTP GET requests for retrieving the
 * application-wide distribution e-mails in a paged fashion.
 *
 * <p>Access is restricted to administrators: the request is only served when the
 * current user is either an Alfresco administrator or a CIRCABC administrator.
 * Otherwise the endpoint responds with an HTTP 403 (Forbidden) status.</p>
 *
 * <p>Supported request parameters:</p>
 * <ul>
 *   <li>{@code page} - the 1-based page number to return (defaults to {@code 1}
 *       when absent or empty; an invalid value is logged and treated as page 0).</li>
 *   <li>{@code limit} - the maximum number of e-mails per page (defaults to
 *       {@code 25} when absent or empty).</li>
 *   <li>{@code search} - an optional free-text query used to filter the e-mails.</li>
 * </ul>
 *
 * <p>On success the returned model exposes the paged e-mails under {@code emails}
 * and the total count of matching e-mails under {@code total}.</p>
 *
 * @author beaurpi
 */
public class AppDistributionMailsGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(AppDistributionMailsGet.class);

  /**
   * API used to retrieve the paged application distribution e-mails.
   */
  @Autowired
  private AppMessageApi appMessageApi;

  /**
   * Service used to verify that the current user has administrator privileges
   * (Alfresco administrator or CIRCABC administrator) before serving the request.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the GET request: parses the {@code page}, {@code limit} and
   * {@code search} parameters, verifies that the current user is an administrator,
   * and retrieves the corresponding page of distribution e-mails.
   *
   * @param req the web script request providing the {@code page}, {@code limit}
   *            and {@code search} parameters
   * @param status the response status, set to {@link Status#STATUS_FORBIDDEN} when
   *               access is denied or {@link Status#STATUS_INTERNAL_SERVER_ERROR}
   *               on unexpected errors
   * @param cache the cache directives for the response
   * @return a model map containing the paged {@code emails} and their {@code total}
   *         count on success, or {@code null} when access is denied or an
   *         unexpected error occurs
   * @throws IllegalArgumentException if the {@code limit} parameter is not a valid
   *                                  integer
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    String pageStr = req.getParameter("page");
    int page = 0;
    try {
      page = (((pageStr == null) || pageStr.isEmpty())
        ? 1
        : Integer.parseInt(pageStr));
    } catch (NumberFormatException e) {
      if (logger.isErrorEnabled()) {
        logger.error("Wrong numeric value for 'page': " + page, e);
      }
    }

    String limitStr = req.getParameter("limit");
    int limit = 0;
    try {
      limit = (((limitStr == null) || limitStr.isEmpty())
        ? 25
        : Integer.parseInt(limitStr));
    } catch (NumberFormatException e) {
      if (logger.isErrorEnabled()) {
        throw new IllegalArgumentException(
          "Wrong numeric value for 'limit': " + limit,
          e
        );
      }
    }

    String query = req.getParameter("search");

    try {
      if (
        !(currentUserPermissionCheckerService.isAlfrescoAdmin() ||
          currentUserPermissionCheckerService.isCircabcAdmin())
      ) {
        throw new AccessDeniedException("");
      }
      PagedEmails emails = appMessageApi.getAppDistributionEmails(
        page,
        limit,
        query
      );

      model.put("emails", emails);
      model.put("total", emails.getTotal());
    } catch (AccessDeniedException ade) {
      logger.error("Access denied to get distribution emails", ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Unexpected error getting distribution emails", e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      return null; // NOSONAR
    }

    return model;
  }
}
