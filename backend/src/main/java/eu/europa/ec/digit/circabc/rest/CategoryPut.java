package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.CategoriesApi;
import io.swagger.model.Category;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.CategoryJsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST webscript endpoint that updates an existing CIRCABC category.
 *
 * <p>Handles the HTTP {@code PUT} request for a single category identified by
 * the {@code id} template variable in the URL. The category's new state is
 * supplied as a JSON body which is parsed into a {@link Category} object and
 * persisted via {@link CategoriesApi#categoriesIdPut(String, Category)}.</p>
 *
 * <p>Key inputs:</p>
 * <ul>
 *   <li>{@code id} — the category identifier, taken from the URL template
 *       variables.</li>
 *   <li>{@code language} — optional request parameter selecting the content
 *       locale. When absent the endpoint operates in multilingual (ML) aware
 *       mode; when present the given locale is applied and ML awareness is
 *       disabled.</li>
 *   <li>request body — the JSON representation of the category to update.</li>
 * </ul>
 *
 * <p>The caller must be a category administrator; otherwise the request is
 * rejected with an HTTP 403 (forbidden) status. Invalid node references or
 * malformed request bodies result in an HTTP 400 (bad request) status.</p>
 *
 * @see CircabcDeclarativeWebScript
 * @see CategoriesApi
 */
public class CategoryPut extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(CategoryPut.class);

  /** API providing the category business operations, including the update. */
  @Autowired
  private CategoriesApi categoriesApi;

  /** Service used to verify that the current user is a category administrator. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Executes the category update request.
   *
   * <p>Reads the category id from the URL template variables and the optional
   * {@code language} parameter, configures the multilingual property
   * interceptor and content locale accordingly, checks that the current user
   * is a category administrator, parses the JSON body into a {@link Category}
   * and delegates the update to {@link CategoriesApi#categoriesIdPut(String, Category)}.
   * The previous ML-aware state is always restored before returning.</p>
   *
   * @param req    the web script request, providing the {@code id} template
   *               variable, the optional {@code language} parameter and the
   *               JSON body describing the updated category
   * @param status the response status; set to 403 when access is denied and to
   *               400 for invalid node references or malformed bodies
   * @param cache  the response cache directives
   * @return a model map containing the updated {@code category}, or
   *         {@code null} when the request fails and a redirect status has been
   *         set
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String categoryId = templateVars.get("id");

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
      if (
        !this.currentUserPermissionCheckerService.throwIfNotCategoryAdmin(
          categoryId
        )
      ) {
        throw new AccessDeniedException("No access on node:" + categoryId);
      }

      Category categoryBody = CategoryJsonParser.parseSimpleJSON(req);

      model.put(
        "category",
        this.categoriesApi.categoriesIdPut(categoryId, categoryBody)
      );
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
    } catch (IOException e) {
      logger.error(ERROR_OCCURRED, e);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad body");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (ParseException e) {
      if (logger.isErrorEnabled()) {
        logger.error("Parse execption " + req, e);
      }
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad body");
      status.setRedirect(true);
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
    return model;
  }
}
