package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.service.app.message.DistributionEmailDAO;
import io.swagger.api.AppMessageApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.EmailJsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author beaurpi
 */
public class AppDistributionMailsPost extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(AppDistributionMailsPost.class);

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
      List<DistributionEmailDAO> list = EmailJsonParser.parseDistributionEmails(
        req
      );

      appMessageApi.addAppDistributionPostEmails(list);
    } catch (AccessDeniedException ade) {
      status.setCode(HttpServletResponse.SC_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null;
    } catch (IOException e) {
      status.setCode(HttpServletResponse.SC_BAD_REQUEST);
      status.setMessage("Error in request, impossible to read request");
      status.setRedirect(true);
      return null;
    } catch (ParseException e) {
      status.setCode(HttpServletResponse.SC_BAD_REQUEST);
      status.setMessage("Error in request, impossible to parse object request");
      status.setRedirect(true);
      return null;
    }

    return model;
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

  /**
   * @return the currentUserPermissionCheckerService
   */
  public CurrentUserPermissionCheckerService getCurrentUserPermissionCheckerService() {
    return currentUserPermissionCheckerService;
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
}
