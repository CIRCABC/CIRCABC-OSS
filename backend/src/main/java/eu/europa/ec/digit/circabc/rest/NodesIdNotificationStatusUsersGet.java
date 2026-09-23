package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.NotificationsApi;
import io.swagger.model.PagedNotificationSubscribedUsers;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.model.permissions.NewsGroupPermissions;
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
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco Web Script endpoint that handles HTTP {@code GET} requests to
 * retrieve the paginated list of users who are subscribed to (notifiable for)
 * notifications on a given node.
 *
 * <p>The node is identified by the {@code id} URL template variable. Access is
 * restricted: the current user must have library administration
 * ({@link LibraryPermissions#LIBADMIN}), newsgroup administration
 * ({@link NewsGroupPermissions#NWSADMIN}) or group administration rights on the
 * node, otherwise the request is rejected with an HTTP 403 (Forbidden)
 * response.
 *
 * <p>The result set can be filtered and paginated through the following request
 * parameters:
 * <ul>
 *   <li>{@code userName}, {@code firstName}, {@code lastName}, {@code email} —
 *       optional case filters applied to the subscribed users;</li>
 *   <li>{@code page} — 1-based page index (must be &gt; 0);</li>
 *   <li>{@code limit} — maximum number of records per page (must be &gt;= 0,
 *       defaults to 0 when omitted).</li>
 * </ul>
 *
 * <p>The response model exposes the page of subscribed users under
 * {@code data} and the overall count under {@code total}.
 */
public class NodesIdNotificationStatusUsersGet extends DeclarativeWebScript {

  /** Logger used to report access, validation and unexpected errors. */
  static final Log logger = LogFactory.getLog(
    NodesIdNotificationStatusUsersGet.class
  );

  /** API used to look up the users subscribed to a node's notifications. */
  @Autowired
  private NotificationsApi notificationsApi;

  /** Service used to verify the current user's permissions on the node. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Executes the endpoint: validates the caller's permissions on the target
   * node, parses the pagination and filter parameters, and retrieves the
   * matching page of notifiable users.
   *
   * <p>On failure the method sets the appropriate HTTP status on the response
   * (403 for access denied, 400 for an invalid node reference or bad
   * parameters, 500 for any other unexpected error) and returns {@code null}.
   * The {@code ML aware} flag is always restored before returning.
   *
   * @param req the incoming web script request; supplies the {@code id}
   *            template variable and the optional {@code page}, {@code limit},
   *            {@code userName}, {@code firstName}, {@code lastName} and
   *            {@code email} parameters
   * @param status the response status, updated with an error code when the
   *               request cannot be fulfilled
   * @param cache the response cache directives
   * @return a model map containing the {@code data} (page of subscribed users)
   *         and {@code total} entries, or {@code null} if an error occurred
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    boolean mlAware = MLPropertyInterceptor.isMLAware();
    String userName = req.getParameter("userName");
    String firstName = req.getParameter("firstName");
    String lastName = req.getParameter("lastName");
    String email = req.getParameter("email");

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("id");

    try {
      if (
        !(this.currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
            id,
            LibraryPermissions.LIBADMIN
          ) ||
          this.currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
            id,
            NewsGroupPermissions.NWSADMIN
          ) ||
          this.currentUserPermissionCheckerService.isGroupAdmin(id))
      ) {
        throw new AccessDeniedException(
          "Impossible to remove notification configuration, not enough permission"
        );
      }

      MLPropertyInterceptor.setMLAware(false);

      String pageStr = req.getParameter("page");
      int page = 0;
      page = getPage(pageStr, page);

      String limitStr = req.getParameter("limit");
      int limit = 0;
      if ((limitStr != null) && !limitStr.isEmpty()) {
        limit = getLimit(limitStr, limit);
      }

      if (page <= 0) {
        throw new IllegalArgumentException("Values for 'page' must be > 0");
      }

      if (limit < 0) {
        throw new IllegalArgumentException("Values for 'limit' must be >= 0");
      }

      MLPropertyInterceptor.setMLAware(false);

      int startRecord = (page - 1) * limit;

      PagedNotificationSubscribedUsers users =
        this.notificationsApi.getNotifiableUsers(
          id,
          startRecord,
          limit,
          userName,
          firstName,
          lastName,
          email
        );

      model.put("data", users.getData());
      model.put("total", users.getTotal());
    } catch (AccessDeniedException ade) {
      logger.error(
        "Access denied when getting notification users for node: " + id,
        ade
      );
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error(
        "Invalid node reference when getting notification users for node: " +
          id,
        inre
      );
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(
        "Unexpected error when getting notification users for node: " + id,
        e
      );
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }

  /**
   * Parses the {@code limit} request parameter into an integer.
   *
   * @param limitStr the raw parameter value to parse
   * @param limit the fallback value returned only if parsing is not performed
   * @return the parsed limit value
   * @throws IllegalArgumentException if {@code limitStr} is not a valid integer
   */
  private int getLimit(String limitStr, int limit) {
    try {
      limit = Integer.parseInt(limitStr);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
        "Wrong numeric value for 'limit': " + limit,
        e
      );
    }
    return limit;
  }

  /**
   * Parses the {@code page} request parameter into an integer, defaulting to
   * {@code 1} when the value is {@code null} or empty.
   *
   * @param pageStr the raw parameter value to parse
   * @param page the fallback value used in the error message
   * @return the parsed page value, or {@code 1} when no value was supplied
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
