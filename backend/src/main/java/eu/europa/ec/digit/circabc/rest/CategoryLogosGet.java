package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.CategoriesApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco Web Script endpoint that handles the HTTP {@code GET} request for
 * retrieving the list of logos associated with a CIRCABC category.
 *
 * <p>The endpoint URL carries the target category identifier as the
 * {@code id} template variable and accepts an optional {@code language}
 * request parameter used to resolve multilingual content. When no language is
 * supplied the response is rendered in a multilingual (ML aware) fashion;
 * otherwise the given locale is applied and ML awareness is disabled for the
 * duration of the call.</p>
 *
 * <p>Access is restricted to category administrators: the current user must
 * pass the category-admin permission check, otherwise the request is rejected
 * with an HTTP {@code 403 Forbidden} response. Invalid node references result
 * in an HTTP {@code 400 Bad Request} response.</p>
 *
 * <p>On success the returned model exposes the retrieved logos under the
 * {@code logos} key, which is subsequently rendered by the associated
 * FreeMarker template.</p>
 */
public class CategoryLogosGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(CategoryLogosGet.class);

  /**
   * API used to look up the logos of a category by its identifier.
   */
  @Autowired
  private CategoriesApi categoriesApi;

  /**
   * Service used to verify that the current user has category administrator
   * permissions before the logos are returned.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Returns the {@link CategoriesApi} used by this endpoint.
   *
   * @return the categoriesApi
   */
  public CategoriesApi getCategoriesApi() {
    return this.categoriesApi;
  }

  /**
   * Sets the {@link CategoriesApi} used by this endpoint.
   *
   * @param categoriesApi the categoriesApi to set
   */
  public void setCategoriesApi(CategoriesApi categoriesApi) {
    this.categoriesApi = categoriesApi;
  }

  /**
   * Executes the web script logic for retrieving the logos of a category.
   *
   * <p>Reads the category identifier from the {@code id} template variable and
   * the optional {@code language} request parameter to configure the content
   * locale and multilingual awareness. After verifying that the current user
   * is a category administrator, it populates the model with the category
   * logos under the {@code logos} key. The original ML awareness state is
   * always restored before the method returns.</p>
   *
   * @param req the web script request, providing the {@code id} template
   *     variable and the optional {@code language} parameter
   * @param status the response status, set to {@code 403 Forbidden} when the
   *     user lacks permissions or {@code 400 Bad Request} on an invalid node
   *     reference
   * @param cache the response cache directives
   * @return the model map containing the {@code logos} entry on success, or
   *     {@code null} when the request fails and a redirect status is set
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
        !this.currentUserPermissionCheckerService.isCategoryAdmin(categoryId)
      ) {
        throw new AccessDeniedException(
          "Cannot get the list of all category logos, not enough permissions"
        );
      }

      if (categoryId != null) {
        model.put(
          "logos",
          this.categoriesApi.getCategoryLogoByCategoryId(categoryId)
        );
      }
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
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
    return model;
  }
}
