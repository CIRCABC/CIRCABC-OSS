package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.ForumsApi;
import io.swagger.model.AbuseReport;
import io.swagger.model.permissions.NewsGroupPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.List;
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
 * REST endpoint (HTTP GET) that retrieves the abuse reports signalled against a
 * forum post / newsgroup node.
 *
 * <p>The node whose abuse reports are requested is identified by the {@code id}
 * URL template variable. Access is restricted to users holding the newsgroup
 * moderation permission ({@link NewsGroupPermissions#NWSMODERATE}) on that node;
 * callers without this permission receive an HTTP 403 (Forbidden) response.</p>
 *
 * <p>On success the resulting model exposes the list of
 * {@link AbuseReport} instances under the {@code abuses} key, which is then
 * rendered by the associated FreeMarker template. Errors are translated into the
 * appropriate HTTP status codes: 403 for access denial, 400 for an invalid node
 * type or any other unexpected failure.</p>
 */
public class PostAbusesGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(PostAbusesGet.class);

  /** API facade providing forum/newsgroup operations, including abuse retrieval. */
  @Autowired
  private ForumsApi forumsApi;

  /** Service used to verify the current user's permissions on the target node. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the GET request by collecting the abuse reports signalled against the
   * node identified by the {@code id} URL template variable.
   *
   * <p>The current user must hold the {@link NewsGroupPermissions#NWSMODERATE}
   * permission on the node. If the check fails, or if the node reference is of an
   * invalid type, or if any other error occurs, the response status is set
   * accordingly and {@code null} is returned so no model is rendered.</p>
   *
   * @param req the web script request; the target node id is read from its
   *     {@code id} URL template variable
   * @param status the response status, updated with the relevant HTTP code and
   *     message when an error occurs (403 for access denial, 400 for a bad
   *     node type or generic failure)
   * @param cache the cache directives for the response
   * @return a model map exposing the retrieved abuse reports under the
   *     {@code abuses} key on success, or {@code null} when an error is handled
   *     and an error status is set instead
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
          "cannot get the abuse of the node, not enough permissions"
        );
      }

      List<AbuseReport> abuses = this.forumsApi.getSignaledAbuses(id);

      model.put("abuses", abuses);
    } catch (AccessDeniedException ade) {
      logger.error("Access denied when getting abuses", ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidTypeException ite) {
      logger.error("Invalid type when getting abuses", ite);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad noderef type");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception inre) {
      logger.error("Error when getting abuses", inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    }

    return model;
  }
}
