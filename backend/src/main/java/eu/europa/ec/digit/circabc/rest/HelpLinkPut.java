package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.HelpApi;
import io.swagger.model.HelpLink;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.HelpJsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco webscript endpoint that updates an existing CIRCABC help link.
 *
 * <p>The {@code Put} suffix in the class name maps to an HTTP {@code PUT} request. The endpoint
 * parses a {@link HelpLink} payload from the request body and persists the update through
 * {@link HelpApi#updateHelpLink(HelpLink)}. Only Alfresco administrators or CIRCABC administrators
 * are allowed to perform the operation; other callers receive a {@code 403 Forbidden} response.
 *
 * <p>Key inputs:
 * <ul>
 *   <li>{@code language} request parameter (optional) &mdash; when provided, the content and UI
 *       locale are set accordingly and multilingual (ML) awareness is disabled so that the update
 *       targets that specific language; when omitted, ML awareness is enabled.</li>
 *   <li>The JSON request body describing the {@link HelpLink} to update.</li>
 * </ul>
 *
 * <p>On success the returned model contains the updated help link under the {@code link} key.
 */
public class HelpLinkPut extends CircabcDeclarativeWebScript {

  /** API used to perform help link business operations such as updating a help link. */
  @Autowired
  private HelpApi helpApi;

  /** Service used to verify whether the current user has administrator privileges. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the help link update request.
   *
   * <p>Resolves the optional {@code language} parameter to configure the content locale and ML
   * awareness, verifies that the current user is an Alfresco or CIRCABC administrator, parses the
   * {@link HelpLink} payload from the request and delegates the update to {@link HelpApi}. The
   * previous ML awareness state is always restored before returning.
   *
   * @param req the web script request, providing the optional {@code language} parameter and the
   *     JSON body describing the help link to update
   * @param status the response status, set to {@code 403 Forbidden} when the caller lacks
   *     permission or to {@code 400 Bad Request} when the payload cannot be read or parsed
   * @param cache the response cache directives
   * @return a model map containing the updated help link under the {@code link} key, or
   *     {@code null} when an error occurred and a redirect status has been set
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

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

      HelpLink body = HelpJsonParser.parseLink(req);

      model.put("link", helpApi.updateHelpLink(body));
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (IOException | ParseException e) {
      logger.error(ERROR_OCCURRED, e);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Internal error");
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }
}
