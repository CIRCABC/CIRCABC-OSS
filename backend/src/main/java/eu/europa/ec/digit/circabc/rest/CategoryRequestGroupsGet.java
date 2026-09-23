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
 * Alfresco Web Script endpoint that lists the pending group membership requests
 * of a given Category.
 *
 * <p>As implied by the {@code Get} suffix in the class name, this endpoint
 * handles an HTTP {@code GET} request. It resolves the target category from the
 * {@code id} template variable in the URL and returns the collection of
 * outstanding group requests for that category under the {@code requests} key of
 * the returned model.</p>
 *
 * <p>Key inputs:</p>
 * <ul>
 *   <li>{@code id} (URL template variable) &ndash; the identifier of the
 *       category whose group requests are queried.</li>
 *   <li>{@code language} (optional request parameter) &ndash; when supplied, the
 *       content and UI locale are set accordingly and multilingual awareness is
 *       disabled so that values are returned in the requested language;
 *       otherwise multilingual (ML) aware mode is enabled.</li>
 *   <li>{@code limit} (optional request parameter) &ndash; maximum number of
 *       requests to return; defaults to {@code 10}.</li>
 *   <li>{@code page} (optional request parameter) &ndash; zero-based page index
 *       for pagination; defaults to {@code 0}.</li>
 *   <li>{@code filter} (optional request parameter) &ndash; a filter expression
 *       applied to the returned requests.</li>
 * </ul>
 *
 * <p>Access is restricted to category administrators: callers who are not
 * administrators of the target category receive an HTTP {@code 403 Forbidden}
 * response. Invalid node references and malformed numeric parameters result in
 * an HTTP {@code 400 Bad Request} response.</p>
 */
public class CategoryRequestGroupsGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(CategoryRequestGroupsGet.class);

  /**
   * API used to retrieve the group membership requests associated with a
   * category.
   */
  @Autowired
  private CategoriesApi categoriesApi;

  /**
   * Service used to verify that the current user has the required category
   * administrator permissions before listing group requests.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the {@code GET} request by returning the pending group requests for
   * the requested category.
   *
   * <p>The category identifier is read from the {@code id} URL template
   * variable, while pagination ({@code limit}, {@code page}), filtering
   * ({@code filter}) and locale ({@code language}) are read from request
   * parameters. The current multilingual awareness setting is preserved and
   * restored once processing completes.</p>
   *
   * @param req the incoming web script request carrying the URL template
   *     variables and query parameters
   * @param status the response status, used to signal error conditions such as
   *     {@code 403 Forbidden} or {@code 400 Bad Request}
   * @param cache the cache directives for the response
   * @return a model map containing the {@code requests} entry with the category
   *     group requests, or {@code null} when an error occurred and the status
   *     has been set to redirect to an error response
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
      // verification if the user is category admin

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
        this.categoriesApi.categoriesIdGroupRequestsGet(
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
