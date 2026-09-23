package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.ForumsApi;
import io.swagger.model.permissions.NewsGroupPermissions;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST webscript endpoint that moderates (verifies) a newsgroup/forum post.
 *
 * <p>As implied by the class name, this endpoint handles an HTTP {@code PUT}
 * request. Given the identifier of a post, it either approves or rejects the
 * post, optionally attaching a rejection reason. The caller must hold the
 * {@link io.swagger.model.permissions.NewsGroupPermissions#NWSMODERATE}
 * permission on the newsgroup (the topic that owns the post); otherwise the
 * request is rejected with an HTTP {@code 403 Forbidden} status.
 *
 * <p>Key inputs:
 * <ul>
 *   <li>{@code id} &ndash; URL template variable identifying the post to verify.</li>
 *   <li>{@code approve} &ndash; request parameter; the string {@code "true"} approves
 *       the post, any other value rejects it.</li>
 *   <li>{@code rejectReason} &ndash; optional request parameter with the reason used
 *       when a post is rejected.</li>
 * </ul>
 *
 * <p>The moderation itself is delegated to {@link io.swagger.api.ForumsApi#verifyPost}.
 *
 * @see CircabcDeclarativeWebScript
 */
public class PostVerifyPut extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(PostVerifyPut.class);

  /** API providing forum/newsgroup operations, including post verification. */
  @Autowired
  private ForumsApi forumsApi;

  /** Service used to check the current user's newsgroup permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /** Alfresco node service used to resolve the post's owning topic/newsgroup. */
  @Autowired
  private NodeService nodeService;

  /**
   * Handles the moderation request for a post.
   *
   * <p>Resolves the newsgroup that owns the post referenced by the {@code id}
   * template variable, verifies that the current user has moderation rights,
   * and then approves or rejects the post based on the {@code approve} request
   * parameter. On success the returned model contains a {@code message} entry
   * set to {@code "ok"}.
   *
   * <p>Errors are handled by setting an appropriate HTTP status on the response
   * and returning {@code null}:
   * <ul>
   *   <li>{@code 403 Forbidden} when the user lacks moderation permissions.</li>
   *   <li>{@code 400 Bad Request} when the referenced node has an invalid type
   *       or any other unexpected error occurs.</li>
   * </ul>
   *
   * @param req the web script request; supplies the {@code id} template
   *            variable and the {@code approve} / {@code rejectReason}
   *            parameters
   * @param status the response status, updated with an error code and message
   *               when moderation cannot be completed
   * @param cache the cache directives for the response (unused)
   * @return a model map containing a {@code "message"} entry on success, or
   *         {@code null} when an error status has been set on the response
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
      NodeRef topicRef = this.nodeService.getPrimaryParent(
        Converter.createNodeRefFromId(id)
      ).getParentRef();

      if (
        !this.currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
          topicRef.getId(),
          NewsGroupPermissions.NWSMODERATE
        )
      ) {
        throw new AccessDeniedException(
          "cannot moderate the post, not enough permissions"
        );
      }

      boolean approve = "true".equals(req.getParameter("approve"));
      String rejectReason = req.getParameter("rejectReason");

      this.forumsApi.verifyPost(
        id,
        approve,
        (rejectReason == null) ? "" : rejectReason
      );

      model.put("message", "ok");
    } catch (AccessDeniedException ade) {
      logger.error("Access denied when verifying post", ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidTypeException ite) {
      logger.error("Invalid node type when verifying post", ite);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad noderef type");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception inre) {
      logger.error("Unexpected error when verifying post", inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    }

    return model;
  }
}
