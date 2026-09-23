package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.KeywordsApi;
import io.swagger.model.permissions.LibraryPermissions;
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
 * REST webscript endpoint that removes a single keyword association from a node.
 *
 * <p>The class name follows the CIRCABC convention {@code <Entity><Method>}, so this endpoint
 * handles the HTTP {@code DELETE} method for the {@code nodes/{id}/keywords/{keywordId}} resource.
 * It expects two URL template variables:
 *
 * <ul>
 *   <li>{@code id} — the identifier of the node whose keyword is being removed;
 *   <li>{@code keywordId} — the identifier of the keyword to detach from the node.
 * </ul>
 *
 * <p>Before performing the deletion the endpoint verifies that the current user holds at least the
 * {@link io.swagger.model.permissions.LibraryPermissions#LIBEDITONLY LIBEDITONLY} library
 * permission on the target node. The actual removal is delegated to
 * {@link io.swagger.api.KeywordsApi#nodesIdKeywordsKeywordIdDelete(String, String)}.
 *
 * <p>On failure the endpoint sets an appropriate HTTP status and redirects instead of rendering the
 * model: {@code 403 Forbidden} when permissions are insufficient, {@code 400 Bad Request} for an
 * invalid node reference, and {@code 500 Internal Server Error} for any other error.
 */
public class NodesKeywordsDelete extends CircabcDeclarativeWebScript {

  /** Reusable log-message fragment used to join the node id and keyword id in error messages. */
  private static final String AND_KEYWORD_ID = " and keywordId: ";

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(NodesKeywordsDelete.class);

  /** API used to perform keyword operations, including detaching a keyword from a node. */
  @Autowired
  private KeywordsApi keywordsApi;

  /** Service used to check whether the current user holds the required library permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the keyword deletion request.
   *
   * <p>Reads the {@code id} and {@code keywordId} template variables from the request, checks that
   * the current user has the {@link io.swagger.model.permissions.LibraryPermissions#LIBEDITONLY}
   * permission on the node, and delegates the removal to {@link KeywordsApi}. Errors are logged and
   * translated into HTTP status codes with a redirect.
   *
   * @param req the web script request, providing the {@code id} and {@code keywordId} URL template
   *     variables
   * @param status the response status, updated to an error code and flagged for redirect when the
   *     operation fails
   * @param cache the cache control for the response
   * @return an empty model map on success, or {@code null} when an error occurred and a redirect
   *     status has been set
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
    String keywordId = templateVars.get("keywordId");

    try {
      if (
        !this.currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
          id,
          LibraryPermissions.LIBEDITONLY
        )
      ) {
        throw new AccessDeniedException(
          "Impossible to remove the keyword of the document, not enough permissions"
        );
      }

      this.keywordsApi.nodesIdKeywordsKeywordIdDelete(id, keywordId);
    } catch (AccessDeniedException ade) {
      logger.error(
        "Access denied when trying to delete keyword for id: " +
          id +
          AND_KEYWORD_ID +
          keywordId,
        ade
      );
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error(
        "Invalid node reference when trying to delete keyword for id: " +
          id +
          AND_KEYWORD_ID +
          keywordId,
        inre
      );
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(
        "Error when trying to delete keyword for id: " +
          id +
          AND_KEYWORD_ID +
          keywordId,
        e
      );
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      return null; // NOSONAR
    }

    return model;
  }
}
