package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.TopicsApi;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.model.permissions.NewsGroupPermissions;
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
 * REST webscript endpoint that deletes a single post (message) from a topic.
 *
 * <p>Bound to an HTTP {@code DELETE} request (as implied by the {@code Delete}
 * suffix of the class name), this endpoint removes the post identified by the
 * {@code id} template variable taken from the request URL. A post may live in
 * two locations within the repository: under a Newsgroup topic or under a
 * Library document's topic. Accordingly, the caller is authorized when they
 * either hold a moderating/administrative Newsgroup permission
 * ({@code NWSMODERATE} or {@code NWSADMIN}) on the post, or the post is a
 * document post and the caller holds a full-edit/administrative Library
 * permission ({@code LIBFULLEDIT} or {@code LIBADMIN}).</p>
 *
 * <p>On success the response model contains a {@code message} entry set to
 * {@code "ok"}. On failure the appropriate HTTP status is set on the response
 * (403 for insufficient permissions, 400 for an invalid node reference, 500
 * for any other unexpected error) and no model is returned.</p>
 */
public class PostsDelete extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(PostsDelete.class);

  /** API used to perform the actual deletion of the post. */
  @Autowired
  private TopicsApi topicsApi;

  /** Service used to verify the current user's Newsgroup and Library permissions on the post. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the delete request for a post.
   *
   * <p>Reads the post {@code id} from the request URL template variables,
   * verifies that the current user has the required Newsgroup or Library
   * permissions, records repository state before deletion and then deletes the
   * post through {@link TopicsApi#postsIdDelete(String)}.</p>
   *
   * @param req the incoming webscript request; the post id is expected as the
   *            {@code id} URL template variable
   * @param status the response status, updated with an error code, message and
   *               redirect flag when the deletion cannot be performed
   * @param cache the response cache directives
   * @return a model map containing a {@code message} entry set to {@code "ok"}
   *         on success, or {@code null} when an error occurs (in which case the
   *         error is reflected on {@code status})
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
      // posts can be located in newsgroups/topics or library/documents/topics
      if (
        !(this.currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
            id,
            NewsGroupPermissions.NWSMODERATE,
            NewsGroupPermissions.NWSADMIN
          ) ||
          (this.currentUserPermissionCheckerService.isDocumentPost(id) &&
            (this.currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
                id,
                LibraryPermissions.LIBFULLEDIT,
                LibraryPermissions.LIBADMIN
              ))))
      ) {
        throw new AccessDeniedException(
          "Cannot delete the post, not enough permissions"
        );
      }
      this.recordBeforeDelete(id);
      this.topicsApi.postsIdDelete(id);
      model.put("message", "ok");
    } catch (AccessDeniedException ade) {
      logger.error("Access denied when deleting post with id: " + id, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error(
        "Invalid node reference when deleting post with id: " + id,
        inre
      );
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Unexpected error when deleting post with id: " + id, e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      return null; // NOSONAR
    }

    return model;
  }
}
