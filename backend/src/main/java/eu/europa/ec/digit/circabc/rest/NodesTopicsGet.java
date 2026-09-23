package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.ContentApi;
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
 * REST webscript endpoint that handles HTTP {@code GET} requests to retrieve the
 * topics associated with a given content node.
 *
 * <p>The endpoint expects the target node identifier to be supplied as the
 * {@code id} template variable in the request URL. Before returning any data it
 * verifies that the current user holds Alfresco read permission on that node.
 * When the check passes, the collection of topics for the node (obtained from
 * {@link ContentApi#contentIdTopicsGet(String)}) is placed in the response model
 * under the {@code topics} key and rendered by the associated FreeMarker
 * template.</p>
 *
 * <p>Error handling maps failures to HTTP status codes: a missing read
 * permission results in {@code 403 Forbidden}, an invalid node reference results
 * in {@code 400 Bad Request}, and any other unexpected failure results in
 * {@code 500 Internal Server Error}.</p>
 */
public class NodesTopicsGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(NodesTopicsGet.class);

  /**
   * API used to look up the topics attached to a content node.
   */
  @Autowired
  private ContentApi contentApi;

  /**
   * Service used to verify that the current user has the required Alfresco
   * permissions on the requested node.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Executes the webscript: resolves the {@code id} template variable, checks
   * that the current user has read permission on the corresponding node and,
   * when authorized, populates the response model with the node's topics.
   *
   * @param req the web script request; must contain an {@code id} template
   *     variable identifying the target node
   * @param status the web script response status, updated with an error code,
   *     message and redirect flag when the request cannot be fulfilled
   * @param cache the web script response cache directives
   * @return a model map containing the {@code topics} entry on success, or
   *     {@code null} when an error occurred and the status has been set to a
   *     failure code
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
        !this.currentUserPermissionCheckerService.hasAlfrescoReadPermission(id)
      ) {
        throw new AccessDeniedException(
          "cannot get the topics, not enough permissions"
        );
      }

      model.put("topics", this.contentApi.contentIdTopicsGet(id));
    } catch (AccessDeniedException ade) {
      logger.error("Access denied getting topics: " + ade.getMessage(), ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error(
        "Invalid node reference for topics: " + inre.getMessage(),
        inre
      );
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Unexpected error getting topics: " + e.getMessage(), e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      return null; // NOSONAR
    }

    return model;
  }
}
