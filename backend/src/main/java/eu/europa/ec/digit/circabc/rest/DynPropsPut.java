package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.DynamicPropertiesApi;
import io.swagger.model.DynamicPropertyDefinition;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.DynamicPropertyDefinitionJsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST webscript endpoint handling the HTTP {@code PUT} request that updates an
 * existing dynamic property definition.
 *
 * <p>The dynamic property is identified by the {@code id} template variable in
 * the request URL. The endpoint resolves the {@link NodeRef} of the dynamic
 * property, walks up the node hierarchy (dynamic property &rarr; dynamic
 * properties folder &rarr; interest group) to locate the owning Interest Group,
 * and verifies that the current user is a group administrator (IG leader) of
 * that group before performing the update. The updated definition is parsed
 * from the JSON request body.</p>
 *
 * <p>On success, the returned model contains the updated
 * {@link DynamicPropertyDefinition} under the {@code dp} key. A permission
 * failure results in an HTTP {@code 403 Forbidden} response, while an invalid
 * node reference or malformed request body results in an HTTP
 * {@code 400 Bad Request} response.</p>
 *
 * @see CircabcDeclarativeWebScript
 * @see DynamicPropertiesApi
 */
public class DynPropsPut extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(DynPropsPut.class);

  /**
   * API providing the business operations for reading and updating dynamic
   * property definitions.
   */
  @Autowired
  private DynamicPropertiesApi dynamicPropertiesApi;

  /**
   * Alfresco node service used to navigate the parent hierarchy of the dynamic
   * property node in order to resolve the owning Interest Group.
   */
  @Autowired
  private NodeService nodeService;

  /**
   * Service used to verify that the current user holds the group administrator
   * (IG leader) permission required to update a dynamic property.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the {@code PUT} request that updates a dynamic property definition.
   *
   * <p>The dynamic property {@code id} is read from the request template
   * variables. The method resolves the owning Interest Group by walking up the
   * node hierarchy and checks that the current user is a group administrator of
   * that group. The updated definition is parsed from the JSON request body and
   * persisted through {@link DynamicPropertiesApi#dynpropsIdPut}.</p>
   *
   * @param req    the web script request carrying the {@code id} template
   *               variable and the JSON body describing the updated definition
   * @param status the response status, set to {@code 403} on access denial or
   *               {@code 400} on an invalid node reference or malformed request
   * @param cache  the cache directives for the response
   * @return a model map containing the updated
   *         {@link DynamicPropertyDefinition} under the {@code dp} key, or
   *         {@code null} when the request fails and a redirect status is set
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
      NodeRef dpRef = Converter.createNodeRefFromId(id);
      NodeRef dpFolderRef = this.nodeService.getPrimaryParent(
        dpRef
      ).getParentRef();
      NodeRef igRef = this.nodeService.getPrimaryParent(
        dpFolderRef
      ).getParentRef();

      if (
        !this.currentUserPermissionCheckerService.isGroupAdmin(igRef.getId())
      ) {
        throw new AccessDeniedException(
          "Cannot delete dynamic property, user is not IG leader"
        );
      }

      DynamicPropertyDefinition ddd =
        DynamicPropertyDefinitionJsonParser.parseJsonDynamicPropertyDefinition(
          req
        );
      model.put("dp", this.dynamicPropertiesApi.dynpropsIdPut(id, ddd));
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
