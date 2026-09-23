package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.AutoUploadApi;
import io.swagger.model.PagedAutoUploadConfiguration;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco Web Script that handles the HTTP {@code GET} request for retrieving the paginated list
 * of auto-upload configurations of a given Interest Group (IG).
 *
 * <p>The IG is identified by the {@code id} URL template variable. Access is restricted to group
 * administrators of the target IG; requests from other users are rejected with an HTTP
 * {@code 403 Forbidden} response.
 *
 * <p>Pagination is controlled through two optional request parameters:
 *
 * <ul>
 *   <li>{@code page} - the 1-based page number to return (must be {@code > 0}; defaults to 1).
 *   <li>{@code limit} - the maximum number of entries per page (must be {@code >= 0}; defaults to
 *       0, meaning no limit).
 * </ul>
 *
 * <p>On success the returned model exposes the retrieved configuration entries under the
 * {@code autouploads} key and the total number of matching entries under the {@code total} key,
 * which are then rendered by the associated FreeMarker template.
 *
 * @author schwerr
 */
public class AutoUploadsGet extends DeclarativeWebScript {

  /** Logger used to report access-denied and processing errors for this endpoint. */
  private static final Log logger = LogFactory.getLog(AutoUploadsGet.class);

  /** API providing access to the auto-upload configuration entries of an Interest Group. */
  @Autowired
  private AutoUploadApi autoUploadApi;

  /** Service used to verify that the current user is a group administrator of the target IG. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Executes the endpoint logic: validates the caller's permissions and pagination parameters and
   * returns the requested page of auto-upload configurations for the Interest Group.
   *
   * <p>If the current user is not a group administrator of the IG, or if any error occurs while
   * processing the request, the response {@link Status} is updated accordingly (403 for access
   * denied, 406 otherwise) and {@code null} is returned. The multilingual (ML) awareness of the
   * property interceptor is temporarily disabled during retrieval and restored afterwards.
   *
   * @param req the web script request; supplies the {@code id} template variable and the optional
   *     {@code page} and {@code limit} parameters
   * @param status the response status, updated to signal forbidden or error conditions
   * @param cache the cache control settings for the response
   * @return a model map containing the {@code autouploads} entries and the {@code total} count on
   *     success, or {@code null} if access is denied or an error occurs
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String igId = templateVars.get("id");
    boolean mlAware = MLPropertyInterceptor.isMLAware();

    try {
      if (!currentUserPermissionCheckerService.isGroupAdmin(igId)) {
        throw new AccessDeniedException("No access on IG: " + igId);
      }

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

      PagedAutoUploadConfiguration pagedItems =
        this.autoUploadApi.getAutoUploadEntries(igId, startRecord, limit);

      model.put("autouploads", pagedItems.getData());
      model.put("total", pagedItems.getTotal());
    } catch (AccessDeniedException ade) {
      logger.error("Access denied for IG: " + igId, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Error processing auto upload request for IG: " + igId, e);
      status.setCode(Status.STATUS_NOT_ACCEPTABLE);
      status.setMessage(e.getMessage());
      status.setException(e);
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
   * @param limit the current limit value, used only for the error message
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
   * Parses the {@code page} request parameter into an integer, defaulting to 1 when the value is
   * {@code null} or empty.
   *
   * @param pageStr the raw {@code page} parameter value to parse
   * @param page the current page value, used only for the error message
   * @return the parsed page value, or 1 if {@code pageStr} is {@code null} or empty
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
