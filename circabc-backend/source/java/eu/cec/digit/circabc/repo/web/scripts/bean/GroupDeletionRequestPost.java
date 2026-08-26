package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.CategoriesApi;
import io.swagger.model.GroupDeletionRequest;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.InterestGroupJsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class GroupDeletionRequestPost extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GroupDeletionRequestPost.class);

  private NodeService nodeService;
  private CategoriesApi categoriesApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private AuthenticationService authenticationService;

  /**
   * @return the categoriesApi
   */
  public CategoriesApi getCategoriesApi() {
    return this.categoriesApi;
  }

  /**
   * @param categoriesApi the categoriesApi to set
   */
  public void setCategoriesApi(CategoriesApi categoriesApi) {
    this.categoriesApi = categoriesApi;
  }

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
      status.setCode(HttpServletResponse.SC_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null;
    } catch (InvalidNodeRefException | ParseException | IOException inre) {
      status.setCode(HttpServletResponse.SC_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null;
    }

    return model;
  }

  public void setCurrentUserPermissionCheckerService(
    CurrentUserPermissionCheckerService currentUserPermissionCheckerService
  ) {
    this.currentUserPermissionCheckerService =
      currentUserPermissionCheckerService;
  }

  /**
   * @return the nodeService
   */
  public NodeService getNodeService() {
    return nodeService;
  }

  /**
   * @param nodeService the nodeService to set
   */
  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  public AuthenticationService getAuthenticationService() {
    return authenticationService;
  }

  public void setAuthenticationService(
    AuthenticationService authenticationService
  ) {
    this.authenticationService = authenticationService;
  }
}
