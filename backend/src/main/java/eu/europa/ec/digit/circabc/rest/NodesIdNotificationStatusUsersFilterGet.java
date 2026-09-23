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
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco declarative web script backing the HTTP {@code GET} endpoint that returns the
 * paginated list of users notifiable for a given node, filtered by optional user attributes.
 *
 * <p>The concrete node is identified by the {@code id} URL template variable. Callers may
 * further narrow the result set through the optional query parameters {@code userName},
 * {@code firstName}, {@code lastName} and {@code email}. Pagination is controlled through the
 * {@code page} (1-based, must be {@code > 0}) and {@code limit} (page size, must be {@code >= 0})
 * query parameters.
 *
 * <p>Access is restricted to callers who are library administrators
 * ({@link LibraryPermissions#LIBADMIN}), newsgroup administrators
 * ({@link NewsGroupPermissions#NWSADMIN}) or group administrators of the target node; otherwise an
 * {@link AccessDeniedException} is raised and the response is set to
 * {@link Status#STATUS_FORBIDDEN}. Multilingual property interception is disabled while the users
 * are resolved and restored afterwards.
 *
 * <p>On success the returned model contains {@code data} (the current page of subscribed users) and
 * {@code total} (the overall number of matching users), which are rendered by the associated
 * FreeMarker template.
 */
public class NodesIdNotificationStatusUsersFilterGet
  extends DeclarativeWebScript
{

  /** Logger for reporting access, validation and unexpected errors during endpoint execution. */
  static final Log logger = LogFactory.getLog(
    NodesIdNotificationStatusUsersFilterGet.class
  );

  /** API used to resolve the notifiable users for the target node. */
  private NotificationsApi notificationsApi;
  /** Service used to verify that the current user holds the permissions required to read the notification status. */
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the {@code GET} request: validates the caller's permissions and the pagination
   * parameters, then retrieves the filtered, paginated list of notifiable users for the node
   * identified by the {@code id} URL template variable.
   *
   * @param req the web script request; supplies the {@code id} template variable and the optional
   *     {@code userName}, {@code firstName}, {@code lastName}, {@code email}, {@code page} and
   *     {@code limit} parameters
   * @param status the response status, updated with an error code and redirect flag when the
   *     request cannot be fulfilled
   * @param cache the response cache directives
   * @return a model map containing {@code data} (the current page of users) and {@code total} (the
   *     total number of matching users), or {@code null} when an error status has been set
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
        "Access denied when getting filtered notification users for node: " +
          id,
        ade
      );
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error(
        "Invalid node reference when getting filtered notification users for node: " +
          id,
        inre
      );
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(
        "Unexpected error when getting filtered notification users for node: " +
          id,
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
   * Parses the {@code limit} query parameter into an integer page size.
   *
   * @param limitStr the raw {@code limit} parameter value
   * @param limit the fallback value returned if parsing is not performed
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
   * Parses the {@code page} query parameter into a 1-based page number.
   *
   * @param pageStr the raw {@code page} parameter value; when {@code null} or empty the page
   *     defaults to {@code 1}
   * @param page the fallback value returned if parsing is not performed
   * @return the parsed page number
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

  /**
   * @param notificationsApi the notificationsApi to set
   */
  public void setNotificationsApi(NotificationsApi notificationsApi) {
    this.notificationsApi = notificationsApi;
  }

  /**
   * Injects the service used to verify the current user's permissions on the target node.
   *
   * @param currentUserPermissionCheckerService the permission checker service to set
   */
  public void setCurrentUserPermissionCheckerService(
    CurrentUserPermissionCheckerService currentUserPermissionCheckerService
  ) {
    this.currentUserPermissionCheckerService =
      currentUserPermissionCheckerService;
  }
}
