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
 * Alfresco web script endpoint that handles HTTP {@code GET} requests to list the
 * pending group deletion requests for a given category.
 *
 * <p>The endpoint resolves the target category from the {@code id} template variable
 * in the URL and returns the group deletion requests belonging to that category. Only
 * a category administrator is allowed to list these requests; otherwise the endpoint
 * responds with an HTTP {@code 403 Forbidden} status.
 *
 * <p>Key inputs:
 * <ul>
 *   <li>{@code id} (URL template variable) &mdash; the identifier of the category.</li>
 *   <li>{@code language} (query parameter, optional) &mdash; when supplied, disables
 *       multilingual awareness and renders content for the given locale; when omitted,
 *       multilingual (ML) awareness is enabled.</li>
 *   <li>{@code limit} (query parameter, optional) &mdash; maximum number of requests to
 *       return; defaults to {@code 10}.</li>
 *   <li>{@code page} (query parameter, optional) &mdash; zero-based page index for
 *       pagination; defaults to {@code 0}.</li>
 *   <li>{@code filter} (query parameter, optional) &mdash; free-text filter applied to
 *       the returned requests.</li>
 * </ul>
 *
 * <p>The resulting model exposes the retrieved requests under the {@code requests} key,
 * which is rendered by the associated FreeMarker template.
 */
public class GroupDeletionRequestGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GroupDeletionRequestGet.class);

  /**
   * Service used to verify that the current user has the required category
   * administrator permissions before listing group deletion requests.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * API providing access to category-related operations, including retrieval of
   * the group deletion requests for a category.
   */
  @Autowired
  private CategoriesApi categoriesApi;

  /**
   * Executes the web script, retrieving the group deletion requests for the category
   * identified by the {@code id} URL template variable.
   *
   * <p>Locale/multilingual handling is derived from the {@code language} parameter, and
   * pagination is controlled by the optional {@code limit}, {@code page} and
   * {@code filter} query parameters. Access is restricted to category administrators.
   * On error, the appropriate HTTP status is set on the response and {@code null} is
   * returned; the original multilingual awareness flag is always restored.
   *
   * @param req the web script request; provides the {@code id} template variable and the
   *     {@code language}, {@code limit}, {@code page} and {@code filter} query parameters
   * @param status the web script response status, updated to signal errors such as
   *     {@code 403 Forbidden} or {@code 400 Bad Request}
   * @param cache the cache control settings for the response
   * @return a model map containing the {@code requests} entry with the retrieved group
   *     deletion requests, or {@code null} if an error occurred and an error status was set
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
      if (!currentUserPermissionCheckerService.isCategoryAdmin(categoryId)) {
        throw new AccessDeniedException(
          "Impossible to list the category group requests. Not enough permissions"
        );
      }

      String limit = req.getParameter("limit");
      int limitInt = 10;
      if (limit != null) {
        limitInt = Integer.parseInt(limit);
      }

      String page = req.getParameter("page");
      int pageInt = 0;
      if (page != null) {
        pageInt = Integer.parseInt(page);
      }

      String filter = req.getParameter("filter");

      model.put(
        "requests",
        this.categoriesApi.categoriesIdGroupDeleteRequestsGet(
          categoryId,
          limitInt,
          pageInt,
          filter
        )
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
    } catch (NumberFormatException e) {
      logger.error(ERROR_OCCURRED, e);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request query parameters");
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }
}
