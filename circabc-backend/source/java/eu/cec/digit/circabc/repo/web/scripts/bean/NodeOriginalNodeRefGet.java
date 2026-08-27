package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.NodesApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Reads the original (source) node reference (ci:originalNodeRef) of a node.
 * Restricted to CIRCABC administrators.
 */
public class NodeOriginalNodeRefGet extends DeclarativeWebScript {

  static final Log logger = LogFactory.getLog(NodeOriginalNodeRefGet.class);

  private NodesApi nodesApi;
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
      status.setCode(HttpServletResponse.SC_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error("Access denied", ade);
      }
      return null;
    } catch (InvalidNodeRefException inre) {
      status.setCode(HttpServletResponse.SC_NOT_FOUND);
      status.setMessage("Node with given ID does not exist!");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error(inre.getMessage(), inre);
      }
      return null;
    }
    return model;
  }

  public void setNodesApi(NodesApi nodesApi) {
    this.nodesApi = nodesApi;
  }

  public void setCurrentUserPermissionCheckerService(
    CurrentUserPermissionCheckerService currentUserPermissionCheckerService
  ) {
    this.currentUserPermissionCheckerService =
      currentUserPermissionCheckerService;
  }
}
