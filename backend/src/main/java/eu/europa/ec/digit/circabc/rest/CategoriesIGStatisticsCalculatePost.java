package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.CategoriesApi;
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
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco webscript endpoint that triggers the (re)calculation of Interest
 * Group (IG) statistics for a given category.
 *
 * <p>Mapped to an HTTP {@code POST} request (as implied by the {@code Post}
 * suffix in the class name). The category is identified by the {@code id}
 * template variable extracted from the request URL.
 *
 * <p>Before performing the calculation the endpoint verifies that the current
 * user is an administrator of the target category. If the check fails, or the
 * node reference is invalid, or an unexpected error occurs, the appropriate
 * HTTP status code is set on the response and no model is returned.
 *
 * <p>The calculation is executed with the multilingual (ML) property
 * interceptor temporarily disabled so that raw, non-localized property values
 * are processed; the previous ML-aware state is always restored afterwards.
 *
 * @see CircabcDeclarativeWebScript
 * @see CategoriesApi#calculateIGStatistics(String)
 */
public class CategoriesIGStatisticsCalculatePost
  extends CircabcDeclarativeWebScript
{

  /** Logger used to record access, validation and unexpected errors. */
  static final Log logger = LogFactory.getLog(
    CategoriesIGStatisticsCalculatePost.class
  );

  /** API used to perform the IG statistics calculation for a category. */
  @Autowired
  private CategoriesApi categoriesApi;

  /** Service used to verify that the current user is a category administrator. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Calculates the IG statistics for the category identified by the {@code id}
   * template variable in the request URL.
   *
   * <p>Verifies that the current user is a category administrator, temporarily
   * disables the multilingual property interceptor, delegates the calculation
   * to {@link CategoriesApi#calculateIGStatistics(String)} and always restores
   * the original ML-aware state. On error the corresponding HTTP status code is
   * set on the response and {@code null} is returned.
   *
   * @param req    the web script request; provides the {@code id} template
   *               variable identifying the target category
   * @param status the response status, updated with an error code
   *               ({@code 403}, {@code 400} or {@code 500}) when the operation
   *               cannot be completed
   * @param cache  the response cache directives
   * @return an empty model map on success, or {@code null} if an error occurred
   *         and an error status was set
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
      this.currentUserPermissionCheckerService.throwIfNotCategoryAdmin(id);
      MLPropertyInterceptor.setMLAware(false);

      this.categoriesApi.calculateIGStatistics(id);
    } catch (AccessDeniedException ade) {
      logger.error(
        "Access denied for user when calculating IG statistics for category: " +
          id,
        ade
      );
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error(
        "Invalid node reference when calculating IG statistics for category: " +
          id,
        inre
      );
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(
        "Unexpected error when calculating IG statistics for category: " + id,
        e
      );
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
