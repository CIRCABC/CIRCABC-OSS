package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.service.profile.permissions.DirectoryPermissions;
import io.swagger.api.GroupsApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class GroupsMembersExpirationDelete extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(
    GroupsMembersExpirationDelete.class
  );

  private GroupsApi groupsApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("igId");
    String userId = templateVars.get("userId");

    try {
      if (
        !this.currentUserPermissionCheckerService.hasAnyOfDirectoryPermission(
            id,
            DirectoryPermissions.DIRMANAGEMEMBERS
          )
      ) {
        throw new AccessDeniedException(
          "Not enough rights for deleting expiration date "
        );
      }

      this.groupsApi.groupsIdMembersUserIdExpirationDelete(id, userId);
    } catch (AccessDeniedException ade) {
      status.setCode(HttpServletResponse.SC_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null;
    } catch (InvalidNodeRefException inre) {
      status.setCode(HttpServletResponse.SC_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null;
    }

    return model;
  }

  /**
   * @return the groupsApi
   */
  public GroupsApi getGroupsApi() {
    return this.groupsApi;
  }

  /**
   * @param groupsApi the groupsApi to set
   */
  public void setGroupsApi(GroupsApi groupsApi) {
    this.groupsApi = groupsApi;
  }

  public void setCurrentUserPermissionCheckerService(
    CurrentUserPermissionCheckerService currentUserPermissionCheckerService
  ) {
    this.currentUserPermissionCheckerService =
      currentUserPermissionCheckerService;
  }
}
