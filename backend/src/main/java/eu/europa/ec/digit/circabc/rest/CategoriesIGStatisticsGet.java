package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.CategoriesApi;
import io.swagger.model.PagedStatisticsContents;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco Web Script endpoint that serves the content statistics of the
 * interest groups belonging to a given category.
 *
 * <p>Mapped to an HTTP {@code GET} request (as implied by the {@code Get}
 * suffix in the class name), this endpoint expects the target category
 * identifier as the {@code id} template variable in the URL and supports the
 * following optional request parameters:
 *
 * <ul>
 *   <li>{@code page} - 1-based page number to retrieve (must be &gt; 0;
 *       defaults to {@code 1} when absent).</li>
 *   <li>{@code limit} - maximum number of records per page (must be &gt;= 0;
 *       defaults to {@code 0} when absent).</li>
 * </ul>
 *
 * <p>Access is restricted to CIRCABC administrators or administrators of the
 * requested category. The retrieval is performed with the Multilingual (ML)
 * property interceptor disabled, and the previous ML-aware state is always
 * restored afterwards.
 *
 * <p>The returned model exposes {@code data} (the page of statistics contents)
 * and {@code total} (the overall number of matching records), which are
 * rendered by the associated FreeMarker template. On error the model is
 * {@code null} and an appropriate HTTP status is set:
 * {@code 403 Forbidden} for access denials, {@code 400 Bad Request} for invalid
 * node references, and {@code 500 Internal Server Error} for any other failure.
 */
public class CategoriesIGStatisticsGet extends DeclarativeWebScript {

  /** Logger used to report access, validation and unexpected errors. */
  static final Log logger = LogFactory.getLog(CategoriesIGStatisticsGet.class);

  /** API used to fetch the paged interest group statistics for a category. */
  @Autowired
  private CategoriesApi categoriesApi;

  /** Service used to verify the current user's administrative permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the GET request, validates the caller's permissions and pagination
   * parameters, and builds the response model with the interest group
   * statistics for the requested category.
   *
   * @param req the web script request; provides the {@code id} template
   *     variable and the optional {@code page} and {@code limit} parameters
   * @param status the response status, updated with an error code and message
   *     when the request cannot be fulfilled
   * @param cache the response cache directives
   * @return a model map containing {@code data} (the page of statistics
   *     contents) and {@code total} (the total number of records), or
   *     {@code null} when an error occurs and the status has been set for
   *     redirection
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    boolean mlAware = MLPropertyInterceptor.isMLAware();

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("id");

    try {
      if (!this.currentUserPermissionCheckerService.isCircabcAdmin()) {
        this.currentUserPermissionCheckerService.throwIfNotCategoryAdmin(id);
      }

      MLPropertyInterceptor.setMLAware(false);

      String pageStr = req.getParameter("page");
      int page = 0;
      page = getPage(pageStr, page);

      String limitStr = req.getParameter("limit");
      int limit = 0;
      if ((limitStr != null) && !limitStr.isEmpty()) {
        limit = getLimit(limitStr, limit);
      }

      if (page <= 0) {
        throw new IllegalArgumentException("Values for 'page' must be > 0");
      }

      if (limit < 0) {
        throw new IllegalArgumentException("Values for 'limit' must be >= 0");
      }

      MLPropertyInterceptor.setMLAware(false);

      int startRecord = (page - 1) * limit;

      PagedStatisticsContents contents =
        this.categoriesApi.getIGStatisticsContents(id, startRecord, limit);

      model.put("data", contents.getData());
      model.put("total", contents.getTotal());
    } catch (AccessDeniedException ade) {
      logger.error("Access denied for user when getting IG statistics", ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error("Invalid node reference when getting IG statistics", inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Unexpected error when getting IG statistics", e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }

  /**
   * Parses the {@code limit} request parameter into an integer.
   *
   * @param limitStr the raw {@code limit} parameter value to parse
   * @param limit the current limit value, used only in the error message
   * @return the parsed limit value
   * @throws IllegalArgumentException if {@code limitStr} is not a valid integer
   */
  private int getLimit(String limitStr, int limit) {
    try {
      limit = Integer.parseInt(limitStr);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
        "Wrong numeric value for 'limit': " + limit,
        e
      );
    }
    return limit;
  }

  /**
   * Parses the {@code page} request parameter into an integer, defaulting to
   * {@code 1} when the value is {@code null} or empty.
   *
   * @param pageStr the raw {@code page} parameter value to parse
   * @param page the current page value, used only in the error message
   * @return the parsed page value, or {@code 1} when no value was supplied
   * @throws IllegalArgumentException if {@code pageStr} is not a valid integer
   */
  private int getPage(String pageStr, int page) {
    try {
      page = (((pageStr == null) || pageStr.isEmpty())
        ? 1
        : Integer.parseInt(pageStr));
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
        "Wrong numeric value for 'page': " + page,
        e
      );
    }
    return page;
  }
}
