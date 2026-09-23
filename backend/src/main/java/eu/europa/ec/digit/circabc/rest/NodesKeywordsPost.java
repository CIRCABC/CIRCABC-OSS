package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.KeywordsApi;
import io.swagger.model.KeywordDefinition;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.KeywordJsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco declarative web script backing the HTTP {@code POST} endpoint that adds a keyword to a
 * given node.
 *
 * <p>The class name follows the CIRCABC {@code <Entity><Method>} convention: {@code Nodes} +
 * {@code Keywords} + {@code Post} maps to a {@code POST} request on a node's keywords collection
 * (e.g. {@code /nodes/{id}/keywords}). The target node is identified by the {@code id} template
 * variable taken from the request URL, and the keyword to add is parsed from the JSON request
 * body.</p>
 *
 * <p>Before performing the operation, the caller must hold at least the
 * {@link LibraryPermissions#LIBEDITONLY} permission on the node; otherwise the request is rejected
 * with an HTTP {@code 403 Forbidden}. Invalid node references or malformed request bodies result in
 * an HTTP {@code 400 Bad Request}, and any other failure produces an HTTP
 * {@code 500 Internal Server Error}.</p>
 */
public class NodesKeywordsPost extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(NodesKeywordsPost.class);

  /**
   * API used to persist the keyword against the target node.
   */
  @Autowired
  private KeywordsApi keywordsApi;

  /**
   * Service used to verify that the current user holds the required library permissions on the
   * target node before the keyword is added.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the {@code POST} request that adds a keyword to the node identified by the {@code id}
   * URL template variable.
   *
   * <p>The method verifies that the current user has the {@link LibraryPermissions#LIBEDITONLY}
   * permission on the node, parses the keyword definition from the JSON request body and delegates
   * the persistence to {@link KeywordsApi#nodesIdKeywordsPost(String, KeywordDefinition)}. On
   * failure the HTTP status on the supplied {@link Status} is set accordingly and {@code null} is
   * returned to trigger the error redirect.</p>
   *
   * @param req the web script request; provides the {@code id} template variable and the JSON body
   *            describing the keyword to add
   * @param status the response status, updated with the appropriate HTTP error code when the
   *               operation cannot be completed
   * @param cache the response cache directives (unused)
   * @return an empty model map on success, or {@code null} when an error status has been set
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
        !this.currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
          id,
          LibraryPermissions.LIBEDITONLY
        )
      ) {
        throw new AccessDeniedException(
          "Impossible to add the keyword of the document, not enough permissions"
        );
      }

      KeywordDefinition body = KeywordJsonParser.parseJsonFullKeyword(req);
      this.keywordsApi.nodesIdKeywordsPost(id, body);
    } catch (AccessDeniedException ade) {
      logger.error("Access denied when posting keywords for node " + id, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException | ParseException | IOException inre) {
      logger.error(
        "Invalid request when posting keywords for node " + id,
        inre
      );
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Unexpected error when posting keywords for node " + id, e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      return null; // NOSONAR
    }

    return model;
  }
}
