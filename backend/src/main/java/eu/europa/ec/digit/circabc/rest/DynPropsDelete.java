package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.DynamicPropertiesApi;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST webscript endpoint that deletes a single dynamic property.
 *
 * <p>This endpoint backs an HTTP {@code DELETE} request (as implied by the
 * {@code Delete} suffix of the class name) identified by the {@code id} URL
 * template variable, which is the node identifier of the dynamic property to
 * remove.
 *
 * <p>Processing steps:
 * <ul>
 *   <li>Resolves the dynamic property node from the {@code id} path variable.</li>
 *   <li>Walks up the node hierarchy (dynamic property → containing folder →
 *       Interest Group) to determine the owning Interest Group.</li>
 *   <li>Authorises the call by requiring the current user to be a group
 *       administrator (IG leader) of that Interest Group.</li>
 *   <li>Delegates the actual deletion to {@link DynamicPropertiesApi}.</li>
 * </ul>
 *
 * <p>On authorisation failure the endpoint responds with HTTP 403 (Forbidden),
 * and on an invalid node reference with HTTP 400 (Bad Request).
 */
public class DynPropsDelete extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(DynPropsDelete.class);

  /** API providing the dynamic property business operations, including deletion. */
  @Autowired
  private DynamicPropertiesApi dynamicPropertiesApi;

  /** Alfresco node service used to navigate the node hierarchy up to the Interest Group. */
  @Autowired
  private NodeService nodeService;

  /** Service used to verify that the current user is a group administrator (IG leader). */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Executes the dynamic property deletion request.
   *
   * <p>Reads the {@code id} template variable from the request, resolves the
   * owning Interest Group, verifies that the current user is a group
   * administrator of that group, and deletes the dynamic property. Any failure
   * is translated into an appropriate HTTP status on the response.
   *
   * @param req    the web script request, providing the {@code id} template
   *               variable of the dynamic property to delete
   * @param status the web script response status, updated to reflect
   *               authorisation or validation errors
   * @param cache  the response cache directives
   * @return an empty model map on success, or {@code null} when an error status
   *         (403 or 400) has been set and a redirect response is returned
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

      this.dynamicPropertiesApi.dynpropsIdDelete(id);
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
