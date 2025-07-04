package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.AppMessageApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.AppMessageJsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author beaurpi
 * @description use this method to enable / disable the display of the old system message in the new
 * ui
 */
public class AppMessagesOldConfigPut extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(AppMessagesOldConfigPut.class);

  private AppMessageApi appMessageApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    try {
      if (
        !(currentUserPermissionCheckerService.isAlfrescoAdmin() ||
          currentUserPermissionCheckerService.isCircabcAdmin())
      ) {
        throw new AccessDeniedException("Not enough permissions");
      }

      Boolean enableOld = AppMessageJsonParser.parseBoolean(req);

      if (enableOld != null) {
        appMessageApi.setEnableOldMessage(enableOld);
      }
    } catch (AccessDeniedException ade) {
      status.setCode(HttpServletResponse.SC_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null;
    } catch (IOException e) {
      status.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      status.setMessage("Server error - invalid request");
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
   * @return the appMessageApi
   */
  public AppMessageApi getAppMessageApi() {
    return appMessageApi;
  }

  /**
   * @param appMessageApi the appMessageApi to set
   */
  public void setAppMessageApi(AppMessageApi appMessageApi) {
    this.appMessageApi = appMessageApi;
  }
}
