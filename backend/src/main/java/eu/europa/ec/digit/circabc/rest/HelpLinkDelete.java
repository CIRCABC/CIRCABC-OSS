package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.HelpApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco Declarative Web Script that handles the deletion of a help link.
 *
 * <p>As implied by the {@code Delete} suffix in the class name, this endpoint services
 * an HTTP DELETE request. The help link to remove is identified by the {@code id} path
 * template variable extracted from the request URL. An optional {@code language} request
 * parameter controls the multilingual (ML) content handling: when it is absent the script
 * operates in ML-aware mode, and when it is supplied the corresponding {@link Locale} is
 * applied as the content and UI locale before the deletion is performed.</p>
 *
 * <p>Only users with Alfresco administrator or CIRCABC administrator privileges are allowed
 * to delete a help link; any other caller results in an access-denied response.</p>
 */
public class HelpLinkDelete extends CircabcDeclarativeWebScript {

  /** API façade providing the business operation used to delete the help link. */
  @Autowired
  private HelpApi helpApi;

  /** Service used to verify that the current user holds the required administrator permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Executes the help link deletion.
   *
   * <p>Reads the {@code id} path variable to determine which help link to delete and the
   * optional {@code language} request parameter to configure ML-awareness and the content
   * locale. After verifying that the caller is an Alfresco or CIRCABC administrator, it
   * delegates the removal to {@link HelpApi#deleteHelpLink(String)}. The previous ML-aware
   * state is always restored before returning.</p>
   *
   * @param req the web script request; provides the {@code id} template variable and the
   *            optional {@code language} parameter
   * @param status the response status, set to {@link Status#STATUS_FORBIDDEN} when the caller
   *               lacks the required permissions
   * @param cache the cache control for the response
   * @return an empty model map on success, or {@code null} when access is denied
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("id");

    boolean mlAware = MLPropertyInterceptor.isMLAware();
    String language = req.getParameter("language");
    if (language == null) {
      MLPropertyInterceptor.setMLAware(true);
    } else {
      Locale locale = Locale.of(language);
      I18NUtil.setContentLocale(locale);
      I18NUtil.setLocale(locale);
      MLPropertyInterceptor.setMLAware(false);
    }

    try {
      if (
        !(currentUserPermissionCheckerService.isAlfrescoAdmin() ||
          currentUserPermissionCheckerService.isCircabcAdmin())
      ) {
        throw new AccessDeniedException(
          "Cannot create help link, not enough permission"
        );
      }

      helpApi.deleteHelpLink(id);
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }
}
