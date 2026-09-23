package eu.europa.ec.digit.circabc.rest;

import eu.europa.ec.digit.circabc.rest.service.log.LogService;
import io.swagger.model.db.LogSearchResultDAO;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST endpoint that retrieves audit log entries for a given CIRCABC node.
 *
 * <p>Backing an HTTP {@code GET} request (as implied by the {@code Get} suffix
 * of the class name), this web script resolves the node referenced by the
 * {@code id} path variable and returns the audit/activity log entries recorded
 * for the corresponding Interest Group. The results are placed in the response
 * model under the {@code logResults} key for rendering by the associated
 * FreeMarker template.</p>
 *
 * <p>Access is restricted: the current user must be a CIRCABC administrator, a
 * category administrator, or a group administrator for the requested node,
 * otherwise the endpoint responds with HTTP 403 (Forbidden).</p>
 *
 * <p>Key inputs:</p>
 * <ul>
 *   <li>{@code id} - path variable identifying the target node.</li>
 *   <li>{@code userId} - optional request parameter filtering by user.</li>
 *   <li>{@code service} - optional request parameter filtering by service.</li>
 *   <li>{@code activity} - optional request parameter filtering by method/activity.</li>
 *   <li>{@code from} / {@code to} - optional request parameters bounding the
 *       date range of the search.</li>
 * </ul>
 */
public class AuditIdGet extends DeclarativeWebScript {

  /** Logger for reporting access-denied and unexpected processing errors. */
  static final Log logger = LogFactory.getLog(AuditIdGet.class);

  /** Service used to search the audit/activity log entries. */
  @Autowired
  private LogService logService;

  /** Alfresco node service used to resolve and inspect the target node. */
  @Autowired
  private NodeService nodeService;

  /** Service used to verify the current user's administrative permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the incoming request: validates the caller's permissions, resolves
   * the target node from the {@code id} path variable, and searches the audit
   * log using the optional filter parameters.
   *
   * <p>Temporarily disables the multilingual property interceptor while
   * resolving node properties, and always restores the previous state before
   * returning. On an {@link AccessDeniedException} the response status is set to
   * {@code 403 Forbidden}; any other failure results in a
   * {@code 406 Not Acceptable} status. In both error cases {@code null} is
   * returned so the framework renders the error status instead of a model.</p>
   *
   * @param req the web script request, providing the {@code id} path variable
   *            and the optional {@code userId}, {@code service},
   *            {@code activity}, {@code from} and {@code to} parameters
   * @param status the response status, updated to reflect access-denied or
   *               processing errors
   * @param cache the response cache control settings
   * @return a model map containing the {@code logResults} list on success, or
   *         {@code null} when an error status has been set
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("id");

    boolean mlAware = MLPropertyInterceptor.isMLAware();

    try {
      MLPropertyInterceptor.setMLAware(false);

      if (
        !this.currentUserPermissionCheckerService.isCircabcAdmin() &&
        !this.currentUserPermissionCheckerService.isCategoryAdmin(id) &&
        !this.currentUserPermissionCheckerService.isGroupAdmin(id)
      ) {
        throw new AccessDeniedException("Access denied:" + id);
      }

      List<LogSearchResultDAO> results;
      NodeRef nodeRef = Converter.createNodeRefFromId(id);

      if (!this.nodeService.exists(nodeRef)) {
        throw new IllegalArgumentException(
          "The item with id '" + id + "' could not be found."
        );
      }

      long igID = (Long) this.nodeService.getProperty(
        nodeRef,
        ContentModel.PROP_NODE_DBID
      );

      String user = req.getParameter("userId").isEmpty()
        ? null
        : req.getParameter("userId");
      String service = req.getParameter("service").isEmpty()
        ? null
        : req.getParameter("service");
      String method = req.getParameter("activity").isEmpty()
        ? null
        : req.getParameter("activity");
      Date fromDate = Converter.convertStringToDate(req.getParameter("from"));
      Date toDate = Converter.convertStringToDate(req.getParameter("to"));

      results = this.logService.search(
        igID,
        user,
        service,
        method,
        fromDate,
        toDate
      );
      model.put("logResults", results);
    } catch (AccessDeniedException ade) {
      logger.error("Access denied error for id: " + id, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(
        "Unexpected error occurred processing audit request for id: " + id,
        e
      );
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
}
