package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.CategoriesApi;
import io.swagger.model.GroupCreationRequest;
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
 * Alfresco declarative web script handling the HTTP {@code PUT} request that updates an existing
 * Interest Group creation request belonging to a category.
 *
 * <p>The endpoint is reached under a category ({@code id}) and targets a specific pending group
 * creation request ({@code requestId}), both supplied as URL template variables. The request body
 * carries the updated {@link io.swagger.model.GroupCreationRequest} payload, which is parsed from
 * the incoming JSON and forwarded to {@link CategoriesApi#categoriesGroupRequestPut(String,
 * GroupCreationRequest)}.
 *
 * <p>Behavioral notes:
 * <ul>
 *   <li>An optional {@code language} request parameter controls multilingual handling: when absent
 *       the {@link MLPropertyInterceptor} is set to be ML aware; when provided the given locale is
 *       applied to the content/UI locale and ML awareness is disabled. The previous ML-aware state
 *       is always restored once processing completes.</li>
 *   <li>Only a category administrator may perform this operation; otherwise the endpoint responds
 *       with HTTP 403 (Forbidden).</li>
 *   <li>Malformed input (invalid node reference, JSON parse error or I/O error) results in an
 *       HTTP 400 (Bad Request) response.</li>
 * </ul>
 */
public class CategoryRequestGroupPut extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(CategoryRequestGroupPut.class);

  /** API facade providing the category-related business operations, including group request updates. */
  @Autowired
  private CategoriesApi categoriesApi;

  /** Service used to verify that the current user holds the required category administrator rights. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Processes the {@code PUT} request that updates a category's group creation request.
   *
   * <p>Reads the category id and request id from the URL template variables, optionally adjusts the
   * multilingual locale from the {@code language} parameter, checks that the caller is a category
   * administrator, parses the {@link GroupCreationRequest} body and delegates the update to
   * {@link CategoriesApi#categoriesGroupRequestPut(String, GroupCreationRequest)}. The original
   * ML-aware state is restored before returning.
   *
   * @param req the web script request; provides the {@code id} and {@code requestId} template
   *     variables, the optional {@code language} parameter and the JSON body
   * @param status the response status, set to {@link Status#STATUS_FORBIDDEN} when the caller lacks
   *     permission or {@link Status#STATUS_BAD_REQUEST} when the input is malformed
   * @param cache the cache control object for the response
   * @return an empty model map on success, or {@code null} when an error status has been set
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
    String requestId = templateVars.get("requestId");

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
      if (!currentUserPermissionCheckerService.isCategoryAdmin(categoryId)) {
        throw new AccessDeniedException(
          "Impossible to edit the category group request. Not enough permissions"
        );
      }

      GroupCreationRequest body =
        InterestGroupJsonParser.parseGroupCreationRequest(req);
      this.categoriesApi.categoriesGroupRequestPut(categoryId, requestId, body);
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
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
    return model;
  }
}
