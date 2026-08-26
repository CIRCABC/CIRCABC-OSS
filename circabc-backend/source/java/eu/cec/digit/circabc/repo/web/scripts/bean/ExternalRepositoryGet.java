package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.AresBridgeApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class ExternalRepositoryGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(ExternalRepositoryGet.class);

  private AresBridgeApi aresBridgeApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    if (currentUserPermissionCheckerService.isGuest()) {
      status.setCode(HttpServletResponse.SC_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error("Access denied");
      }
      return null;
    }

    try {
      model.put("repos", this.aresBridgeApi.getAvailableExternalRepositories());
    } catch (Exception e) {
      status.setCode(HttpServletResponse.SC_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error("Bad request", e);
      }
      return null;
    }

    return model;
  }

  public CurrentUserPermissionCheckerService getCurrentUserPermissionCheckerService() {
    return currentUserPermissionCheckerService;
  }

  public void setCurrentUserPermissionCheckerService(
    CurrentUserPermissionCheckerService currentUserPermissionCheckerService
  ) {
    this.currentUserPermissionCheckerService =
      currentUserPermissionCheckerService;
  }

  public AresBridgeApi getAresBridgeApi() {
    return aresBridgeApi;
  }

  public void setAresBridgeApi(AresBridgeApi aresBridgeApi) {
    this.aresBridgeApi = aresBridgeApi;
  }
}
