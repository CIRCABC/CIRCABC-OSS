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
 * Reads the original (source) node reference ({@code ci:originalNodeRef}) of a node.
 * Restricted to CIRCABC administrators.
 *
 * <p>Handles {@code GET /circabc/nodes/{id}/originalnoderef}. Returns a JSON body
 * {@code {"id": "...", "originalNodeRef": "..."}} where {@code originalNodeRef} is
 * empty when the node is not migrated.
 *
 * @see io.swagger.api.NodesApi#getOriginalNodeRef(String)
 */
public class NodeOriginalNodeRefGet extends DeclarativeWebScript {

  /** A logger for the class. */
  static final Log logger = LogFactory.getLog(NodeOriginalNodeRefGet.class);

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
    Map<String, Object> model = new HashMap<>(2, 1.0f);
    String id = req.getServiceMatch().getTemplateVars().get("id");
    try {
      this.currentUserPermissionCheckerService.throwIfNotCircabcAdmin();
      String ref = this.nodesApi.getOriginalNodeRef(id);
      model.put("id", id);
      model.put("originalNodeRef", ref == null ? "" : ref);
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
