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
 * Alfresco Web Script endpoint that deletes a single logo attached to a
 * category.
 *
 * <p>As implied by the {@code Delete} suffix in the class name, this endpoint
 * is bound to the HTTP {@code DELETE} method. It resolves the target logo from
 * the {@code id} (category identifier) and {@code logoId} (logo identifier)
 * URL template variables and removes it via {@link CategoriesApi}.
 *
 * <p>Key inputs:
 * <ul>
 *   <li>{@code id} &ndash; URL template variable identifying the category.</li>
 *   <li>{@code logoId} &ndash; URL template variable identifying the logo to
 *       delete.</li>
 *   <li>{@code language} &ndash; optional request parameter controlling the
 *       content locale; when absent the interceptor is left multilingual
 *       aware.</li>
 * </ul>
 *
 * <p>The caller must be a category administrator; otherwise the request is
 * rejected with HTTP {@code 403 Forbidden}. Missing required arguments result
 * in HTTP {@code 400 Bad Request}.
 */
public class CategoryLogosDelete extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(CategoryLogosDelete.class);

  /**
   * API used to perform category-related operations, including deleting a
   * category logo.
   */
  @Autowired
  private CategoriesApi categoriesApi;

  /**
   * Service used to verify that the current user has category administrator
   * permissions before allowing the deletion.
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
   * Executes the logo deletion request.
   *
   * <p>Reads the {@code id} and {@code logoId} URL template variables and the
   * optional {@code language} parameter, configures the multilingual content
   * locale accordingly, checks that the current user is a category
   * administrator and then deletes the requested logo. On success the model
   * contains the updated {@code logos} entry returned by the API.
   *
   * @param req the incoming web script request, providing the URL template
   *            variables ({@code id}, {@code logoId}) and the optional
   *            {@code language} parameter
   * @param status the response status, set to {@code 403} when the user lacks
   *               permission or {@code 400} when required arguments are missing
   * @param cache the cache directives for the response
   * @return a model map containing the {@code logos} result on success, or
   *         {@code null} when the request is rejected (forbidden or bad
   *         request)
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

      if ((categoryId != null) && (logoId != null)) {
        model.put(
          "logos",
          this.categoriesApi.deleteCategoryLogoByLogoId(categoryId, logoId)
        );
      } else {
        throw new InvalidNodeRefException("Missing required args", null);
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

  /**
   * @param currentUserPermissionCheckerService the permission checker service
   *                                             to set
   */
  public void setCurrentUserPermissionCheckerService(
    CurrentUserPermissionCheckerService currentUserPermissionCheckerService
  ) {
    this.currentUserPermissionCheckerService =
      currentUserPermissionCheckerService;
  }
}
