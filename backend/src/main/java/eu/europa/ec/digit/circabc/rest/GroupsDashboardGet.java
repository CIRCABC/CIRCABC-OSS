package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.GroupsApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
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
 * Alfresco Declarative Web Script that handles the HTTP {@code GET} request for
 * retrieving the dashboard of an Interest Group (IG).
 *
 * <p>The endpoint expects the IG identifier to be supplied as the {@code igId}
 * URL template variable and returns the corresponding dashboard data. An
 * optional {@code language} request parameter controls localisation of the
 * returned content: when it is absent the response is rendered in a
 * multilingual (ML) aware fashion, otherwise the supplied locale is applied to
 * both the content and the current thread locale.</p>
 *
 * <p>Access is guarded by {@link CurrentUserPermissionCheckerService}: the
 * current user must be allowed to access the requested Interest Group. If the
 * user lacks the required permission the response is set to
 * {@code 403 Forbidden}; if the {@code igId} does not resolve to a valid node
 * the response is set to {@code 400 Bad Request}.</p>
 *
 * <p>On success the returned model contains a single {@code dashboard} entry
 * holding the dashboard produced by {@link GroupsApi#getGroupDashboard(String)},
 * which is subsequently rendered by the associated FreeMarker template.</p>
 */
public class GroupsDashboardGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GroupsDashboardGet.class);

  /** API providing access to Interest Group operations, including the dashboard retrieval. */
  @Autowired
  private GroupsApi groupsApi;

  /** Service used to verify that the current user is allowed to access the requested Interest Group. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Executes the web script, retrieving the dashboard for the Interest Group
   * identified by the {@code igId} URL template variable.
   *
   * <p>When the {@code language} request parameter is provided the matching
   * {@link Locale} is applied to the content and thread locale and multilingual
   * awareness is disabled; otherwise multilingual awareness is enabled. The
   * previous multilingual-awareness setting is always restored before the method
   * returns.</p>
   *
   * <p>If the current user cannot access the Interest Group the response status
   * is set to {@code 403 Forbidden}; if the identifier does not reference a
   * valid node the status is set to {@code 400 Bad Request}. In both error cases
   * {@code null} is returned so that the status redirect is honoured.</p>
   *
   * @param req the web script request, providing the {@code igId} template
   *     variable and the optional {@code language} parameter
   * @param status the web script response status, updated on access or
   *     validation errors
   * @param cache the cache control object for the response
   * @return a model map containing the {@code dashboard} entry on success, or
   *     {@code null} when an access or validation error is handled
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
    String id = templateVars.get("igId");

    try {
      if (
        !this.currentUserPermissionCheckerService.canAccessInterestGroup(id)
      ) {
        throw new AccessDeniedException(
          "User cannot get dashboard of IG, not enough permission"
        );
      }
      model.put("dashboard", this.groupsApi.getGroupDashboard(id));
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
