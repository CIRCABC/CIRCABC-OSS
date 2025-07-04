package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.HistoryApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Webscript entry to retrieve if a user is recoverable, thus had a previous membership in the
 * group.
 *
 * @author beaurpi
 */
public class HistoryGroupMembershipRecoverablePost
  extends CircabcDeclarativeWebScript {

  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private HistoryApi historyApi;

  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String userId = templateVars.get("userId");
    String groupId = templateVars.get("groupId");
    String profileId = templateVars.get("profileId");

    try {
      if (
        !(this.currentUserPermissionCheckerService.isAlfrescoAdmin() ||
          this.currentUserPermissionCheckerService.isCircabcAdmin() ||
          this.currentUserPermissionCheckerService.isInterestGroupDirAdmin(
              groupId
            ))
      ) {
        throw new AccessDeniedException(
          "Cannot recover the membership, not enough permissions"
        );
      }

      historyApi.recoverMembershipFromGroup(userId, groupId, profileId);
    } catch (AccessDeniedException ade) {
      status.setCode(HttpServletResponse.SC_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null;
    } catch (Exception e) {
      status.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      status.setMessage(e.getMessage());
      status.setException(e);
      status.setRedirect(true);
      return null;
    }
    return model;
  }

  /**
   * @param currentUserPermissionCheckerService the currentUserPermissionCheckerService to set
   */
  public void setCurrentUserPermissionCheckerService(
    CurrentUserPermissionCheckerService currentUserPermissionCheckerService
  ) {
    this.currentUserPermissionCheckerService =
      currentUserPermissionCheckerService;
  }

  /**
   * @param historyApi the historyApi to set
   */
  public void setHistoryApi(HistoryApi historyApi) {
    this.historyApi = historyApi;
  }
}
