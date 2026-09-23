package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.ForumsApi;
import io.swagger.model.permissions.NewsGroupPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco Declarative Web Script endpoint that reports (signals) an abuse on a
 * newsgroup post.
 *
 * <p>As implied by the {@code Post} suffix in the class name, this endpoint is
 * bound to the HTTP {@code POST} method. It reports abusive content for a given
 * post/node so that it can be reviewed.
 *
 * <p>Key inputs:
 * <ul>
 *   <li>{@code id} &mdash; template variable identifying the post/node on which
 *       abuse is being reported.</li>
 *   <li>{@code abuseText} &mdash; optional request parameter carrying the free
 *       text describing the abuse; treated as an empty string when absent.</li>
 * </ul>
 *
 * <p>The caller must hold newsgroup access and post permissions
 * ({@link NewsGroupPermissions#NWSACCESS} and
 * {@link NewsGroupPermissions#NWSPOST}) on the target node; otherwise the
 * request is rejected with an HTTP 403 (Forbidden). Invalid node types result
 * in an HTTP 400 (Bad request), as do other processing errors.
 */
public class PostAbuseSignalPost extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(PostAbuseSignalPost.class);

  /**
   * API used to perform forum/newsgroup operations, including signaling abuse
   * on a post.
   */
  @Autowired
  private ForumsApi forumsApi;

  /**
   * Service used to verify that the current user holds the required newsgroup
   * permissions before the abuse is reported.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the POST request that signals an abuse on a newsgroup post.
   *
   * <p>Reads the post {@code id} from the URL template variables and the
   * optional {@code abuseText} request parameter, checks that the current user
   * has the required newsgroup permissions, and delegates the reporting to
   * {@link ForumsApi#signalAbuse(String, String)}. Multilingual property
   * awareness is temporarily enabled for the duration of the call and restored
   * afterwards.
   *
   * <p>On failure the method sets an appropriate HTTP status on the response
   * (403 for permission problems, 400 for invalid node types or other errors)
   * and returns {@code null}.
   *
   * @param req the incoming web script request; provides the {@code id}
   *            template variable and the {@code abuseText} parameter
   * @param status the response status, updated with an error code and message
   *               when the request cannot be processed
   * @param cache the cache directives for the response
   * @return a model map containing a {@code message} entry set to {@code "ok"}
   *         on success, or {@code null} when an error occurs and an error
   *         status has been set
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    boolean mlAware = MLPropertyInterceptor.isMLAware();

    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    try {
      MLPropertyInterceptor.setMLAware(true);

      String id = templateVars.get("id");

      if (
        !this.currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
          id,
          NewsGroupPermissions.NWSACCESS,
          NewsGroupPermissions.NWSPOST
        )
      ) {
        throw new AccessDeniedException(
          "cannot report an abuse on the post, not enough permissions"
        );
      }

      String abuseText = req.getParameter("abuseText");

      this.forumsApi.signalAbuse(id, (abuseText == null) ? "" : abuseText);

      model.put("message", "ok");
    } catch (AccessDeniedException ade) {
      logger.error("Access denied when signaling abuse", ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidTypeException ite) {
      logger.error("Invalid type when signaling abuse", ite);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad noderef type");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception inre) {
      logger.error("Error when signaling abuse", inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }
}
