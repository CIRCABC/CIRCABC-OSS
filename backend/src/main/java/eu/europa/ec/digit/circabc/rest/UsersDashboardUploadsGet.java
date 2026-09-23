package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.DashboardApi;
import io.swagger.model.UserActionLog;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST webscript endpoint that returns the list of uploads shown on a user's
 * dashboard.
 *
 * <p>The class name follows the CIRCABC {@code <Entity><Method>} convention and
 * implies an HTTP {@code GET} request. It is bound to a URL carrying a
 * {@code userId} path variable (e.g. {@code /users/{userId}/dashboard/uploads})
 * and delegates the actual retrieval to {@link DashboardApi}.</p>
 *
 * <p>Key inputs:</p>
 * <ul>
 *   <li>{@code userId} &ndash; template (path) variable identifying the user whose
 *       upload dashboard is requested. A user may only read their own dashboard;
 *       requesting another user's dashboard results in an
 *       {@link AccessDeniedException} that maps to HTTP 403.</li>
 *   <li>{@code language} &ndash; optional request parameter. When provided the
 *       content and UI locale are set accordingly and multilingual
 *       ({@code MLAware}) resolution is disabled; when absent, multilingual
 *       resolution is enabled.</li>
 * </ul>
 *
 * <p>On success the response model contains an {@code uploads} entry holding the
 * list of {@link UserActionLog} items. Errors are translated to the appropriate
 * HTTP status codes (403 Forbidden for access denial, 400 Bad Request for an
 * invalid node reference).</p>
 */
public class UsersDashboardUploadsGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(UsersDashboardUploadsGet.class);

  /** API providing dashboard-related business operations, injected by Spring. */
  @Autowired
  private DashboardApi dashboardApi;

  /**
   * Service used to resolve the currently authenticated user, so that access to
   * another user's dashboard can be rejected.
   */
  @Autowired
  private AuthenticationService authenticationService;

  /**
   * Executes the webscript: retrieves the upload activity for the requested user
   * and populates the response model.
   *
   * <p>The method first configures locale/multilingual handling based on the
   * optional {@code language} parameter, then verifies that the caller is
   * requesting their own dashboard before delegating to
   * {@link DashboardApi#usersUserIdDashboardUploadsGet(String)}. The original
   * {@code MLAware} state is always restored before returning.</p>
   *
   * @param req the web script request; supplies the {@code userId} template
   *            variable and the optional {@code language} parameter
   * @param status the response status, updated to 403 or 400 when the request is
   *               forbidden or invalid
   * @param cache the cache control object for the response
   * @return a model map containing the {@code uploads} list of
   *         {@link UserActionLog} entries, or {@code null} when the request is
   *         rejected and the status has been set to an error/redirect
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
      if (!(this.authenticationService.getCurrentUserName().equals(userId))) {
        throw new AccessDeniedException(
          "Cannot get user upload dashboard of somebody else"
        );
      }

      List<UserActionLog> result =
        this.dashboardApi.usersUserIdDashboardUploadsGet(userId);
      model.put("uploads", result);
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error(ERROR_OCCURRED, inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }
}
