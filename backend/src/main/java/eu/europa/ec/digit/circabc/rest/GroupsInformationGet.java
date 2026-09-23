package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.InformationApi;
import io.swagger.model.permissions.InformationPermissions;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST webscript endpoint that retrieves the Information service configuration
 * of a given Interest Group.
 *
 * <p>The class name follows the CIRCABC {@code <Entity><Method>} convention, so
 * this endpoint handles the HTTP {@code GET} method. It expects the Interest
 * Group identifier as the {@code igId} URL template variable, locates the
 * group's child "Information" node and returns its details.
 *
 * <p>Before returning the data it enforces that the current user holds the
 * {@link InformationPermissions#INFACCESS} permission on the Information node;
 * otherwise the response is set to HTTP {@code 403 Forbidden}. An invalid or
 * unresolvable group identifier results in an HTTP {@code 400 Bad Request}.
 *
 * <p>On success the returned model exposes the retrieved information under the
 * {@code "information"} key, which is rendered as JSON by the associated
 * FreeMarker template.
 */
public class GroupsInformationGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GroupsInformationGet.class);

  /** API providing the business logic for retrieving Information service data. */
  @Autowired
  private InformationApi informationApi;

  /** Alfresco node service used to resolve the group's child "Information" node. */
  @Autowired
  private NodeService nodeService;

  /** Service used to verify that the current user holds the required Information permission. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the {@code GET} request for an Interest Group's Information service.
   *
   * <p>Reads the {@code igId} URL template variable, resolves the group's child
   * "Information" node, checks that the current user has the
   * {@link InformationPermissions#INFACCESS} permission and, when authorized,
   * places the retrieved information into the response model under the
   * {@code "information"} key.
   *
   * <p>If the user lacks the required permission the status is set to
   * {@link Status#STATUS_FORBIDDEN} and {@code null} is returned; if the group
   * identifier cannot be resolved the status is set to
   * {@link Status#STATUS_BAD_REQUEST} and {@code null} is returned.
   *
   * @param req the web script request; must contain the {@code igId} template variable
   * @param status the web script response status, updated on error conditions
   * @param cache the web script cache directives for the response
   * @return a model map containing the {@code "information"} entry on success,
   *         or {@code null} when access is denied or the request is invalid
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
      NodeRef infRef = this.nodeService.getChildByName(
        Converter.createNodeRefFromId(id),
        ContentModel.ASSOC_CONTAINS,
        "Information"
      );
      if (
        !this.currentUserPermissionCheckerService.hasAnyOfInformationPermission(
          infRef.getId(),
          InformationPermissions.INFACCESS
        )
      ) {
        throw new AccessDeniedException(
          "Not enought permissions on the News node"
        );
      }

      model.put("information", this.informationApi.groupsIdInformationGet(id));
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
