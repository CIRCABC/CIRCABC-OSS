package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.NodesApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Sets (creates or updates) the original (source) node reference
 * ({@code ci:originalNodeRef}, via the {@code ci:migrated} aspect) of a node.
 * Restricted to CIRCABC administrators.
 *
 * <p>Handles {@code PUT /circabc/nodes/{id}/originalnoderef} with a JSON body
 * {@code {"originalNodeRef": "..."}}.
 *
 * @see io.swagger.api.NodesApi#setOriginalNodeRef(String, String)
 */
public class NodeOriginalNodeRefPut extends DeclarativeWebScript {

  /** A logger for the class. */
  static final Log logger = LogFactory.getLog(NodeOriginalNodeRefPut.class);

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
      JSONObject json = new JSONObject(req.getContent().getContent());
      String value = json.getString("originalNodeRef");
      this.nodesApi.setOriginalNodeRef(id, value);
      model.put("id", id);
      model.put("originalNodeRef", value);
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
    } catch (JSONException | IOException je) {
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage(
        "Invalid request body: expected { \"originalNodeRef\": \"...\" }"
      );
      status.setRedirect(true);
      logger.error(je.getMessage(), je);
      return null; // NOSONAR
    }
    return model;
  }
}
