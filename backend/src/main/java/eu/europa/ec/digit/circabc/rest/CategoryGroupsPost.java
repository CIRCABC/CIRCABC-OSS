package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.CategoriesApi;
import io.swagger.model.InterestGroupPostModel;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.InterestGroupJsonParser;
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
 * REST webscript endpoint that creates a new Interest Group under a given Category.
 *
 * <p>The class name follows the CIRCABC convention {@code <Entity><Method>}, so this handles
 * the HTTP {@code POST} request bound to a category's groups collection (e.g.
 * {@code POST /categories/{id}/groups}). The target category is identified by the {@code id}
 * template variable in the URL, and the new group's definition is supplied in the JSON request
 * body (parsed into an {@link InterestGroupPostModel}).</p>
 *
 * <p>Key inputs:</p>
 * <ul>
 *   <li>{@code id} (URL template variable) — the identifier of the parent category.</li>
 *   <li>{@code language} (optional request parameter) — when provided, response content is
 *       resolved for the given locale and multilingual awareness is disabled; when absent,
 *       multilingual awareness is enabled.</li>
 *   <li>JSON request body — the new Interest Group payload.</li>
 * </ul>
 *
 * <p>Only category administrators are authorized to invoke this endpoint. The response model
 * exposes the created group under the {@code group} key. Errors are mapped to HTTP status codes:
 * {@code 403} for access denial, {@code 400} for invalid or malformed requests, and {@code 500}
 * for unexpected failures.</p>
 */
public class CategoryGroupsPost extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(CategoryGroupsPost.class);

  /** API facade exposing category operations, including creation of Interest Groups. */
  @Autowired
  private CategoriesApi categoriesApi;

  /** Service used to verify that the current user holds category administrator rights. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the POST request that creates a new Interest Group within the target category.
   *
   * <p>Reads the category identifier from the {@code id} URL template variable and the optional
   * {@code language} request parameter to configure locale/multilingual handling, verifies that
   * the current user is a category administrator, parses the Interest Group payload from the
   * request body, and delegates group creation to {@link CategoriesApi}. The original
   * multilingual-awareness state is always restored before returning.</p>
   *
   * @param req the incoming webscript request; provides the {@code id} template variable, the
   *     optional {@code language} parameter and the JSON body describing the new group
   * @param status the response status holder; set to {@code 403}, {@code 400} or {@code 500} when
   *     the request is denied, invalid or fails unexpectedly
   * @param cache the response cache directives holder
   * @return a model map containing the created group under the {@code group} key on success, or
   *     {@code null} when an error occurs and the status has been set for redirection
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
      this.currentUserPermissionCheckerService.throwIfNotCategoryAdmin(
        categoryId
      );

      InterestGroupPostModel body = InterestGroupJsonParser.parseNewGroup(req);

      model.put(
        "group",
        this.categoriesApi.categoriesIdGroupsPost(categoryId, body)
      );
    } catch (AccessDeniedException ade) {
      logger.error("Access denied to category: " + categoryId, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException | ParseException | IOException inre) {
      logger.error("Invalid request for category: " + categoryId, inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(
        "Unexpected error creating category group for: " + categoryId,
        e
      );
      logger.error("Exception details:", e);
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
