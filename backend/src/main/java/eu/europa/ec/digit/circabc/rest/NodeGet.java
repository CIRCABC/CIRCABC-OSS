package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.NodesApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST webscript endpoint that retrieves a single Alfresco node by its identifier.
 *
 * <p>As implied by the {@code Get} suffix in the class name, this endpoint handles HTTP
 * {@code GET} requests. It expects the node identifier to be supplied as a {@code id} URL
 * template variable and optionally accepts a {@code language} request parameter to control the
 * locale used when resolving multilingual (ML) content properties.</p>
 *
 * <p>Behavior:</p>
 * <ul>
 *   <li>When no {@code language} parameter is provided, the ML property interceptor is set to
 *       ML-aware mode so multilingual properties are returned in their raw multilingual form.</li>
 *   <li>When a {@code language} parameter is provided, the corresponding {@link java.util.Locale}
 *       is applied to the content and UI locale and ML-awareness is disabled so properties are
 *       resolved for that specific language.</li>
 * </ul>
 *
 * <p>Before returning the node, the current user's read permission is verified. The endpoint
 * translates error conditions into HTTP status codes: {@code 403 Forbidden} when the user lacks
 * read permission, {@code 404 Not Found} when the node does not exist, {@code 400 Bad Request} for
 * other invalid node references, and {@code 500 Internal Server Error} for unexpected failures.</p>
 *
 * @see io.swagger.api.NodesApi
 */
public class NodeGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(NodeGet.class);

  /**
   * API used to resolve and load the requested node by its identifier.
   */
  @Autowired
  private NodesApi nodesApi;

  /**
   * Service used to verify that the current user holds read permission on the requested node.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the GET request by loading the node identified by the {@code id} URL template variable
   * and placing it in the response model under the key {@code n}.
   *
   * <p>The optional {@code language} request parameter controls locale handling for multilingual
   * properties as described in the class documentation. The ML-awareness flag is always restored
   * to its original value before the method returns.</p>
   *
   * @param req the web script request; provides the {@code id} template variable and the optional
   *            {@code language} parameter
   * @param status the web script response status, used to signal HTTP error codes (403, 404, 400,
   *               500) when the node cannot be returned
   * @param cache the web script cache control object for the response
   * @return a model map containing the resolved node under the key {@code n}, or {@code null} when
   *         an error occurred and an HTTP error status/redirect was set instead
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);
    String language = req.getParameter("language");
    boolean mlAware = MLPropertyInterceptor.isMLAware();

    if (language == null) {
      MLPropertyInterceptor.setMLAware(true);
    } else {
      Locale locale = Locale.of(language);
      I18NUtil.setContentLocale(locale);
      I18NUtil.setLocale(locale);
      MLPropertyInterceptor.setMLAware(false);
    }

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("id");
    try {
      if (
        !this.currentUserPermissionCheckerService.hasAlfrescoReadPermission(id)
      ) {
        throw new AccessDeniedException(
          "Not enough permission to delete the news"
        );
      }

      model.put("n", this.nodesApi.getNodeById(id));
    } catch (AccessDeniedException ade) {
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      logger.error("Error getting node - access denied", ade);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      // BUG DIGITCIRCABC-4899
      String detailMessage = inre.getMessage();
      if (
        detailMessage != null && detailMessage.startsWith("Node does not exist")
      ) {
        status.setCode(Status.STATUS_NOT_FOUND);
        status.setMessage("Node with given ID does not exist!");
      } else {
        status.setCode(Status.STATUS_BAD_REQUEST);
        status.setMessage("Bad request");
      }
      status.setRedirect(true);
      logger.error("Error getting node - invalid node reference", inre);
      return null; // NOSONAR
    } catch (Exception e) {
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      logger.error("Unexpected error getting node", e);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
    return model;
  }
}
