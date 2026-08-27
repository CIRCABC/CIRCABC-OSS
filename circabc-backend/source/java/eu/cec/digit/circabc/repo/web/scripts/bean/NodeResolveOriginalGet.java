package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.NodesApi;
import io.swagger.model.Node;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletResponse;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Resolves the migrated node whose ci:originalNodeRef matches the given original
 * node id (workspace://SpacesStore uuid). Available to any user, but the current
 * user must have READ permission on the resolved node:
 * <ul>
 *   <li>404 - no node references the given original id</li>
 *   <li>403 - a node is found but the current user has no READ permission</li>
 * </ul>
 */
public class NodeResolveOriginalGet extends DeclarativeWebScript {

  static final Log logger = LogFactory.getLog(NodeResolveOriginalGet.class);

  private static final Pattern UUID_PATTERN = Pattern.compile(
    "^[0-9a-fA-F-]{36}$"
  );

  private NodesApi nodesApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(1, 1.0f);
    String originalId = req
      .getServiceMatch()
      .getTemplateVars()
      .get("originalId");

    if (originalId == null || !UUID_PATTERN.matcher(originalId).matches()) {
      status.setCode(HttpServletResponse.SC_BAD_REQUEST);
      status.setMessage("Invalid original node id");
      status.setRedirect(true);
      return null;
    }

    boolean mlAware = MLPropertyInterceptor.isMLAware();
    MLPropertyInterceptor.setMLAware(true);
    try {
      Node n = this.nodesApi.resolveByOriginalNodeRef(originalId);
      if (n == null) {
        status.setCode(HttpServletResponse.SC_NOT_FOUND);
        status.setMessage("No migrated node for the given original node ref");
        status.setRedirect(true);
        return null;
      }
      model.put("n", n);
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
      status.setMessage("No migrated node for the given original node ref");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error(inre.getMessage(), inre);
      }
      return null;
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
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
