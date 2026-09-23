package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.NodesApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
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
 * Removes the {@code ci:migrated} aspect (and its {@code ci:originalNodeRef}
 * property) from a node. Restricted to CIRCABC administrators.
 *
 * <p>Handles {@code DELETE /circabc/nodes/{id}/originalnoderef}.
 *
 * @see io.swagger.api.NodesApi#deleteOriginalNodeRef(String)
 */
public class NodeOriginalNodeRefDelete extends DeclarativeWebScript {

  /** A logger for the class. */
  static final Log logger = LogFactory.getLog(NodeOriginalNodeRefDelete.class);

  @Autowired
  private NodesApi nodesApi;

  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(1, 1.0f);
    String id = req.getServiceMatch().getTemplateVars().get("id");
    try {
      this.currentUserPermissionCheckerService.throwIfNotCircabcAdmin();
      this.nodesApi.deleteOriginalNodeRef(id);
      model.put("success", Boolean.TRUE);
    } catch (AccessDeniedException ade) {
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      logger.error("Access denied", ade);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      status.setCode(Status.STATUS_NOT_FOUND);
      status.setMessage("Node with given ID does not exist!");
      status.setRedirect(true);
      logger.error(inre.getMessage(), inre);
      return null; // NOSONAR
    }
    return model;
  }
}
