package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.SyncApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class SyncGroupPost extends CircabcDeclarativeWebScript {

  private SyncApi syncApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    try {
      if (
        !this.currentUserPermissionCheckerService.isCircabcAdmin() &&
        !this.currentUserPermissionCheckerService.isAlfrescoAdmin()
      ) {
        throw new AccessDeniedException(
          "Only CIRCABC or Alfresco administrators can trigger a group sync"
        );
      }

      Map<String, String> templateVars = req
        .getServiceMatch()
        .getTemplateVars();
      String id = templateVars.get("id");

      if (id == null || id.isEmpty()) {
        status.setCode(HttpServletResponse.SC_BAD_REQUEST);
        status.setMessage("Group ID is required");
        status.setRedirect(true);
        return null;
      }

      long start = System.currentTimeMillis();
      syncApi.syncGroup(id);
      long duration = System.currentTimeMillis() - start;

      Map<String, Object> model = new HashMap<>(3, 1.0f);
      model.put("message", "Group sync completed successfully");
      model.put("groupId", id);
      model.put("durationMs", duration);
      return model;
    } catch (AccessDeniedException ade) {
      status.setCode(HttpServletResponse.SC_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null;
    }
  }

  public void setSyncApi(SyncApi syncApi) {
    this.syncApi = syncApi;
  }

  public void setCurrentUserPermissionCheckerService(
    CurrentUserPermissionCheckerService currentUserPermissionCheckerService
  ) {
    this.currentUserPermissionCheckerService =
      currentUserPermissionCheckerService;
  }
}
