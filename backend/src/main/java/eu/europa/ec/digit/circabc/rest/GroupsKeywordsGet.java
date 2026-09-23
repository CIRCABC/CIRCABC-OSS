package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.KeywordsApi;
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
 * Alfresco declarative web script that handles HTTP {@code GET} requests to retrieve the
 * keywords associated with an Interest Group.
 *
 * <p>The endpoint is bound to a URL carrying an {@code igId} template variable that identifies
 * the target Interest Group. Before the keywords are read, the current user's permission to
 * access that Interest Group is verified. An optional {@code language} request parameter controls
 * the locale used when resolving multilingual (ML) content:
 *
 * <ul>
 *   <li>When {@code language} is absent, ML-aware mode is enabled so that raw multilingual
 *       values are returned.</li>
 *   <li>When {@code language} is provided, the corresponding {@link Locale} is set as the content
 *       and UI locale and ML-aware mode is disabled so that values are resolved for that language.</li>
 * </ul>
 *
 * <p>On success the resolved keywords are placed in the response model under the {@code keywords}
 * key, which is subsequently rendered by the associated FreeMarker template.
 */
public class GroupsKeywordsGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GroupsKeywordsGet.class);

  /** API providing access to the keywords defined for an Interest Group. */
  @Autowired
  private KeywordsApi keywordsApi;

  /** Service used to verify that the current user is allowed to access the requested Interest Group. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the web script request: resolves the keywords for the Interest Group identified by the
   * {@code igId} template variable and exposes them in the response model.
   *
   * <p>The optional {@code language} request parameter selects the locale used to resolve
   * multilingual content, as described in the class documentation. Access to the Interest Group is
   * checked before the keywords are read. If access is denied the response status is set to
   * {@code 403 Forbidden}; if the group identifier is invalid the status is set to
   * {@code 400 Bad Request}. In both error cases {@code null} is returned and the redirect flag is
   * set so the standard status template is rendered. The previous ML-aware state is always restored
   * before returning.
   *
   * @param req the web script request; supplies the {@code igId} template variable and the optional
   *     {@code language} parameter
   * @param status the response status object, updated to signal forbidden or bad-request outcomes
   * @param cache the response cache directives (unused)
   * @return a model map containing the {@code keywords} entry on success, or {@code null} when
   *     access is denied or the Interest Group identifier is invalid
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
      this.currentUserPermissionCheckerService.throwIfCanNotAccessInterestGroup(
        id
      );

      model.put("keywords", this.keywordsApi.groupsIdKeywordsGet(id));
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
