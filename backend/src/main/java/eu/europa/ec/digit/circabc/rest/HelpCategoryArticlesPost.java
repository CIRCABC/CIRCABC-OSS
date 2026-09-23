package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.HelpApi;
import io.swagger.model.HelpArticle;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.HelpJsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco Declarative Web Script backing the HTTP {@code POST} endpoint used to create a new help
 * article within a given help category.
 *
 * <p>The HTTP method ({@code POST}) is implied by the {@code Post} suffix of the class name. The
 * target help category is identified by the {@code id} template variable taken from the request
 * URL, and the article payload is supplied as JSON in the request body (parsed by
 * {@link HelpJsonParser#parseArticle(WebScriptRequest)}).</p>
 *
 * <p>Only Alfresco administrators or CIRCABC administrators are allowed to create help articles;
 * any other caller results in an HTTP {@code 403 Forbidden} response. The endpoint also honours an
 * optional {@code language} request parameter: when present, the content and UI locale are set
 * accordingly and multilingual (ML) awareness is disabled so the article is created for that
 * specific locale; when absent, ML awareness is enabled.</p>
 *
 * <p>On success the created {@link HelpArticle} is returned in the model under the {@code article}
 * key for rendering by the associated FreeMarker template.</p>
 *
 * @author beaurpi
 */
public class HelpCategoryArticlesPost extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(HelpCategoryArticlesPost.class);

  /**
   * API providing the help article business operations (creation, retrieval, etc.).
   */
  @Autowired
  private HelpApi helpApi;

  /**
   * Service used to verify whether the current user holds the administrative permissions required
   * to create a help article.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the incoming {@code POST} request to create a help article in the category identified
   * by the {@code id} template variable.
   *
   * <p>The optional {@code language} request parameter controls the locale under which the article
   * is created and toggles multilingual awareness accordingly. The caller must be an Alfresco or
   * CIRCABC administrator; otherwise an {@link AccessDeniedException} is raised and translated into
   * a {@code 403 Forbidden} response. Parsing or processing failures are translated into a
   * {@code 500 Internal Server Error} response. The original ML-awareness state is always restored
   * before the method returns.</p>
   *
   * @param req the web script request; provides the {@code id} template variable, the optional
   *     {@code language} parameter and the JSON body describing the article
   * @param status the response status object, updated with an error code, message and redirect flag
   *     when the request cannot be fulfilled
   * @param cache the web script cache directives for the response
   * @return a model map containing the created {@link HelpArticle} under the {@code article} key on
   *     success, or {@code null} when an error status has been set on the response
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
          "Cannot create help article, not enough permission"
        );
      }

      String id = templateVars.get("id");

      if ("".equals(id)) {
        throw new InvalidArgumentException();
      }

      HelpArticle body = HelpJsonParser.parseArticle(req);

      model.put("article", helpApi.createHelpArticle(id, body));
    } catch (AccessDeniedException e) {
      logger.error(ERROR_OCCURRED, e);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidArgumentException | ParseException | IOException e) {
      logger.error(ERROR_OCCURRED, e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal error");
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }
}
