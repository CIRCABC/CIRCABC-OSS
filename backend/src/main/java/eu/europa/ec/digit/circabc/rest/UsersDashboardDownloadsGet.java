package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.DashboardApi;
import io.swagger.model.UserActionLog;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco webscript endpoint that handles HTTP {@code GET} requests for a
 * user's "downloads" dashboard.
 *
 * <p>The endpoint returns the list of download-related action logs
 * ({@link io.swagger.model.UserActionLog}) recorded for the user identified by
 * the {@code userId} path variable. It is exposed as a read-only webscript and
 * delegates the actual retrieval to {@link io.swagger.api.DashboardApi}.
 *
 * <p>Key inputs:
 * <ul>
 *   <li>{@code userId} &ndash; template (path) variable identifying the target
 *       user. Access is only granted when this matches the currently
 *       authenticated user; otherwise the request is rejected with
 *       {@code 403 Forbidden}.</li>
 *   <li>{@code language} &ndash; optional request parameter used to set the
 *       content and UI locale. When absent, multilingual (ML) awareness is
 *       enabled so that raw multilingual values are returned.</li>
 * </ul>
 *
 * <p>The resulting model exposes the retrieved logs under the {@code downloads}
 * key, which the associated FreeMarker template renders as JSON.
 */
public class UsersDashboardDownloadsGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(UsersDashboardDownloadsGet.class);

  /**
   * API used to retrieve the dashboard data (download action logs) for a user.
   */
  @Autowired
  private DashboardApi dashboardApi;

  /**
   * Service used to verify that the authenticated user is allowed to access the
   * requested user's dashboard.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the webscript request and builds the response model containing the
   * user's download action logs.
   *
   * <p>The {@code language} request parameter, when present, sets the content
   * and UI locale and disables ML awareness; when absent, ML awareness is
   * enabled. The {@code userId} path variable is checked against the current
   * user, and only the user's own dashboard may be retrieved. Any error is
   * translated into an appropriate HTTP status code and results in a
   * {@code null} model (redirected response); the previous ML-awareness state
   * is always restored.
   *
   * @param req the webscript request, providing the {@code language} parameter
   *            and the {@code userId} template variable
   * @param status the response status, updated to reflect forbidden, bad
   *               request or internal server error conditions
   * @param cache the cache directives for the response
   * @return a model map containing the {@code downloads} list of
   *         {@link io.swagger.model.UserActionLog} entries, or {@code null} when
   *         an error occurred and a redirect status was set
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);
    String language = req.getParameter("language");
    boolean mlAware = MLPropertyInterceptor.isMLAware();

    if (language == null) {
      MLPropertyInterceptor.setMLAware(true);
    } else {
      Locale locale = Locale.of(language);
      I18NUtil.setContentLocale(locale);
      I18NUtil.setLocale(locale);
      MLPropertyInterceptor.setMLAware(false);
    }

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String userId = templateVars.get("userId");

    try {
      if (!currentUserPermissionCheckerService.isCurrentUserEqualTo(userId)) {
        throw new AccessDeniedException(
          "Cannot get user download dashboard of somebody else"
        );
      }
      List<UserActionLog> result =
        this.dashboardApi.usersUserIdDashboardDownloadsGet(userId);
      model.put("downloads", result);
    } catch (AccessDeniedException ade) {
      logger.error("Access denied when getting user downloads dashboard", ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error(
        "Invalid node reference when getting user downloads dashboard",
        inre
      );
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Unexpected error when getting user downloads dashboard", e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }
}
