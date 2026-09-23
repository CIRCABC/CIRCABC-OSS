package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.SpacesApi;
import io.swagger.model.PagedShares;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco Web Script endpoint that handles the HTTP {@code GET} request for
 * the shares of a given space (the {@code Get} suffix in the class name maps to
 * the HTTP GET method).
 *
 * <p>It returns the paginated list of interest groups a space has been shared
 * with. The target space is identified by the {@code id} URL template variable,
 * and pagination is controlled through the optional {@code page} and
 * {@code limit} request parameters. The caller must hold the
 * {@link LibraryPermissions#LIBMANAGEOWN} permission on the space; otherwise the
 * response is rejected with an HTTP 403 (Forbidden) status.</p>
 *
 * <p>The resulting model exposes:</p>
 * <ul>
 *   <li>{@code shares} &ndash; the list of shares for the requested page;</li>
 *   <li>{@code total} &ndash; the total number of shares available.</li>
 * </ul>
 *
 * @author schwerr
 */
public class SpacesIdSharesGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(SpacesIdSharesGet.class);

  /** API used to look up the interest groups a space is shared with. */
  @Autowired
  private SpacesApi spacesApi;

  /** Service used to verify the current user's permissions on the space. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Executes the endpoint logic: validates the caller's permissions, parses the
   * pagination parameters and retrieves the paginated list of shares for the
   * requested space.
   *
   * <p>The {@code id} template variable identifies the space. The optional
   * {@code page} (1-based, must be {@code > 0}) and {@code limit} (must be
   * {@code >= 0}) request parameters control pagination. Multilingual property
   * interception is temporarily disabled while the shares are fetched and
   * restored afterwards.</p>
   *
   * @param req the web script request, providing the {@code id} template
   *     variable and the {@code page}/{@code limit} parameters
   * @param status the response status, set to {@code 403} when access is denied
   *     or {@code 406} when an unexpected error occurs
   * @param cache the cache directives for the response (unused)
   * @return a model map containing the {@code shares} for the requested page and
   *     the {@code total} count, or {@code null} when an error status has been
   *     set on the response
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String spaceId = templateVars.get("id");
    boolean mlAware = MLPropertyInterceptor.isMLAware();

    try {
      if (
        !this.currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
          spaceId,
          LibraryPermissions.LIBMANAGEOWN
        )
      ) {
        throw new AccessDeniedException(
          "Cannot post the share space, not enough permissions"
        );
      }

      String pageStr = req.getParameter("page");
      int page = 0;
      page = getPage(pageStr, page);

      String limitStr = req.getParameter("limit");
      int limit = 0;
      limit = getLimit(limitStr, limit);

      if (page <= 0) {
        throw new IllegalArgumentException("Values for 'page' must be > 0");
      }

      if (limit < 0) {
        throw new IllegalArgumentException("Values for 'limit' must be >= 0");
      }

      MLPropertyInterceptor.setMLAware(false);

      int startRecord = (page - 1) * limit;

      PagedShares pagedItems = this.spacesApi.getInvitedInterestGroups(
        spaceId,
        startRecord,
        limit
      );

      model.put("shares", pagedItems.getData());
      model.put("total", pagedItems.getTotal());
    } catch (AccessDeniedException ade) {
      logger.error(
        "Access denied when retrieving shares for space: " + spaceId,
        ade
      );
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Error retrieving shares for space: " + spaceId, e);
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

  /**
   * Parses the {@code limit} request parameter.
   *
   * @param limitStr the raw {@code limit} parameter value, may be {@code null}
   *     or empty
   * @param limit the default limit to return when {@code limitStr} is
   *     {@code null} or empty
   * @return the parsed limit, or the supplied default when no value was provided
   * @throws IllegalArgumentException if {@code limitStr} is not a valid integer
   */
  private int getLimit(String limitStr, int limit) {
    if ((limitStr != null) && !limitStr.isEmpty()) {
      try {
        limit = Integer.parseInt(limitStr);
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException(
          "Wrong numeric value for 'limit': " + limit,
          e
        );
      }
    }
    return limit;
  }

  /**
   * Parses the {@code page} request parameter, defaulting to the first page.
   *
   * @param pageStr the raw {@code page} parameter value, may be {@code null} or
   *     empty
   * @param page the current page value used in the error message when parsing
   *     fails
   * @return the parsed page number, or {@code 1} when no value was provided
   * @throws IllegalArgumentException if {@code pageStr} is not a valid integer
   */
  private int getPage(String pageStr, int page) {
    try {
      page = (((pageStr == null) || pageStr.isEmpty())
        ? 1
        : Integer.parseInt(pageStr));
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
        "Wrong numeric value for 'page': " + page,
        e
      );
    }
    return page;
  }
}
