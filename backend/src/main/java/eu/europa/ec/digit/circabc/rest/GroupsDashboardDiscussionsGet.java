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
 * Alfresco declarative web script backing the HTTP {@code GET} endpoint that
 * returns the list of recent discussions shown on the dashboard of an Interest
 * Group (IG).
 *
 * <p>The interest group is identified by the {@code igId} URL template variable.
 * An optional {@code language} request parameter controls localization: when it
 * is absent the script runs in multilingual-aware mode, otherwise the given
 * locale is applied to the content and UI before rendering. Access is guarded by
 * {@link CurrentUserPermissionCheckerService#canAccessInterestGroup(String)};
 * callers without permission receive an HTTP 403 response, and an invalid or
 * unknown node reference results in an HTTP 400 response.</p>
 *
 * <p>On success the returned model exposes the recent discussions under the
 * {@code discussions} key for the associated FreeMarker template to render.</p>
 */
public class GroupsDashboardDiscussionsGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(
    GroupsDashboardDiscussionsGet.class
  );

  /** API providing group-related business operations, including retrieval of an
   * interest group's recent discussions. */
  @Autowired
  private GroupsApi groupsApi;

  /** Service used to verify that the current user is allowed to access the
   * requested interest group. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the web script request by resolving the target interest group,
   * applying the requested locale (if any), checking access permission and
   * collecting its recent discussions into the response model.
   *
   * @param req the web script request; supplies the {@code igId} URL template
   *     variable and the optional {@code language} parameter
   * @param status the response status, set to {@code 403} when access is denied
   *     or {@code 400} when the node reference is invalid
   * @param cache the cache control object for the response
   * @return the model containing the recent discussions under the
   *     {@code discussions} key, or {@code null} when an error status has been
   *     set (access denied or bad request)
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
          "User cannot get recent discussions of IG, not enough permission"
        );
      }
      model.put("discussions", this.groupsApi.getGroupRecentDiscussions(id));
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

  /**
   * @return the groupsApi
   */
  public GroupsApi getGroupsApi() {
    return this.groupsApi;
  }

  /**
   * @param groupsApi the groupsApi to set
   */
  public void setGroupsApi(GroupsApi groupsApi) {
    this.groupsApi = groupsApi;
  }

  /**
   * @param currentUserPermissionCheckerService the permission checker service to
   *     set
   */
  public void setCurrentUserPermissionCheckerService(
    CurrentUserPermissionCheckerService currentUserPermissionCheckerService
  ) {
    this.currentUserPermissionCheckerService =
      currentUserPermissionCheckerService;
  }
}
