package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.DashboardApi;
import io.swagger.model.UserNewsFeed;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
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
 * Alfresco Web Script endpoint that handles HTTP {@code GET} requests for a user's
 * dashboard news feed.
 *
 * <p>Given a {@code userId} (supplied as a URL template variable), this endpoint returns
 * the aggregated {@link io.swagger.model.UserNewsFeed} for that user. Access is restricted:
 * a user may only retrieve their own news feed. If the authenticated user does not match the
 * requested {@code userId}, the request is rejected with an HTTP 403 (Forbidden).</p>
 *
 * <p>Supported request parameters:</p>
 * <ul>
 *   <li>{@code language} (optional) &ndash; ISO language code used to resolve the content
 *       locale. When omitted, multilingual (ML) awareness is enabled so language-neutral
 *       values are returned.</li>
 *   <li>{@code when} (optional) &ndash; the time window for the feed. Defaults to
 *       {@code "today"} when absent or empty.</li>
 * </ul>
 *
 * <p>On success the resulting feed is placed in the model under the key {@code "feed"} for
 * rendering by the associated FreeMarker template.</p>
 */
public class UsersDashboardNewsFeedGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(UsersDashboardNewsFeedGet.class);

  /**
   * API used to retrieve dashboard-related data, including the user news feed.
   */
  @Autowired
  private DashboardApi dashboardApi;

  /**
   * Service used to verify that the current authenticated user matches the requested user,
   * enforcing that users can only access their own news feed.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the {@code GET} request and builds the response model containing the user's
   * news feed.
   *
   * <p>Reads the optional {@code language} and {@code when} request parameters and the
   * required {@code userId} URL template variable, checks that the current user is allowed to
   * access the feed, retrieves it via {@link DashboardApi}, and stores it in the model under
   * the {@code "feed"} key. Multilingual awareness and content locale are configured based on
   * the {@code language} parameter and always restored in the {@code finally} block.</p>
   *
   * <p>Error handling sets the appropriate HTTP status and returns {@code null} (indicating a
   * redirect to the status template) instead of throwing:</p>
   * <ul>
   *   <li>{@link AccessDeniedException} &rarr; HTTP 403 (Forbidden)</li>
   *   <li>{@link InvalidNodeRefException} &rarr; HTTP 400 (Bad Request)</li>
   *   <li>any other {@link Exception} &rarr; HTTP 500 (Internal Server Error)</li>
   * </ul>
   *
   * @param req the web script request providing parameters and URL template variables
   * @param status the response status, updated to reflect the outcome of the request
   * @param cache the cache directives for the response
   * @return a model map containing the {@code "feed"} entry on success, or {@code null} when
   *         an error occurs and the response is redirected to the status template
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

    String when = req.getParameter("when");
    if ((when == null) || Objects.equals(when, "")) {
      when = "today";
    }

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String userId = templateVars.get("userId");

    try {
      if (!(currentUserPermissionCheckerService.isCurrentUserEqualTo(userId))) {
        throw new AccessDeniedException(
          "Cannot get user feed dashboard of somebody else"
        );
      }

      UserNewsFeed result = this.dashboardApi.usersUserIdDashboardNewsfeedGet(
        userId,
        when
      );
      model.put("feed", result);
    } catch (AccessDeniedException ade) {
      logger.error("Access denied for user " + userId, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error("Invalid node reference for user " + userId, inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Unexpected error getting news feed for user " + userId, e);
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
