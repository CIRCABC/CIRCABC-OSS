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
 * Read-only Alfresco web script endpoint that resolves the ancestor path of a node.
 *
 * <p>Implied HTTP method: {@code GET} (the class name ends with {@code Get}). Given a
 * node identifier supplied as the {@code id} URL template variable, this endpoint
 * returns the ordered list of nodes forming the path from the repository root down to
 * the requested node. The result is placed in the response model under the
 * {@code nodes} key for rendering by the associated FreeMarker template.</p>
 *
 * <p>Key inputs:</p>
 * <ul>
 *   <li>{@code id} — URL template variable identifying the target node.</li>
 *   <li>{@code language} — optional request parameter. When provided, the content and
 *       UI locale are set accordingly and multilingual (ML) awareness is disabled so
 *       properties resolve to the requested language; when absent, ML awareness is
 *       enabled to expose multilingual property values.</li>
 * </ul>
 *
 * <p>Before resolving the path the caller must hold Alfresco read permission on the
 * node; otherwise access is denied. Errors are translated into HTTP status codes:
 * {@code 403 Forbidden} when read permission is missing, {@code 400 Bad Request} for an
 * invalid node reference, and {@code 500 Internal Server Error} for any other failure.
 * The previous ML-awareness state is always restored once processing completes.</p>
 */
public class PathGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(PathGet.class);

  /**
   * API used to resolve the ancestor path of a node from its identifier.
   */
  @Autowired
  private NodesApi nodesApi;

  /**
   * Service used to verify that the current user has Alfresco read permission on the
   * requested node before its path is exposed.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Resolves and returns the ancestor path of the node identified by the {@code id}
   * URL template variable.
   *
   * <p>Configures locale and multilingual awareness based on the optional
   * {@code language} request parameter, checks that the current user holds Alfresco
   * read permission on the node, and, if permitted, places the resolved path under the
   * {@code nodes} model key. On error the appropriate HTTP status is set on
   * {@code status} and {@code null} is returned; the prior ML-awareness state is
   * restored in all cases.</p>
   *
   * @param req the web script request; supplies the {@code language} parameter and the
   *            {@code id} URL template variable
   * @param status the response status, updated with an HTTP code and message when the
   *               request cannot be fulfilled
   * @param cache the response cache directives (unused)
   * @return a model map containing the resolved path under the {@code nodes} key, or
   *         {@code null} when an error occurs and a redirect status has been set
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
          "cannot get the path to the node, not enough permissions"
        );
      }

      model.put("nodes", this.nodesApi.getPathByNode(id));
    } catch (AccessDeniedException ade) {
      logger.error("Access denied getting path: " + ade.getMessage(), ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error(
        "Invalid node reference for path: " + inre.getMessage(),
        inre
      );
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Unexpected error getting path: " + e.getMessage(), e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
    return model;
  }
}
