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
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco declarative web script endpoint that selects (activates) a specific
 * logo for a CIRCABC category.
 *
 * <p>The class name implies an HTTP {@code PUT} request. The endpoint marks the
 * logo identified by {@code logoId} as the currently selected logo of the
 * category identified by {@code id}, delegating the actual update to
 * {@link io.swagger.api.CategoriesApi#selectCategoryLogoByLogoId(String, String)}.
 *
 * <p>Key inputs:
 * <ul>
 *   <li>{@code id} &ndash; template variable identifying the target category.</li>
 *   <li>{@code logoId} &ndash; template variable identifying the logo to select.</li>
 *   <li>{@code language} &ndash; optional request parameter controlling the
 *       content locale; when absent the call is executed in ML-aware mode.</li>
 * </ul>
 *
 * <p>Only category administrators are allowed to perform this operation;
 * otherwise an {@link org.alfresco.repo.security.permissions.AccessDeniedException}
 * is raised and the response is set to {@code 403 Forbidden}. Invalid node
 * references result in a {@code 400 Bad Request} response.
 */
public class CategoryLogosSelectionPut extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(CategoryLogosSelectionPut.class);

  /**
   * API used to perform category-related operations, including selecting the
   * active logo of a category.
   */
  @Autowired
  private CategoriesApi categoriesApi;

  /**
   * Service used to verify that the current user holds category administrator
   * permissions before the logo selection is applied.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * @return the categoriesApi
   */
  public CategoriesApi getCategoriesApi() {
    return this.categoriesApi;
  }

  /**
   * @param categoriesApi the categoriesApi to set
   */
  public void setCategoriesApi(CategoriesApi categoriesApi) {
    this.categoriesApi = categoriesApi;
  }

  /**
   * Handles the web script request by selecting the requested logo for the
   * target category.
   *
   * <p>Reads the {@code id} (category) and {@code logoId} template variables and
   * the optional {@code language} request parameter, configures the content
   * locale / ML-awareness accordingly, checks that the current user is a
   * category administrator and then delegates to
   * {@link io.swagger.api.CategoriesApi#selectCategoryLogoByLogoId(String, String)}.
   * The previous ML-aware state is always restored before returning.
   *
   * @param req the incoming web script request providing template variables and
   *            parameters
   * @param status the response status, updated to {@code 403} on access denial
   *               or {@code 400} on an invalid node reference
   * @param cache the cache directives for the response
   * @return an empty model map on success, or {@code null} when an error occurs
   *         and a redirect status has been set
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
    String logoId = templateVars.get("logoId");

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

      this.categoriesApi.selectCategoryLogoByLogoId(categoryId, logoId);
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
