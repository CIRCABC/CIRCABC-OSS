package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.CategoriesApi;
import io.swagger.model.GroupDeletionRequest;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.InterestGroupJsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST webscript endpoint that handles the submission of a deletion request for an
 * Interest Group.
 *
 * <p>As implied by the {@code Post} suffix in the class name, this endpoint is bound to the
 * HTTP {@code POST} method. The Interest Group to be flagged for deletion is identified by the
 * {@code id} template variable taken from the request URL. The endpoint records a deletion
 * request against that group on behalf of the currently authenticated user.</p>
 *
 * <p>Processing performed by {@link #executeImpl(WebScriptRequest, Status, Cache)}:</p>
 * <ul>
 *   <li>Verifies that the current user is an administrator of the target group; otherwise the
 *       request is rejected with HTTP 403 (Forbidden).</li>
 *   <li>Resolves the group node and its parent category, parses the deletion request payload
 *       from the request body, and delegates the persistence of the request to
 *       {@link CategoriesApi#groupIdDeleteRequestPost(GroupDeletionRequest)}.</li>
 *   <li>Malformed input (invalid node reference, JSON parse or I/O errors) results in
 *       HTTP 400 (Bad Request).</li>
 * </ul>
 */
public class GroupDeletionRequestPost extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GroupDeletionRequestPost.class);

  /** Alfresco node service used to navigate the repository (e.g. to resolve parent categories). */
  @Autowired
  private NodeService nodeService;

  /** Service used to verify that the current user has the required permissions on the group. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /** API used to record the group deletion request against the owning category. */
  @Autowired
  private CategoriesApi categoriesApi;

  /** Alfresco authentication service used to identify the current user submitting the request. */
  @Autowired
  private AuthenticationService authenticationService;

  /**
   * Handles the {@code POST} request that registers a deletion request for an Interest Group.
   *
   * <p>The target group id is read from the {@code id} URL template variable. When present, the
   * current user's group-admin permission is checked, the group node and its parent category are
   * resolved, the request body is parsed into a {@link GroupDeletionRequest}, and the request is
   * forwarded to {@link CategoriesApi#groupIdDeleteRequestPost(GroupDeletionRequest)}.</p>
   *
   * @param req the web script request; provides the {@code id} template variable and the JSON body
   * @param status the response status, updated to 403 on access denial or 400 on invalid input
   * @param cache the response cache control settings
   * @return an (empty) model map on success, or {@code null} when an error status/redirect is set
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String groupId = templateVars.get("id");
    String from = this.authenticationService.getCurrentUserName();

    try {
      if (groupId != null) {
        if (!this.currentUserPermissionCheckerService.isGroupAdmin(groupId)) {
          throw new AccessDeniedException(
            "Current Authority cannot access group, not enough permission"
          );
        }
        NodeRef igRef = Converter.createNodeRefFromId(groupId);
        NodeRef category = nodeService.getPrimaryParent(igRef).getParentRef();
        GroupDeletionRequest body =
          InterestGroupJsonParser.parseGroupDeletionRequest(
            req,
            from,
            category.getId(),
            groupId
          );

        this.categoriesApi.groupIdDeleteRequestPost(body);
      }
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
