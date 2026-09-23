package eu.europa.ec.digit.circabc.rest;

import eu.europa.ec.digit.circabc.rest.service.log.LogService;
import io.swagger.model.db.LogSearchResultDAO;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco Declarative Web Script that serves an HTTP {@code GET} request to
 * retrieve the audit history (log entries) of a repository item such as a
 * document, event, topic, etc.
 *
 * <p>The item is identified by its {@code id} path variable, which is resolved
 * to an Alfresco {@link NodeRef}. Before returning any data the endpoint checks
 * that the current user has read permission on the node and that the node
 * actually exists. The history is then looked up through the {@link LogService},
 * optionally paginated via the request parameters below.</p>
 *
 * <p>Key inputs:</p>
 * <ul>
 *   <li>{@code id} (path variable) &mdash; the identifier of the item whose
 *       history is requested.</li>
 *   <li>{@code page} (query parameter, optional) &mdash; 1-based page number;
 *       defaults to {@code 1} when absent or empty and must be greater than 0.</li>
 *   <li>{@code limit} (query parameter, optional) &mdash; maximum number of
 *       entries per page; defaults to {@code 25} when absent or empty and must
 *       be greater than or equal to 0. A value of {@code 0} disables pagination
 *       and returns the full history.</li>
 * </ul>
 *
 * <p>The response model exposes {@code logResults} (the list of matching log
 * entries) and {@code total} (the total number of history entries). On an
 * access-denied condition the endpoint responds with HTTP 403 (Forbidden);
 * other unexpected errors result in HTTP 406 (Not Acceptable).</p>
 *
 * @author schwerr
 */
public class AuditIdHistoryGet extends DeclarativeWebScript {

  /** Logger for this web script. */
  // add logger
  static final Log logger = org.apache.commons.logging.LogFactory.getLog(
    AuditIdHistoryGet.class
  );

  /** Service used to look up and count the audit/log history of an item. */
  @Autowired
  private LogService logService;

  /** Alfresco node service used to resolve node properties and existence. */
  @Autowired
  private NodeService nodeService;

  /** Service used to verify the current user's read permission on the item. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the {@code GET} request and builds the response model containing the
   * audit history of the requested item.
   *
   * <p>Reads the {@code id} path variable together with the optional
   * {@code page} and {@code limit} query parameters, validates the current
   * user's read permission and the existence of the node, and retrieves the
   * (optionally paginated) history through the {@link LogService}. During the
   * lookup the multilingual property interceptor is temporarily disabled and
   * restored afterwards.</p>
   *
   * @param req the web script request; provides the {@code id} path variable and
   *            the {@code page} and {@code limit} query parameters
   * @param status the response status, updated to 403 on access denied or 406 on
   *               any other unexpected error
   * @param cache the cache directives for the response
   * @return a model map with {@code logResults} (the list of
   *         {@link LogSearchResultDAO} entries) and {@code total} (the history
   *         entry count), or {@code null} when an error status/redirect has been
   *         set
   * @throws IllegalArgumentException if {@code page} or {@code limit} are not
   *         valid numeric values, if {@code page} is not greater than 0, if
   *         {@code limit} is negative, or if the item cannot be found
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

    String pageStr = req.getParameter("page");
    long page = 0;
    try {
      page = (((pageStr == null) || pageStr.isEmpty())
        ? 1
        : Long.parseLong(pageStr));
    } catch (NumberFormatException e) {
      logger.error("Error parsing page parameter: " + pageStr, e);
      throw new IllegalArgumentException(
        "Wrong numeric value for 'page': " + page,
        e
      );
    }

    String limitStr = req.getParameter("limit");
    long limit = 0;
    try {
      limit = (((limitStr == null) || limitStr.isEmpty())
        ? 25
        : Long.parseLong(limitStr));
    } catch (NumberFormatException e) {
      logger.error("Error parsing limit parameter: " + limitStr, e);
      throw new IllegalArgumentException(
        "Wrong numeric value for 'limit': " + limit,
        e
      );
    }

    if (page <= 0) {
      throw new IllegalArgumentException("Values for 'page' must be > 0");
    }

    if (limit < 0) {
      throw new IllegalArgumentException("Values for 'limit' must be >= 0");
    }

    boolean mlAware = MLPropertyInterceptor.isMLAware();

    try {
      MLPropertyInterceptor.setMLAware(false);

      NodeRef nodeRef = Converter.createNodeRefFromId(id);

      if (
        !this.currentUserPermissionCheckerService.hasAlfrescoReadPermission(id)
      ) {
        throw new AccessDeniedException("No access on node:" + id);
      }

      if (!this.nodeService.exists(nodeRef)) {
        throw new IllegalArgumentException(
          "The item with id '" + id + "' could not be found."
        );
      }

      Long documentId = (Long) this.nodeService.getProperty(
        nodeRef,
        ContentModel.PROP_NODE_DBID
      );

      List<LogSearchResultDAO> results;
      long historyCount;

      if (limit == 0) {
        results = this.logService.getHistory(documentId, id);

        historyCount = results.size();
      } else {
        historyCount = this.logService.countHistory(documentId, id);

        long startRecord = (page - 1) * limit;

        results = this.logService.getHistory(
          documentId,
          id,
          startRecord,
          limit
        );
      }

      model.put("logResults", results);
      model.put("total", historyCount);
    } catch (AccessDeniedException ade) {
      logger.error("Access denied error for id: " + id, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(
        "Unexpected error while getting audit history for id: " + id,
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
