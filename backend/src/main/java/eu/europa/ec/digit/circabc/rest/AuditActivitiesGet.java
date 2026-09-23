package eu.europa.ec.digit.circabc.rest;

import eu.europa.ec.digit.circabc.rest.service.log.LogService;
import io.swagger.model.db.LogActivityDAO;
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
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco Web Script endpoint that returns the audit activity log for a given
 * CIRCABC node (typically an Interest Group).
 *
 * <p>The class name follows the {@code <Entity><Method>} convention, so this
 * endpoint handles the HTTP <strong>GET</strong> request bound to its webscript
 * descriptor. The node whose activities are requested is identified by the
 * {@code id} template variable extracted from the request URL.</p>
 *
 * <p>Behavior:</p>
 * <ul>
 *   <li>Authorization is enforced: the caller must be a CIRCABC administrator,
 *       or an administrator of the category or group identified by {@code id};
 *       otherwise the endpoint responds with HTTP 403 (Forbidden).</li>
 *   <li>The {@code id} is resolved to a {@link NodeRef}; if the node does not
 *       exist the endpoint responds with HTTP 406 (Not Acceptable).</li>
 *   <li>On success, the model contains an {@code activities} entry holding the
 *       list of {@link LogActivityDAO} records for the node's database id.</li>
 * </ul>
 *
 * <p>Multilingual property interception is temporarily disabled while the node
 * properties are read, and its previous state is restored afterwards.</p>
 */
public class AuditActivitiesGet extends DeclarativeWebScript {

  /** Logger used to report authorization failures and processing errors. */
  static final Log logger = LogFactory.getLog(AuditActivitiesGet.class);

  /** Service used to retrieve the audit activity log entries for a node. */
  @Autowired
  private LogService logService;

  /** Service used to verify the current user's administrative permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /** Alfresco node service used to resolve nodes and read their properties. */
  @Autowired
  private NodeService nodeService;

  /**
   * Handles the GET request by returning the audit activities for the node
   * identified by the {@code id} template variable.
   *
   * <p>Verifies that the current user has administrative rights over the target
   * node, resolves the node and reads its database id, then loads the
   * associated activity log entries. On authorization or processing failures
   * the appropriate error status is set on the response and {@code null} is
   * returned.</p>
   *
   * @param req the incoming web script request; the target node id is read from
   *            the {@code id} template variable
   * @param status the response status object, updated to 403 on access denial
   *               and 406 on other errors
   * @param cache the response cache directives (unused)
   * @return a model map containing the {@code activities} list of
   *         {@link LogActivityDAO} entries, or {@code null} if an error occurred
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

      List<LogActivityDAO> activities = this.logService.getActivitiesById(igID);

      model.put("activities", activities);
    } catch (AccessDeniedException ade) {
      logger.error(
        "Access denied when getting audit activities for id=" + id,
        ade
      );
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Error getting audit activities for id=" + id, e);
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
