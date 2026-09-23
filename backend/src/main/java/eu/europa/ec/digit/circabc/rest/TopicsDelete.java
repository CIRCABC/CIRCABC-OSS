package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.TopicsApi;
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
 * Alfresco Web Script endpoint that deletes a discussion topic.
 *
 * <p>This declarative web script backs an HTTP {@code DELETE} request (as implied by the
 * {@code Delete} suffix in the class name). The target topic is identified by the {@code id}
 * template variable extracted from the request URL.</p>
 *
 * <p>Behaviour:</p>
 * <ul>
 *   <li>Verifies that the current user holds Alfresco delete permission on the node referenced by
 *       {@code id}; otherwise the request is rejected with HTTP 403 (Forbidden).</li>
 *   <li>Records audit information before the deletion via {@link #recordBeforeDelete(String)}
 *       (inherited from {@link CircabcDeclarativeWebScript}).</li>
 *   <li>Delegates the actual removal to {@link TopicsApi#topicsIdDelete(String)}.</li>
 * </ul>
 *
 * <p>On success the response model contains a {@code message} entry set to {@code "ok"}. Failure
 * cases are mapped to appropriate HTTP status codes (403 for access denial, 400 for an invalid
 * node reference, and 500 for any other error).</p>
 *
 * @see CircabcDeclarativeWebScript
 * @see TopicsApi
 */
public class TopicsDelete extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(TopicsDelete.class);

  /** API providing the business operations for topics, including deletion. */
  @Autowired
  private TopicsApi topicsApi;

  /** Service used to check whether the current user has the required Alfresco permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the topic deletion request.
   *
   * <p>Extracts the {@code id} template variable from the request URL, checks that the current
   * user has Alfresco delete permission on that node, records audit information, and then deletes
   * the topic. Errors are translated into HTTP status codes and result in a {@code null} model
   * with a redirect status.</p>
   *
   * @param req the web script request; must provide an {@code id} template variable identifying
   *     the topic to delete
   * @param status the response status object, updated with an error code and message when the
   *     deletion cannot be completed
   * @param cache the cache control object for the response
   * @return a model map containing a {@code message} entry set to {@code "ok"} on success, or
   *     {@code null} when the operation fails and an error status has been set
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
        !this.currentUserPermissionCheckerService.hasAlfrescoDeletePermission(
          id
        )
      ) {
        throw new AccessDeniedException(
          "Cannot get the replies of the topic, not enough permissions"
        );
      }
      this.recordBeforeDelete(id);
      this.topicsApi.topicsIdDelete(id);
      model.put("message", "ok");
    } catch (AccessDeniedException ade) {
      if (logger.isErrorEnabled()) {
        logger.error("Access denied when deleting topic", ade);
      }
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      if (logger.isErrorEnabled()) {
        logger.error("Invalid node reference when deleting topic", inre);
      }
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error when deleting topic", e);
      }
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      return null; // NOSONAR
    }

    return model;
  }
}
