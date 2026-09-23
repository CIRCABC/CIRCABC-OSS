package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.NodesApi;
import io.swagger.model.Node;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.alfresco.repo.node.MLPropertyInterceptor;
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
 * Resolves the migrated node whose {@code ci:originalNodeRef} matches the given original
 * node id (a {@code workspace://SpacesStore} uuid). Available to any user, but the current
 * user must have READ permission on the resolved node:
 * <ul>
 *   <li>404 - no node references the given original id</li>
 *   <li>403 - a node is found but the current user has no READ permission</li>
 * </ul>
 *
 * <p>Handles {@code GET /circabc/nodes/resolve/{originalId}}.
 *
 * @see io.swagger.api.NodesApi#resolveByOriginalNodeRef(String)
 */
public class NodeResolveOriginalGet extends DeclarativeWebScript {

  /** A logger for the class. */
  static final Log logger = LogFactory.getLog(NodeResolveOriginalGet.class);

  private static final Pattern UUID_PATTERN = Pattern.compile(
    "^[0-9a-fA-F-]{36}$"
  );

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
    String originalId = req
      .getServiceMatch()
      .getTemplateVars()
      .get("originalId");

    if (originalId == null || !UUID_PATTERN.matcher(originalId).matches()) {
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Invalid original node id");
      status.setRedirect(true);
      return null; // NOSONAR
    }

    boolean mlAware = MLPropertyInterceptor.isMLAware();
    MLPropertyInterceptor.setMLAware(true);
    try {
      Node n = this.nodesApi.resolveByOriginalNodeRef(originalId);
      if (n == null) {
        status.setCode(Status.STATUS_NOT_FOUND);
        status.setMessage("No migrated node for the given original node ref");
        status.setRedirect(true);
        return null; // NOSONAR
      }
      model.put("n", n);
    } catch (AccessDeniedException ade) {
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      logger.error("Access denied", ade);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      status.setCode(Status.STATUS_NOT_FOUND);
      status.setMessage("No migrated node for the given original node ref");
      status.setRedirect(true);
      logger.error(inre.getMessage(), inre);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
    return model;
  }
}
