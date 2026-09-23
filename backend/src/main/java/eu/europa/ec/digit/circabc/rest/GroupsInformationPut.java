package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.InformationApi;
import io.swagger.model.InformationPage;
import io.swagger.model.permissions.InformationPermissions;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.InformationJsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco web script endpoint that handles the HTTP {@code PUT} request used to
 * update the "Information" service configuration of an Interest Group (IG).
 *
 * <p>The endpoint resolves the IG node from the {@code igId} URL template
 * variable, locates its child "Information" node and verifies that the current
 * user holds administrative permissions ({@link InformationPermissions#INFADMIN})
 * on that node. When authorized, the JSON request body is parsed into an
 * {@link InformationPage} and applied through
 * {@link InformationApi#groupsIdInformationPut(String, InformationPage)}.</p>
 *
 * <p>Key inputs:</p>
 * <ul>
 *   <li>{@code igId} — URL template variable identifying the Interest Group.</li>
 *   <li>Request body — JSON representation of an {@link InformationPage},
 *       parsed by {@link InformationJsonParser}.</li>
 * </ul>
 *
 * <p>Error handling: insufficient permissions result in an HTTP
 * {@code 403 Forbidden}; invalid node references, malformed URLs and JSON parse
 * failures result in an HTTP {@code 400 Bad Request}.</p>
 */
public class GroupsInformationPut extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GroupsInformationPut.class);

  /** API used to apply the updated Information configuration for an Interest Group. */
  @Autowired
  private InformationApi informationApi;

  /** Alfresco node service used to resolve the IG's child "Information" node. */
  @Autowired
  private NodeService nodeService;

  /** Service used to check whether the current user holds the required permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Executes the PUT request that updates an Interest Group's Information service.
   *
   * <p>Resolves the IG node from the {@code igId} template variable, finds its
   * child "Information" node, enforces {@link InformationPermissions#INFADMIN}
   * permission, then parses the request body and applies the update via
   * {@link InformationApi#groupsIdInformationPut(String, InformationPage)}.</p>
   *
   * @param req the web script request; supplies the {@code igId} template
   *            variable and the JSON body describing the {@link InformationPage}
   * @param status the response status, set to {@code 403} on access denial or
   *               {@code 400} on invalid input
   * @param cache the response cache directives (unused by this endpoint)
   * @return an empty model map on success, or {@code null} when an error status
   *         and redirect have been set
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
          InformationPermissions.INFADMIN
        )
      ) {
        throw new AccessDeniedException(
          "Not enought permissions on the News node"
        );
      }

      InformationPage body = InformationJsonParser.parse(req);
      this.informationApi.groupsIdInformationPut(id, body);
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error(ERROR_OCCURRED, inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request - InvalidNodeRef");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (IOException e) {
      logger.error(ERROR_OCCURRED, e);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request - bad URL");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (ParseException e) {
      logger.error(ERROR_OCCURRED, e);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request - parse error");
      status.setRedirect(true);
      return null; // NOSONAR
    }

    return model;
  }
}
