package eu.europa.ec.digit.circabc.rest;

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
 * Alfresco web script endpoint that handles HTTP {@code GET} requests for a single
 * CIRCABC category.
 *
 * <p>The endpoint is invoked with a category identifier supplied as the {@code id}
 * template variable in the URL and an optional {@code language} request parameter:
 *
 * <ul>
 *   <li>When {@code language} is omitted the node is read in multilingual-aware mode
 *       (all localized property values are returned).</li>
 *   <li>When {@code language} is provided the content and UI locale are set to that
 *       language and only the corresponding localized values are resolved.</li>
 * </ul>
 *
 * <p>Before returning any data the endpoint verifies that the current user has
 * Alfresco read permission on the requested node. The resulting category is placed
 * in the model under the {@code category} key for the FreeMarker template to render.
 * Failures are reported through the web script {@link Status} object rather than by
 * propagating exceptions (see {@link #executeImpl(WebScriptRequest, Status, Cache)}).
 */
public class CategoryGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(CategoryGet.class);

  /**
   * API used to retrieve category data from the underlying repository.
   */
  @Autowired
  private CategoriesApi categoriesApi;

  /**
   * Service used to check that the current user holds the required Alfresco
   * permissions on the requested node before its data is exposed.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Returns the API used to retrieve category data.
   *
   * @return the categoriesApi
   */
  public CategoriesApi getCategoriesApi() {
    return this.categoriesApi;
  }

  /**
   * Sets the API used to retrieve category data.
   *
   * @param categoriesApi the categoriesApi to set
   */
  public void setCategoriesApi(CategoriesApi categoriesApi) {
    this.categoriesApi = categoriesApi;
  }

  /**
   * Executes the GET request for a single category.
   *
   * <p>Reads the {@code id} template variable and the optional {@code language}
   * request parameter, configures multilingual awareness and the content/UI locale
   * accordingly, checks the current user's read permission on the node and, when
   * granted, adds the retrieved category to the model under the {@code category} key.
   * The original multilingual-aware state is always restored before returning.
   *
   * <p>Error conditions are translated into HTTP status codes and result in a
   * {@code null} model:
   *
   * <ul>
   *   <li>{@link AccessDeniedException} &rarr; {@link Status#STATUS_FORBIDDEN}</li>
   *   <li>{@link InvalidNodeRefException} &rarr; {@link Status#STATUS_BAD_REQUEST}</li>
   *   <li>any other exception &rarr; {@link Status#STATUS_INTERNAL_SERVER_ERROR}</li>
   * </ul>
   *
   * @param req the web script request; provides the {@code id} template variable and
   *     the optional {@code language} parameter
   * @param status the web script status, updated with an HTTP error code and message
   *     when the request cannot be fulfilled
   * @param cache the web script response cache directives
   * @return a model map containing the resolved {@code category}, or {@code null} when
   *     an error occurred and an error status was set
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
        !this.currentUserPermissionCheckerService.hasAlfrescoReadPermission(
          categoryId
        )
      ) {
        throw new AccessDeniedException("No access on node:" + categoryId);
      }

      model.put("category", this.categoriesApi.categoriesIdGet(categoryId));
    } catch (AccessDeniedException ade) {
      logger.error("Access denied for category: " + categoryId, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error("Invalid node reference for category: " + categoryId, inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Unexpected error getting category: " + categoryId, e);
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
