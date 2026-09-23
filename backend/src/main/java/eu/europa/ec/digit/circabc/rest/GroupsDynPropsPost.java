package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.DynamicPropertiesApi;
import io.swagger.model.DynamicPropertyDefinition;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.DynamicPropertyDefinitionJsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco declarative web script backing the HTTP {@code POST} endpoint that
 * creates a new dynamic property definition for an Interest Group.
 *
 * <p>The Interest Group identifier is read from the {@code igId} template
 * variable of the request URL, and the property definition payload is parsed
 * from the JSON request body. The call is only permitted for users who are
 * administrators of the target group; otherwise the endpoint responds with an
 * HTTP {@code 403 Forbidden} status.</p>
 *
 * <p>On success the model exposes the created
 * {@link io.swagger.model.DynamicPropertyDefinition} under the {@code dp} key
 * for rendering by the associated FreeMarker template. Invalid node references,
 * malformed JSON or I/O problems result in an HTTP {@code 400 Bad Request}
 * status.</p>
 *
 * @see CircabcDeclarativeWebScript
 * @see io.swagger.api.DynamicPropertiesApi
 */
public class GroupsDynPropsPost extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GroupsDynPropsPost.class);

  /**
   * API providing the business operations for managing group dynamic
   * properties (e.g. creating a property definition).
   */
  @Autowired
  private DynamicPropertiesApi dynamicPropertiesApi;

  /**
   * Service used to verify that the current user has the required permissions
   * (group administrator) before the operation is performed.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the {@code POST} request that creates a dynamic property
   * definition for the Interest Group identified by the {@code igId} template
   * variable.
   *
   * <p>The method verifies that the current user is a group administrator,
   * parses the {@link DynamicPropertyDefinition} from the JSON request body and
   * delegates its creation to {@link DynamicPropertiesApi}. When permission is
   * denied it sets an HTTP {@code 403 Forbidden} status; when the request is
   * invalid it sets an HTTP {@code 400 Bad Request} status. In both error cases
   * the method returns {@code null} after configuring the response status.</p>
   *
   * @param req the web script request; supplies the {@code igId} template
   *            variable and the JSON body describing the property definition
   * @param status the response status, updated to {@code 403} or {@code 400}
   *               when the operation cannot be completed
   * @param cache the cache control directives for the response
   * @return a model map containing the created property definition under the
   *         {@code dp} key on success, or {@code null} when an error status has
   *         been set
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
      DynamicPropertyDefinition ddd =
        DynamicPropertyDefinitionJsonParser.parseJsonDynamicPropertyDefinition(
          req
        );
      model.put("dp", this.dynamicPropertiesApi.groupsIdDynpropsPost(id, ddd));
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException | ParseException | IOException inre) {
      logger.error(ERROR_OCCURRED, inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    }

    return model;
  }
}
