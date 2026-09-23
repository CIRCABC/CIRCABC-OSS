package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import eu.europa.ec.digit.circabc.rest.exception.InvalidIdException;
import io.swagger.api.HelpApi;
import io.swagger.model.HelpArticle;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.HelpJsonParser;
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
 * Alfresco Declarative Web Script backing the HTTP {@code PUT} endpoint that updates an existing
 * help article.
 *
 * <p>The article to update is identified by the {@code id} template variable taken from the request
 * URL, and the new content is parsed from the JSON request body. Updates are restricted to
 * administrators: the request is rejected with {@code 403 Forbidden} unless the current user is an
 * Alfresco administrator or a CIRCABC administrator.
 *
 * <p>An optional {@code language} request parameter controls the locale used when persisting the
 * article. When a language is supplied, the corresponding {@link Locale} is set as the content and
 * UI locale and multilingual (ML) awareness is disabled so that the value is written for that
 * specific language; when it is omitted, ML awareness is enabled instead. The previous ML-awareness
 * state is always restored once the request completes.
 *
 * <p>On failure the endpoint sets an appropriate HTTP status and returns {@code null}:
 * {@code 403 Forbidden} for permission errors, {@code 400 Bad Request} for an invalid/empty
 * identifier, and {@code 500 Internal Server Error} for any other exception.
 */
public class HelpArticlePut extends CircabcDeclarativeWebScript {

  /** API used to perform the actual help-article update in the underlying repository. */
  @Autowired
  private HelpApi helpApi;

  /** Service used to verify that the current user has the required administrator privileges. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the {@code PUT} request by updating the help article identified in the request URL with
   * the content provided in the JSON body.
   *
   * <p>Verifies that the caller is an Alfresco or CIRCABC administrator, validates the article
   * identifier, parses the request body into a {@link HelpArticle} and delegates the update to
   * {@link HelpApi#updateHelpArticle(String, HelpArticle)}. The multilingual locale is configured
   * according to the optional {@code language} parameter and the original ML-awareness state is
   * restored before returning.
   *
   * @param req the web script request; supplies the {@code id} template variable, the optional
   *     {@code language} parameter and the JSON body describing the article
   * @param status the response status object, set to an error code and marked as a redirect when a
   *     failure occurs
   * @param cache the response cache control object
   * @return an empty model map on success, or {@code null} when an error occurred and the status
   *     has been set accordingly
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);
    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();

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
          "Cannot update help article, not enough permission"
        );
      }

      String id = templateVars.get("id");

      if ("".equals(id)) {
        throw new InvalidIdException("Help article ID cannot be empty");
      }

      HelpArticle body = HelpJsonParser.parseArticle(req);

      helpApi.updateHelpArticle(id, body);
    } catch (AccessDeniedException e) {
      logger.error(ERROR_OCCURRED, e);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidIdException e) {
      logger.error(ERROR_OCCURRED, e);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage(e.getMessage());
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(ERROR_OCCURRED, e);
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
