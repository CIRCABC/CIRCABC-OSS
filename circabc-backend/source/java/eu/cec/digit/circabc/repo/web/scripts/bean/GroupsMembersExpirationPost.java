package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.service.profile.permissions.DirectoryPermissions;
import io.swagger.api.GroupsApi;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.Date;
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

public class GroupsMembersExpirationPost extends CircabcDeclarativeWebScript {

  public static final String BAD_REQUEST = "Bad request";
  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(
    GroupsMembersExpirationPost.class
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
          "Not enough rights for adding expiration date"
        );
      }

      Date expirationDate;

      if (req.getParameter("expirationDate") != null) {
        expirationDate = Converter.convertStringToDate(
          req.getParameter("expirationDate")
        );
        // Validate that the expiration date is in the future
        if (!expirationDate.after(new Date())) {
          status.setCode(Status.STATUS_BAD_REQUEST);
          status.setMessage("Expiration date must be in the future");
          status.setRedirect(true);
          return null;
        }
      } else {
        status.setCode(HttpServletResponse.SC_BAD_REQUEST);
        status.setMessage(BAD_REQUEST);
        status.setRedirect(true);
        return null;
      }
      String profileId = req.getParameter("profileId");
      String alfrescoGroup = req.getParameter("alfrescoGroup");

      if (profileId == null || alfrescoGroup == null) {
        status.setCode(HttpServletResponse.SC_BAD_REQUEST);
        status.setMessage(BAD_REQUEST);
        status.setRedirect(true);
        return null;
      }

      this.groupsApi.groupsIdMembersUserIdExpirationPost(
          id,
          userId,
          expirationDate,
          profileId,
          alfrescoGroup
        );
    } catch (AccessDeniedException ade) {
      status.setCode(HttpServletResponse.SC_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null;
    } catch (InvalidNodeRefException inre) {
      status.setCode(HttpServletResponse.SC_BAD_REQUEST);
      status.setMessage(BAD_REQUEST);
      status.setRedirect(true);
      return null;
    } catch (java.text.ParseException e) {
      status.setCode(HttpServletResponse.SC_NOT_ACCEPTABLE);
      status.setMessage("Bad body");
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
