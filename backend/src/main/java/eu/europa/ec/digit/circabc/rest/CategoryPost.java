package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.CategoriesApi;
import io.swagger.model.Category;
import io.swagger.model.Node;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.NodeJsonParser;
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
 * REST webscript endpoint that creates a new Category under a given Header.
 *
 * <p>The class name follows the CIRCABC convention {@code <Entity><Method>};
 * being a {@code Post} endpoint it handles the HTTP {@code POST} method,
 * conceptually mapping to {@code POST /headers/{id}/category}.
 *
 * <p>Key inputs:
 * <ul>
 *   <li>{@code id} (URL template variable) — the identifier of the parent
 *       Header under which the category is created.</li>
 *   <li>{@code language} (optional request parameter) — when supplied, the
 *       content and UI locale are set accordingly and multilingual (ML)
 *       awareness is disabled; when omitted, ML awareness is enabled.</li>
 *   <li>Request body (JSON) — parsed into a {@link Node} from which the
 *       {@code name} and {@code title} of the new category are taken.</li>
 * </ul>
 *
 * <p>Only CIRCABC administrators are allowed to invoke this endpoint. On
 * success the created {@link Category} is returned in the model under the
 * {@code "category"} key. Errors are translated into the appropriate HTTP
 * status codes (403 for access denial, 400 for malformed input, 500 for
 * unexpected failures).
 *
 * @see CircabcDeclarativeWebScript
 * @see CategoriesApi
 */
public class CategoryPost extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(CategoryPost.class);

  /**
   * API providing the business operations used to create categories.
   */
  @Autowired
  private CategoriesApi categoriesApi;

  /**
   * Service used to verify that the current user has the required CIRCABC
   * administrator privileges before the category is created.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the {@code POST} request that creates a category under the header
   * identified by the {@code id} URL template variable.
   *
   * <p>The optional {@code language} request parameter controls locale and
   * multilingual awareness handling; the request body is parsed as JSON to
   * obtain the new category's name and title. The current user must be a
   * CIRCABC administrator.
   *
   * @param req the incoming webscript request, providing the {@code id}
   *            template variable, the optional {@code language} parameter and
   *            the JSON body describing the category
   * @param status the response status, updated to the relevant HTTP error code
   *               when the request cannot be completed
   * @param cache the cache directives for the response
   * @return a model map containing the created {@link Category} under the
   *         {@code "category"} key on success, or {@code null} when an error
   *         occurred and the status has been set accordingly
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String headerId = templateVars.get("id");

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
      this.currentUserPermissionCheckerService.throwIfNotCircabcAdmin();

      Node body = NodeJsonParser.parseSimpleJSON(req);
      Category categoryBody = new Category();
      categoryBody.setName(body.getName());
      categoryBody.setTitle(body.getTitle());
      Category categ = this.categoriesApi.headersIdCategoryPost(
        headerId,
        categoryBody
      );
      model.put("category", categ);
    } catch (AccessDeniedException ade) {
      logger.error("Access denied", ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException | ParseException | IOException inre) {
      logger.error("Bad request", inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Unexpected error", e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Unexpected error");
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
    return model;
  }
}
