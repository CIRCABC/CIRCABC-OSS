package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.TopicsApi;
import io.swagger.model.permissions.NewsGroupPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco webscript endpoint that adds a link attachment to a newsgroup post.
 *
 * <p>This endpoint handles an HTTP {@code POST} request (as implied by the
 * {@code Add} suffix in the class name) that attaches an existing node,
 * identified by {@code destinationId}, as a link attachment to the post
 * identified by {@code id}.
 *
 * <p>Both identifiers are read from the URL template variables of the request:
 * <ul>
 *   <li>{@code id} &mdash; the identifier of the target post that will receive
 *       the attachment.</li>
 *   <li>{@code destinationId} &mdash; the identifier of the node to link as an
 *       attachment.</li>
 * </ul>
 *
 * <p>Before performing the operation the endpoint verifies that the current
 * user holds the {@link NewsGroupPermissions#NWSPOST} permission on the post.
 * If the permission is missing an {@link AccessDeniedException} is raised and
 * the response is set to {@code 403 Forbidden}; any other failure results in a
 * {@code 406 Not Acceptable} response.
 *
 * @author schwerr
 */
public class PostAttachmentLinkAdd extends DeclarativeWebScript {

  /** Logger used to report access-denied and processing errors. */
  private static final Logger logger = LoggerFactory.getLogger(
    PostAttachmentLinkAdd.class
  );

  /** API used to perform topic/post operations such as adding attachments. */
  @Autowired
  private TopicsApi topicsApi;

  /** Service used to verify the current user's newsgroup permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Adds a link attachment to the target post after checking permissions.
   *
   * <p>The post identifier ({@code id}) and the attachment node identifier
   * ({@code destinationId}) are extracted from the request's URL template
   * variables. The current user must hold the
   * {@link NewsGroupPermissions#NWSPOST} permission on the post; otherwise the
   * response status is set to {@code 403 Forbidden}. Any other error sets the
   * status to {@code 406 Not Acceptable}. The multilingual property
   * interceptor is temporarily disabled while the attachment is created and
   * restored afterwards.
   *
   * @param req the web script request providing the {@code id} and
   *            {@code destinationId} template variables
   * @param status the response status, updated when an error occurs
   * @param cache the cache directives for the response
   * @return an empty model map on success, or {@code null} when an error
   *         occurred and the response was redirected to the status template
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
    String destinationId = templateVars.get("destinationId");

    boolean mlAware = MLPropertyInterceptor.isMLAware();

    try {
      if (
        !this.currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
          id,
          NewsGroupPermissions.NWSPOST
        )
      ) {
        throw new AccessDeniedException(
          "Cannot add attachment, not enough permissions"
        );
      }

      MLPropertyInterceptor.setMLAware(false);

      topicsApi.addLinkAttachment(id, destinationId);
    } catch (AccessDeniedException ade) {
      logger.error("Access denied when adding link attachment", ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Error when adding link attachment", e);
      status.setCode(Status.STATUS_NOT_ACCEPTABLE);
      status.setMessage(e.getMessage());
      status.setException(e);
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }
}
