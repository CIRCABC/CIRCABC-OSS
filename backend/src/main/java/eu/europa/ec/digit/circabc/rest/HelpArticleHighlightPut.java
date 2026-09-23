package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import eu.europa.ec.digit.circabc.rest.exception.InvalidIdException;
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
 * Alfresco Declarative Web Script backing the HTTP {@code PUT} endpoint that toggles the
 * "highlighted" flag of a help article.
 *
 * <p>Given a help article identifier supplied as the {@code id} path variable, this endpoint
 * flips the highlight state of the corresponding article and returns the updated article in the
 * response model under the {@code article} key.
 *
 * <p>Only administrators may perform this operation: the current user must be either an Alfresco
 * administrator or a CIRCABC administrator, otherwise the request is rejected with HTTP
 * {@code 403 Forbidden}.
 *
 * <p>Inputs:
 * <ul>
 *   <li>{@code id} (path variable) &ndash; the identifier of the help article to toggle. Must not
 *       be empty.</li>
 *   <li>{@code language} (optional request parameter) &ndash; when provided, the content and UI
 *       locale are set accordingly and multilingual (ML) awareness is disabled so that a single
 *       language variant is handled; when absent, ML awareness is enabled.</li>
 * </ul>
 */
public class HelpArticleHighlightPut extends CircabcDeclarativeWebScript {

  /** API providing help article operations, including toggling the highlight flag. */
  @Autowired
  private HelpApi helpApi;

  /** Service used to verify that the current user has the required administrator permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Toggles the highlight state of the help article identified by the {@code id} path variable.
   *
   * <p>The method optionally adjusts the content/UI locale and multilingual awareness based on the
   * {@code language} request parameter, enforces administrator permissions, performs the toggle via
   * {@link HelpApi#toggleHighlightArticle(String)} and places the updated article in the returned
   * model. The original ML awareness state is always restored before the method returns.
   *
   * <p>On error the method sets the appropriate HTTP status on {@code status}, marks it as a
   * redirect and returns {@code null}:
   * <ul>
   *   <li>{@code 403 Forbidden} &ndash; the current user is not an administrator;</li>
   *   <li>{@code 400 Bad Request} &ndash; the supplied article id is invalid;</li>
   *   <li>{@code 500 Internal Server Error} &ndash; any other unexpected failure.</li>
   * </ul>
   *
   * @param req the web script request; supplies the {@code id} path variable and the optional
   *     {@code language} parameter
   * @param status the response status to be populated in case of success or failure
   * @param cache the response cache control settings
   * @return a model map containing the updated article under the {@code article} key on success, or
   *     {@code null} when an error occurred (with {@code status} set accordingly)
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

      model.put("article", helpApi.toggleHighlightArticle(id));
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
