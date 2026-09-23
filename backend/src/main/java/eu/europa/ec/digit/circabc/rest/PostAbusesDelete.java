package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.ForumsApi;
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
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST endpoint that clears the abuse reports flagged on a forum post (newsgroup message).
 *
 * <p>Bound to the {@code DELETE /circabc/posts/{id}/abuse} web script (descriptor
 * {@code post.abuse.delete}); the HTTP DELETE method is implied by the {@code Delete} suffix in the
 * class name. The {@code {id}} path variable identifies the post node whose abuse signals are to be
 * removed.
 *
 * <p>The caller must hold the {@link NewsGroupPermissions#NWSMODERATE} permission on the target
 * node; otherwise the request is rejected. On success the endpoint returns a model containing a
 * {@code message} entry set to {@code "ok"}. Failures are translated into HTTP status codes:
 * {@code 403 Forbidden} when permissions are insufficient and {@code 400 Bad Request} for an invalid
 * node type or any other error.
 */
public class PostAbusesDelete extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(PostAbusesDelete.class);

  /** API used to remove the abuse reports associated with the target post. */
  @Autowired
  private ForumsApi forumsApi;

  /** Service used to verify that the current user holds the required newsgroup permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Removes all abuse reports flagged on the post identified by the {@code id} path variable.
   *
   * <p>Verifies that the current user has the {@link NewsGroupPermissions#NWSMODERATE} permission on
   * the node before delegating deletion to {@link ForumsApi#removeAbuses(String)}. When an error
   * occurs the appropriate HTTP status is set on {@code status} and {@code null} is returned so the
   * framework renders the error response.
   *
   * @param req the web script request; the {@code id} template variable identifies the post node
   * @param status the response status, used to signal forbidden or bad-request outcomes
   * @param cache the cache directives for the response
   * @return a model containing a {@code message} entry set to {@code "ok"} on success, or
   *     {@code null} if the operation failed
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
        !this.currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
          id,
          NewsGroupPermissions.NWSMODERATE
        )
      ) {
        throw new AccessDeniedException(
          "cannot remove the abuse of the node, not enough permissions"
        );
      }

      this.forumsApi.removeAbuses(id);

      model.put("message", "ok");
    } catch (AccessDeniedException ade) {
      logger.error("Access denied when removing abuse", ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidTypeException ite) {
      logger.error("Invalid type when removing abuse", ite);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad noderef type");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception inre) {
      logger.error("Error when removing abuse", inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    }

    return model;
  }
}
