/**
 *
 */
package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.service.profile.permissions.LibraryPermissions;
import io.swagger.api.SpacesApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.GroupLockGuard;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author trifapa
 *
 */
public class SpacesFolderSizeGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(SpacesFolderSizeGet.class);

  private SpacesApi spacesApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private GroupLockGuard groupLockGuard;

  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String folderId = templateVars.get("id");
    boolean mlAware = MLPropertyInterceptor.isMLAware();

    try {
      this.groupLockGuard.checkAccess(folderId);

      if (
        !this.currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
            folderId,
            LibraryPermissions.LIBACCESS
          )
      ) {
        throw new AccessDeniedException(
          "Cannot get the folder size, not enough permissions"
        );
      }

      MLPropertyInterceptor.setMLAware(false);

      int folderSize = this.spacesApi.getFolderSize(folderId);

      model.put("result", folderSize);
    } catch (AccessDeniedException ade) {
      status.setCode(HttpServletResponse.SC_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error("Access denied", ade);
      }
      return null;
    } catch (Exception e) {
      status.setCode(HttpServletResponse.SC_NOT_ACCEPTABLE);
      status.setMessage(e.getMessage());
      status.setException(e);
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error("Impossible to get the folder size", e);
      }
      return null;
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }

  /**
   * @param spacesApi the spacesApi to set
   */
  public void setSpacesApi(SpacesApi spacesApi) {
    this.spacesApi = spacesApi;
  }

  public void setCurrentUserPermissionCheckerService(
    CurrentUserPermissionCheckerService currentUserPermissionCheckerService
  ) {
    this.currentUserPermissionCheckerService =
      currentUserPermissionCheckerService;
  }

  public void setGroupLockGuard(GroupLockGuard groupLockGuard) {
    this.groupLockGuard = groupLockGuard;
  }
}
