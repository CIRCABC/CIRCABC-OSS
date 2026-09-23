package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

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
 * Alfresco Web Script endpoint that reports the remaining size (in bytes) still
 * available for attachments on a newsgroup post/topic.
 *
 * <p>As implied by the {@code Get} prefix of the class name, this endpoint is
 * bound to the HTTP {@code GET} method. It expects the target node's identifier
 * to be supplied as the {@code id} template variable in the request URL.
 *
 * <p>Behaviour:
 * <ul>
 *   <li>Unless the {@code id} is the literal string {@code "null"}, the current
 *       user must hold the {@link NewsGroupPermissions#NWSACCESS} permission on
 *       the referenced node; otherwise the endpoint responds with HTTP 403
 *       (Forbidden).</li>
 *   <li>On success, the remaining attachment size is placed into the response
 *       model under the {@code size} key and rendered by the associated
 *       FreeMarker template.</li>
 *   <li>An invalid node type results in HTTP 400 (Bad Request); any other
 *       unexpected failure also results in HTTP 400.</li>
 * </ul>
 */
public class GetPostAttachmentRemainingSize extends DeclarativeWebScript {

  /** Logger used to record errors that occur while handling the request. */
  static final Log logger = LogFactory.getLog(
    GetPostAttachmentRemainingSize.class
  );

  /** API used to compute the remaining attachment size for a topic/post. */
  @Autowired
  private TopicsApi topicsApi;

  /** Service used to verify the current user's newsgroup permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the incoming GET request by resolving the target node from the
   * {@code id} template variable, verifying that the current user has the
   * required newsgroup access permission, and computing the remaining size
   * available for attachments.
   *
   * @param req the web script request; must provide an {@code id} template
   *     variable identifying the target node
   * @param status the response status, updated to an error code (403 or 400)
   *     when the request cannot be fulfilled
   * @param cache the response cache directives
   * @return a model map containing the {@code size} (remaining bytes) on
   *     success, or {@code null} when an error occurred and the status has been
   *     set accordingly
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

      if (
        !"null".equals(id) &&
        !this.currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
          id,
          NewsGroupPermissions.NWSACCESS
        )
      ) {
        throw new AccessDeniedException(
          "Cannot get size, not enough permissions"
        );
      }

      long size = this.topicsApi.getAttachmentsRemainingSize(id);

      model.put("size", size);
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidTypeException ite) {
      logger.error(ERROR_OCCURRED, ite);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad noderef type");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception inre) {
      logger.error(
        "An unexpected exception occurred: " + inre.getMessage(),
        inre
      );
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    }

    return model;
  }
}
