package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.ContentApi;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco Web Script endpoint that serves an HTTP {@code GET} request returning the first
 * (up to ten) versions of a content node.
 *
 * <p>The endpoint is resolved from the {@code id} template variable in the request URL, which
 * identifies the target content node in the Alfresco repository. Before returning any data the
 * endpoint verifies that the current user holds at least {@link LibraryPermissions#LIBACCESS}
 * permission on the node; otherwise the request is rejected with HTTP 403 (Forbidden). If the
 * supplied identifier does not resolve to a valid node, the request is rejected with HTTP 400
 * (Bad Request).</p>
 *
 * <p>On success the model exposes a single {@code versions} entry holding the list of versions as
 * produced by {@link ContentApi#contentIdFirstVersionsGet(String)}, which is then rendered by the
 * associated FreeMarker template.</p>
 */
public class ContentFirstTenVersionsGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(ContentFirstTenVersionsGet.class);

  /** Business API used to retrieve the version history of a content node. */
  @Autowired
  private ContentApi contentApi;

  /** Service used to verify that the current user holds the required library permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the {@code GET} request by loading the first versions of the content node identified
   * by the {@code id} template variable.
   *
   * <p>Multilingual (ML) awareness is temporarily enabled while retrieving the versions and
   * restored to its previous state afterwards. Access is only granted when the current user has
   * the {@link LibraryPermissions#LIBACCESS} permission on the node.</p>
   *
   * @param req the web script request; must provide an {@code id} template variable identifying
   *     the content node
   * @param status the response status, updated to {@code 403} on access denial or {@code 400} on
   *     an invalid node reference
   * @param cache the cache directives for the response
   * @return a model map containing a {@code versions} entry with the retrieved versions, or
   *     {@code null} when the request fails and a redirect status has been set
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    boolean mlAware = MLPropertyInterceptor.isMLAware();
    MLPropertyInterceptor.setMLAware(true);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("id");
    try {
      if (
        !this.currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
          id,
          LibraryPermissions.LIBACCESS
        )
      ) {
        throw new AccessDeniedException(
          "Cannot read versions of content, not enough permissions"
        );
      }
      model.put("versions", this.contentApi.contentIdFirstVersionsGet(id));
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error(ERROR_OCCURRED, inre);
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
