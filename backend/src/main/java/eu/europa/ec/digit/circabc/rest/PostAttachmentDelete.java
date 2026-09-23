package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.TopicsApi;
import io.swagger.model.permissions.NewsGroupPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST webscript endpoint that deletes a single attachment from a newsgroup post
 * (topic/message node).
 *
 * <p>The class name follows the {@code <Entity><Method>} convention, implying an HTTP
 * {@code DELETE} request. The endpoint expects two URL template variables:
 * {@code id} (the node reference of the post that owns the attachment) and
 * {@code attachmentId} (the identifier of the attachment to remove).
 *
 * <p>Before removing the attachment, the caller must hold the
 * {@link NewsGroupPermissions#NWSMODERATE} permission on the post node; otherwise the
 * request is rejected with an HTTP {@code 403 Forbidden}. On success a JSON model
 * containing a single {@code "message"} entry set to {@code "ok"} is returned. Invalid
 * node types result in an HTTP {@code 400 Bad Request}, as do any other unexpected
 * errors.
 */
public class PostAttachmentDelete extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(PostAttachmentDelete.class);

  /** API used to perform topic/post operations, including attachment removal. */
  @Autowired
  private TopicsApi topicsApi;

  /** Service used to verify that the current user holds the required newsgroup permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the request to delete an attachment from a post.
   *
   * <p>Reads the {@code id} and {@code attachmentId} URL template variables, checks that
   * the current user has the {@link NewsGroupPermissions#NWSMODERATE} permission on the
   * post, and delegates the removal to {@link TopicsApi#removeAttachment(String, String)}.
   *
   * @param req the web script request, providing the URL template variables
   * @param status the response status object; set to {@code 403} on permission failure
   *     and {@code 400} on invalid type or other errors
   * @param cache the web script response cache directives
   * @return a model map containing a {@code "message"} entry set to {@code "ok"} on
   *     success, or {@code null} when an error occurs and the status is set with a
   *     redirect
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    try {
      String id = templateVars.get("id");
      String attachmentId = templateVars.get("attachmentId");

      if (
        !this.currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
          id,
          NewsGroupPermissions.NWSMODERATE
        )
      ) {
        throw new AccessDeniedException(
          "Cannot remove the attachment of the node, not enough permissions"
        );
      }

      this.topicsApi.removeAttachment(id, attachmentId);

      model.put("message", "ok");
    } catch (AccessDeniedException ade) {
      logger.error("Access denied when deleting post attachment", ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidTypeException ite) {
      logger.error("Invalid type when deleting post attachment", ite);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad noderef type");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception inre) {
      logger.error("Error when deleting post attachment", inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    }

    return model;
  }
}
