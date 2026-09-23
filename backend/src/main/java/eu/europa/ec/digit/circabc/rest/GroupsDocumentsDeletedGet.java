package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.ArchiveApi;
import io.swagger.model.PagedArchiveNodes;
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
 * Alfresco webscript endpoint that handles HTTP {@code GET} requests for
 * retrieving the deleted (archived) documents of an Interest Group.
 *
 * <p>The endpoint is mapped to an Interest Group identified by the
 * {@code igId} URL template variable and returns a paginated list of the
 * documents that have been moved to the archive for that group. Access is
 * restricted to group administrators; a non-admin caller results in an HTTP
 * {@code 403 Forbidden} response.</p>
 *
 * <p>Supported request parameters:</p>
 * <ul>
 *   <li>{@code language} - optional content locale. When omitted the
 *       response is returned in a multilingual-aware mode; when provided the
 *       content and UI locale are set accordingly.</li>
 *   <li>{@code limit} - optional page size (defaults to {@code 10} when the
 *       parameter is present but empty).</li>
 *   <li>{@code page} - optional page number (defaults to {@code 1} when the
 *       parameter is present but empty).</li>
 *   <li>{@code order} - optional sort order for the returned nodes.</li>
 * </ul>
 *
 * <p>The resulting model exposes {@code data} (the list of archived nodes)
 * and {@code total} (the total number of archived documents).</p>
 */
public class GroupsDocumentsDeletedGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GroupsDocumentsDeletedGet.class);

  /**
   * API used to query the archive (deleted documents) of an Interest Group.
   */
  @Autowired
  private ArchiveApi archiveApi;

  /**
   * Service used to verify that the current user has the required
   * group-administrator permissions before returning the deleted documents.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Retrieves the paginated list of deleted (archived) documents for the
   * Interest Group identified by the {@code igId} URL template variable.
   *
   * <p>The current user must be an administrator of the group; otherwise an
   * HTTP {@code 403 Forbidden} status is set and {@code null} is returned. If
   * the group reference is invalid, an HTTP {@code 400 Bad Request} status is
   * set and {@code null} is returned.</p>
   *
   * @param req the web script request; provides the {@code igId} template
   *     variable and the optional {@code language}, {@code limit},
   *     {@code page} and {@code order} request parameters
   * @param status the response status, updated to {@code 403} or {@code 400}
   *     when the request cannot be fulfilled
   * @param cache the cache control object for the response
   * @return a model map containing {@code data} (the archived nodes) and
   *     {@code total} (the total count), or {@code null} when access is
   *     denied or the node reference is invalid
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);
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

    String limitStr = req.getParameter("limit");
    Integer limit = null;
    if (limitStr != null) {
      limit = (limitStr.isEmpty() ? 10 : Integer.parseInt(limitStr));
    }

    String pageStr = req.getParameter("page");
    Integer page = null;
    if (pageStr != null) {
      page = (pageStr.isEmpty() ? 1 : Integer.parseInt(pageStr));
    }

    String order = req.getParameter("order");

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("igId");

    try {
      if (!this.currentUserPermissionCheckerService.isGroupAdmin(id)) {
        throw new AccessDeniedException(
          "User is not Group admin to get the deleted documents"
        );
      }
      PagedArchiveNodes result = this.archiveApi.groupsIdDocumentsDeletedGet(
        id,
        limit,
        page,
        order
      );
      model.put("data", result.getData());
      model.put("total", result.getTotal());
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
