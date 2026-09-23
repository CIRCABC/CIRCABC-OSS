package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.NotificationsApi;
import io.swagger.model.PagedNotificationConfigurations;
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
 * Alfresco web script endpoint that handles the HTTP {@code GET} request for
 * retrieving the notification status configurations attached to a given node.
 *
 * <p>The endpoint returns a paged list of notification configurations for the
 * node identified by the {@code id} URL template variable. Access is restricted
 * to callers who hold library administration ({@link LibraryPermissions#LIBADMIN}),
 * newsgroup administration ({@link NewsGroupPermissions#NWSADMIN}) or group
 * administrator rights on the target node; otherwise the request is rejected
 * with an HTTP 403 (Forbidden) response.</p>
 *
 * <p>Key inputs:</p>
 * <ul>
 *   <li>{@code id} (URL template variable) &ndash; the node reference identifier.</li>
 *   <li>{@code page} (query parameter) &ndash; 1-based page number, must be &gt; 0.</li>
 *   <li>{@code limit} (query parameter) &ndash; page size, must be &gt;= 0.</li>
 *   <li>{@code language} (query parameter) &ndash; locale for localized values,
 *       defaults to {@code en-US} when absent.</li>
 *   <li>{@code type}, {@code userName}, {@code status} (query parameters) &ndash;
 *       optional filters applied to the returned configurations.</li>
 * </ul>
 *
 * <p>On success the response model exposes the paged {@code data} collection and
 * the {@code total} number of matching configurations.</p>
 */
public class NodesIdNotificationStatusConfigurationsGet
  extends DeclarativeWebScript
{

  /** Logger for this web script. */
  static final Log logger = LogFactory.getLog(
    NodesIdNotificationStatusConfigurationsGet.class
  );

  /** API used to retrieve the paged notification configurations for a node. */
  @Autowired
  private NotificationsApi notificationsApi;

  /** Service used to verify the current user's permissions on the target node. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the {@code GET} request and builds the response model containing the
   * paged notification configurations of the requested node.
   *
   * <p>Permissions are validated first; the node identifier is taken from the
   * {@code id} URL template variable and pagination/filter values from the request
   * parameters. Multilingual property interception is temporarily disabled while
   * the configurations are fetched and always restored in the {@code finally}
   * block.</p>
   *
   * @param req    the web script request, providing the {@code id} template
   *               variable and the {@code page}, {@code limit}, {@code language},
   *               {@code type}, {@code userName} and {@code status} parameters
   * @param status the web script status used to signal the HTTP result code
   * @param cache  the cache directives for the response
   * @return a model map with the {@code data} and {@code total} entries on
   *         success, or {@code null} when an error status (403, 400 or 500) has
   *         been set and a redirect response is returned
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    boolean mlAware = MLPropertyInterceptor.isMLAware();
    String queryType = req.getParameter("type");
    String queryUserName = req.getParameter("userName");
    String queryStatus = req.getParameter("status");

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
      limit = getLimit(limitStr, limit);

      if (page <= 0) {
        throw new IllegalArgumentException("Values for 'page' must be > 0");
      }

      if (limit < 0) {
        throw new IllegalArgumentException("Values for 'limit' must be >= 0");
      }

      String language = req.getParameter("language");
      if ((language == null) || language.isEmpty()) {
        language = "en-US";
      }

      MLPropertyInterceptor.setMLAware(false);

      int startRecord = (page - 1) * limit;

      PagedNotificationConfigurations notifications =
        this.notificationsApi.getNotifications(
          id,
          startRecord,
          limit,
          language,
          queryType,
          queryUserName,
          queryStatus
        );

      model.put("data", notifications.getData());
      model.put("total", notifications.getTotal());
    } catch (AccessDeniedException ade) {
      logger.error(
        "Access denied when getting notification configurations for node: " +
          id,
        ade
      );
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error(
        "Invalid node reference when getting notification configurations for node: " +
          id,
        inre
      );
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(
        "Unexpected error when getting notification configurations for node: " +
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
   * Parses the {@code limit} request parameter into an integer page size.
   *
   * @param limitStr the raw {@code limit} parameter value, may be {@code null}
   *                 or empty
   * @param limit    the default value returned when {@code limitStr} is absent
   * @return the parsed limit, or the provided default when {@code limitStr} is
   *         {@code null} or empty
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
   * Parses the {@code page} request parameter into a 1-based page number.
   *
   * @param pageStr the raw {@code page} parameter value, may be {@code null} or
   *                empty
   * @param page    the fallback value used in the exception message
   * @return the parsed page number, or {@code 1} when {@code pageStr} is
   *         {@code null} or empty
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
