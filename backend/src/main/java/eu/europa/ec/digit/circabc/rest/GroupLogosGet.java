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
 * Alfresco webscript endpoint that serves the logos of an Interest Group.
 *
 * <p>This read-only endpoint handles the HTTP {@code GET} request (as implied by the
 * {@code Get} suffix in the class name) that retrieves the logo(s) associated with a
 * given Interest Group. The target group is identified by the {@code id} template
 * variable extracted from the request URL.</p>
 *
 * <p>Behavior:</p>
 * <ul>
 *   <li>An optional {@code language} request parameter controls localization. When it is
 *       absent, the endpoint operates in multilingual-aware mode (returning language-agnostic
 *       content). When provided, the corresponding {@link java.util.Locale} is applied to the
 *       content and interface locale and multilingual awareness is disabled so that the logos
 *       are resolved for that specific language.</li>
 *   <li>Access is restricted: only a group administrator of the requested group may retrieve
 *       its logos. Otherwise the request is rejected with HTTP {@code 403 Forbidden}.</li>
 *   <li>On success the resolved logos are placed in the response model under the {@code logos}
 *       key, which the associated FreeMarker template renders as JSON.</li>
 * </ul>
 */
public class GroupLogosGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GroupLogosGet.class);

  /**
   * API providing group-related business operations, used here to fetch the
   * Interest Group logos.
   */
  @Autowired
  private GroupsApi groupsApi;

  /**
   * Service used to verify that the current authenticated user has the required
   * (group administrator) permission to access the requested group's logos.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the {@code GET} request and builds the response model containing the
   * Interest Group logos.
   *
   * <p>The group identifier is read from the {@code id} template variable and the optional
   * {@code language} request parameter selects the locale used to resolve the content.
   * If the current user is a group administrator of the requested group, the logos are
   * added to the model under the {@code logos} key.</p>
   *
   * @param req    the web script request, providing the {@code id} template variable and
   *               the optional {@code language} parameter
   * @param status the web script response status, set to {@code 403 Forbidden} on access
   *               denial or {@code 400 Bad Request} on an invalid node reference
   * @param cache  the cache directives for the response
   * @return a model map containing the {@code logos} entry when the group is accessible,
   *         an empty map when no group id is supplied, or {@code null} when the request is
   *         rejected (access denied or bad request)
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String groupId = templateVars.get("id");

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
    try {
      if (groupId != null) {
        if (!this.currentUserPermissionCheckerService.isGroupAdmin(groupId)) {
          throw new AccessDeniedException(
            "Current Authority cannot access group, not enough permission"
          );
        }

        model.put("logos", this.groupsApi.getInterestGroupLogos(groupId));
      }
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
