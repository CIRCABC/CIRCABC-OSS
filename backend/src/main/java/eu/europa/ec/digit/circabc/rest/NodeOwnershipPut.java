package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

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
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST webscript endpoint that lets the current user take ownership of a node.
 *
 * <p>The class name follows the CIRCABC {@code <Entity><Method>} convention, so this handles the
 * HTTP {@code PUT} verb on a node's ownership resource (e.g. {@code /nodes/{id}/ownership}). The
 * node is identified by the {@code id} URL template variable.
 *
 * <p>Before delegating to the {@link NodesApi}, the endpoint verifies that the caller holds the
 * "take ownership" permission on the target node. If the check fails an
 * {@link AccessDeniedException} is raised and the response is set to HTTP 403. On success the
 * updated node is returned to the FreeMarker template under the {@code node} model key.
 */
public class NodeOwnershipPut extends CircabcDeclarativeWebScript {

  /** Logger used to report errors occurring while processing the request. */
  static final Log logger = LogFactory.getLog(NodeOwnershipPut.class);

  /** API used to perform the ownership change on the target node. */
  @Autowired
  private NodesApi nodesApi;

  /** Service used to check whether the current user may take ownership of the node. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Takes ownership of the node identified by the {@code id} URL template variable on behalf of the
   * current user.
   *
   * <p>The method first validates that the caller has the "take ownership" permission. If allowed,
   * it invokes {@link NodesApi#nodesIdOwnershipPut(String)} and exposes the resulting node under the
   * {@code node} key. Failures are translated into HTTP status codes: {@code 403 FORBIDDEN} for
   * permission errors, {@code 400 BAD REQUEST} for invalid node references and
   * {@code 500 INTERNAL SERVER ERROR} for any other unexpected error; in those cases {@code null} is
   * returned and the response is marked as a redirect to the error handler.
   *
   * @param req the web script request; must provide the {@code id} template variable
   * @param status the response status, updated with an error code when processing fails
   * @param cache the cache directives for the response
   * @return a model map containing the updated node under the {@code node} key, or {@code null} if
   *     an error occurred
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
    try {
      if (
        !this.currentUserPermissionCheckerService.hasTakeOwnershipPermission(id)
      ) {
        throw new AccessDeniedException(
          "Impossible to take ownership, not enough permission"
        );
      }
      model.put("node", this.nodesApi.nodesIdOwnershipPut(id));
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      logger.error("Error taking ownership - access denied", ade);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error(ERROR_OCCURRED, inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      logger.error("Error taking ownership - invalid node reference", inre);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(ERROR_OCCURRED, e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      logger.error("Unexpected error taking ownership", e);
      return null; // NOSONAR
    }

    return model;
  }
}
