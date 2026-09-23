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
 * Alfresco Web Script endpoint that handles HTTP {@code GET} requests to
 * retrieve the dynamic properties configured for an Interest Group.
 *
 * <p>The target Interest Group is identified by the {@code igId} URL template
 * variable. Access is restricted to Group administrators: the current user's
 * permissions are validated via {@link CurrentUserPermissionCheckerService}
 * before the properties are read through {@link DynamicPropertiesApi}.</p>
 *
 * <p>On success, the resolved dynamic properties are placed in the response
 * model under the {@code dynproperties} key for FreeMarker rendering. If the
 * caller is not a Group administrator the endpoint responds with HTTP
 * {@code 403 Forbidden}; if the supplied identifier does not reference a valid
 * node it responds with HTTP {@code 400 Bad Request}.</p>
 */
public class GroupsDynPropsGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GroupsDynPropsGet.class);

  /**
   * API used to read the dynamic properties associated with an Interest Group.
   */
  @Autowired
  private DynamicPropertiesApi dynamicPropertiesApi;

  /**
   * Service used to verify that the current user has Group administrator
   * rights before exposing the dynamic properties.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the {@code GET} request by resolving and returning the dynamic
   * properties of the Interest Group identified by the {@code igId} URL
   * template variable.
   *
   * <p>The current user must be a Group administrator. When the check fails an
   * {@link AccessDeniedException} is caught internally and translated into an
   * HTTP {@code 403 Forbidden} response; an {@link InvalidNodeRefException} is
   * translated into an HTTP {@code 400 Bad Request} response. In both error
   * cases {@code null} is returned and the response status is set accordingly.</p>
   *
   * @param req the web script request, providing the {@code igId} template
   *            variable that identifies the Interest Group
   * @param status the response status used to signal errors to the client
   * @param cache the cache directives for the response
   * @return a model map containing the {@code dynproperties} entry on success,
   *         or {@code null} when access is denied or the node reference is
   *         invalid
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("igId");

    try {
      if (!this.currentUserPermissionCheckerService.isGroupAdmin(id)) {
        throw new AccessDeniedException(
          "User is not Group admin to manage the dynamic properties"
        );
      }
      model.put(
        "dynproperties",
        this.dynamicPropertiesApi.groupsIdDynpropsGet(id)
      );
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
