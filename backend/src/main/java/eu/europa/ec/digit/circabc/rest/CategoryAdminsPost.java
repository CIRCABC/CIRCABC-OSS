package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.CategoriesApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.SimpleIdJsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
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
 * Alfresco declarative web script backing the {@code POST} endpoint that assigns
 * administrators to a category.
 *
 * <p>The HTTP {@code POST} method is implied by the {@code Post} suffix of the class
 * name. The endpoint expects the target category identifier as the {@code id} URL
 * template variable and a JSON body containing the list of user identifiers to grant
 * category administrator rights to. An optional {@code language} request parameter
 * controls the content locale used while processing the request; when omitted the
 * script operates in multilingual (ML aware) mode.</p>
 *
 * <p>Only CIRCABC administrators or administrators of the target category are allowed
 * to invoke this endpoint. Unauthorized calls result in an HTTP 403 response, malformed
 * input in an HTTP 400 response, and unexpected failures in an HTTP 500 response.</p>
 */
public class CategoryAdminsPost extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(CategoryAdminsPost.class);

  /**
   * API used to perform category-related business operations, including adding
   * administrators to a category.
   */
  @Autowired
  private CategoriesApi categoriesApi;

  /**
   * Service used to check the permissions of the current user, in particular whether
   * the caller is a CIRCABC administrator or an administrator of the target category.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the {@code POST} request that assigns administrators to a category.
   *
   * <p>Resolves the target category from the {@code id} URL template variable, sets up
   * the content locale from the optional {@code language} request parameter, verifies
   * that the current user is allowed to administer the category, parses the list of
   * user identifiers from the request body and delegates the assignment to
   * {@link CategoriesApi#categoriesIdAdminsPost(String, List)}. The original ML-aware
   * state is always restored before returning.</p>
   *
   * @param req the web script request, providing the {@code id} template variable, the
   *            optional {@code language} parameter and the JSON body with user
   *            identifiers
   * @param status the response status, set to 403 on access denial, 400 on bad input
   *               and 500 on unexpected errors
   * @param cache the cache control for the response
   * @return an empty model map on success, or {@code null} when an error status and
   *         redirect have been set
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
      boolean isAllowed =
        currentUserPermissionCheckerService.isCircabcAdmin() ||
        currentUserPermissionCheckerService.isCategoryAdmin(categoryId);
      if (!isAllowed) {
        throw new AccessDeniedException(
          "The user don't have enough permissions"
        );
      }

      List<String> userIds = SimpleIdJsonParser.parseListOfId(req);
      this.categoriesApi.categoriesIdAdminsPost(categoryId, userIds);
    } catch (AccessDeniedException ade) {
      logger.error("Access denied for user on category " + categoryId, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException | ParseException | IOException inre) {
      logger.error("Bad request for category " + categoryId, inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Unexpected error occurred for category " + categoryId, e);
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
