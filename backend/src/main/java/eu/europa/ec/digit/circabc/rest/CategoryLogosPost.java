package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.CategoriesApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.InputStream;
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
import org.springframework.extensions.webscripts.servlet.FormData;

/**
 * Alfresco declarative web script backing the HTTP {@code POST} endpoint that
 * uploads a logo image for a given category.
 *
 * <p>The endpoint expects a {@code multipart/form-data} request whose payload
 * contains the logo file. The target category is identified by the {@code id}
 * template variable taken from the request URL, and an optional {@code language}
 * request parameter controls the content locale used while storing the logo:
 * when omitted the script operates in a multilingual-aware mode, otherwise the
 * provided locale is applied and multilingual awareness is disabled.</p>
 *
 * <p>Only category administrators are allowed to perform this operation; a
 * permission check is enforced before the upload and an access-denied result is
 * returned otherwise. Once the first file field of the multipart request is
 * stored, the (now updated) list of category logos is returned in the response
 * model under the {@code logos} key.</p>
 *
 * @see CircabcDeclarativeWebScript
 * @see io.swagger.api.CategoriesApi
 */
public class CategoryLogosPost extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(CategoryLogosPost.class);

  /**
   * API used to store the uploaded logo and to retrieve the category's logos.
   */
  @Autowired
  private CategoriesApi categoriesApi;

  /**
   * Service used to verify that the current user has category administrator
   * permissions before allowing the logo upload.
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
   * Executes the logo upload for the requested category.
   *
   * <p>Reads the {@code id} template variable to determine the target category
   * and the optional {@code language} request parameter to set the content
   * locale. After verifying that the current user is a category administrator,
   * it parses the multipart request body, stores the first file field found as
   * the category logo and populates the response model with the resulting list
   * of logos.</p>
   *
   * @param req the web script request, expected to be a multipart form-data
   *            request carrying the logo file and the {@code id} template
   *            variable
   * @param status the response status, updated to {@code 403 Forbidden} on
   *               permission errors or {@code 400 Bad Request} on invalid node
   *               references
   * @param cache the cache control for the response
   * @return a model map containing the category logos under the {@code logos}
   *         key, or {@code null} when the request is rejected (forbidden or bad
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

      FormData form = (FormData) req.parseContent();

      if ((form == null) || !form.getIsMultiPart()) {
        throw new IllegalArgumentException("Not a multipart request.");
      }

      if (categoryId != null) {
        for (FormData.FormField field : form.getFields()) {
          if (field.getIsFile()) {
            InputStream inputStream = field.getInputStream();
            this.categoriesApi.postCategoryLogoByCategoryId(
              categoryId,
              inputStream,
              field.getFilename()
            );
            break;
          }
        }

        model.put(
          "logos",
          this.categoriesApi.getCategoryLogoByCategoryId(categoryId)
        );
      } else {
        model.put("logos", null);
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
