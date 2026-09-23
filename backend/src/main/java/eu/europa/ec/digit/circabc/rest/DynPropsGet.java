package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.DynamicPropertiesApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
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
 * Read-only REST endpoint that retrieves the dynamic properties of a repository
 * node.
 *
 * <p>As implied by the {@code Get} suffix in the class name, this webscript
 * handles an HTTP {@code GET} request. The target node is identified by the
 * {@code id} URL template variable (the Alfresco node reference / identifier).
 *
 * <p>Behaviour:
 * <ul>
 *   <li>Verifies that the current user holds read permission on the node; if
 *       not, the response is set to {@code 403 Forbidden}.</li>
 *   <li>When permitted, delegates to {@link DynamicPropertiesApi} to load the
 *       dynamic properties and exposes them in the model under the {@code dp}
 *       key for rendering by the associated FreeMarker template.</li>
 *   <li>If the supplied identifier does not resolve to a valid node, the
 *       response is set to {@code 400 Bad Request}.</li>
 * </ul>
 */
public class DynPropsGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(DynPropsGet.class);

  /**
   * API used to load the dynamic properties associated with a node.
   */
  @Autowired
  private DynamicPropertiesApi dynamicPropertiesApi;

  /**
   * Service used to check the current user's Alfresco permissions on the node.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Executes the webscript, returning the dynamic properties of the node
   * identified by the {@code id} URL template variable.
   *
   * <p>The current user must have read permission on the node. On success the
   * returned model contains the dynamic properties under the {@code dp} key. On
   * a permission failure or an invalid node reference, the {@code status} is
   * updated accordingly and {@code null} is returned so the error is rendered.
   *
   * @param req the web script request, providing the {@code id} template
   *     variable that identifies the target node
   * @param status the response status, updated to {@code 403 Forbidden} when
   *     access is denied or {@code 400 Bad Request} when the node reference is
   *     invalid
   * @param cache the cache directives for the response
   * @return a model map containing the dynamic properties under the {@code dp}
   *     key, or {@code null} when an error status has been set
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
        !this.currentUserPermissionCheckerService.hasAlfrescoReadPermission(id)
      ) {
        throw new AccessDeniedException("Cannot access dynamic property");
      }
      model.put("dp", this.dynamicPropertiesApi.dynpropsIdGet(id));
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
    }

    return model;
  }
}
