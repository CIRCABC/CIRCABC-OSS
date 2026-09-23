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
 * REST webscript endpoint that deletes a help article.
 *
 * <p>As implied by the {@code Delete} suffix in the class name, this endpoint handles the HTTP
 * {@code DELETE} method. The target help article is identified by the {@code id} template variable
 * taken from the request URL. An optional {@code language} request parameter controls the content
 * locale used while performing the deletion: when supplied, multilingual (ML) awareness is disabled
 * and the given locale is applied; when absent, ML awareness is enabled so that the operation is
 * performed in a language-aware manner.</p>
 *
 * <p>Only Alfresco administrators or CIRCABC administrators are allowed to invoke this endpoint;
 * any other caller receives an HTTP 403 (Forbidden) response. A missing/empty {@code id} results in
 * an HTTP 400 (Bad Request), and unexpected failures produce an HTTP 500 (Internal Server Error).</p>
 */
public class HelpArticleDelete extends CircabcDeclarativeWebScript {

  /** API used to perform help article operations, including deletion. */
  @Autowired
  private HelpApi helpApi;

  /** Service used to verify that the current user has administrator privileges. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Deletes the help article identified by the {@code id} template variable in the request URL.
   *
   * <p>The optional {@code language} request parameter selects the content locale: if provided,
   * ML awareness is disabled and the locale is applied; otherwise ML awareness is enabled. The
   * previous ML awareness state is always restored before the method returns. The caller must be an
   * Alfresco or CIRCABC administrator, and the {@code id} must not be empty.</p>
   *
   * <p>On error the method sets the appropriate HTTP status on {@code status}, marks it as a
   * redirect and returns {@code null}: 403 for insufficient permissions, 400 for an invalid id, and
   * 500 for any other failure.</p>
   *
   * @param req the web script request; supplies the {@code id} template variable and the optional
   *     {@code language} parameter
   * @param status the response status to populate, notably on error conditions
   * @param cache the response cache control settings
   * @return an empty model map on success, or {@code null} when an error occurred and the status has
   *     been set accordingly
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
          "Cannot delete help article, not enough permission"
        );
      }

      String id = templateVars.get("id");

      if ("".equals(id)) {
        throw new InvalidIdException("Help article ID cannot be empty");
      }

      helpApi.deleteHelpArticle(id);
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
